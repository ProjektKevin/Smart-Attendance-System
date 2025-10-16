/*
 # Modified by: Chue Wan Yan
 # Step: 3
 # Date: 13 Oct 2025
*/

package com.smartattendance.model;
import java.time.LocalDateTime; public class AttendanceRecord {
  private final Student student; 
  private final Session session; 
  // private final String status;
  private AttendanceStatus status;
  private LocalDateTime timestamp; 
  private MarkMethod method;
  // private final String method; 
  // private final double confidence; 
  // private final LocalDateTime timestamp; 
  private String notes;

  // public AttendanceRecord(Student s, Session sess, AttendanceStatus st, MarkMethod m, double c, LocalDateTime ts){ 
  //   this.student=s; 
  //   this.session=sess; 
  //   this.status=st; 
  //   this.method=m; 
  //   this.confidence=c; 
  //   this.timestamp=ts; 
  // }

  public AttendanceRecord(Student student, Session session) {
    this.student = student;
    this.session = session;
    this.status = AttendanceStatus.ABSENT;
    this.timestamp = null;
    this.method = null;
    this.notes = "";
  }

  public Student getStudent(){ 
    return student; 
  }
  
  public Session getSession(){ 
    return session; 
  } 
  
  public AttendanceStatus getStatus(){ 
    return status; 
  } 
  
  public MarkMethod getMethod(){ 
    return method; 
  } 
  
  // public double getConfidence(){ 
  //   return confidence; 
  // } 
  
  public LocalDateTime getTimestamp(){ 
    return timestamp; 
  } 
  
  public String getNote(){ 
    return notes; 
  } 
  
  public void setNote(String note){ 
    this.notes = note; 
  } 

  /**
     * Marks attendance with all relevant info.
     */
    public void mark(AttendanceStatus status, LocalDateTime timestamp, MarkMethod method, String notes) {
        this.status = status;
        this.timestamp = timestamp;
        this.method = method;
        if (notes != null) {
            this.notes = notes;
        }
    }
}
