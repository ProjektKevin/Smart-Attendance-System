package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;

/**
 * Defines the contract for automatic session processing rules in the Smart
 * Attendance System.
 * 
 * This interface represents the core component of the Rule Pattern
 * implementation
 * for automatic session management. Rules implementing this interface can be
 * composed
 * using the Decorator Pattern to create complex validation chains.
 * 
 * Key features:
 * - Separates business rules from core session logic
 * - Supports rule composition through decorators
 * - Provides self-documenting rule descriptions
 * - Enables flexible rule evaluation for auto-start and auto-stop operations
 * 
 * Usage Pattern:
 * 
 * <pre>
 * {@code
 * // Create a rule chain using decorators
 * AutoSessionRule ruleChain = new SessionEndedRule(
 *         new StatusValidationRule(
 *                 new ConflictPreventionRule(
 *                         new TimeRule())));
 * 
 * // Evaluate rules for a session
 * if (ruleChain.canAutoStart(session)) {
 *     // Proceed with auto-start
 * }
 * }
 * </pre>
 * 
 * @author Lim Jia Hui
 * @version 21:30 16 Nov 2025
 */
public interface AutoSessionRule {

    /**
     * Checks whether a session is allowed to auto-start based on
     * all applied business rules.
     *
     * Implementations may evaluate factors such as session state,
     * schedule timing, conflicts with other active sessions, and
     * system policies. The method should return true only if every
     * relevant rule is satisfied.
     *
     * @param session the session to evaluate, must not be null
     * @return true if the session passes all auto-start rules
     * @throws IllegalArgumentException if session is null
     */
    boolean canAutoStart(Session session);

    /**
     * Checks whether a session is allowed to auto-stop based on
     * all applied business rules.
     *
     * Implementations may evaluate factors such as session status,
     * end time, attendance completeness, minimum duration, and
     * system policies. The method should return true only if every
     * relevant rule is satisfied.
     *
     * @param session the session to evaluate, must not be null
     * @return true if the session passes all auto-stop rules
     * @throws IllegalArgumentException if session is null
     */
    boolean canAutoStop(Session session);

    /**
     * Returns a human-readable description of the rule.
     *
     * @return a short description of the rule's purpose
     */
    String getRuleDescription();
}