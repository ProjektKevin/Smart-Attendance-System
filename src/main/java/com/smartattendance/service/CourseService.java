package com.smartattendance.service;

import java.util.List;

import com.smartattendance.model.dto.student.StudentDTO;
import com.smartattendance.model.entity.Course;
import com.smartattendance.repository.CourseRepository;

public class CourseService {
    public final CourseRepository courseRepo;

    public CourseService() {
        this.courseRepo = new CourseRepository();
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
}
