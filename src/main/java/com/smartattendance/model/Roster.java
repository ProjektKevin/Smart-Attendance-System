package com.smartattendance.model;

import java.util.*;

public class Roster {
    private final List<Student> students = new ArrayList<>();

    public void addStudent(Student s) {
        if (!students.contains(s)) {
            students.add(s);
        }
    }

    public void removeStudent(Student s) {
        students.remove(s);
    }

    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }

    public boolean contains(Student s) {
        return students.contains(s);
    }
}
