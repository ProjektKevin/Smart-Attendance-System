package com.smartattendance.util.report;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * {@code PdfReportGenerator} is a {@link ReportGenerator} implementation
 * that exports attendance reports as a nicely formatted PDF.
 *
 * <p>Key responsibilities:
 * <ul>
 *     <li>Create a landscape A4 PDF document</li>
 *     <li>Add a title section ("Attendance Report")</li>
 *     <li>Render a table whose columns are controlled by {@link ReportSpec}</li>
 *     <li>Apply basic styling: header background, fonts, zebra striping, alignment</li>
 * </ul>
 *
 * <p>This class uses iText (com.itextpdf) for PDF generation.
 * 
 * @author Ernest Lun
 */
public class PdfReportGenerator implements ReportGenerator {

    /**
     * Formatter for the timestamp column when rendered to PDF.
     * Example: {@code "2025-11-13 09:30"}.
     */
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ===== fonts used throughout the document =====

    /**
     * Font for the report title (larger and bold).
     */
    private static final Font TITLE_FONT =
            new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);

    /**
     * Font for table headers: small, bold, white text on colored background.
     */
    private static final Font HEADER_FONT =
            new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);

    /**
     * Font for regular table cells.
     */
    private static final Font CELL_FONT =
            new Font(Font.FontFamily.HELVETICA, 9);

    /**
     * Generate a PDF report using the given specification and rows, writing the
     * output into the supplied target file.
     *
     * <p>Behaviour:
     * <ul>
     *     <li>Creates a landscape A4 document with margins</li>
     *     <li>Writes a title paragraph</li>
     *     <li>Resolves which columns to show based on {@link ReportSpec}</li>
     *     <li>Builds a {@link PdfPTable} with headers and data rows</li>
     *     <li>Applies zebra striping for readability and aligns selected columns</li>
     * </ul>
     *
     * @param spec   report configuration controlling which fields to include
     * @param rows   data rows to export
     * @param target target PDF file
     * @throws Exception if writing the PDF fails (e.g. I/O or iText errors)
     */
    @Override
    public void generate(ReportSpec spec,
                         List<AttendanceReportRow> rows,
                         File target) throws Exception {

        // Create a landscape A4 document with 36pt margins on all sides.
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(target));
        doc.open();

        // ---- Title section ----
        Paragraph title = new Paragraph("Attendance Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_LEFT);
        title.setSpacingAfter(12f); // space between title and table
        doc.add(title);

        // Decide which columns should be displayed for this report.
        List<Column> columns = resolveColumns(spec);

        // Table with one column per configured field.
        PdfPTable table = new PdfPTable(columns.size());
        table.setWidthPercentage(100);
        table.setHeaderRows(1); // first row is a header row

        // ---- Header row ----
        for (Column c : columns) {
            PdfPCell cell = new PdfPCell(new Phrase(c.header, HEADER_FONT));
            // Blue-ish header background.
            cell.setBackgroundColor(new BaseColor(33, 150, 243));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6f);
            table.addCell(cell);
        }

        // ---- Data rows ----
        boolean odd = false; // used to alternate row background colors
        for (AttendanceReportRow r : rows) {
            odd = !odd;
            for (Column c : columns) {
                String text = c.valueOf(r);
                PdfPCell cell = new PdfPCell(
                        new Phrase(text != null ? text : "", CELL_FONT)
                );
                cell.setPadding(5f);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                // Zebra striping for every other row to improve readability.
                if (odd) {
                    cell.setBackgroundColor(new BaseColor(245, 245, 245));
                }

                // Center-align some specific columns; others are left-aligned.
                if (c.header.equals("Date/Time") ||
                    c.header.equals("Session ID") ||
                    c.header.equals("Student ID") ||
                    c.header.equals("Confidence")) {
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                } else {
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                }

                table.addCell(cell);
            }
        }

        // Add the completed table to the document and close the PDF.
        doc.add(table);
        doc.close();
    }

    /**
     * Resolve the list of columns to be displayed in the table based on the
     * given {@link ReportSpec}.
     *
     * <p>Each column consists of:
     * <ul>
     *     <li>A header label (used in the table header row)</li>
     *     <li>A {@link ValueExtractor} that knows how to pull that value from an {@link AttendanceReportRow}</li>
     * </ul>
     *
     * @param spec report configuration
     * @return ordered list of columns to render
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
     * Tiny helper to bind a header label to a row value extractor.
     *
     * <p>Each instance represents one PDF table column.
     */
    private static class Column {
        /** Header text shown in the table's header row. */
        final String header;
        /** Strategy for extracting the column value from an {@link AttendanceReportRow}. */
        final ValueExtractor extractor;

        Column(String header, ValueExtractor extractor) {
            this.header = header;
            this.extractor = extractor;
        }

        /**
         * Get the string value for this column from the given row.
         *
         * @param r attendance report row
         * @return string representation for this column
         */
        String valueOf(AttendanceReportRow r) {
            return extractor.valueOf(r);
        }
    }

    /**
     * Functional interface used to extract a single column value
     * from an {@link AttendanceReportRow}.
     */
    @FunctionalInterface
    private interface ValueExtractor {
        /**
         * Extract a value from the given row for use in the PDF table.
         *
         * @param r attendance report row
         * @return value as a string (may be {@code null})
         */
        String valueOf(AttendanceReportRow r);
    }
}
