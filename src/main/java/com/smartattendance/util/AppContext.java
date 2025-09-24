package com.smartattendance.util;

import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.StudentService;

public final class AppContext {
  private static final StudentService STUDENTS = new StudentService();
  private static final AttendanceService ATTENDANCE = new AttendanceService();

  private AppContext() {}

  public static StudentService getStudentService() { return STUDENTS; }
  public static AttendanceService getAttendanceService() { return ATTENDANCE; }
}
