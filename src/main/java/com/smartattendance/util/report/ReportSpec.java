package com.smartattendance.util.report;

/**
 * Which columns to include in the report.
 * Controller + generators both look at these flags.
 */
public class ReportSpec {

    private boolean includeDateTime;
    private boolean includeSessionId;
    private boolean includeCourseCode;
    private boolean includeStudentId;
    private boolean includeStudentName;
    private boolean includeStatus;
    private boolean includeMethod;
    private boolean includeConfidence;
    private boolean includeNote;

    public ReportSpec() {
    }

    // ===== getters =====
    public boolean isIncludeDateTime() {
        return includeDateTime;
    }

    public boolean isIncludeSessionId() {
        return includeSessionId;
    }

    public boolean isIncludeCourseCode() {
        return includeCourseCode;
    }

    public boolean isIncludeStudentId() {
        return includeStudentId;
    }

    public boolean isIncludeStudentName() {
        return includeStudentName;
    }

    public boolean isIncludeStatus() {
        return includeStatus;
    }

    public boolean isIncludeMethod() {
        return includeMethod;
    }

    public boolean isIncludeConfidence() {
        return includeConfidence;
    }

    public boolean isIncludeNote() {
        return includeNote;
    }

    // ===== setters =====
    public void setIncludeDateTime(boolean includeDateTime) {
        this.includeDateTime = includeDateTime;
    }

    public void setIncludeSessionId(boolean includeSessionId) {
        this.includeSessionId = includeSessionId;
    }

    public void setIncludeCourseCode(boolean includeCourseCode) {
        this.includeCourseCode = includeCourseCode;
    }

    public void setIncludeStudentId(boolean includeStudentId) {
        this.includeStudentId = includeStudentId;
    }

    public void setIncludeStudentName(boolean includeStudentName) {
        this.includeStudentName = includeStudentName;
    }

    public void setIncludeStatus(boolean includeStatus) {
        this.includeStatus = includeStatus;
    }

    public void setIncludeMethod(boolean includeMethod) {
        this.includeMethod = includeMethod;
    }

    public void setIncludeConfidence(boolean includeConfidence) {
        this.includeConfidence = includeConfidence;
    }

    public void setIncludeNote(boolean includeNote) {
        this.includeNote = includeNote;
    }

    /* =========================================================
       Builder added to satisfy ReportController.buildReportSpec()
       ========================================================= */
    public static class Builder {
        private final ReportSpec spec = new ReportSpec();

        public Builder includeDateTime(boolean v) {
            spec.setIncludeDateTime(v);
            return this;
        }

        public Builder includeSessionId(boolean v) {
            spec.setIncludeSessionId(v);
            return this;
        }

        public Builder includeCourseCode(boolean v) {
            spec.setIncludeCourseCode(v);
            return this;
        }

        public Builder includeStudentId(boolean v) {
            spec.setIncludeStudentId(v);
            return this;
        }

        public Builder includeStudentName(boolean v) {
            spec.setIncludeStudentName(v);
            return this;
        }

        public Builder includeStatus(boolean v) {
            spec.setIncludeStatus(v);
            return this;
        }

        public Builder includeMethod(boolean v) {
            spec.setIncludeMethod(v);
            return this;
        }

        public Builder includeConfidence(boolean v) {
            spec.setIncludeConfidence(v);
            return this;
        }

        public Builder includeNote(boolean v) {
            spec.setIncludeNote(v);
            return this;
        }

        public ReportSpec build() {
            return spec;
        }
    }
}
