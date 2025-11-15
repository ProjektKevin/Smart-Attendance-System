package com.smartattendance.controller;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import com.smartattendance.service.AttendanceReportService;
import com.smartattendance.util.report.AttendanceReportRow;
import com.smartattendance.util.report.ReportSpec;
import com.smartattendance.util.security.log.ApplicationLogger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 * Controller for the Attendance Report screen.
 *
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Read filter / column selections from the UI</li>
 * <li>Call {@link AttendanceReportService} to fetch data and generate
 * reports</li>
 * <li>Trigger exports (PDF, Excel, CSV) and email sending</li>
 * <li>Update the status label with user-friendly messages</li>
 * </ul>
 *
 * <p>
 * All heavy lifting (DB queries, report generation, email sending) is delegated
 * to {@link AttendanceReportService}; this class stays as a thin orchestration
 * layer.
 */
public class ReportController {

    // ===== Date range controls =====

    @FXML
    private DatePicker fromDate;
    @FXML
    private DatePicker toDate;

    // ===== Filter dropdowns =====

    @FXML
    private ComboBox<String> sessionFilter;
    @FXML
    private ComboBox<String> courseFilter;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private ComboBox<String> methodFilter;
    @FXML
    private ComboBox<String> confidenceFilter;

    // ===== Column selection checkboxes =====

    @FXML
    private CheckBox selectAllFieldsCheck;
    @FXML
    private CheckBox includeDateTimeCheck;
    @FXML
    private CheckBox includeSessionIdCheck;
    @FXML
    private CheckBox includeCourseCodeCheck;
    @FXML
    private CheckBox includeStudentIdCheck;
    @FXML
    private CheckBox includeStudentNameCheck;
    @FXML
    private CheckBox includeStatusCheck;
    @FXML
    private CheckBox includeMethodCheck;
    @FXML
    private CheckBox includeConfidenceCheck;
    @FXML
    private CheckBox includeNoteCheck;

    // ===== Email controls =====

    @FXML
    private TextField emailTo;
    @FXML
    private TextField emailSubject;
    @FXML
    private TextArea emailBody;

    // Label used to show status messages (export/email result, errors, etc.)
    @FXML
    private Label reportStatus;

    /**
     * Service that encapsulates all report-related logic:
     * fetching data, generating files, and sending emails.
     */
    private final AttendanceReportService reportService = new AttendanceReportService();

    /**
     * References to the most recently exported files (per format),
     * so the email buttons can attach them without regenerating.
     */
    private File lastPdf;
    private File lastCsv;
    private File lastXlsx;

    /**
     * Guard flag to prevent infinite recursion when we update the
     * "Select All" checkbox based on individual checkbox changes.
     */
    private boolean updatingSelectAll = false;

    /**
     * Application Logger to show on terminal and write file to refer back to
     */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    /**
     * Called automatically by JavaFX after FXML fields are injected.
     * Sets up default values, wiring for the "Select All" behaviour,
     * and loads dropdown options from the service.
     */
    @FXML
    public void initialize() {
        // Default the date range to today's date, if controls exist in this view.
        LocalDate today = LocalDate.now();
        if (fromDate != null)
            fromDate.setValue(today);
        if (toDate != null)
            toDate.setValue(today);

        // Initial status text for the report screen.
        if (reportStatus != null) {
            reportStatus.setText("Select filters and export.");
        }

        // Default email subject/body for convenience.
        if (emailSubject != null) {
            emailSubject.setText("Attendance Report");
        }
        if (emailBody != null) {
            emailBody.setText("Please find the attached attendance report.");
        }

        // By default, include all columns in the export.
        setColumnChecks(true);
        if (selectAllFieldsCheck != null) {
            selectAllFieldsCheck.setSelected(true);
            // When "Select All" changes → update all individual checkboxes.
            selectAllFieldsCheck.selectedProperty().addListener((obs, ov, nv) -> {
                if (updatingSelectAll)
                    return;
                updatingSelectAll = true;
                setColumnChecks(nv);
                updatingSelectAll = false;
            });
        }

        // When individual boxes change, keep the "Select All" checkbox in sync.
        registerColumnForSelectAll(includeDateTimeCheck);
        registerColumnForSelectAll(includeSessionIdCheck);
        registerColumnForSelectAll(includeCourseCodeCheck);
        registerColumnForSelectAll(includeStudentIdCheck);
        registerColumnForSelectAll(includeStudentNameCheck);
        registerColumnForSelectAll(includeStatusCheck);
        registerColumnForSelectAll(includeMethodCheck);
        registerColumnForSelectAll(includeConfidenceCheck);
        registerColumnForSelectAll(includeNoteCheck);

        // Load dropdown options (sessions, courses, status, etc.) from the service.
        loadDropDowns();
    }

    /**
     * Populate the filter dropdowns (session, course, status, method, confidence)
     * using data and fixed lists from {@link AttendanceReportService}.
     */
    private void loadDropDowns() {
        // Sessions (usually includes an "All" option)
        if (sessionFilter != null) {
            sessionFilter.getItems().setAll(reportService.getSessionOptions());
            if (!sessionFilter.getItems().isEmpty()) {
                sessionFilter.getSelectionModel().select(0); // "All" or first entry
            }
        }
        // Courses
        if (courseFilter != null) {
            courseFilter.getItems().setAll(reportService.getCourseOptions());
            if (!courseFilter.getItems().isEmpty()) {
                courseFilter.getSelectionModel().select(0);
            }
        }
        // Status / method / confidence – fixed sets from the service.
        if (statusFilter != null) {
            statusFilter.getItems().setAll(reportService.getStatusOptions());
            statusFilter.getSelectionModel().selectFirst();
        }
        if (methodFilter != null) {
            methodFilter.getItems().setAll(reportService.getMethodOptions());
            methodFilter.getSelectionModel().selectFirst();
        }
        if (confidenceFilter != null) {
            confidenceFilter.getItems().setAll(reportService.getConfidenceOptions());
            confidenceFilter.getSelectionModel().selectFirst();
        }
    }

    /**
     * Set all column checkboxes (if present) to the given value.
     *
     * @param v {@code true} to check all; {@code false} to uncheck all
     */
    private void setColumnChecks(boolean v) {
        safeSet(includeDateTimeCheck, v);
        safeSet(includeSessionIdCheck, v);
        safeSet(includeCourseCodeCheck, v);
        safeSet(includeStudentIdCheck, v);
        safeSet(includeStudentNameCheck, v);
        safeSet(includeStatusCheck, v);
        safeSet(includeMethodCheck, v);
        safeSet(includeConfidenceCheck, v);
        safeSet(includeNoteCheck, v);
    }

    /**
     * Register a checkbox so its changes keep the "Select All" checkbox in sync.
     * If any child is unchecked, "Select All" is unchecked. If all are checked, it
     * re-checks "Select All".
     */
    private void registerColumnForSelectAll(CheckBox cb) {
        if (cb == null)
            return;
        cb.selectedProperty().addListener((obs, ov, nv) -> {
            if (updatingSelectAll)
                return;
            if (!nv && selectAllFieldsCheck != null) {
                // A column got unchecked → uncheck "Select All".
                selectAllFieldsCheck.setSelected(false);
            } else if (nv && selectAllFieldsCheck != null && areAllColumnsSelected()) {
                // All columns are now selected → check "Select All".
                selectAllFieldsCheck.setSelected(true);
            }
        });
    }

    /**
     * @return {@code true} if all column checkboxes are currently selected.
     */
    private boolean areAllColumnsSelected() {
        return isChecked(includeDateTimeCheck)
                && isChecked(includeSessionIdCheck)
                && isChecked(includeCourseCodeCheck)
                && isChecked(includeStudentIdCheck)
                && isChecked(includeStudentNameCheck)
                && isChecked(includeStatusCheck)
                && isChecked(includeMethodCheck)
                && isChecked(includeConfidenceCheck)
                && isChecked(includeNoteCheck);
    }

    /**
     * Null-safe check for a checkbox.
     */
    private boolean isChecked(CheckBox cb) {
        return cb != null && cb.isSelected();
    }

    /**
     * Null-safe setter for a checkbox.
     */
    private void safeSet(CheckBox cb, boolean v) {
        if (cb != null)
            cb.setSelected(v);
    }

    /* =================== button handlers: filters =================== */

    /**
     * Handler for the "Latest Session" button.
     * <ul>
     * <li>Asks the service for the latest session</li>
     * <li>Updates session/course dropdowns and date range to match that
     * session</li>
     * <li>Updates the status label</li>
     * </ul>
     */
    @FXML
    private void onLatestSession() {
        // Ask the service for the latest session summary.
        var latest = reportService.getLatestSession();
        if (latest == null) {
            setStatus("No sessions found.");
            return;
        }

        // Select session in combo box.
        if (sessionFilter != null) {
            sessionFilter.getSelectionModel().select(latest.display);
        }
        // Also set the course dropdown to that session's course (if available).
        if (courseFilter != null && latest.courseDisplay != null) {
            courseFilter.getSelectionModel().select(latest.courseDisplay);
        }

        // Restrict date range to that session's date.
        if (fromDate != null)
            fromDate.setValue(latest.date);
        if (toDate != null)
            toDate.setValue(latest.date);

        setStatus("Latest session selected.");
    }

    /**
     * Handler for the "Reset Filters" button.
     * Resets date range to today, and sets all dropdowns back to their first
     * option.
     */
    @FXML
    private void onResetFilters() {
        LocalDate today = LocalDate.now();
        if (fromDate != null)
            fromDate.setValue(today);
        if (toDate != null)
            toDate.setValue(today);

        if (sessionFilter != null && !sessionFilter.getItems().isEmpty())
            sessionFilter.getSelectionModel().selectFirst();
        if (courseFilter != null && !courseFilter.getItems().isEmpty())
            courseFilter.getSelectionModel().selectFirst();
        if (statusFilter != null && !statusFilter.getItems().isEmpty())
            statusFilter.getSelectionModel().selectFirst();
        if (methodFilter != null && !methodFilter.getItems().isEmpty())
            methodFilter.getSelectionModel().selectFirst();
        if (confidenceFilter != null && !confidenceFilter.getItems().isEmpty())
            confidenceFilter.getSelectionModel().selectFirst();

        setStatus("Filters reset.");
    }

    /* =================== button handlers: exports =================== */

    /**
     * Handler for "Export PDF" button.
     * Validates column selection, fetches filtered data, allows user to choose
     * a file, then delegates to the service to generate the PDF.
     */
    @FXML
    private void onExportPDF() {
        if (!atLeastOneColumnSelected()) {
            setStatus("Select at least one column.");
            return;
        }
        var filter = buildFilter();
        var spec = buildReportSpec();

        List<AttendanceReportRow> rows = reportService.getAttendance(filter);
        if (rows.isEmpty()) {
            setStatus("No data to export.");
            return;
        }

        File target = chooseFile("attendance-report.pdf", "PDF files", "*.pdf");
        if (target == null) {
            setStatus("PDF export cancelled.");
            return;
        }

        try {
            reportService.generatePdfReport(rows, spec, target);
            lastPdf = target;
            setStatus("PDF exported: " + target.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("PDF export failed: " + e.getMessage());
        }
    }

    /**
     * Handler for "Export Excel" button (.xlsx).
     */
    @FXML
    private void onExportExcel() {
        if (!atLeastOneColumnSelected()) {
            setStatus("Select at least one column.");
            return;
        }
        var filter = buildFilter();
        var spec = buildReportSpec();

        List<AttendanceReportRow> rows = reportService.getAttendance(filter);
        if (rows.isEmpty()) {
            setStatus("No data to export.");
            return;
        }

        File target = chooseFile("attendance-report.xlsx", "Excel files", "*.xlsx");
        if (target == null) {
            setStatus("Excel export cancelled.");
            return;
        }

        try {
            reportService.generateXlsxReport(rows, spec, target);
            lastXlsx = target;
            setStatus("Excel exported: " + target.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Excel export failed: " + e.getMessage());
        }
    }

    /**
     * Handler for "Export CSV" button.
     */
    @FXML
    private void onExportCSV() {
        if (!atLeastOneColumnSelected()) {
            setStatus("Select at least one column.");
            return;
        }
        var filter = buildFilter();
        var spec = buildReportSpec();

        List<AttendanceReportRow> rows = reportService.getAttendance(filter);
        if (rows.isEmpty()) {
            setStatus("No data to export.");
            return;
        }

        File target = chooseFile("attendance-report.csv", "CSV files", "*.csv");
        if (target == null) {
            setStatus("CSV export cancelled.");
            return;
        }

        try {
            reportService.generateCsvReport(rows, spec, target);
            lastCsv = target;
            setStatus("CSV exported: " + target.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("CSV export failed: " + e.getMessage());
        }
    }

    // ====== email buttons ======

    /**
     * Handler for "Email PDF" button.
     * Requires that a PDF has already been exported in this session.
     */
    @FXML
    private void onEmailPdf() {
        if (lastPdf == null || !lastPdf.exists()) {
            setStatus("Export a PDF first.");
            return;
        }
        sendEmailWith(lastPdf);
    }

    /**
     * Handler for "Email CSV" button.
     */
    @FXML
    private void onEmailCsv() {
        if (lastCsv == null || !lastCsv.exists()) {
            setStatus("Export a CSV first.");
            return;
        }
        sendEmailWith(lastCsv);
    }

    /**
     * Handler for "Email Excel" button.
     */
    @FXML
    private void onEmailExcel() {
        if (lastXlsx == null || !lastXlsx.exists()) {
            setStatus("Export an Excel first.");
            return;
        }
        sendEmailWith(lastXlsx);
    }

    /**
     * Common helper for sending email with the given report file attached.
     * Runs the actual sending in a background thread, then updates the UI status
     * on the JavaFX Application Thread using {@link Platform#runLater(Runnable)}.
     */
    private void sendEmailWith(File file) {
        String to = emailTo != null ? emailTo.getText() : "";
        if (to == null || to.isBlank()) {
            setStatus("Enter recipient email.");
            return;
        }
        String subject = (emailSubject != null && !emailSubject.getText().isBlank())
                ? emailSubject.getText()
                : "Attendance Report";
        String body = (emailBody != null && !emailBody.getText().isBlank())
                ? emailBody.getText()
                : "Please find the report attached.";

        setStatus("Sending email...");

        // Perform email sending off the UI thread to avoid freezing the interface.
        new Thread(() -> {
            try {
                reportService.sendEmail(to, subject, body, file);
                Platform.runLater(() -> setStatus("Email sent to " + to));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Email failed: " + e.getMessage()));
            }
        }, "mail-thread").start();
    }

    /* ================= helpers ================= */

    /**
     * @return {@code true} if at least one column checkbox is selected.
     *         Used to prevent generating empty reports.
     */
    private boolean atLeastOneColumnSelected() {
        return isChecked(includeDateTimeCheck)
                || isChecked(includeSessionIdCheck)
                || isChecked(includeCourseCodeCheck)
                || isChecked(includeStudentIdCheck)
                || isChecked(includeStudentNameCheck)
                || isChecked(includeStatusCheck)
                || isChecked(includeMethodCheck)
                || isChecked(includeConfidenceCheck)
                || isChecked(includeNoteCheck);
    }

    /**
     * Build a report filter object (inner type defined in
     * {@link AttendanceReportService})
     * from the current UI selections.
     */
    private AttendanceReportService.ReportFilter buildFilter() {
        AttendanceReportService.ReportFilter f = new AttendanceReportService.ReportFilter();
        f.fromDate = fromDate != null ? fromDate.getValue() : null;
        f.toDate = toDate != null ? toDate.getValue() : null;
        f.sessionDisplay = sessionFilter != null ? sessionFilter.getValue() : null;
        f.courseDisplay = courseFilter != null ? courseFilter.getValue() : null;
        f.status = statusFilter != null ? statusFilter.getValue() : null;
        f.method = methodFilter != null ? methodFilter.getValue() : null;
        f.confidenceExpr = confidenceFilter != null ? confidenceFilter.getValue() : null;
        return f;
    }

    /**
     * Build a {@link ReportSpec} from the current column checkbox states.
     * This determines which columns will appear in the exported report.
     */
    private ReportSpec buildReportSpec() {
        return new ReportSpec.Builder()
                .includeDateTime(isChecked(includeDateTimeCheck))
                .includeSessionId(isChecked(includeSessionIdCheck))
                .includeCourseCode(isChecked(includeCourseCodeCheck))
                .includeStudentId(isChecked(includeStudentIdCheck))
                .includeStudentName(isChecked(includeStudentNameCheck))
                .includeStatus(isChecked(includeStatusCheck))
                .includeMethod(isChecked(includeMethodCheck))
                .includeConfidence(isChecked(includeConfidenceCheck))
                .includeNote(isChecked(includeNoteCheck))
                .build();
    }

    /**
     * Helper to show a "Save As" dialog with default name and extension filter.
     *
     * @param defaultName default file name suggestion
     * @param desc        description for the extension filter (e.g. "PDF files")
     * @param exts        allowed extensions (e.g. "*.pdf")
     * @return the selected file, or {@code null} if the user cancelled
     */
    private File chooseFile(String defaultName, String desc, String... exts) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, exts));
        return fc.showSaveDialog(null);
    }

    /**
     * Update the status label (if present) and also log to stdout for debugging.
     *
     * @param msg status message to show/log
     */
    private void setStatus(String msg) {
        if (reportStatus != null) {
            reportStatus.setText(msg);
        }
        appLogger.info("[ReportController] " + msg);
    }
}
