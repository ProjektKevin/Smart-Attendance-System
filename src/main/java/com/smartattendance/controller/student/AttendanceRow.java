package com.smartattendance.controller.student;

import java.time.LocalDate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * View-model for a single row in the student's attendance table.
 *
 * <p>
 * This class wraps attendance data in JavaFX properties so that it can be
 * bound directly to {@code TableView} columns in the student attendance
 * screen. Each instance represents a single (date, course, session, status)
 * record.
 * </p>
 *
 * @author Ernest Lun
 */
public class AttendanceRow {

    /**
     * Date of the attendance record (e.g. the session date).
     */
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    /**
     * Course name or course code associated with this attendance record.
     */
    private final StringProperty course = new SimpleStringProperty();

    /**
     * Session label or identifier (e.g. "AM Session", "Lab 1", etc.).
     */
    private final StringProperty session = new SimpleStringProperty();

    /**
     * Attendance status for this row (e.g. "Present", "Absent", "Late").
     */
    private final StringProperty status = new SimpleStringProperty();

    /**
     * Constructs a new {@code AttendanceRow} with the given values.
     *
     * @param date    date of the attendance record
     * @param course  course name or code
     * @param session session label or identifier
     * @param status  attendance status string
     */
    public AttendanceRow(LocalDate date, String course, String session, String status) {
        this.date.set(date);
        this.course.set(course);
        this.session.set(session);
        this.status.set(status);
    }

    /**
     * Property accessor for the date.
     *
     * <p>
     * This is used by JavaFX to bind the {@code date} column in the table.
     * </p>
     *
     * @return the {@link ObjectProperty} wrapping the attendance date
     */
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    /**
     * Property accessor for the course.
     *
     * @return the {@link StringProperty} wrapping the course name/code
     */
    public StringProperty courseProperty() {
        return course;
    }

    /**
     * Property accessor for the session.
     *
     * @return the {@link StringProperty} wrapping the session label
     */
    public StringProperty sessionProperty() {
        return session;
    }

    /**
     * Property accessor for the status.
     *
     * @return the {@link StringProperty} wrapping the attendance status
     */
    public StringProperty statusProperty() {
        return status;
    }

    /**
     * Convenience getter for the date value.
     *
     * @return the underlying {@link LocalDate} for this row
     */
    public LocalDate getDate() {
        return date.get();
    }

    /**
     * Convenience getter for the course value.
     *
     * @return the course name or code
     */
    public String getCourse() {
        return course.get();
    }

    /**
     * Convenience getter for the session value.
     *
     * @return the session label/identifier
     */
    public String getSession() {
        return session.get();
    }

    /**
     * Convenience getter for the status value.
     *
     * @return the attendance status string
     */
    public String getStatus() {
        return status.get();
    }
}
