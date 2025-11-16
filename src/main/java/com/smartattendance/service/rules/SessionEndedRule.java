package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;
import com.smartattendance.util.security.log.ApplicationLogger;

import java.time.LocalDateTime;

/**
 * A decorator rule that prevents automatic starting of sessions that have
 * already ended.
 * 
 * This rule implements the Decorator Pattern to wrap another AutoSessionRule
 * and adds
 * temporal validation logic to ensure sessions are only automatically started
 * if they
 * haven't already passed their scheduled end time.
 * 
 * Rule Logic:
 * - Auto-Start Prevention: Sessions that have ended cannot be
 * automatically started.
 * This prevents starting sessions that are already in the past.
 * 
 * - Auto-Stop Allowance: Sessions that have ended can still be
 * automatically stopped.
 * This ensures cleanup of sessions that might still be open past their end
 * time.
 * 
 * - Temporal Validation: Uses current system time compared to session
 * end time
 * to determine if a session has ended.
 * 
 * This rule is typically placed early in the decorator chain to quickly reject
 * invalid sessions without performing unnecessary downstream rule evaluations.
 * 
 * @author 21:50 Lim Jia Hui
 * @version
 */
public class SessionEndedRule implements AutoSessionRule {

    // ========== DEPENDENCIES ==========

    /** The wrapped rule that this decorator enhances with temporal validation */
    private final AutoSessionRule wrappedRule;

    /** Logger for tracking rule evaluation and temporal validation events */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    // ========== CONSTRUCTOR ==========

    /**
     * Constructs a new SessionEndedRule that wraps the specified rule.
     *
     * @param wrappedRule the rule to be decorated with temporal validation logic,
     *                    must not be null
     * @throws IllegalArgumentException if wrappedRule is null
     * 
     */
    public SessionEndedRule(AutoSessionRule wrappedRule) {
        this.wrappedRule = wrappedRule;
    }

    // ========== RULE EVALUATION METHODS ==========

    /**
     * Determines if a session can be automatically started based on temporal
     * validity.
     * 
     * This method performs the following validation:
     * Temporal Check:
     * - Verifies the session has not already ended by comparing
     * current system time with the session's scheduled end time
     * 
     * Quick Rejection:
     * - Immediately returns false if the session has ended,
     * preventing unnecessary downstream rule evaluations
     * 
     * Delegation:
     * - If the session has not ended, delegates to the wrapped rule
     * for additional validation
     * 
     * This rule acts as an early filter to quickly reject sessions that are
     * temporally
     * invalid, improving performance by avoiding complex rule evaluations for ended
     * sessions.
     *
     * @param session the session to evaluate for automatic starting, must not be
     *                null
     * @return true if the session hasn't ended and the wrapped rule allows
     *         auto-start, false otherwise
     * @throws IllegalArgumentException if session is null
     * 
     * @see #hasSessionEnded(Session)
     */
    @Override
    public boolean canAutoStart(Session session) {
        // Cannot auto-start sessions that have already ended
        if (hasSessionEnded(session)) {
            appLogger.info(
                    "SessionEndedRule: Session " + session.getSessionId() + " has already ended - CANNOT auto-start");
            return false;
        }

        return wrappedRule.canAutoStart(session);
    }

    /**
     * Determines if a session can be automatically stopped.
     * 
     * This method does not perform temporal validation for auto-stop operations
     * because:
     * - Sessions that have ended should still be allowed to auto-stop for cleanup
     * - Auto-stop might be needed for sessions that are still open past their end
     * time
     * - Temporal constraints are less critical for stopping operations
     * 
     * The method immediately delegates to the wrapped rule for auto-stop
     * validation,
     * allowing ended sessions to be properly closed if necessary.
     *
     * @param session the session to evaluate for automatic stopping, must not be
     *                null
     * @return the result of the wrapped rule's auto-stop evaluation
     * @throws IllegalArgumentException if session is null
     */
    @Override
    public boolean canAutoStop(Session session) {
        // For auto-stop, we still want to allow stopping sessions that have ended
        // (in case they are still open for some reason)
        return wrappedRule.canAutoStop(session);
    }

    // ========== RULE DESCRIPTION METHOD ==========

    /**
     * Provides a comprehensive description of this rule's purpose and logic.
     * 
     * The description includes both this rule's temporal validation logic
     * and the description of the wrapped rule, demonstrating the decorator pattern.
     *
     * @return a descriptive string explaining the complete rule chain including
     *         temporal validation
     */
    @Override
    public String getRuleDescription() {
        return wrappedRule.getRuleDescription() + " + No ended sessions";
    }

    // ========== TEMPORAL VALIDATION METHODS ==========

    /**
     * Determines if a session has ended based on current system time and session
     * end time.
     * 
     * A session is considered ended if the current system time is after the
     * session's
     * scheduled end time. The comparison uses the session date combined with the
     * end time
     * to create a complete LocalDateTime for accurate temporal comparison.
     *
     * @param session the session to check for ended status, must not be null
     * @return true if the current time is after the session's end time, false
     *         otherwise
     * @throws IllegalArgumentException if session is null
     * 
     */
    private boolean hasSessionEnded(Session session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.of(session.getSessionDate(), session.getEndTime());
        return now.isAfter(endDateTime);
    }
}