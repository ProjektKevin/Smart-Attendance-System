package com.smartattendance.controller;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.AttendanceStatus;
import com.smartattendance.model.entity.MarkMethod;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.service.AttendanceService;

import javafx.fxml.FXML;

public class TestAutoMarkController {
    // Use shared attendance service so Reports can see the records

    private final AttendanceService attendanceService = ApplicationContext.getAttendanceService();
    private final double threshold = Double.parseDouble(Config.get("recognition.threshold"));

    public AttendanceService getAttendanceService() {
        return attendanceService;
    }

    // test normal auto mark when confindence > threshold
    @FXML
    private void onTestAutoMark1() {
        try {
            // 1️⃣ Create a Student
            Student s = new Student(1, "harry", "CS102");

            // 2️⃣ Create a Session
            Session sess = new Session(
                    1,
                    "CS102",
                    LocalDate.now(),
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    "Room 1",
                    15,
                    "Closed"
            );

            // 3️⃣ Create an AttendanceRecord
            AttendanceRecord rec = new AttendanceRecord(
                    s,
                    sess,
                    AttendanceStatus.PRESENT,
                    0.95,
                    MarkMethod.AUTO,
                    LocalDateTime.now()
            );

            // 4️⃣ Call the service to mark attendance
            attendanceService.markAttendance(rec);

            System.out.println("✅ Auto mark completed for " + s.getName());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error during auto mark: " + e.getMessage());
        }
    }

    // test normal auto mark when confindence > threshold, session already started
    @FXML
    private void onTestAutoMark2() {
        try {
            // 1️⃣ Create a Student
            Student s = new Student(1, "harry", "CS301");

            // 2️⃣ Create a Session
            Session sess = new Session(
                    3,
                    "CS301",
                    LocalDate.now(),
                    LocalTime.of(18, 30),
                    LocalTime.of(19, 0),
                    "Room 1",
                    15,
                    "Open"
            );

            // 3️⃣ Create an AttendanceRecord
            AttendanceRecord rec = new AttendanceRecord(
                    s,
                    sess,
                    AttendanceStatus.PRESENT,
                    0.95,
                    MarkMethod.AUTO,
                    LocalDateTime.now()
            );

            // 4️⃣ Call the service to mark attendance
            attendanceService.markAttendance(rec);

            System.out.println("✅ Auto mark completed for " + s.getName());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error during auto mark: " + e.getMessage());
        }
    }
}
