package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * A decorator rule that prevents session conflicts by enforcing single active
 * session policies.
 * 
 * This rule implements the Decorator Pattern to wrap another AutoSessionRule
 * and adds
 * conflict prevention logic to ensure system integrity. It prevents multiple
 * automatic
 * sessions from running concurrently and maintains the single-active-session
 * constraint.
 * 
 * Conflict Prevention Rules:
 * Auto-Start Conflict Prevention:
 * - Only one session can have auto-start enabled at a time
 * - No other sessions can be open when starting automatically
 * - Applies only to sessions with auto-start enabled
 * 
 * Auto-Stop Conflict Prevention:
 * - Only one session can have auto-stop enabled at a time
 * - Applies only to sessions with auto-stop enabled
 * 
 * This rule works in conjunction with other rules in the decorator chain, only
 * applying its conflict checks when relevant and delegating to the wrapped rule
 * for additional validation.
 * 
 * @author Lim Jia Hui
 * @version 21:44 16 Nov 2025
 */
public class ConflictPreventionRule implements AutoSessionRule {

    // ========== DEPENDENCIES ==========

    /** The wrapped rule that this decorator enhances with conflict prevention */
    private final AutoSessionRule wrappedRule;

    /** Service for checking session states and conflicts */
    private final SessionService sessionService;

    /** Logger for tracking rule evaluation and conflict events */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    // ========== CONSTRUCTOR ==========

    /**
     * Constructs a new ConflictPreventionRule that wraps the specified rule.
     *
     * @param wrappedRule    the rule to be decorated with conflict prevention
     *                       logic, must not be null
     * @param sessionService the service for session state checks, must not be null
     * @throws IllegalArgumentException if wrappedRule or sessionService is null
     * 
     */
    public ConflictPreventionRule(AutoSessionRule wrappedRule, SessionService sessionService) {
        this.wrappedRule = wrappedRule;
        this.sessionService = sessionService;
    }

    // ========== RULE EVALUATION METHODS ==========

    /**
     * Determines if a session can be automatically started while preventing
     * conflicts.
     * 
     * This method performs the following conflict checks:
     * - Auto-Start Relevance Check: Only applies conflict rules if the
     * session has auto-start enabled
     * - Other Auto-Start Sessions Check: Ensures no other sessions have
     * auto-start enabled
     * - Open Session Check: Ensures no sessions are currently open
     * - Wrapped Rule Delegation: Delegates to the wrapped rule if all
     * conflict checks pass
     * 
     * If the session doesn't have auto-start enabled, this rule immediately
     * delegates
     * to the wrapped rule without performing conflict checks.
     *
     * @param session the session to evaluate for automatic starting, must not be
     *                null
     * @return true if no conflicts exist and the wrapped rule allows auto-start,
     *         false otherwise
     * @throws IllegalArgumentException if session is null
     * 
     * @see SessionService#hasOtherAutoStartSession(int)
     * @see SessionService#isSessionOpen()
     */
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

    /**
     * Determines if a session can be automatically stopped while preventing
     * conflicts.
     * 
     * This method performs the following conflict checks:
     * - Auto-Stop Relevance Check: Only applies conflict rules if the
     * session has auto-stop enabled
     * - Other Auto-Stop Sessions Check: Ensures no other sessions have
     * auto-stop enabled
     * - Wrapped Rule Delegation: Delegates to the wrapped rule if all
     * conflict checks pass
     * 
     * If the session doesn't have auto-stop enabled, this rule immediately
     * delegates
     * to the wrapped rule without performing conflict checks.
     *
     * @param session the session to evaluate for automatic stopping, must not be
     *                null
     * @return true if no conflicts exist and the wrapped rule allows auto-stop,
     *         false otherwise
     * @throws IllegalArgumentException if session is null
     * 
     * @see SessionService#hasOtherAutoStopSession(int)
     */
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

    // ========== RULE DESCRIPTION METHOD ==========

    /**
     * Provides a comprehensive description of this rule's purpose and logic.
     * 
     * The description includes both this rule's conflict prevention logic
     * and the description of the wrapped rule, demonstrating the decorator pattern.
     *
     * @return a descriptive string explaining the complete rule chain including
     *         conflict prevention
     */
    @Override
    public String getRuleDescription() {
        return wrappedRule.getRuleDescription() + " + Single auto-start and no open sessions enforcement";
    }
}