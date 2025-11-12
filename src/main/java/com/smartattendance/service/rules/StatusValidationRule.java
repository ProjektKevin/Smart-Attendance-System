package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;

public class StatusValidationRule implements AutoSessionRule {
    private final AutoSessionRule wrappedRule;
    
    public StatusValidationRule(AutoSessionRule wrappedRule) {
        this.wrappedRule = wrappedRule;
    }
    
    @Override
    public boolean canAutoStart(Session session) {
        // Can only auto-start if session is in Pending status
        if (!"Pending".equals(session.getStatus())) {
            System.out.println("StatusValidationRule: Session " + session.getSessionId() + 
                              " status is " + session.getStatus() + " - CANNOT auto-start");
            return false;
        }
        
        return wrappedRule.canAutoStart(session);
    }
    
    @Override
    public boolean canAutoStop(Session session) {
        // Can only auto-stop if session is in Pending or Open status
        if ("Closed".equals(session.getStatus())) {
            System.out.println("StatusValidationRule: Session " + session.getSessionId() + 
                              " status is " + session.getStatus() + " - CANNOT auto-stop");
            return false;
        }
        
        return wrappedRule.canAutoStop(session);
    }
    
    @Override
    public String getRuleDescription() {
        return wrappedRule.getRuleDescription() + " + Status validation";
    }
}