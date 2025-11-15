package com.smartattendance.repository;

import java.sql.SQLException;
import java.util.List;

import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.model.dto.dashboard.DashboardFilter;
import com.smartattendance.model.dto.dashboard.DashboardTopCards;

public interface DashboardRepository {

    List<AttendanceRecord> findAttendance(DashboardFilter filter) throws SQLException;

    DashboardTopCards computeTopCards(DashboardFilter filter) throws SQLException;

    List<String> listCourseLabels() throws SQLException;

    List<String> listSessionLabels(Integer courseId) throws SQLException;

    String findLatestSessionLabel(Integer courseId) throws SQLException;
}
