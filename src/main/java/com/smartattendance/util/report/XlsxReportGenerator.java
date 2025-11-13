package com.smartattendance.util.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * {@code XlsxReportGenerator} is a {@link ReportGenerator} implementation
 * that exports attendance reports as an Excel (.xlsx) spreadsheet.
 *
 * <p>Key behaviour:
 * <ul>
 *     <li>Creates a workbook with a single sheet named "Attendance"</li>
 *     <li>Writes a header row based on {@link ReportSpec}</li>
 *     <li>Writes one row per {@link AttendanceReportRow}</li>
 *     <li>Auto-sizes all used columns for readability</li>
 * </ul>
 *
 * <p>This implementation uses Apache POI's XSSF API (XSSFWorkbook) to produce
 * modern .xlsx files.
 */
public class XlsxReportGenerator implements ReportGenerator {

    /**
     * Generate an Excel report using the given {@link ReportSpec} and rows.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Creates a new workbook and "Attendance" sheet</li>
     *     <li>Writes the header row, including only selected columns</li>
     *     <li>Iterates through all {@code rows} and writes each as a data row</li>
     *     <li>Auto-sizes all created columns</li>
     *     <li>Writes the workbook out to {@code target}</li>
     * </ul>
     *
     * @param spec   report configuration indicating which columns to include
     * @param rows   list of {@link AttendanceReportRow} entries to export
     * @param target output file for the .xlsx report
     * @throws Exception if workbook creation or file I/O fails
     */
    @Override
    public void generate(ReportSpec spec,
                         List<AttendanceReportRow> rows,
                         File target) throws Exception {

        // Use try-with-resources so the workbook is closed automatically.
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Attendance");

            int rowIdx = 0;

            // ==================
            //       HEADER
            // ==================
            Row header = sheet.createRow(rowIdx++);
            int col = 0;

            // The order here defines the column order in the sheet and must
            // match the per-row writing logic below.
            if (spec.isIncludeDateTime())   header.createCell(col++).setCellValue("Date/Time");
            if (spec.isIncludeSessionId())  header.createCell(col++).setCellValue("Session ID");
            if (spec.isIncludeCourseCode()) header.createCell(col++).setCellValue("Course");
            if (spec.isIncludeStudentId())  header.createCell(col++).setCellValue("Student ID");
            if (spec.isIncludeStudentName())header.createCell(col++).setCellValue("Student Name");
            if (spec.isIncludeStatus())     header.createCell(col++).setCellValue("Status");
            if (spec.isIncludeMethod())     header.createCell(col++).setCellValue("Method");
            if (spec.isIncludeConfidence()) header.createCell(col++).setCellValue("Confidence");
            if (spec.isIncludeNote())       header.createCell(col++).setCellValue("Note");

            // ==================
            //      DATA ROWS
            // ==================
            for (AttendanceReportRow r : rows) {
                Row xRow = sheet.createRow(rowIdx++);
                col = 0;

                // For simplicity, the Date/Time is exported as a formatted string.
                // (You could optionally use a true Excel date cell + cell style instead.)
                if (spec.isIncludeDateTime()) {
                    Cell c = xRow.createCell(col++);
                    c.setCellValue(r.getTimestampFormatted());
                }
                if (spec.isIncludeSessionId())   xRow.createCell(col++).setCellValue(nz(r.getSessionId()));
                if (spec.isIncludeCourseCode())  xRow.createCell(col++).setCellValue(nz(r.getCourseCode()));
                if (spec.isIncludeStudentId())   xRow.createCell(col++).setCellValue(nz(r.getStudentId()));
                if (spec.isIncludeStudentName()) xRow.createCell(col++).setCellValue(nz(r.getStudentName()));
                if (spec.isIncludeStatus())      xRow.createCell(col++).setCellValue(nz(r.getStatus()));
                if (spec.isIncludeMethod())      xRow.createCell(col++).setCellValue(nz(r.getMethod()));
                if (spec.isIncludeConfidence())  xRow.createCell(col++).setCellValue(nz(r.getConfidence()));
                if (spec.isIncludeNote())        xRow.createCell(col++).setCellValue(nz(r.getNote()));
            }

            // Auto-size all columns actually used, based on the spec.
            int totalCols = countColumns(spec);
            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i);
            }

            // Finally, write the workbook contents to disk.
            try (FileOutputStream fos = new FileOutputStream(target)) {
                wb.write(fos);
            }
        }
    }

    /**
     * Count how many columns will be present based on the given spec,
     * so we know how many columns to auto-size.
     *
     * @param spec report configuration
     * @return total number of columns to be written
     */
    private int countColumns(ReportSpec spec) {
        int c = 0;
        if (spec.isIncludeDateTime())   c++;
        if (spec.isIncludeSessionId())  c++;
        if (spec.isIncludeCourseCode()) c++;
        if (spec.isIncludeStudentId())  c++;
        if (spec.isIncludeStudentName())c++;
        if (spec.isIncludeStatus())     c++;
        if (spec.isIncludeMethod())     c++;
        if (spec.isIncludeConfidence()) c++;
        if (spec.isIncludeNote())       c++;
        return c;
    }

    /**
     * Null-safe helper: return an empty string instead of {@code null}.
     *
     * <p>This prevents "null" from appearing in Excel cells.
     *
     * @param s input string (may be {@code null})
     * @return {@code s} if non-null, otherwise an empty string
     */
    private String nz(String s) {
        return s == null ? "" : s;
    }
}
