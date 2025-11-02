package com.smartattendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.model.Session;
import com.smartattendance.model.Student;
import com.smartattendance.ApplicationContext;

public class RecognitionService {
  // Use shared attendance service so Reports can see the records
  private final AttendanceService attendanceService = ApplicationContext.getAttendanceService();

  public AttendanceService getAttendanceService() {
    return attendanceService;
  }

  public void startRecognition() {
    new Thread(() -> {
      try {
        Thread.sleep(1200);
      } catch (Exception ignored) {
      }
      Student s = new Student(10, "Alice Tan", "CS102");
      Session sess = new Session(1, "CS102", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), "Room 1",
          15, "PENDING");
      AttendanceRecord rec = new AttendanceRecord(s, sess, "Present", "Auto", 0.95, LocalDateTime.now());
      attendanceService.markAttendance(rec);
    }).start();
  }

  public void stopRecognition() {
  }
}
