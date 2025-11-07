package com.smartattendance.service;

import com.smartattendance.model.entity.*;
import com.smartattendance.repository.*;
import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RecognitionServiceTest {

    // private final AttendanceService attendanceService;
    private final AttendanceService attendanceService = ApplicationContext.getAttendanceService();
    private final AttendanceRecordRepository attendanceRecordRepo;
    private final Session session;
    private final Student student1, student2;

    public RecognitionServiceTest() {
        this.attendanceRecordRepo = new AttendanceRecordRepository();
        // this.attendanceService = new AttendanceService(attendanceRecordRepo);

        // session = new Session();
        session = new Session(1, "CS102", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), "Room 1",
          15, "PENDING");
        // session.setSessionId(1);
        // session.setLateThresholdMinutes(15);

        student1 = new Student(1, "harry", "CS102");
        student2 = new Student(2, "kevin", "CS102");

        // student1 = new Student(101, "Emily Tan");
        // student2 = new Student(102, "John Lim");
    }

    // === Test Case 1 ===
    public void testHighConfidence() {
        simulateDetection(student1, 0.95);
    }

    // === Test Case 2 ===
    public void testLowConfidence() {
        simulateDetection(student2, 0.60);
    }

    // === Test Case 3 ===
    public void testWithinCooldown() {
        // First mark once
        simulateDetection(student1, 0.95);
        // Immediately try again to simulate within cooldown
        simulateDetection(student1, 0.98);
    }

    // === Test Case 4 ===
    public void testAfterCooldown() {
        simulateDetection(student1, 0.95);
        new Thread(() -> {
            try {
                Thread.sleep(35_000); // wait 35s
                simulateDetection(student1, 0.98);
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void simulateDetection(Student student, double confidence) {
        Platform.runLater(() -> {
            LocalDateTime now = LocalDateTime.now();
            AttendanceRecord rec = new AttendanceRecord(
                student, session, AttendanceStatus.PENDING, confidence, MarkMethod.AUTO, now
            );

            try {
                // double threshold = ConfigUtil.getConfidenceThreshold(); // e.g., 0.8
                double threshold = Double.parseDouble(Config.get("recognition.threshold"));

                System.out.println("[Recognition] Detected " + student.getName() +
                        " (confidence: " + confidence + ")");

                if (confidence < threshold) {
                    showConfirmationPopup(rec);
                } else {
                    attendanceService.markAttendance(rec);
                    showMessage("✅ Marked attendance for " + student.getName());
                }

            } catch (Exception e) {
                showMessage("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void showConfirmationPopup(AttendanceRecord rec) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Student Identity");
        alert.setHeaderText("Low Confidence Detection");
        alert.setContentText(
            "Detected student:\nName: " + rec.getStudent().getName() +
            "\nID: " + rec.getStudent().getStudentId() +
            "\nConfidence: " + rec.getConfidence() +
            "\n\nIs this correct?"
        );

        alert.showAndWait().ifPresent(result -> {
            if (result.getText().equalsIgnoreCase("OK")) {
                attendanceService.markAttendance(rec);
                showMessage("✅ Confirmed manually for " + rec.getStudent().getName());
            } else {
                showMessage("❌ Cancelled attendance for " + rec.getStudent().getName());
            }
        });
    }

    private void showMessage(String message) {
        Alert info = new Alert(AlertType.INFORMATION);
        info.setHeaderText(null);
        info.setContentText(message);
        info.show();
    }
}
