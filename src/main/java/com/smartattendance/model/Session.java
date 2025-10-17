package com.smartattendance.model;
import java.time.*;
import java.util.*;

public class Session extends AbstractEntity{
  private final String sessionId, courseId, location; 
  private final LocalDate sessionDate; 
  private final LocalTime startTime, endTime; 
  private final int lateThresholdMinutes; 
  private boolean open;
  private final Roster roster = new Roster();
  private final List<AttendanceTracker> attendanceTrackers = new ArrayList<>();

  public Session(String sessionId,String courseId, LocalDate sessionDate,LocalTime startTime,LocalTime endTime,String location,int late){ 
    this.sessionId=sessionId; 
    this.courseId=courseId; 
    this.sessionDate=sessionDate; 
    this.startTime=startTime; 
    this.endTime=endTime; 
    this.location=location; 
    this.lateThresholdMinutes=late; 
  }

  public String getSessionId(){
    return sessionId; 
  } 
  
  public String getCourseId(){ 
    return courseId; 
  } 
  
  public LocalDate getSessionDate(){ 
    return sessionDate; 
  } 
  
  public LocalTime getStartTime(){
    return startTime;
  } 

  public LocalTime getEndTime(){ 
    return endTime; 
  } 
  
  public String getLocation(){
    return location;
  } 
   
  public int getLateThresholdMinutes(){ 
    return lateThresholdMinutes; 
  } 
  
  public boolean isOpen(){
    return open;
  } 
  
  public void open(){ 
    open=true; 
  } 
  
  public void close(){ 
    open=false; 
  }

  public Roster getRoster() {
    return roster;
  } 

  public void addStudentToRoster(Student student) {
        roster.addStudent(student);
        attendanceTrackers.add(new AttendanceTracker(student));
  }

  public List<AttendanceTracker> getAttendanceTrackers() {
    return Collections.unmodifiableList(attendanceTrackers);
}

}