package com.smartattendance.util.report;

import java.io.File;
import java.util.List;

public interface ReportGenerator {
    void generate(ReportSpec spec, List<AttendanceReportRow> rows, File targetFile) throws Exception;
}
