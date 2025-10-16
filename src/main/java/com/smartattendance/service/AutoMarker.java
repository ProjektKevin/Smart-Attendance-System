/*
 # Done by: Chue Wan Yan
 # Step: 2
 # Date: 13 Oct 2025
*/

package com.smartattendance.service;

import com.smartattendance.model.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Automatically marks attendance using face recognition confidence values.
 * Implements Strategy + Observer pattern.
 */
public class AutoMarker implements AttendanceMarker {

    private final Map<String, AttendanceRecord> attendanceMap; // All attendance records
    private final Session session;                             // Session being managed
    private final Set<AttendanceObserver> observers = new HashSet<>();

    // Configurable thresholds and timers
    private final float confidenceThreshold;
    private final float lowConfidenceThreshold;
    private final int lateThresholdMinutes;
    private final int cooldownSeconds;

    // Track last seen time for each student to avoid duplicate marking
    private final Map<String, LocalDateTime> lastMarkedTime = new HashMap<>();

    /**
     * Callback interface for confirming low-confidence matches.
     */
    public interface LowConfidenceCallback {
        boolean confirm(String studentName);
    }

    private final LowConfidenceCallback lowConfidenceCallback;

    /**
     * Constructor with configurable parameters.
     */
    public AutoMarker(Session session,
                      Map<String, AttendanceRecord> attendanceMap,
                      float confidenceThreshold,
                      float lowConfidenceThreshold,
                      int lateThresholdMinutes,
                      int cooldownSeconds,
                      LowConfidenceCallback lowConfidenceCallback) {
        this.session = session;
        this.attendanceMap = attendanceMap;
        this.confidenceThreshold = confidenceThreshold;
        this.lowConfidenceThreshold = lowConfidenceThreshold;
        this.lateThresholdMinutes = lateThresholdMinutes;
        this.cooldownSeconds = cooldownSeconds;
        this.lowConfidenceCallback = lowConfidenceCallback;
    }

    /**
     * Core method to mark attendance based on confidence level.
     */
    @Override
    public void markAttendance(String studentId, float confidence) {
        AttendanceRecord record = attendanceMap.get(studentId);
        if (record == null) {
            System.out.println("Student not in roster.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Prevent duplicate markings within cooldown period
        LocalDateTime lastTime = lastMarkedTime.get(studentId);
        if (lastTime != null && now.isBefore(lastTime.plusSeconds(cooldownSeconds))) {
            System.out.println("Cooldown active: skipping duplicate mark.");
            return;
        }

        // Handle low-confidence match with confirmation
        if (confidence >= lowConfidenceThreshold && confidence < confidenceThreshold) {
            boolean confirmed = lowConfidenceCallback.confirm(record.getStudent().getName());
            if (!confirmed) {
                System.out.println("Recognition not confirmed by user.");
                return;
            }
        }

        // Reject matches below confidence threshold
        if (confidence < confidenceThreshold) {
            System.out.println("Confidence too low.");
            return;
        }

        // Determine if late
        AttendanceStatus status = AttendanceStatus.PRESENT;
        if (now.isAfter(session.getStartTime().plusMinutes(lateThresholdMinutes))) {
            status = AttendanceStatus.LATE;
        }

        // Update record only if not already marked
        if (record.getStatus() == AttendanceStatus.ABSENT) {
            record.mark(status, now, MarkMethod.AUTO, null);
            lastMarkedTime.put(studentId, now);
            notifyObservers(record);
        }
    }

    @Override
    public void registerObserver(AttendanceObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregisterObserver(AttendanceObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies GUI or log observers that attendance was updated.
     */
    private void notifyObservers(AttendanceRecord record) {
        for (AttendanceObserver obs : observers) {
            obs.attendanceUpdated(record);
        }
    }
}
