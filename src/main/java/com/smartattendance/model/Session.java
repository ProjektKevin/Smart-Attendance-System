// package com.smartattendance.model;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;

// public class Session extends AbstractEntity {

//   private final String course, location;
//   private final LocalDate sessionDate;
//   private final LocalTime startTime, endTime;
//   private final int lateThresholdMinutes;
//   private int sessionId;
//   private String status;

//   public Session(String course, LocalDate sessionDate,
//       LocalTime startTime, LocalTime endTime, String location, int late) {
//       this(0, course, sessionDate, startTime, endTime, location, late, "Pending");
//       this.status = determineStatus(sessionDate, startTime, endTime);
//   }

//   public Session(int sessionId, String course, LocalDate sessionDate,
//       LocalTime startTime, LocalTime endTime, String location, int late, String status) {
//     this.sessionId = sessionId;
//     this.course = course;
//     this.sessionDate = sessionDate;
//     this.startTime = startTime;
//     this.endTime = endTime;
//     this.location = location;
//     this.lateThresholdMinutes = late;
//     this.status = status;
//   }

//   // ---------------------------------------------------------
//   // Determine initial status based on current date/time
//   // ---------------------------------------------------------
//   private String determineStatus(LocalDate sessionDate, LocalTime startTime, LocalTime endTime) {
//     LocalDateTime now = LocalDateTime.now();
//     LocalDateTime start = LocalDateTime.of(sessionDate, startTime);
//     LocalDateTime end = LocalDateTime.of(sessionDate, endTime);

//     if (now.isBefore(start)) {
//       return status = "Pending"; // not started yet
//     } else if (now.isAfter(end)) {
//       return status = "Closed"; // already ended
//     } else {
//       return status = "Open"; // currently active
//     }
//   }

//   // ---------------------------------------------------------
//   // Getters & utility methods
//   // ---------------------------------------------------------
//   public int getSessionId() {
//     return sessionId;
//   }

//   public String getCourse() {
//     return course;
//   }

//   public LocalDate getSessionDate() {
//     return sessionDate;
//   }

//   public LocalTime getStartTime() {
//     return startTime;
//   }

//   public LocalTime getEndTime() {
//     return endTime;
//   }

//   public String getLocation() {
//     return location;
//   }

//   public int getLateThresholdMinutes() {
//     return lateThresholdMinutes;
//   }

//   public String getStatus(){
//     return status;
//   }

//   public void setSessionId(int id){
//     this.sessionId = id;
//   }

//   public void open() {
//     this.status = "Open";
//   }

//   public void close() {
//     this.status = "Closed";
//   }

//   public void setPending() {
//     this.status = "Pending";
//   }

//   public boolean isOpen() {
//     return status == "Open";
//   }
// }