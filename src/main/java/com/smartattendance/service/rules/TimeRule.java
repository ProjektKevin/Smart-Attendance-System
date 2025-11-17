package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;
import com.smartattendance.util.security.log.ApplicationLogger;

import java.time.LocalDateTime;

/**
 * A concrete rule implementation that validates temporal conditions for
 * automatic session operations.
 * 
 * This rule serves as the foundational rule in the AutoSessionRule decorator
 * chain,
 * providing core time-based validation for both automatic starting and stopping
 * of sessions.
 * It evaluates whether the current system time meets or exceeds the scheduled
 * session times.
 * 
 * Temporal Validation Rules:
 * Auto-Start Temporal Condition:
 * - Current time must be equal to or after the session's scheduled start time
 * - Uses inclusive comparison (isAfter OR isEqual)
 * - Ensures sessions only start when their scheduled time arrives
 * 
 * Auto-Stop Temporal Condition:
 * - Current time must be equal to or after the session's scheduled end time
 * - Uses inclusive comparison (isAfter OR isEqual)
 * - Ensures sessions only stop when their scheduled end time arrives
 * 
 * This rule is typically placed at the end of the decorator chain as it
 * performs
 * the fundamental time-based checks that other rules build upon with additional
 * business logic constraints.
 * 
 * @author Lim Jia Hui
 * @version 22:00 16 Nov 2025
 */
public class TimeRule implements AutoSessionRule {

    // ========== DEPENDENCIES ==========

    /** Logger for tracking temporal validation events and rule evaluation */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

    // ========== RULE EVALUATION METHODS ==========

    /**
     * Determines if a session can be automatically started based on temporal
     * conditions.
     * 
     * This method performs precise temporal validation by comparing the current
     * system time with the session's scheduled start time. The validation uses
     * inclusive comparison to ensure sessions start exactly at their scheduled
     * time.
     * 
     * Temporal Logic:
     * - currentTime >= sessionStartTime
     * 
     * The method combines the session date and start time to create a complete
     * LocalDateTime for accurate comparison with the current system time.
     *
     * @param session the session to evaluate for automatic starting, must not be
     *                null
     * @return true if current time meets or exceeds the session start time, false
     *         otherwise
     * @throws IllegalArgumentException if session is null or missing temporal
     *                                  attributes
     * 
     */
    @Override
    public boolean canAutoStart(Session session) {
        // For auto-start: only check if current time >= session start time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(session.getSessionDate(), session.getStartTime());

        boolean canStart = now.isAfter(startDateTime) || now.isEqual(startDateTime);
        appLogger.info("TimeRule: Can auto-start session " + session.getSessionId() +
                "? Current: " + now + ", Start: " + startDateTime + " - " + (canStart ? "YES" : "NO"));
        return canStart;
    }

    /**
     * Determines if a session can be automatically stopped based on temporal
     * conditions.
     * 
     * This method performs precise temporal validation by comparing the current
     * system time with the session's scheduled end time. The validation uses
     * inclusive comparison to ensure sessions stop exactly at their scheduled end
     * time.
     * 
     * Temporal Logic:
     * - currentTime >= sessionEndTime
     * 
     * The method combines the session date and end time to create a complete
     * LocalDateTime for accurate comparison with the current system time.
     *
     * @param session the session to evaluate for automatic stopping, must not be
     *                null
     * @return true if current time meets or exceeds the session end time, false
     *         otherwise
     * @throws IllegalArgumentException if session is null or missing temporal
     *                                  attributes
     * 
     */
    @Override
    public boolean canAutoStop(Session session) {
        // For auto-stop: only check if current time >= session end time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDateTime = LocalDateTime.of(session.getSessionDate(), session.getEndTime());

        boolean canStop = now.isAfter(endDateTime) || now.isEqual(endDateTime);
        appLogger.info("TimeRule: Can auto-stop session " + session.getSessionId() +
                "? Current: " + now + ", End: " + endDateTime + " - " + (canStop ? "YES" : "NO"));
        return canStop;
    }

    // ========== RULE DESCRIPTION METHOD ==========

    /**
     * Provides a comprehensive description of this rule's purpose and logic.
     * 
     * The description clearly explains the temporal validation logic and
     * emphasises that this rule is designed for background processing scenarios.
     *
     * @return a descriptive string explaining the temporal rule logic
     */
    @Override
    public String getRuleDescription() {
        return "Time-based auto session rules (for background processing only)";
    }
}