package com.smartattendance.util.report;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class CsvReportGenerator implements ReportGenerator {

    @Override
    public void generate(ReportSpec spec,
                         List<AttendanceReportRow> rows,
                         File target) throws Exception {

        try (PrintWriter out = new PrintWriter(new FileWriter(target))) {

            // header
            boolean first = true;
            if (spec.isIncludeDateTime()) {
                out.print("Date/Time");
                first = false;
            }
            if (spec.isIncludeSessionId()) {
                if (!first) out.print(",");
                out.print("Session ID");
                first = false;
            }
            if (spec.isIncludeCourseCode()) {
                if (!first) out.print(",");
                out.print("Course");
                first = false;
            }
            if (spec.isIncludeStudentId()) {
                if (!first) out.print(",");
                out.print("Student ID");
                first = false;
            }
            if (spec.isIncludeStudentName()) {
                if (!first) out.print(",");
                out.print("Student Name");
                first = false;
            }
            if (spec.isIncludeStatus()) {
                if (!first) out.print(",");
                out.print("Status");
                first = false;
            }
            if (spec.isIncludeMethod()) {
                if (!first) out.print(",");
                out.print("Method");
                first = false;
            }
            if (spec.isIncludeConfidence()) {
                if (!first) out.print(",");
                out.print("Confidence");
                first = false;
            }
            if (spec.isIncludeNote()) {
                if (!first) out.print(",");
                out.print("Note");
            }
            out.println();

            // rows
            for (AttendanceReportRow r : rows) {
                first = true;
                if (spec.isIncludeDateTime()) {
                    out.print(escape(r.getTimestampFormatted()));
                    first = false;
                }
                if (spec.isIncludeSessionId()) {
                    if (!first) out.print(",");
                    out.print(escape(r.getSessionId()));
                    first = false;
                }
                if (spec.isIncludeCourseCode()) {
                    if (!first) out.print(",");
                    out.print(escape(r.getCourseCode()));
                    first = false;
                }
                if (spec.isIncludeStudentId()) {
                    if (!first) out.print(",");
                    out.print(escape(r.getStudentId()));
                    first = false;
                }
                if (spec.isIncludeStudentName()) {
                    if (!first) out.print(",");
                    out.print(escape(r.getStudentName()));
                    first = false;
                }
                if (spec.isIncludeStatus()) {
                    if (!first) out.print(",");
                    out.print(escape(r.getStatus()));
                    first = false;
                }
                if (spec.isIncludeMethod()) {
                    if (!first) out.print(",");
                    out.print(escape(r.getMethod()));
                    first = false;
                }
                if (spec.isIncludeConfidence()) {
                    if (!first) out.print(",");
                    out.print(escape(r.getConfidence()));
                    first = false;
                }
                if (spec.isIncludeNote()) {
                    if (!first) out.print(",");
                    out.print(escape(r.getNote()));
                }
                out.println();
            }
        }
    }

    private String escape(String v) {
        if (v == null) return "";
        // simple CSV escape
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
