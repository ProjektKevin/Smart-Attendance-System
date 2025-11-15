package com.smartattendance.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import com.smartattendance.service.SessionService;

public class AutoSessionProcessor {

    private final SessionService sessionService;
    private final SessionTableService tableService;
    private Timeline timeline;

    public AutoSessionProcessor(SessionService sessionService, SessionTableService tableService) {
        this.sessionService = sessionService;
        this.tableService = tableService;
    }

    public void start() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            // Uses decorator pattern rules
            sessionService.processAutoSessions();
            // Uses decorator pattern rules
            tableService.loadSessions();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}