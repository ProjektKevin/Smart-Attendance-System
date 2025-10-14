package com.smartattendance.controller.student;

import javafx.beans.property.*;
import java.time.LocalDate;

public class AttendanceRow {
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty course = new SimpleStringProperty();
    private final StringProperty session = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public AttendanceRow(LocalDate date, String course, String session, String status) {
        this.date.set(date);
        this.course.set(course);
        this.session.set(session);
        this.status.set(status);
    }

    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty courseProperty() { return course; }
    public StringProperty sessionProperty() { return session; }
    public StringProperty statusProperty() { return status; }

    public LocalDate getDate() { return date.get(); }
    public String getCourse() { return course.get(); }
    public String getSession() { return session.get(); }
    public String getStatus() { return status.get(); }
}
