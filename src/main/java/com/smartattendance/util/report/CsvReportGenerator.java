package com.smartattendance.util.report;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * {@code CsvReportGenerator} is a {@link ReportGenerator} implementation that
 * writes attendance reports as CSV files.
 *
 * <p>Key characteristics:
 * <ul>
 *     <li>Uses {@link ReportSpec} to decide which columns to include</li>
 *     <li>Writes a header row followed by one row per {@link AttendanceReportRow}</li>
 *     <li>Performs simple CSV escaping for values containing commas, quotes, or newlines</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 *     ReportGenerator generator = new CsvReportGenerator();
 *     generator.generate(spec, rows, targetFile);
 * </pre>
 * 
 * @author Ernest Lun
 */
public class CsvReportGenerator implements ReportGenerator {

    /**
     * Generate a CSV report file using the provided specification and data rows.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Opens a {@link PrintWriter} on the target file (overwriting if it exists)</li>
     *     <li>Writes a header line containing only the selected columns from {@link ReportSpec}</li>
     *     <li>Writes one CSV line per {@link AttendanceReportRow}, in the same column order</li>
     *     <li>Applies {@link #escape(String)} to each value to ensure a valid CSV format</li>
     * </ul>
     *
     * @param spec   report configuration indicating which fields to include
     * @param rows   list of rows to export
     * @param target file to write the CSV into
     * @throws Exception if file creation or writing fails
     */
    @Override
    public void generate(ReportSpec spec,
                         List<AttendanceReportRow> rows,
                         File target) throws Exception {

        // try-with-resources guarantees the writer is closed even if an exception occurs.
        try (PrintWriter out = new PrintWriter(new FileWriter(target))) {

            // =====================
            //     HEADER LINE
            // =====================
            boolean first = true;

            // Each "if" block conditionally writes a column header, adding
            // a comma prefix when it's not the first column.
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
            out.println(); // end of header row

            // =====================
            //       DATA ROWS
            // =====================
            for (AttendanceReportRow r : rows) {
                first = true;

                // For each row, we mirror the same column order and inclusion
                // logic as in the header section above.
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

                // End of one CSV record (row).
                out.println();
            }
        }
    }

    /**
     * Escape a value for safe inclusion in a CSV cell.
     *
     * <p>Rules:
     * <ul>
     *     <li>{@code null} is converted to an empty string</li>
     *     <li>If the value contains a comma, quote, or newline:
     *         <ul>
     *             <li>All double quotes are doubled ({@code "} → {@code ""})</li>
     *             <li>The entire value is wrapped in double quotes</li>
     *         </ul>
     *     </li>
     *     <li>Otherwise the value is returned as-is</li>
     * </ul>
     *
     * @param v raw string value
     * @return CSV-escaped representation, never {@code null}
     */
    private String escape(String v) {
        if (v == null) return "";
        // Simple CSV escape: quote the value if it contains special characters.
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
