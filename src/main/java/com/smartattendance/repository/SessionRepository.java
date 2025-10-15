package com.smartattendance.repository;

import com.smartattendance.model.Session;
import java.util.*;

public interface SessionRepository {
    List<Session> findAll();
    Session findById(String id);
    Session save(Session s);
    void delete(Session s);
    boolean existsBySessionId(String id);
}
