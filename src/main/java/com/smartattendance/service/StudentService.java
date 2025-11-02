package com.smartattendance.service;

import java.util.List;

import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.StudentRepository;

/**
 * Simple service layer around the StudentRepository.
 * Uses an in-memory repository by default (swap later for SQLite).
 */
public class StudentService {

    private final StudentRepository repo;

    /** Default: use in-memory repository. */
    public StudentService() {
        this.repo = new StudentRepository();
    }

    /** For DI/tests: pass any implementation of StudentRepository. */
    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    /** Return all students. */
    public List<Student> getAllStudents() {
        return repo.findAll();
    }

    /** Find a student by ID, or null if not found. */
    public Student findById(String id) {
        if (id == null || id.isBlank())
            return null;
        return repo.findById(id);
    }

    /** Add/save a student (id should be unique). */
    public void addStudent(Student s) {
        if (s == null)
            return;
        repo.save(s);
    }
}
