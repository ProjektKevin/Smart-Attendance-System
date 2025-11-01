package com.smartattendance.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.util.AttendanceObserver;

public class AttendanceService {
  private final List<AttendanceObserver> observers = new ArrayList<>();
  private final List<AttendanceRecord> records = new ArrayList<>();

  public void addObserver(AttendanceObserver o) {
    observers.add(o);
  }

  public synchronized void markAttendance(AttendanceRecord r) {
    records.add(r);
    for (AttendanceObserver o : observers)
      o.onAttendanceMarked(r);
  }

  public synchronized List<AttendanceRecord> getAll() {
    return new ArrayList<>(records);
  }

  public synchronized List<AttendanceRecord> getBetween(LocalDate from, LocalDate to) {
    List<AttendanceRecord> out = new ArrayList<>();
    for (AttendanceRecord r : records) {
      LocalDate d = r.getTimestamp().toLocalDate();
      boolean ok = (from == null || !d.isBefore(from)) && (to == null || !d.isAfter(to));
      if (ok)
        out.add(r);
    }
    return out;
  }
}
