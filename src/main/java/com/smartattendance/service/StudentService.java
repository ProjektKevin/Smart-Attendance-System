package com.smartattendance.service;

import java.util.List;

import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.StudentRepository;

/**
 * Service layer that wraps {@link StudentRepository} for student-related operations.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Provide a simple API for controllers to query and modify student data</li>
 *     <li>Delegate all persistence logic to {@link StudentRepository}</li>
 *     <li>Offer a default JDBC-backed repository, while allowing alternative
 *         implementations to be injected for testing</li>
 * </ul>
 * </p>
 *
 * <p>
 * By default, this service uses the standard {@link StudentRepository}, which is
 * backed by the application's database via {@code DatabaseUtil}.
 * </p>
 *
 * @author Ernest Lun
 */
public class StudentService {

    /**
     * Underlying repository used for all student persistence and queries.
     */
    private final StudentRepository repo;

    /**
     * Creates a {@code StudentService} using the default JDBC-backed
     * {@link StudentRepository}.
     */
    public StudentService() {
        this.repo = new StudentRepository();
    }

    /**
     * Creates a {@code StudentService} with a custom {@link StudentRepository}.
     *
     * <p>
     * Useful for dependency injection or testing with mocks/fakes.
     * </p>
     *
     * @param repo repository implementation to delegate to
     */
    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    /**
     * Returns all students known to the repository.
     *
     * @return list of all {@link Student} entities
     */
    public List<Student> getAllStudents() {
        return repo.findAll();
    }

    /**
     * Finds a student by their unique ID.
     *
     * @param id student ID to search for
     * @return matching {@link Student}, or {@code null} if not found
     */
    public Student findById(int id) {
        return repo.findById(id);
    }

    /**
     * Adds/saves a new student enrollment via the repository.
     *
     * <p>
     * This method assumes the student's ID is valid and unique according to the
     * database constraints. If {@code s} is {@code null}, the call is ignored.
     * </p>
     *
     * @param s student to persist
     */
    public void addStudent(Student s) {
        if (s == null) {
            return;
        }
        repo.save(s);
    }

    /**
     * Retrieves students based on the course associated with a given session.
     *
     * <p>
     * This is effectively a convenience method that delegates to
     * {@link StudentRepository#findByCourse(String)} using the course
     * from the provided {@link Session}.
     * </p>
     *
     * @param session session whose course is used to filter students
     * @return list of students enrolled in the same course as the session
     */
    public List<Student> getStudentsBySessionId(Session session) {
        return repo.findByCourse(session.getCourse());
    }
}
