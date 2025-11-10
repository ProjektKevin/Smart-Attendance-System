package com.smartattendance.service;

import com.smartattendance.model.dto.student.StudentProfileDTO;
import com.smartattendance.model.dto.user.UserListDTO;
import com.smartattendance.model.entity.Profile;
import com.smartattendance.model.entity.User;
import com.smartattendance.repository.PostgresUserRepository;
import com.smartattendance.repository.ProfileRepository;
import com.smartattendance.repository.CourseRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class UserService {
    private final PostgresUserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CourseRepository courseRepository;

    public UserService() {
        this.userRepository = new PostgresUserRepository();
        this.profileRepository = new ProfileRepository();
        this.courseRepository = new CourseRepository();
    }

    public UserService(PostgresUserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.courseRepository = new CourseRepository();
    }

    public UserService(PostgresUserRepository userRepository, ProfileRepository profileRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.courseRepository = courseRepository;
    }

    // ==================== Entity Methods ====================

    /**
     * Get all users by role
     *
     * @param role The role filtered
     * @return List of User objects by the filtered role
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findUsersByRole(role);
    }

    /**
     * Get user by the id
     *
     * @param userid The id of the user
     * @return User object found by id
     */
    public User getUserById(Integer userId) {
        return userRepository.findUserById(userId);
    }

    /**
     * Delete user
     *
     * @param userId The user id
     * @return Boolean: if deleted > true. If not > false
     */
    public boolean deleteUser(Integer userId) {
        return userRepository.deleteUserById(userId);
    }

    // ==================== DTO Methods ====================

    /**
     * Get all students as StudentListDTO for table display
     *
     * @return List of StudentListDTO objects
     */
    public List<UserListDTO> getStudentListDTOs() {
        return getUsersByRole("STUDENT")
                .stream()
                .map(UserListDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get student profile DTO with full details for profile dialog
     *
     * @param userId The user ID
     * @return UserProfileDTO with user, profile information, and enrolled courses
     */
    public StudentProfileDTO getUserProfileDTO(Integer userId) {
        User user = getUserById(userId);
        Profile profile = profileRepository.getProfileById(userId);

        // Load enrolled courses for the student
        List<String> enrolledCourses = new ArrayList<>();
        try {
            List<com.smartattendance.model.entity.Course> courses = courseRepository.findAllCourses();
            for (com.smartattendance.model.entity.Course course : courses) {
                if (courseRepository.isStudentEnrolledInCourse(userId, course.getId())) {
                    // Display as "CODE - NAME" format
                    enrolledCourses.add(course.getCode() + " - " + course.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new StudentProfileDTO(user, profile, enrolledCourses);
    }

    // ==================== Enrollment Methods ====================

    /**
     * Enroll a student in multiple courses
     *
     * @param userId    The user ID to enroll
     * @param courseIds List of course IDs to enroll in
     * @return true if all enrollments were successful, false if any failed
     */
    public boolean enrollStudentInCourses(Integer userId, List<Integer> courseIds) {
        return courseRepository.enrollStudentInCourses(userId, courseIds);
    }

    /**
     * Enroll a student in a single course
     *
     * @param userId   The user ID to enroll
     * @param courseId The course ID to enroll in
     * @return true if enrollment was successful, false otherwise
     */
    public boolean enrollStudentInCourse(Integer userId, Integer courseId) {
        return courseRepository.enrollStudentInCourse(userId, courseId);
    }

    /**
     * Unenroll a student from a course
     *
     * @param userId   The user ID to unenroll
     * @param courseId The course ID to unenroll from
     * @return true if unenrollment was successful, false otherwise
     */
    public boolean unenrollStudentFromCourse(Integer userId, Integer courseId) {
        return courseRepository.unenrollStudentFromCourse(userId, courseId);
    }

    /**
     * Check if a student is already enrolled in a course
     *
     * @param userId   The user ID
     * @param courseId The course ID
     * @return true if student is enrolled, false otherwise
     */
    public boolean isStudentEnrolledInCourse(Integer userId, Integer courseId) {
        return courseRepository.isStudentEnrolledInCourse(userId, courseId);
    }
}