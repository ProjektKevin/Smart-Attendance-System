package com.smartattendance.service;

import com.smartattendance.model.Session;
import com.smartattendance.repository.InMemorySessionRepository;
import com.smartattendance.repository.SessionRepository;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class SessionService {
    private final Map<String, Session> sessions = new HashMap<>();
    private final InMemorySessionRepository repo;
    private int sessionCounter = 0; // track last used session number

    public SessionService() {
        this.repo = new InMemorySessionRepository();
        // Initialize counter from existing sessions if needed
        int max = repo.findAll().stream()
                      .map(Session::getSessionId)
                      .map(id -> id.replaceAll("[^0-9]", "")) // remove 's'
                      .mapToInt(Integer::parseInt)
                      .max().orElse(0);
        sessionCounter = max;
        startLifecycleChecker();
    }

    private String generateSessionId() {
        sessionCounter++;
        return "S" + sessionCounter;
    }

    public Session createSession(String courseId, LocalDate date, LocalTime start,
                                 LocalTime end, String loc, int lateThreshold) {
        String newId = generateSessionId();
        Session session = new Session(newId, courseId.toUpperCase(), date, start, end, loc, lateThreshold);
        repo.save(session);
        sessions.put(newId, session);
        return session;
    }

    public void createSession(Session s) {
        repo.save(s);
    }

    // public boolean deleteSession(String sid) {
    // Session s = sessions.get(sid);
    // if (s != null && !s.isOpen()) {
    // sessions.remove(sid);
    // return true;
    // }
    // return false; // can't delete active session
    // }

    public void deleteSession(Session s) {
        repo.delete(s);
    }

    public void openSession(String sid) {
        Session s = sessions.get(sid);
        if (s != null)
            s.open();
    }

    public void closeSession(String sid) {
        Session s = sessions.get(sid);
        if (s != null)
            s.close();
    }

    public Collection<Session> getAllSessions() {
        return repo.findAll();
        // return Collections.unmodifiableCollection(sessions.values());
    }

    private void startLifecycleChecker() {
        Timeline lifecycleChecker = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    LocalDateTime now = LocalDateTime.now();

                    for (Session s : repo.findAll()) {
                        LocalDateTime start = LocalDateTime.of(s.getSessionDate(), s.getStartTime());
                        LocalDateTime end = LocalDateTime.of(s.getSessionDate(), s.getEndTime());

                        // Session should open if within time range
                        if (now.isAfter(start) && now.isBefore(end)
                                && s.getStatusEnum() != Session.SessionStatus.OPEN) {
                            s.open();
                            System.out.println("[Auto OPEN] Session " + s.getSessionId());
                        }
                        // Session should close if time has passed
                        else if (now.isAfter(end) && s.getStatusEnum() != Session.SessionStatus.CLOSED) {
                            s.close();
                            System.out.println("[Auto CLOSE] Session " + s.getSessionId());
                        }
                        // Otherwise keep pending
                        else if (now.isBefore(start) && s.getStatusEnum() != Session.SessionStatus.PENDING) {
                            s.setPending();
                            System.out.println("[Pending] Session " + s.getSessionId());
                        }
                    }
                }));
        lifecycleChecker.setCycleCount(Timeline.INDEFINITE);
        lifecycleChecker.play();
    }
}
