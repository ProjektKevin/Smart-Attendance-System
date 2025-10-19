package com.smartattendance.model;

import java.time.*;
import java.util.*;

public class Session extends AbstractEntity {

  // public enum SessionStatus {
  //   PENDING, OPEN, CLOSED
  // }

  private final String course, location;
  private final LocalDate sessionDate;
  private final LocalTime startTime, endTime;
  private final int lateThresholdMinutes;
  private int sessionId;
  private String status;

  public Session(String course, LocalDate sessionDate,
      LocalTime startTime, LocalTime endTime, String location, int late) {
      this(0, course, sessionDate, startTime, endTime, location, late, "PENDING");
  }

  public Session(int sessionId, String course, LocalDate sessionDate,
      LocalTime startTime, LocalTime endTime, String location, int late, String status) {
    this.sessionId = sessionId;
    this.course = course;
    this.sessionDate = sessionDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.lateThresholdMinutes = late;
    this.status = status;
  }

  // ---------------------------------------------------------
  // Determine initial status based on current date/time
  // ---------------------------------------------------------
  // private String determineInitialStatus(LocalDate sessionDate, LocalTime startTime, LocalTime endTime) {
  //   LocalDateTime now = LocalDateTime.now();
  //   LocalDateTime start = LocalDateTime.of(sessionDate, startTime);
  //   LocalDateTime end = LocalDateTime.of(sessionDate, endTime);

  //   if (now.isBefore(start)) {
  //     return status = "PENDING"; // not started yet
  //   } else if (now.isAfter(end)) {
  //     return status = "CLOSED"; // already ended
  //   } else {
  //     return status = "OPEN"; // currently active
  //   }
  // }

  // ---------------------------------------------------------
  // Getters & utility methods
  // ---------------------------------------------------------
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

  public String getStatus(){
    return status;
  }

  public void setSessionId(int id){
    this.sessionId = id;
  }


//   public void open() {
//     status = SessionStatus.OPEN;
//   }

//   public void close() {
//     status = SessionStatus.CLOSED;
//   }

//   public void setPending() {
//     status = SessionStatus.PENDING;
//   }

//   public boolean isOpen() {
//     return status == SessionStatus.OPEN;
//   }
}