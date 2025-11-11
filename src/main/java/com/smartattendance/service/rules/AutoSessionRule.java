package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;

public interface AutoSessionRule {
    boolean canAutoStart(Session session);
    boolean canAutoStop(Session session);
    String getRuleDescription();
}