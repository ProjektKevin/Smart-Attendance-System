package com.smartattendance.service.rules;

import com.smartattendance.model.entity.Session;
import com.smartattendance.util.security.log.ApplicationLogger;

import java.time.LocalDateTime;

/**
 * A concrete rule implementation that validates temporal conditions for automatic session operations.
 * 
 * <p>This rule serves as the foundational rule in the AutoSessionRule decorator chain,
 * providing core time-based validation for both automatic starting and stopping of sessions.
 * It evaluates whether the current system time meets or exceeds the scheduled session times.</p>
 * 
 * <p>Temporal Validation Rules:
 * <ul>
 *   <li><b>Auto-Start Temporal Condition:</b>
 *     <ul>
 *       <li>Current time must be equal to or after the session's scheduled start time</li>
 *       <li>Uses inclusive comparison (isAfter OR isEqual)</li>
 *       <li>Ensures sessions only start when their scheduled time arrives</li>
 *     </ul>
 *   </li>
 *   <li><b>Auto-Stop Temporal Condition:</b>
 *     <ul>
 *       <li>Current time must be equal to or after the session's scheduled end time</li>
 *       <li>Uses inclusive comparison (isAfter OR isEqual)</li>
 *       <li>Ensures sessions only stop when their scheduled end time arrives</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * <p>This rule is typically placed at the end of the decorator chain as it performs
 * the fundamental time-based checks that other rules build upon with additional
 * business logic constraints.</p>
 * 
 * @author Smart Attendance System Team
 * @version 1.0
 * @since 2025
 */
public class TimeRule implements AutoSessionRule {
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

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

    @Override
    public String getRuleDescription() {
        return "Time-based auto session rules (for background processing only)";
    }
}