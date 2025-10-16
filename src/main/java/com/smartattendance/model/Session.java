/*
 # Modified by: Chue Wan Yan
 # Step: 8
 # Date: 13 Oct 2025
 */

package com.smartattendance.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Session {

    private final String sessionId, courseId, location;
    private final LocalDate sessionDate;
    private final LocalTime startTime, endTime;
    private final int lateThresholdMinutes;
    private final List<Student> roster;
    private boolean open;

    public Session(String sid, String cid, LocalDate d, LocalTime s, LocalTime e, String loc, int late) {
        this.sessionId = sid;
        this.courseId = cid;
        this.sessionDate = d;
        this.startTime = s;
        this.endTime = e;
        this.location = loc;
        this.lateThresholdMinutes = late;
        this.roster = new ArrayList<>();
    }

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

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
    }

    public void close() {
        open = false;
    }

    public void addStudent(Student s) {
        roster.add(s);
    }
}
