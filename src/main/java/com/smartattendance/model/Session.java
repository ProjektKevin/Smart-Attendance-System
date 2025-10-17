package com.smartattendance.model;

import java.time.*;
import java.util.*;

public class Session extends AbstractEntity {

  public enum SessionStatus {
    PENDING, OPEN, CLOSED
  }

  private final String sessionId, courseId, location;
  private final LocalDate sessionDate;
  private final LocalTime startTime, endTime;
  private final int lateThresholdMinutes;
  private SessionStatus status; // ✅ replaces boolean open
  private final Roster roster = new Roster();
  private final List<AttendanceTracker> attendanceTrackers = new ArrayList<>();

  public Session(String sessionId, String courseId, LocalDate sessionDate,
      LocalTime startTime, LocalTime endTime, String location, int late) {
    this.sessionId = sessionId;
    this.courseId = courseId;
    this.sessionDate = sessionDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.lateThresholdMinutes = late;

    // Automatically determine initial status
    this.status = determineInitialStatus();
  }

  // ---------------------------------------------------------
  // Determine initial status based on current date/time
  // ---------------------------------------------------------
  private SessionStatus determineInitialStatus() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime start = LocalDateTime.of(sessionDate, startTime);
    LocalDateTime end = LocalDateTime.of(sessionDate, endTime);

    if (now.isBefore(start)) {
      return SessionStatus.PENDING; // not started yet
    } else if (now.isAfter(end)) {
      return SessionStatus.CLOSED; // already ended
    } else {
      return SessionStatus.OPEN; // currently active
    }
  }

  // ---------------------------------------------------------
  // Getters & utility methods
  // ---------------------------------------------------------
  public String getSessionId() {
    return sessionId;
  }

  public String getCourseId() {
    return courseId;
  }

  public LocalDate getSessionDate() {
    return sessionDate;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public String getLocation() {
    return location;
  }

  public int getLateThresholdMinutes() {
    return lateThresholdMinutes;
  }

  public String getStatus() {
    return status.name();
  }

  public SessionStatus getStatusEnum() {
    return status;
  }

  public void open() {
    status = SessionStatus.OPEN;
  }

  public void close() {
    status = SessionStatus.CLOSED;
  }

  public void setPending() {
    status = SessionStatus.PENDING;
  }

  public boolean isOpen() {
    return status == SessionStatus.OPEN;
  }

  public Roster getRoster() {
    return roster;
  }

  public List<AttendanceTracker> getAttendanceTrackers() {
    return Collections.unmodifiableList(attendanceTrackers);
  }

  public void addStudentToRoster(Student student) {
    roster.addStudent(student);
    attendanceTrackers.add(new AttendanceTracker(student));
  }
}