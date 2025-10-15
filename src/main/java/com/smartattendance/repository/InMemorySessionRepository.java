package com.smartattendance.repository;

import com.smartattendance.model.Session;
import com.smartattendance.model.Student;

import java.util.*;

public class InMemorySessionRepository {
    private final List<Session> sessions = new ArrayList<>();

    public InMemorySessionRepository() {
        sessions.add(new Session("S1", "CS102", java.time.LocalDate.now(),
                java.time.LocalTime.of(9, 0), java.time.LocalTime.of(11, 0), "Room A", 10));
        sessions.add(new Session("S2", "CS104", java.time.LocalDate.now(),
                java.time.LocalTime.of(13, 0), java.time.LocalTime.of(15, 0), "Room B", 10));
        sessions.add(new Session("S3", "CS106", java.time.LocalDate.now(),
                java.time.LocalTime.of(15, 30), java.time.LocalTime.of(17, 30), "Room C", 10));
    }

    /** Returns a copy of all sessions */
    public List<Session> findAll() {
        return new ArrayList<>(sessions);
    }

    /** Find session by session ID */
    public Session findById(String id) {
        return sessions.stream()
                .filter(s -> s.getSessionId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /** Save a new session */
    public Session save(Session s) {
        sessions.add(s);
        return s; 
    }

    /** Delete a session */
    public void delete(Session s) {
        sessions.remove(s);
    }

    /** Check if a session with a given ID exists */
    public boolean existsBySessionId(String id) {
        return sessions.stream().anyMatch(s -> s.getSessionId().equals(id));
    }
}
