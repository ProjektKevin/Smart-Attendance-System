package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.security.log.ApplicationLogger;

public class ConflictPreventionRule implements AutoSessionRule {
    private final AutoSessionRule wrappedRule;
    private final SessionService sessionService;
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    public ConflictPreventionRule(AutoSessionRule wrappedRule, SessionService sessionService) {
        this.wrappedRule = wrappedRule;
        this.sessionService = sessionService;
    }

    @Override
    public boolean canAutoStart(Session session) {
        // Only check conflict prevention for sessions that actually have autoStart
        // enabled
        if (!session.isAutoStart()) {
            appLogger.info("ConflictPreventionRule: Session " + session.getSessionId()
                    + " doesn't have autoStart - skipping conflict check");
            return wrappedRule.canAutoStart(session);
        }

        // Check if there are OTHER auto-start sessions (excluding this one)
        if (sessionService.hasOtherAutoStartSession(session.getSessionId())) {
            appLogger.warn("ConflictPreventionRule: Other auto-start sessions exist - BLOCKED");
            return false;
        }

        // Check if there is already an open session
        if (sessionService.isSessionOpen()) {
            appLogger.warn("ConflictPreventionRule: There is already an open session - BLOCKED");
            return false;
        }

        appLogger.info("ConflictPreventionRule: No conflicts - ALLOWED");
        return wrappedRule.canAutoStart(session);
    }

    @Override
    public boolean canAutoStop(Session session) {
        // Only check conflict prevention for sessions that actually have autoStart
        // enabled
        if (!session.isAutoStop()) {
            appLogger.info("ConflictPreventionRule: Session " + session.getSessionId()
                    + " doesn't have autoStop - skipping conflict check");
            return wrappedRule.canAutoStop(session);
        }

        // Check if there are OTHER auto-start sessions (excluding this one)
        if (sessionService.hasOtherAutoStopSession(session.getSessionId())) {
            appLogger.warn("ConflictPreventionRule: Other auto-stop sessions exist - BLOCKED");
            return false;
        }

        appLogger.info("ConflictPreventionRule: No other auto-stop sessions - ALLOWED");
        return wrappedRule.canAutoStop(session);
    }

    @Override
    public String getRuleDescription() {
        return wrappedRule.getRuleDescription() + " + Single auto-start and no open sessions enforcement";
    }
}