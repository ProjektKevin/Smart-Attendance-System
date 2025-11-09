package com.smartattendance.service;

import java.util.List;

import com.smartattendance.model.dto.student.StudentDTO;
import com.smartattendance.model.entity.Course;
import com.smartattendance.repository.CourseRepository;

public class CourseService {
    public final CourseRepository courseRepo;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepo = courseRepository;
    }

    // ==================== Entity Methods ====================

    /**
     * Get course by id
     *
     * @param id The id filtered
     * @return Course object by filtered id
     */
    public Course getCourse(Integer id) {
        return courseRepo.findCourseById(id);
    }

    /**
     * Get all courses
     *
     * @return List of Course object
     */
    public List<Course> getCourses() {
        return courseRepo.findAllCourses();
    }

    // ==================== DTO Methods ====================

    /**
     * Get all students enrolled in a specific course with their full names
     *
     * @param courseId The course ID to filter by
     * @return List of StudentDTO containing studentId, fullName, and courseName
     */
    public List<StudentDTO> getStudentsByCourseId(Integer courseId) {
        return courseRepo.findStudentsByCourseId(courseId);
    }

    /**
     * Get all students enrolled in a course by course name with their full names
     *
     * @param courseName The course name to filter by
     * @return List of StudentDTO containing studentId, fullName, and courseName
     */
    public List<StudentDTO> getStudentsByCourseName(String courseName) {
        return courseRepo.findStudentsByCourseName(courseName);
    }

    /**
     * Get all students across all courses with their full names
     *
     * @return List of StudentDTO containing studentId, fullName, and courseName
     */
    public List<StudentDTO> getAllStudents() {
        return courseRepo.findAllStudents();
    }

    // ==================== Enrollment Methods ====================

    /**
     * Enroll a student in a course
     *
     * @param userId   The user ID to enroll
     * @param courseId The course ID to enroll in
     * @return true if enrollment was successful, false otherwise
     */
    public boolean enrollStudentInCourse(Integer userId, Integer courseId) {
        return courseRepo.enrollStudentInCourse(userId, courseId);
    }

    /**
     * Enroll a student in multiple courses
     *
     * @param userId    The user ID to enroll
     * @param courseIds List of course IDs to enroll in
     * @return true if all enrollments were successful, false if any failed
     */
    public boolean enrollStudentInCourses(Integer userId, List<Integer> courseIds) {
        return courseRepo.enrollStudentInCourses(userId, courseIds);
    }

    /**
     * Unenroll a student from a course
     *
     * @param userId   The user ID to unenroll
     * @param courseId The course ID to unenroll from
     * @return true if unenrollment was successful, false otherwise
     */
    public boolean unenrollStudentFromCourse(Integer userId, Integer courseId) {
        return courseRepo.unenrollStudentFromCourse(userId, courseId);
    }

    /**
     * Check if a student is already enrolled in a course
     *
     * @param userId   The user ID
     * @param courseId The course ID
     * @return true if student is enrolled, false otherwise
     */
    public boolean isStudentEnrolledInCourse(Integer userId, Integer courseId) {
        return courseRepo.isStudentEnrolledInCourse(userId, courseId);
    }
}
