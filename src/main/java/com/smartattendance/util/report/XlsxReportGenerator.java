package com.smartattendance.util.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XlsxReportGenerator implements ReportGenerator {

    @Override
    public void generate(ReportSpec spec,
                         List<AttendanceReportRow> rows,
                         File target) throws Exception {

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Attendance");

            int rowIdx = 0;

            // header
            Row header = sheet.createRow(rowIdx++);
            int col = 0;
            if (spec.isIncludeDateTime()) header.createCell(col++).setCellValue("Date/Time");
            if (spec.isIncludeSessionId()) header.createCell(col++).setCellValue("Session ID");
            if (spec.isIncludeCourseCode()) header.createCell(col++).setCellValue("Course");
            if (spec.isIncludeStudentId()) header.createCell(col++).setCellValue("Student ID");
            if (spec.isIncludeStudentName()) header.createCell(col++).setCellValue("Student Name");
            if (spec.isIncludeStatus()) header.createCell(col++).setCellValue("Status");
            if (spec.isIncludeMethod()) header.createCell(col++).setCellValue("Method");
            if (spec.isIncludeConfidence()) header.createCell(col++).setCellValue("Confidence");
            if (spec.isIncludeNote()) header.createCell(col++).setCellValue("Note");

            // data rows
            for (AttendanceReportRow r : rows) {
                Row xRow = sheet.createRow(rowIdx++);
                col = 0;

                if (spec.isIncludeDateTime()) {
                    Cell c = xRow.createCell(col++);
                    // simplest: just write the formatted string
                    c.setCellValue(r.getTimestampFormatted());
                }
                if (spec.isIncludeSessionId()) xRow.createCell(col++).setCellValue(nz(r.getSessionId()));
                if (spec.isIncludeCourseCode()) xRow.createCell(col++).setCellValue(nz(r.getCourseCode()));
                if (spec.isIncludeStudentId()) xRow.createCell(col++).setCellValue(nz(r.getStudentId()));
                if (spec.isIncludeStudentName()) xRow.createCell(col++).setCellValue(nz(r.getStudentName()));
                if (spec.isIncludeStatus()) xRow.createCell(col++).setCellValue(nz(r.getStatus()));
                if (spec.isIncludeMethod()) xRow.createCell(col++).setCellValue(nz(r.getMethod()));
                if (spec.isIncludeConfidence()) xRow.createCell(col++).setCellValue(nz(r.getConfidence()));
                if (spec.isIncludeNote()) xRow.createCell(col++).setCellValue(nz(r.getNote()));
            }

            // autosize
            int totalCols = countColumns(spec);
            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(target)) {
                wb.write(fos);
            }
        }
    }

    private int countColumns(ReportSpec spec) {
        int c = 0;
        if (spec.isIncludeDateTime()) c++;
        if (spec.isIncludeSessionId()) c++;
        if (spec.isIncludeCourseCode()) c++;
        if (spec.isIncludeStudentId()) c++;
        if (spec.isIncludeStudentName()) c++;
        if (spec.isIncludeStatus()) c++;
        if (spec.isIncludeMethod()) c++;
        if (spec.isIncludeConfidence()) c++;
        if (spec.isIncludeNote()) c++;
        return c;
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}
