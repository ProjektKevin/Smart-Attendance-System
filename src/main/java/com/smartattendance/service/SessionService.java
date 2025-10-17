package com.smartattendance.service;

import com.smartattendance.model.Session;
import com.smartattendance.model.Roster;
import com.smartattendance.repository.InMemorySessionRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class SessionService {
    private final Map<String, Session> sessions = new HashMap<>();
    private final InMemorySessionRepository repo;

    public SessionService(){
        this.repo = new InMemorySessionRepository();
    }

    // public Session createSession(String sid, String cid, LocalDate date, LocalTime start,
    //                              LocalTime end, String loc, int lateThreshold) {
    //     Session session = new Session(sid, cid, date, start, end, loc, lateThreshold);
    //     return repo.save(session);
    // }

    public void createSession(Session s) {
        repo.save(s);
    }

    public boolean deleteSession(String sid) {
        Session s = sessions.get(sid);
        if (s != null && !s.isOpen()) {
            sessions.remove(sid);
            return true;
        }
        return false; // can't delete active session
    }

    public void openSession(String sid) {
        Session s = sessions.get(sid);
        if (s != null) s.open();
    }

    public void closeSession(String sid) {
        Session s = sessions.get(sid);
        if (s != null) s.close();
    }

    public Collection<Session> getAllSessions() {
        return repo.findAll();
        // return Collections.unmodifiableCollection(sessions.values());
    }
}
