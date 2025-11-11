package com.smartattendance.util.report;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Nicer looking PDF export.
 */
public class PdfReportGenerator implements ReportGenerator {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // fonts
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 9);

    @Override
    public void generate(ReportSpec spec,
                         List<AttendanceReportRow> rows,
                         File target) throws Exception {

        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36); // landscape
        PdfWriter.getInstance(doc, new FileOutputStream(target));
        doc.open();

        // title
        Paragraph title = new Paragraph("Attendance Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_LEFT);
        title.setSpacingAfter(12f);
        doc.add(title);

        // build list of columns to actually show
        List<Column> columns = resolveColumns(spec);

        PdfPTable table = new PdfPTable(columns.size());
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        // header row
        for (Column c : columns) {
            PdfPCell cell = new PdfPCell(new Phrase(c.header, HEADER_FONT));
            cell.setBackgroundColor(new BaseColor(33, 150, 243)); // blue-ish
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6f);
            table.addCell(cell);
        }

        // rows
        boolean odd = false;
        for (AttendanceReportRow r : rows) {
            odd = !odd;
            for (Column c : columns) {
                String text = c.valueOf(r);
                PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", CELL_FONT));
                cell.setPadding(5f);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                // alternate background
                if (odd) {
                    cell.setBackgroundColor(new BaseColor(245, 245, 245));
                }

                // align some columns
                if (c.header.equals("Date/Time") || c.header.equals("Session ID")
                        || c.header.equals("Student ID") || c.header.equals("Confidence")) {
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                } else {
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                }

                table.addCell(cell);
            }
        }

        doc.add(table);
        doc.close();
    }

    /**
     * Decide which columns to show, based on ReportSpec.
     */
    private List<Column> resolveColumns(ReportSpec spec) {
        List<Column> cols = new ArrayList<>();

        if (spec.isIncludeDateTime()) {
            cols.add(new Column("Date/Time", r ->
                    r.getTimestamp() != null ? TS_FMT.format(r.getTimestamp()) : ""));
        }
        if (spec.isIncludeSessionId()) {
            cols.add(new Column("Session ID", AttendanceReportRow::getSessionId));
        }
        if (spec.isIncludeCourseCode()) {
            cols.add(new Column("Course", AttendanceReportRow::getCourseCode));
        }
        if (spec.isIncludeStudentId()) {
            cols.add(new Column("Student ID", AttendanceReportRow::getStudentId));
        }
        if (spec.isIncludeStudentName()) {
            cols.add(new Column("Student Name", AttendanceReportRow::getStudentName));
        }
        if (spec.isIncludeStatus()) {
            cols.add(new Column("Status", AttendanceReportRow::getStatus));
        }
        if (spec.isIncludeMethod()) {
            cols.add(new Column("Method", AttendanceReportRow::getMethod));
        }
        if (spec.isIncludeConfidence()) {
            cols.add(new Column("Confidence", r -> {
                String c = r.getConfidence();
                return (c == null || c.isBlank()) ? "" : c;
            }));
        }
        if (spec.isIncludeNote()) {
            cols.add(new Column("Note", r -> {
                String n = r.getNote();
                return n != null ? n : "";
            }));
        }

        return cols;
    }

    /**
     * Tiny helper to bind a header to a value extractor.
     */
    private static class Column {
        final String header;
        final ValueExtractor extractor;

        Column(String header, ValueExtractor extractor) {
            this.header = header;
            this.extractor = extractor;
        }

        String valueOf(AttendanceReportRow r) {
            return extractor.valueOf(r);
        }
    }

    @FunctionalInterface
    private interface ValueExtractor {
        String valueOf(AttendanceReportRow r);
    }
}
