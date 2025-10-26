package com.smartattendance.controller;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.model.Session;
import com.smartattendance.model.Student;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.util.EmailService;
import com.smartattendance.util.EmailSettings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportController {

  @FXML private DatePicker fromDate;
  @FXML private DatePicker toDate;
  @FXML private Label reportStatus;

  // Email UI
  @FXML private TextField emailTo;
  @FXML private TextField emailSubject;
  @FXML private TextArea  emailBody;

  private final AttendanceService attendance = ApplicationContext.getAttendanceService();

  // keep track of the last exports to attach easily
  private File lastExportedPdf;
  private File lastExportedCsv;

  @FXML
  public void initialize() {
    LocalDate today = LocalDate.now();
    fromDate.setValue(today);
    toDate.setValue(today);
    reportStatus.setText("Select a range and export/import.");
    emailSubject.setText("Attendance Report");
    emailBody.setText("Hi,\n\nPlease find the attendance report attached.\n\nRegards,\nSmart Attendance");
  }

  /* ---------------- CSV export ---------------- */
  @FXML
  private void onExportCSV() {
    List<AttendanceRecord> rows = dataForRange();
    if (rows.isEmpty()) { reportStatus.setText("No data in range."); return; }

    FileChooser fc = new FileChooser();
    fc.setTitle("Save Attendance CSV");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
    fc.setInitialFileName("attendance.csv");
    File file = fc.showSaveDialog(null);
    if (file == null) return;

    try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
      w.write("date,time,session_id,course_id,student_id,student_name,status,method,confidence,note\n");
      DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
      DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
      for (AttendanceRecord r : rows) {
        String date = r.getTimestamp().toLocalDate().format(df);
        String time = r.getTimestamp().toLocalTime().format(tf);
        Session s = r.getSession();
        Student st = r.getStudent();
        w.write(String.join(",",
            esc(date),
            esc(time),
            esc(s.getSessionId()),
            esc(s.getCourseId()),
            esc(st.getStudentId()),
            esc(st.getUserName()),
            esc(r.getStatus()),
            esc(r.getMethod()),
            String.format("%.2f", r.getConfidence()),
            esc(r.getNote() == null ? "" : r.getNote())
        ));
        w.write("\n");
      }
      lastExportedCsv = file;
      reportStatus.setText("CSV exported: " + file.getName());
    } catch (IOException e) {
      reportStatus.setText("CSV export failed: " + e.getMessage());
    }
  }

  /* ---------------- PDF export ---------------- */
  @FXML
  private void onExportPDF() {
    List<AttendanceRecord> rows = dataForRange();
    if (rows.isEmpty()) { reportStatus.setText("No data in range."); return; }

    FileChooser fc = new FileChooser();
    fc.setTitle("Save Attendance PDF");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
    fc.setInitialFileName("attendance.pdf");
    File file = fc.showSaveDialog(null);
    if (file == null) return;

    try (PDDocument doc = new PDDocument()) {

      // page + layout params
      float margin = 36f;
      float leading = 14f;
      float y;
      float x = margin;

      // reusable header
      final String[] headers = {"Date","Time","Session","Course","Student","Status","Method","Conf"};
      final float[] widths   = { 55f, 45f,   60f,     55f,     150f,     55f,     55f,     35f };
      DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
      DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

      // helpers to create a new page + header
      PDPage page = new PDPage(PDRectangle.A4);
      doc.addPage(page);
      PDPageContentStream cs = new PDPageContentStream(doc, page);
      try {
        y = page.getMediaBox().getHeight() - margin;

        // Title
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(x, y);
        cs.showText("Attendance Report");
        cs.endText();
        y -= leading * 2;

        // Header row
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
        y = drawRow(cs, x, y, leading, headers, widths);
        cs.setFont(PDType1Font.HELVETICA, 10);

        // Data rows
        for (AttendanceRecord r : rows) {
          if (y < margin + leading * 2) {
            // new page
            cs.close();
            page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = page.getMediaBox().getHeight() - margin;
            // header on new page
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            y = drawRow(cs, x, y, leading, headers, widths);
            cs.setFont(PDType1Font.HELVETICA, 10);
          }
          String[] row = {
              r.getTimestamp().toLocalDate().format(df),
              r.getTimestamp().toLocalTime().format(tf),
              r.getSession().getSessionId(),
              r.getSession().getCourseId(),
              r.getStudent().getUserName() + " (" + r.getStudent().getStudentId() + ")",
              r.getStatus(),
              r.getMethod(),
              String.format("%.2f", r.getConfidence())
          };
          y = drawRow(cs, x, y, leading, row, widths);
        }
      } finally {
        cs.close();
      }

      doc.save(file);
      lastExportedPdf = file;
      reportStatus.setText("PDF exported: " + file.getName());
    } catch (Exception e) {
      reportStatus.setText("PDF export failed: " + e.getMessage());
    }
  }

  /* ---------------- Email buttons ---------------- */
  @FXML
  private void onEmailPdf() {
    if (lastExportedPdf == null || !lastExportedPdf.exists()) {
      reportStatus.setText("Export a PDF first.");
      return;
    }
    sendEmailWithAttachments(List.of(lastExportedPdf));
  }

  @FXML
  private void onEmailCsv() {
    if (lastExportedCsv == null || !lastExportedCsv.exists()) {
      reportStatus.setText("Export a CSV first.");
      return;
    }
    sendEmailWithAttachments(List.of(lastExportedCsv));
  }

  private void sendEmailWithAttachments(List<File> files) {
    final String to = safe(emailTo.getText());
    if (to.isBlank()) { reportStatus.setText("Enter recipient email."); return; }

    final String subject = (emailSubject.getText() == null || emailSubject.getText().isBlank())
            ? "Attendance Report" : emailSubject.getText();

    final String body = (emailBody.getText() == null || emailBody.getText().isBlank())
            ? "Please find the report attached." : emailBody.getText();

    final List<File> attachments = (files == null) ? List.of() : List.copyOf(files);
    final EmailService svc = new EmailService(EmailSettings.fromEnv());

    reportStatus.setText("Sending email…");

    Thread t = new Thread(() -> {
      try {
        svc.send(to, subject, body, attachments);
        Platform.runLater(() -> reportStatus.setText("Email sent to " + to));
      } catch (Exception ex) {
        final String msg = ex.getMessage();
        Platform.runLater(() -> reportStatus.setText("Email failed: " + msg));
      }
    }, "mail-sender");
    t.setDaemon(true);
    t.start();
  }

  /* ---------------- Imports ---------------- */
  @FXML
  private void onImportStudentsCSV() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Import Students CSV");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
    File file = fc.showOpenDialog(null);
    if (file == null) return;

    int imported = 0;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      String line; boolean headerSkipped = false;
      while ((line = br.readLine()) != null) {
        if (!headerSkipped) { headerSkipped = true; continue; }
        String[] parts = splitCsv(line);
        if (parts.length < 3) continue;
        String id = parts[0].trim();
        String name = parts[1].trim();
        String group = parts[2].trim();
        if (id.isEmpty() || name.isEmpty()) continue;

        var svc = ApplicationContext.getStudentService();
        var found = svc.findById(id);
        if (found == null) {
          svc.addStudent(new Student(id, name, group));
          imported++;
        }
      }
      reportStatus.setText("Imported students: " + imported);
    } catch (IOException e) {
      reportStatus.setText("Import failed: " + e.getMessage());
    }
  }

  @FXML
  private void onImportAttendanceCSV() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Import Attendance CSV");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
    File file = fc.showOpenDialog(null);
    if (file == null) return;

    int imported = 0;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      String line; boolean headerSkipped = false;
      DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
      DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm[:ss]");
      while ((line = br.readLine()) != null) {
        if (!headerSkipped) { headerSkipped = true; continue; }
        String[] p = splitCsv(line);
        if (p.length < 9) continue;

        LocalDate date = LocalDate.parse(p[0].trim(), df);
        LocalTime time = LocalTime.parse(p[1].trim(), tf);
        String sessId = p[2].trim();
        String courseId = p[3].trim();
        String stuId = p[4].trim();
        String stuName = p[5].trim();
        String status = p[6].trim();
        String method = p[7].trim();
        double conf = safeDouble(p[8].trim());
        String note = p.length > 9 ? p[9] : "";

        Student st = ApplicationContext.getStudentService().findById(stuId);
        if (st == null) {
          st = new Student(stuId, stuName.isEmpty() ? stuId : stuName, "G?");
          ApplicationContext.getStudentService().addStudent(st);
        }
        Session sess = new Session(sessId.isEmpty() ? "IMP-" + System.nanoTime() : sessId,
            courseId.isEmpty() ? "COURSE?" : courseId,
            date, LocalTime.of(9,0), LocalTime.of(10,0), "Imported", 15);

        AttendanceRecord rec = new AttendanceRecord(st, sess,
            (status.isEmpty() ? "Present" : status),
            (method.isEmpty() ? "Import" : method),
            conf, LocalDateTime.of(date, time));
        rec.setNote(note);
        ApplicationContext.getAttendanceService().markAttendance(rec);
        imported++;
      }
      reportStatus.setText("Imported attendance rows: " + imported);
    } catch (Exception e) {
      reportStatus.setText("Import failed: " + e.getMessage());
    }
  }

  /* ---------------- helpers ---------------- */
  private List<AttendanceRecord> dataForRange() {
    LocalDate from = fromDate.getValue();
    LocalDate to = toDate.getValue();
    return attendance.getBetween(from, to);
  }

  private static String esc(String s) {
    String v = s.replace("\"", "\"\"");
    return (v.contains(",") || v.contains("\"") || v.contains("\n")) ? "\"" + v + "\"" : v;
  }

  private static String[] splitCsv(String line) {
    java.util.List<String> out = new java.util.ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean q = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '"') q = !q;
      else if (c == ',' && !q) { out.add(cur.toString().trim()); cur.setLength(0); }
      else cur.append(c);
    }
    out.add(cur.toString().trim());
    return out.toArray(new String[0]);
  }

  private static float drawRow(PDPageContentStream cs, float x, float y, float leading,
                               String[] cols, float[] widths) throws IOException {
    float cursorX = x;
    cs.beginText();
    cs.newLineAtOffset(cursorX, y);
    for (int i = 0; i < cols.length; i++) {
      cs.showText(trunc(cols[i], (int)(widths[i] / 6))); // rough fit
      cs.newLineAtOffset(widths[i], 0);
    }
    cs.endText();
    return y - leading;
  }

  private static String trunc(String s, int max) {
    if (s == null) return "";
    return (s.length() <= max) ? s : s.substring(0, Math.max(0, max - 1)) + "…";
  }

  private static double safeDouble(String s) {
    try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
  }

  private static String safe(String s) { return (s == null) ? "" : s.trim(); }
}
