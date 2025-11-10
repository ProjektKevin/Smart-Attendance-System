package com.smartattendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;

public class RecognitionService {
    // Use shared attendance service so Reports can see the records

    private final AttendanceService attendanceService = ApplicationContext.getAttendanceService();
    private final double threshold = Double.parseDouble(Config.get("recognition.threshold"));

    public AttendanceService getAttendanceService() {
        return attendanceService;
    }

    public void startRecognition() {
        try {
            // 1️⃣ Create a Student
            // Student s = new Student(1, "harry", "CS102");

            // 1️⃣ Create a Student
            Student s = new Student(3, "felicia", "CS104");

            // 2️⃣ Create a Session
            // Session sess = new Session(
            //         8,
            //         "CS102",
            //         LocalDate.now(),
            //         LocalTime.of(23, 40),
            //         LocalTime.of(23, 45),
            //         "Room 5",
            //         15,
            //         "Open"
            // );

            // Session sess = new Session(
            //         9,
            //         "CS102",
            //         LocalDate.now(),
            //         LocalTime.of(23, 56),
            //         LocalTime.of(00, 10),
            //         "Room 5",
            //         15,
            //         "Open"
            // );

            // Session sess = new Session(
            //         14,
            //         "CS102",
            //         LocalDate.now(),
            //         LocalTime.of(00, 10),
            //         LocalTime.of(00, 30),
            //         "Room 5",
            //         15,
            //         "Open"
            // );

            // Session sess = new Session(
            //         6,
            //         "CS102",
            //         LocalDate.now(),
            //         LocalTime.of(01, 10),
            //         LocalTime.of(01, 30),
            //         "Room 5",
            //         15,
            //         "Open"
            // );

            // Session sess = new Session(
            //         7,
            //         "CS102",
            //         LocalDate.now(),
            //         LocalTime.of(01, 20),
            //         LocalTime.of(01, 50),
            //         "Room 5",
            //         15,
            //         "Open"
            // );

            // Session sess = new Session(
            //         9,
            //         "CS102",
            //         LocalDate.now(),
            //         LocalTime.of(01, 45),
            //         LocalTime.of(02, 30),
            //         "Room 5",
            //         15,
            //         "Open"
            // );

            Session sess = new Session(
                    10,
                    "CS104",
                    LocalDate.now(),
                    LocalTime.of(9, 15),
                    LocalTime.of(10, 00),
                    "Room D",
                    15,
                    "Open"
            );

            // // 3️⃣ Create an AttendanceRecord
            // AttendanceRecord rec = new AttendanceRecord(
            //         s,
            //         sess,
            //         AttendanceStatus.PRESENT,
            //         0.95,
            //         MarkMethod.AUTO,
            //         LocalDateTime.now()
            // );

            // 3️⃣ Create an AttendanceRecord
            AttendanceRecord rec = new AttendanceRecord(
                    s,
                    sess,
                    AttendanceStatus.PRESENT,
                    // 0.35,
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

    public void stopRecognition() {
    }
}
