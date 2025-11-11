package com.smartattendance.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.smartattendance.controller.student.StudentAttendanceRow;
import com.smartattendance.controller.student.StudentModuleSummary;
import com.smartattendance.model.dto.dashboard.AttendanceRecord;
import com.smartattendance.model.dto.dashboard.DashboardFilter;
import com.smartattendance.repository.DashboardRepository;
import com.smartattendance.repository.JdbcDashboardRepository;

public class StudentAttendanceService {

    private final DashboardRepository repo = new JdbcDashboardRepository();

    public List<StudentAttendanceRow> loadRowsForStudent(int studentId) {
        DashboardFilter filter = new DashboardFilter();
        filter.from = LocalDate.of(2020, 1, 1);
        filter.to   = LocalDate.now().plusDays(1);

        List<AttendanceRecord> all;
        try {
            all = repo.findAttendance(filter);
        } catch (Exception e) {
            System.out.println("[StudentAttendanceService] fetch failed: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }

        List<StudentAttendanceRow> rows = new ArrayList<>();
        for (AttendanceRecord rec : all) {
            if (rec.userId == null || rec.userId != studentId) continue;

            LocalDate date =
                    rec.sessionDate != null ? rec.sessionDate :
                    (rec.markedAt != null ? rec.markedAt.toLocalDate() : LocalDate.now());

            String module =
                    (rec.courseCode != null && !rec.courseCode.isBlank())
                            ? rec.courseCode
                            : (rec.courseId != null ? "Course " + rec.courseId : "Unknown");

            String status =
                    (rec.status != null && !rec.status.isBlank())
                            ? capitalize(rec.status)
                            : "Pending";

            String markedAtStr = (rec.markedAt != null)
                    ? rec.markedAt.toString().replace('T', ' ')
                    : "";

            // placeholder, unless your DTO has method
            String method = "-";

            rows.add(new StudentAttendanceRow(date, module, status, method, markedAtStr));
        }

        return rows;
    }

    public FilterResult applyFilters(
            List<StudentAttendanceRow> master,
            String selectedModule,
            LocalDate from,
            LocalDate to
    ) {
        List<StudentAttendanceRow> filtered = master.stream()
                .filter(r -> selectedModule == null
                        || "All modules".equals(selectedModule)
                        || r.getModule().equals(selectedModule))
                .filter(r -> from == null || !r.getDate().isBefore(from))
                .filter(r -> to   == null || !r.getDate().isAfter(to))
                .sorted(Comparator.comparing(StudentAttendanceRow::getDate).reversed())
                .toList();

        Map<String, List<StudentAttendanceRow>> byModule = new TreeMap<>();
        for (StudentAttendanceRow r : filtered) {
            byModule.computeIfAbsent(r.getModule(), k -> new ArrayList<>()).add(r);
        }

        List<StudentModuleSummary> summaries = new ArrayList<>();
        byModule.forEach((module, list) -> {
            long attended = list.stream()
                    .filter(r -> "present".equalsIgnoreCase(r.getStatus()))
                    .count();
            int total = list.size();
            double ratio = total == 0 ? Double.NaN : (attended * 1.0 / total);
            summaries.add(new StudentModuleSummary(module, (int) attended, total, ratio));
        });

        return new FilterResult(filtered, summaries);
    }

    public static class FilterResult {
        private final List<StudentAttendanceRow> rows;
        private final List<StudentModuleSummary> summaries;

        public FilterResult(List<StudentAttendanceRow> rows, List<StudentModuleSummary> summaries) {
            this.rows = rows;
            this.summaries = summaries;
        }

        public List<StudentAttendanceRow> getRows() {
            return rows;
        }

        public List<StudentModuleSummary> getSummaries() {
            return summaries;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        s = s.toLowerCase();
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
