package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;
import com.smartattendance.util.security.log.ApplicationLogger;

/**
 * A decorator rule that validates session status transitions for automatic
 * operations.
 * 
 * This rule implements the Decorator Pattern to wrap another AutoSessionRule
 * and adds
 * status validation logic to ensure sessions are only automatically started or
 * stopped
 * when they are in appropriate states according to the session lifecycle.
 * 
 * Session Lifecycle Status Rules:
 * Auto-Start Status Requirements:
 * - Session must be in "Pending" status
 * - Prevents auto-starting of already "Open" or "Closed" sessions
 * - Ensures only scheduled sessions can be automatically activated
 * 
 * Auto-Stop Status Requirements:
 * - Session must be in "Pending" or "Open" status
 * - Prevents auto-stopping of already "Closed" sessions
 * - Allows stopping of both scheduled and active sessions
 * 
 * This rule enforces the valid state transitions in the session lifecycle:
 * Pending → Open → Closed. It prevents invalid transitions that would violate
 * business logic and session integrity.
 * 
 * @author Lim Jia Hui
 * @version 21:57 16 Nov 2025
 */
public class StatusValidationRule implements AutoSessionRule {

    // ========== DEPENDENCIES ==========

    /** The wrapped rule that this decorator enhances with status validation */
    private final AutoSessionRule wrappedRule;

    /** Logger for tracking rule evaluation and status validation events */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    /**
     * Constructs a new StatusValidationRule that wraps the specified rule.
     *
     * @param wrappedRule the rule to be decorated with status validation logic,
     *                    must not be null
     * @throws IllegalArgumentException if wrappedRule is null
     * 
     */
    public StatusValidationRule(AutoSessionRule wrappedRule) {
        this.wrappedRule = wrappedRule;
    }

    // ========== RULE EVALUATION METHODS ==========

    /**
     * Determines if a session can be automatically started based on status
     * validation.
     * 
     * This method performs the following status validation:
     * Status Check:
     * - Verifies the session is in "Pending" status
     * 
     * Quick Rejection:
     * - Immediately returns false if the session is not
     * in "Pending" status,
     * preventing unnecessary downstream rule evaluations
     * 
     * Delegation:
     * - If the session is in "Pending" status, delegates to
     * the wrapped rule
     * for additional validation
     * 
     * This rule ensures that only scheduled sessions (Pending status) can be
     * automatically
     * started, preventing invalid state transitions like starting already open or
     * closed sessions.
     *
     * @param session the session to evaluate for automatic starting, must not be
     *                null
     * @return true if the session is in "Pending" status and the wrapped rule
     *         allows auto-start, false otherwise
     * @throws IllegalArgumentException if session is null
     */
    @Override
    public boolean canAutoStart(Session session) {
        // Can only auto-start if session is in Pending status
        if (!"Pending".equals(session.getStatus())) {
            appLogger.warn("StatusValidationRule: Session " + session.getSessionId() +
                    " status is " + session.getStatus() + " - CANNOT auto-start");
            return false;
        }

        return wrappedRule.canAutoStart(session);
    }

    /**
     * Determines if a session can be automatically stopped based on status
     * validation.
     * 
     * This method performs the following status validation:
     * Status Check:
     * - Verifies the session is not in "Closed" status
     * 
     * Quick Rejection:
     * - Immediately returns false if the session is in "Closed" status,
     * preventing unnecessary downstream rule evaluations
     * 
     * Delegation:
     * - If the session is in "Pending" or "Open" status, delegates to the wrapped
     * rule
     * for additional validation
     * 
     * This rule ensures that only active or scheduled sessions can be automatically
     * stopped,
     * preventing redundant stopping of already closed sessions while allowing both
     * pending
     * and open sessions to be stopped automatically.
     *
     * @param session the session to evaluate for automatic stopping, must not be
     *                null
     * @return true if the session is not in "Closed" status and the wrapped rule
     *         allows auto-stop, false otherwise
     * @throws IllegalArgumentException if session is null
     */
    @Override
    public boolean canAutoStop(Session session) {
        // Can only auto-stop if session is in Pending or Open status
        if ("Closed".equals(session.getStatus())) {
            appLogger.warn("StatusValidationRule: Session " + session.getSessionId() +
                    " status is " + session.getStatus() + " - CANNOT auto-stop");
            return false;
        }

        return wrappedRule.canAutoStop(session);
    }

    // ========== RULE DESCRIPTION METHOD ==========

    /**
     * Provides a comprehensive description of this rule's purpose and logic.
     * 
     * The description includes both this rule's status validation logic
     * and the description of the wrapped rule, demonstrating the decorator pattern.
     *
     * @return a descriptive string explaining the complete rule chain including
     *         status validation
     */
    @Override
    public String getRuleDescription() {
        return wrappedRule.getRuleDescription() + " + Status validation";
    }
}