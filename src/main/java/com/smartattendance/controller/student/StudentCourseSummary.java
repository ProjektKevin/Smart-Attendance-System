package com.smartattendance.controller.student;

/**
 * Immutable summary model for a student's attendance in a single course.
 *
 * <p>
 * Each instance represents the aggregate attendance for one course:
 * <ul>
 *     <li>Course name/code</li>
 *     <li>Number of sessions attended</li>
 *     <li>Total number of sessions</li>
 *     <li>Attendance ratio (0.0 – 1.0)</li>
 * </ul>
 * This is typically used as the backing model for the summary
 * {@code TableView<StudentCourseSummary>} in the student attendance screen.
 * </p>
 *
 * @author Ernest Lun
 */
public class StudentCourseSummary {

    /**
     * Course name or course code.
     */
    private final String course;

    /**
     * Number of sessions the student attended for this course.
     */
    private final int attended;

    /**
     * Total number of sessions offered for this course.
     */
    private final int total;

    /**
     * Attendance ratio in the range {@code 0.0} to {@code 1.0}.
     * Typically computed as {@code attended / (double) total}.
     */
    private final double ratio;

    /**
     * Constructs a new {@code StudentCourseSummary}.
     *
     * @param course   course name or code
     * @param attended number of attended sessions
     * @param total    total number of sessions
     * @param ratio    attendance ratio (0.0–1.0) for this course
     */
    public StudentCourseSummary(String course, int attended, int total, double ratio) {
        this.course = course;
        this.attended = attended;
        this.total = total;
        this.ratio = ratio;
    }

    /**
     * Returns the course name or code.
     *
     * @return course identifier
     */
    public String getCourse() {
        return course;
    }

    /**
     * Returns the number of attended sessions.
     *
     * @return count of attended sessions
     */
    public int getAttended() {
        return attended;
    }

    /**
     * Returns the total number of sessions.
     *
     * @return total session count
     */
    public int getTotal() {
        return total;
    }

    /**
     * Returns the attendance ratio as a {@link Double}.
     *
     * <p>
     * The wrapper type is convenient for JavaFX table columns that expect
     * {@code Double} instead of the primitive {@code double}.
     * </p>
     *
     * @return attendance ratio in the range {@code 0.0}–{@code 1.0}
     */
    public Double getRatio() {
        return ratio;
    }
}
