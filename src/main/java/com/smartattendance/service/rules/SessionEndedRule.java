package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;
import java.time.LocalDateTime;

public class SessionEndedRule implements AutoSessionRule {
    private final AutoSessionRule wrappedRule;
    
    public SessionEndedRule(AutoSessionRule wrappedRule) {
        this.wrappedRule = wrappedRule;
    }
    
    @Override
    public boolean canAutoStart(Session session) {
        // Cannot auto-start sessions that have already ended
        if (hasSessionEnded(session)) {
            System.out.println("SessionEndedRule: Session " + session.getSessionId() + " has already ended - CANNOT auto-start");
            return false;
        }
        
        return wrappedRule.canAutoStart(session);
    }
    
    @Override
    public boolean canAutoStop(Session session) {
        // For auto-stop, we still want to allow stopping sessions that have ended
        // (in case they're still open for some reason)
        return wrappedRule.canAutoStop(session);
    }
    
    @Override
    public String getRuleDescription() {
        return wrappedRule.getRuleDescription() + " + No ended sessions";
    }
    
    private boolean hasSessionEnded(Session session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.of(session.getSessionDate(), session.getEndTime());
        return now.isAfter(endDateTime);
    }
}