package com.smartattendance.util;

import com.smartattendance.model.AttendanceRecord;

public interface AttendanceObserver {
  void onAttendanceMarked(AttendanceRecord record);
}
