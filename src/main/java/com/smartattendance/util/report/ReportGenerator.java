package com.smartattendance.util.report;

import java.io.File;
import java.util.List;

/**
 * Strategy interface for generating attendance reports in different output formats.
 *
 * <p>Implementations of this interface (e.g. CSV, PDF, XLSX) encapsulate the
 * logic for transforming a list of {@link AttendanceReportRow} objects into a
 * concrete file, using the options provided by {@link ReportSpec}.
 *
 * <p>Typical usage:
 * <pre>
 *     ReportGenerator generator = new CsvReportGenerator();  // or PdfReportGenerator, etc.
 *     generator.generate(spec, rows, targetFile);
 * </pre>
 * 
 * @author Ernest Lun
 */
public interface ReportGenerator {

    /**
     * Generate an attendance report file.
     *
     * <p>Responsibilities for implementors:
     * <ul>
     *     <li>Interpret the {@link ReportSpec} to decide which fields/columns to include</li>
     *     <li>Iterate over {@code rows} and write them in the chosen format</li>
     *     <li>Write the result to {@code targetFile} (creating or overwriting as needed)</li>
     * </ul>
     *
     * @param spec       report configuration (selected columns, formatting options, etc.)
     * @param rows       ordered list of report rows to export
     * @param targetFile destination file for the generated report
     * @throws Exception if any error occurs during report generation
     *                   (e.g. I/O errors, library-specific exceptions)
     */
    void generate(ReportSpec spec,
                  List<AttendanceReportRow> rows,
                  File targetFile) throws Exception;
}
