package com.smartattendance.util.report;

/**
 * Configuration object describing which columns should be included
 * in a generated attendance report.
 *
 * <p>This spec is shared between:
 * <ul>
 *     <li>The controller, which sets these flags based on user choices (e.g. checkboxes)</li>
 *     <li>Report generators (CSV, PDF, etc.), which read these flags to decide
 *         which columns to output and in what order</li>
 * </ul>
 * 
 * @author Ernest Lun
 */
public class ReportSpec {

    /**
     * Whether to include the "Date/Time" column in the report.
     */
    private boolean includeDateTime;

    /**
     * Whether to include the "Session ID" column.
     */
    private boolean includeSessionId;

    /**
     * Whether to include the "Course" (course code) column.
     */
    private boolean includeCourseCode;

    /**
     * Whether to include the "Student ID" column.
     */
    private boolean includeStudentId;

    /**
     * Whether to include the "Student Name" column.
     */
    private boolean includeStudentName;

    /**
     * Whether to include the "Status" column (Present/Late/Absent/Pending).
     */
    private boolean includeStatus;

    /**
     * Whether to include the "Method" column (e.g. Manual, Face Recognition).
     */
    private boolean includeMethod;

    /**
     * Whether to include the "Confidence" column (e.g. for face recognition).
     */
    private boolean includeConfidence;

    /**
     * Whether to include the "Note" column (free-form text).
     */
    private boolean includeNote;

    /**
     * Default constructor creates an "empty" spec where all flags are false.
     * Callers are expected to set the desired flags via setters or the Builder.
     */
    public ReportSpec() {
    }

    // ===== getters =====

    /**
     * @return {@code true} if the Date/Time column should be included
     */
    public boolean isIncludeDateTime() {
        return includeDateTime;
    }

    /**
     * @return {@code true} if the Session ID column should be included
     */
    public boolean isIncludeSessionId() {
        return includeSessionId;
    }

    /**
     * @return {@code true} if the Course column should be included
     */
    public boolean isIncludeCourseCode() {
        return includeCourseCode;
    }

    /**
     * @return {@code true} if the Student ID column should be included
     */
    public boolean isIncludeStudentId() {
        return includeStudentId;
    }

    /**
     * @return {@code true} if the Student Name column should be included
     */
    public boolean isIncludeStudentName() {
        return includeStudentName;
    }

    /**
     * @return {@code true} if the Status column should be included
     */
    public boolean isIncludeStatus() {
        return includeStatus;
    }

    /**
     * @return {@code true} if the Method column should be included
     */
    public boolean isIncludeMethod() {
        return includeMethod;
    }

    /**
     * @return {@code true} if the Confidence column should be included
     */
    public boolean isIncludeConfidence() {
        return includeConfidence;
    }

    /**
     * @return {@code true} if the Note column should be included
     */
    public boolean isIncludeNote() {
        return includeNote;
    }

    // ===== setters =====

    /**
     * @param includeDateTime whether to include the Date/Time column
     */
    public void setIncludeDateTime(boolean includeDateTime) {
        this.includeDateTime = includeDateTime;
    }

    /**
     * @param includeSessionId whether to include the Session ID column
     */
    public void setIncludeSessionId(boolean includeSessionId) {
        this.includeSessionId = includeSessionId;
    }

    /**
     * @param includeCourseCode whether to include the Course column
     */
    public void setIncludeCourseCode(boolean includeCourseCode) {
        this.includeCourseCode = includeCourseCode;
    }

    /**
     * @param includeStudentId whether to include the Student ID column
     */
    public void setIncludeStudentId(boolean includeStudentId) {
        this.includeStudentId = includeStudentId;
    }

    /**
     * @param includeStudentName whether to include the Student Name column
     */
    public void setIncludeStudentName(boolean includeStudentName) {
        this.includeStudentName = includeStudentName;
    }

    /**
     * @param includeStatus whether to include the Status column
     */
    public void setIncludeStatus(boolean includeStatus) {
        this.includeStatus = includeStatus;
    }

    /**
     * @param includeMethod whether to include the Method column
     */
    public void setIncludeMethod(boolean includeMethod) {
        this.includeMethod = includeMethod;
    }

    /**
     * @param includeConfidence whether to include the Confidence column
     */
    public void setIncludeConfidence(boolean includeConfidence) {
        this.includeConfidence = includeConfidence;
    }

    /**
     * @param includeNote whether to include the Note column
     */
    public void setIncludeNote(boolean includeNote) {
        this.includeNote = includeNote;
    }

    /* =========================================================
       Builder added to satisfy ReportController.buildReportSpec()
       ========================================================= */

    /**
     * Fluent builder for constructing a {@link ReportSpec} instance.
     *
     * <p>Typical usage:
     * <pre>
     *     ReportSpec spec = new ReportSpec.Builder()
     *         .includeDateTime(true)
     *         .includeStudentName(true)
     *         .includeStatus(true)
     *         .build();
     * </pre>
     */
    public static class Builder {
        private final ReportSpec spec = new ReportSpec();

        /**
         * Configure whether to include the Date/Time column.
         *
         * @param v {@code true} to include, {@code false} to exclude
         * @return this builder instance for method chaining
         */
        public Builder includeDateTime(boolean v) {
            spec.setIncludeDateTime(v);
            return this;
        }

        /**
         * Configure whether to include the Session ID column.
         */
        public Builder includeSessionId(boolean v) {
            spec.setIncludeSessionId(v);
            return this;
        }

        /**
         * Configure whether to include the Course column.
         */
        public Builder includeCourseCode(boolean v) {
            spec.setIncludeCourseCode(v);
            return this;
        }

        /**
         * Configure whether to include the Student ID column.
         */
        public Builder includeStudentId(boolean v) {
            spec.setIncludeStudentId(v);
            return this;
        }

        /**
         * Configure whether to include the Student Name column.
         */
        public Builder includeStudentName(boolean v) {
            spec.setIncludeStudentName(v);
            return this;
        }

        /**
         * Configure whether to include the Status column.
         */
        public Builder includeStatus(boolean v) {
            spec.setIncludeStatus(v);
            return this;
        }

        /**
         * Configure whether to include the Method column.
         */
        public Builder includeMethod(boolean v) {
            spec.setIncludeMethod(v);
            return this;
        }

        /**
         * Configure whether to include the Confidence column.
         */
        public Builder includeConfidence(boolean v) {
            spec.setIncludeConfidence(v);
            return this;
        }

        /**
         * Configure whether to include the Note column.
         */
        public Builder includeNote(boolean v) {
            spec.setIncludeNote(v);
            return this;
        }

        /**
         * Finalise the builder and return the constructed {@link ReportSpec}.
         *
         * @return the configured {@code ReportSpec} instance
         */
        public ReportSpec build() {
            return spec;
        }
    }
}
