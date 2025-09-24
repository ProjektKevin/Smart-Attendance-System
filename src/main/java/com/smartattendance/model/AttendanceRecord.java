package com.smartattendance.model;
import java.time.*; public class AttendanceRecord {
  private final Student student; private final Session session; private final String status, method; private final double confidence; private final LocalDateTime timestamp; private String note;
  public AttendanceRecord(Student s, Session sess, String st, String m, double c, LocalDateTime ts){ this.student=s; this.session=sess; this.status=st; this.method=m; this.confidence=c; this.timestamp=ts; }
  public Student getStudent(){ return student; } public Session getSession(){ return session; } public String getStatus(){ return status; } public String getMethod(){ return method; } public double getConfidence(){ return confidence; } public LocalDateTime getTimestamp(){ return timestamp; } public String getNote(){ return note; } public void setNote(String n){ note=n; } }
