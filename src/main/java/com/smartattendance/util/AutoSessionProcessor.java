package com.smartattendance.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import com.smartattendance.service.SessionService;

/**
 * Background processor for automatic session management using scheduled tasks.
 * 
 * This class provides automated processing of session lifecycle events through
 * a scheduled timeline that runs at regular intervals. It handles both
 * automatic
 * session starting and stopping based on configured rules and timing.
 * 
 * Key features:
 * - Scheduled background processing every 30 seconds
 * - Automatic session start/stop using decorator pattern rules
 * - Real-time table updates to reflect session status changes
 * - Thread-safe operation through JavaFX Timeline
 * - Start/stop control for processor lifecycle management
 * 
 * The processor utilizes the Decorator Pattern implemented in SessionService
 * to evaluate complex business rules for automatic session management.
 * 
 * @author Lim Jia Hui
 * @version 20:40 16 Nov 2025
 */
public class AutoSessionProcessor {

    // ========== DEPENDENCIES ==========

    /** Service for session business logic and automatic session processing */
    private final SessionService sessionService;

    /** Utility for refreshing session table data in the UI */
    private final SessionTableUtil tableUtil;

    /** Timeline for scheduling background processing tasks */
    private Timeline timeline;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructs a new AutoSessionProcessor with required dependencies.
     *
     * @param sessionService the service handling session business logic and
     *                       auto-processing,
     *                       must not be null
     * @param tableUtil      the utility for refreshing session table data in the
     *                       UI,
     *                       must not be null
     * @throws IllegalArgumentException if sessionService or tableUtil is null
     */
    public AutoSessionProcessor(SessionService sessionService, SessionTableUtil tableUtil) {
        this.sessionService = sessionService;
        this.tableUtil = tableUtil;
    }

    // ========== PROCESSOR LIFECYCLE METHODS ==========

    /**
     * Executes a single processing cycle including auto-session management and UI
     * updates.
     * 
     * This method is called automatically by the timeline at scheduled intervals
     * and performs the core processing logic:
     * - Processes automatic sessions using decorator pattern rules
     * - Refreshes the session table to reflect status changes
     * 
     */
    public void start() {
        // Creates and configures the processing timeline with the scheduled task.
        // Creates a key frame that defines the processing task to be executed.
        timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            // Uses decorator pattern rules
            sessionService.processAutoSessions();

            // Refresh table to reflect any session status changes
            tableUtil.loadSessions();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}