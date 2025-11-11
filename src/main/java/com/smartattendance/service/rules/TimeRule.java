package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;
import java.time.LocalDateTime;

public class TimeRule implements AutoSessionRule {
    
    @Override
    public boolean canAutoStart(Session session) {
        // For auto-start: only check if current time >= session start time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        
        boolean canStart = now.isAfter(startDateTime) || now.isEqual(startDateTime);
        System.out.println("TimeRule: Can auto-start session " + session.getSessionId() + 
                          "? Current: " + now + ", Start: " + startDateTime + " - " + (canStart ? "YES" : "NO"));
        return canStart;
    }
    
    @Override
    public boolean canAutoStop(Session session) {
        // For auto-stop: only check if current time >= session end time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.of(session.getSessionDate(), session.getEndTime());
        
        boolean canStop = now.isAfter(endDateTime) || now.isEqual(endDateTime);
        System.out.println("TimeRule: Can auto-stop session " + session.getSessionId() + 
                          "? Current: " + now + ", End: " + endDateTime + " - " + (canStop ? "YES" : "NO"));
        return canStop;
    }
    
    @Override
    public String getRuleDescription() {
        return "Time-based auto session rules (for background processing only)";
    }
}