package com.smartattendance.controller;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import com.smartattendance.service.AttendanceReportService;
import com.smartattendance.util.report.AttendanceReportRow;
import com.smartattendance.util.report.ReportSpec;

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
 * Thin controller:
 * - read UI
 * - call service
 * - show status
 */
public class ReportController {

    // dates
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;

    // filters
    @FXML private ComboBox<String> sessionFilter;
    @FXML private ComboBox<String> courseFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> methodFilter;
    @FXML private ComboBox<String> confidenceFilter;

    // columns
    @FXML private CheckBox selectAllFieldsCheck;
    @FXML private CheckBox includeDateTimeCheck;
    @FXML private CheckBox includeSessionIdCheck;
    @FXML private CheckBox includeCourseCodeCheck;
    @FXML private CheckBox includeStudentIdCheck;
    @FXML private CheckBox includeStudentNameCheck;
    @FXML private CheckBox includeStatusCheck;
    @FXML private CheckBox includeMethodCheck;
    @FXML private CheckBox includeConfidenceCheck;
    @FXML private CheckBox includeNoteCheck;

    // email
    @FXML private TextField emailTo;
    @FXML private TextField emailSubject;
    @FXML private TextArea  emailBody;

    @FXML private Label reportStatus;

    private final AttendanceReportService reportService = new AttendanceReportService();

    // we store the last generated files so the email buttons can attach them
    private File lastPdf;
    private File lastCsv;
    private File lastXlsx;

    private boolean updatingSelectAll = false;

    @FXML
    public void initialize() {
        // defaults
        LocalDate today = LocalDate.now();
        if (fromDate != null) fromDate.setValue(today);
        if (toDate   != null) toDate.setValue(today);

        if (reportStatus != null) {
            reportStatus.setText("Select filters and export.");
        }

        if (emailSubject != null) {
            emailSubject.setText("Attendance Report");
        }
        if (emailBody != null) {
            emailBody.setText("Please find the attached attendance report.");
        }

        // column defaults
        setColumnChecks(true);
        if (selectAllFieldsCheck != null) {
            selectAllFieldsCheck.setSelected(true);
            selectAllFieldsCheck.selectedProperty().addListener((obs, ov, nv) -> {
                if (updatingSelectAll) return;
                updatingSelectAll = true;
                setColumnChecks(nv);
                updatingSelectAll = false;
            });
        }
        // when individual boxes change → maybe uncheck selectAll
        registerColumnForSelectAll(includeDateTimeCheck);
        registerColumnForSelectAll(includeSessionIdCheck);
        registerColumnForSelectAll(includeCourseCodeCheck);
        registerColumnForSelectAll(includeStudentIdCheck);
        registerColumnForSelectAll(includeStudentNameCheck);
        registerColumnForSelectAll(includeStatusCheck);
        registerColumnForSelectAll(includeMethodCheck);
        registerColumnForSelectAll(includeConfidenceCheck);
        registerColumnForSelectAll(includeNoteCheck);

        // load dropdowns from DB via service
        loadDropDowns();
    }

    private void loadDropDowns() {
        // sessions
        if (sessionFilter != null) {
            sessionFilter.getItems().setAll(reportService.getSessionOptions());
            if (!sessionFilter.getItems().isEmpty()) {
                sessionFilter.getSelectionModel().select(0); // "All" or first
            }
        }
        // courses
        if (courseFilter != null) {
            courseFilter.getItems().setAll(reportService.getCourseOptions());
            if (!courseFilter.getItems().isEmpty()) {
                courseFilter.getSelectionModel().select(0);
            }
        }
        // statuses / methods / confidences – fixed in service
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

    private void registerColumnForSelectAll(CheckBox cb) {
        if (cb == null) return;
        cb.selectedProperty().addListener((obs, ov, nv) -> {
            if (updatingSelectAll) return;
            if (!nv && selectAllFieldsCheck != null) {
                selectAllFieldsCheck.setSelected(false);
            } else if (nv && selectAllFieldsCheck != null && areAllColumnsSelected()) {
                selectAllFieldsCheck.setSelected(true);
            }
        });
    }

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

    private boolean isChecked(CheckBox cb) {
        return cb != null && cb.isSelected();
    }

    private void safeSet(CheckBox cb, boolean v) {
        if (cb != null) cb.setSelected(v);
    }

    /* =================== buttons =================== */

    @FXML
    private void onLatestSession() {
        // ask the service for latest
        var latest = reportService.getLatestSession();
        if (latest == null) {
            setStatus("No sessions found.");
            return;
        }

        // select in combo
        if (sessionFilter != null) {
            sessionFilter.getSelectionModel().select(latest.display);
        }
        // also set course to that session's course
        if (courseFilter != null && latest.courseDisplay != null) {
            courseFilter.getSelectionModel().select(latest.courseDisplay);
        }

        // set dates to that day
        if (fromDate != null) fromDate.setValue(latest.date);
        if (toDate   != null) toDate.setValue(latest.date);

        setStatus("Latest session selected.");
    }

    @FXML
    private void onResetFilters() {
        LocalDate today = LocalDate.now();
        if (fromDate != null) fromDate.setValue(today);
        if (toDate   != null) toDate.setValue(today);

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

    @FXML
    private void onExportPDF() {
        if (!atLeastOneColumnSelected()) {
            setStatus("Select at least one column.");
            return;
        }
        var filter = buildFilter();
        var spec   = buildReportSpec();

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

    @FXML
    private void onExportExcel() {
        if (!atLeastOneColumnSelected()) {
            setStatus("Select at least one column.");
            return;
        }
        var filter = buildFilter();
        var spec   = buildReportSpec();

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

    @FXML
    private void onExportCSV() {
        if (!atLeastOneColumnSelected()) {
            setStatus("Select at least one column.");
            return;
        }
        var filter = buildFilter();
        var spec   = buildReportSpec();

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

    @FXML
    private void onEmailPdf() {
        if (lastPdf == null || !lastPdf.exists()) {
            setStatus("Export a PDF first.");
            return;
        }
        sendEmailWith(lastPdf);
    }

    @FXML
    private void onEmailCsv() {
        if (lastCsv == null || !lastCsv.exists()) {
            setStatus("Export a CSV first.");
            return;
        }
        sendEmailWith(lastCsv);
    }

    @FXML
    private void onEmailExcel() {
        if (lastXlsx == null || !lastXlsx.exists()) {
            setStatus("Export an Excel first.");
            return;
        }
        sendEmailWith(lastXlsx);
    }

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

        // run async
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

    private AttendanceReportService.ReportFilter buildFilter() {
        AttendanceReportService.ReportFilter f = new AttendanceReportService.ReportFilter();
        f.fromDate = fromDate != null ? fromDate.getValue() : null;
        f.toDate   = toDate   != null ? toDate.getValue()   : null;
        f.sessionDisplay = sessionFilter != null ? sessionFilter.getValue() : null;
        f.courseDisplay  = courseFilter  != null ? courseFilter.getValue()  : null;
        f.status         = statusFilter  != null ? statusFilter.getValue()  : null;
        f.method         = methodFilter  != null ? methodFilter.getValue()  : null;
        f.confidenceExpr = confidenceFilter != null ? confidenceFilter.getValue() : null;
        return f;
    }

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

    private File chooseFile(String defaultName, String desc, String... exts) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(defaultName);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, exts));
        return fc.showSaveDialog(null);
    }

    private void setStatus(String msg) {
        if (reportStatus != null) {
            reportStatus.setText(msg);
        }
        System.out.println("[ReportController] " + msg);
    }
}
