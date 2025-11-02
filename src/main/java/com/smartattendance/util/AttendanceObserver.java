package com.smartattendance.util;

import com.smartattendance.model.entity.AttendanceRecord;

public interface AttendanceObserver {
  void onAttendanceMarked(AttendanceRecord record);
}
