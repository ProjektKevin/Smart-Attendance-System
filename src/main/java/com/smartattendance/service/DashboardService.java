package com.smartattendance.service;

import java.sql.SQLException;
import java.util.List;

import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.model.dto.dashboard.DashboardFilter;
import com.smartattendance.model.dto.dashboard.DashboardTopCards;
import com.smartattendance.repository.DashboardRepository;

public class DashboardService {

    private final DashboardRepository repository;

    public DashboardService(DashboardRepository repository) {
        this.repository = repository;
    }

    public List<AttendanceRecord> loadAttendance(DashboardFilter filter) throws SQLException {
        return repository.findAttendance(filter);
    }

    public DashboardTopCards computeTopCards(DashboardFilter filter) throws SQLException {
        return repository.computeTopCards(filter);
    }

    public List<String> listCourses() throws SQLException {
        return repository.listCourseLabels();
    }

    public List<String> listSessions(Integer courseId) throws SQLException {
        return repository.listSessionLabels(courseId);
    }

    public String latestSessionLabel(Integer courseId) throws SQLException {
        return repository.findLatestSessionLabel(courseId);
    }
}
