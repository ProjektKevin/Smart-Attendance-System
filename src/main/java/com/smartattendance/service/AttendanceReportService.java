package com.smartattendance.service;

import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.smartattendance.repository.AttendanceRepository;
import com.smartattendance.util.report.AttendanceReportRow;
import com.smartattendance.util.report.CsvReportGenerator;
import com.smartattendance.util.report.PdfReportGenerator;
import com.smartattendance.util.report.ReportSpec;
import com.smartattendance.util.report.XlsxReportGenerator;

/**
 * Facade for the report UI (ReportController).
 * Delegates to AttendanceRepository + generators + EmailService.
 */
public class AttendanceReportService {

    private final AttendanceRepository repo = new AttendanceRepository();
    private final EmailService emailService = new EmailService();

    /* ================= DTOs that the controller expects ================= */

    // controller uses: latest.display, latest.courseDisplay, latest.date
    public static class LatestSessionInfo {
        public String display;
        public String courseDisplay;
        public LocalDate date;
    }

    // controller builds this in buildFilter()
    public static class ReportFilter {
        public LocalDate fromDate;
        public LocalDate toDate;
        public String sessionDisplay;
        public String courseDisplay;
        public String status;
        public String method;
        public String confidenceExpr;
    }

    /* ================== option lists for comboboxes ================== */

    public List<String> getSessionOptions() {
        return repo.fetchSessionOptions();
    }

    public List<String> getCourseOptions() {
        return repo.fetchCourseOptions();
    }

    public List<String> getStatusOptions() {
        return java.util.Arrays.asList("All", "Pending", "Present", "Late", "Absent");
    }

    public List<String> getMethodOptions() {
        return java.util.Arrays.asList("All", "Manual", "Face", "QR", "-");
    }

    public List<String> getConfidenceOptions() {
        return java.util.Arrays.asList("All", ">= 0.90", ">= 0.80", "< 0.80");
    }

    /* ================== latest session ================== */

    public LatestSessionInfo getLatestSession() {
        return repo.fetchLatestSession();
    }

    /* ================== data ================== */

    public List<AttendanceReportRow> getAttendance(ReportFilter filter) {
        return repo.findAttendance(
                filter.fromDate,
                filter.toDate,
                filter.sessionDisplay,
                filter.courseDisplay,
                filter.status,
                filter.method,
                filter.confidenceExpr
        );
    }

    /* ================== exports ================== */

    public void generatePdfReport(List<AttendanceReportRow> rows,
                                  ReportSpec spec,
                                  File target) throws Exception {
        new PdfReportGenerator().generate(spec, rows, target);
    }

    public void generateCsvReport(List<AttendanceReportRow> rows,
                                  ReportSpec spec,
                                  File target) throws Exception {
        new CsvReportGenerator().generate(spec, rows, target);
    }

    public void generateXlsxReport(List<AttendanceReportRow> rows,
                                   ReportSpec spec,
                                   File target) throws Exception {
        new XlsxReportGenerator().generate(spec, rows, target);
    }

    /* ================== email ================== */

    public void sendEmail(String to,
                          String subject,
                          String body,
                          File attachment) throws Exception {

        // turn the single file into a List<File> because EmailService expects a list
        List<File> attachments = (attachment != null)
                ? Collections.singletonList(attachment)
                : Collections.<File>emptyList();

        // use the correct method name from EmailService
        emailService.sendReport(to, subject, body, attachments);
    }
}
