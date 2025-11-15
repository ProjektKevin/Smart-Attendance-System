package com.smartattendance.model.entity;

import java.time.*;

import com.smartattendance.util.security.log.ApplicationLogger;

public class Session {

  private final String course, location;
  private final LocalDate sessionDate;
  private final LocalTime startTime, endTime;
  private final int lateThresholdMinutes;
  private int sessionId;
  private String status;
  private boolean autoStart;
  private boolean autoStop;

  // Logger
  private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

  // For session creation using form
  public Session(String course, LocalDate sessionDate,
      LocalTime startTime, LocalTime endTime, String location, int late) {
    this(0, course, sessionDate, startTime, endTime, location, late, "Pending", false, false);
  }

  // For session creation when fetching from database
  public Session(int sessionId, String course, LocalDate sessionDate,
      LocalTime startTime, LocalTime endTime, String location, int late, String status, boolean autoStart,
      boolean autoStop) {
    this.sessionId = sessionId;
    this.course = course;
    this.sessionDate = sessionDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.lateThresholdMinutes = late;
    this.status = status;
    this.autoStart = autoStart;
    this.autoStop = autoStop;
  }

  // Getters & utility methods
  public int getSessionId() {
    return sessionId;
  }

  public String getCourse() {
    return course;
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
    return status;
  }

  public void setSessionId(int id) {
    this.sessionId = id;
  }

  public void open() {
    this.status = "Open";
  }

  public void close() {
    this.status = "Closed";
  }

  public void setPending() {
    this.status = "Pending";
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isAutoStart() {
    return autoStart;
  }

  public void setAutoStart(boolean autoStart) {
    this.autoStart = autoStart;
  }

  public boolean isAutoStop() {
    return autoStop;
  }

  public void setAutoStop(boolean autoStop) {
    this.autoStop = autoStop;
  }
}