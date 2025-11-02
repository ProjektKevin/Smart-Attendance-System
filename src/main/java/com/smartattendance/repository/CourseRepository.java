package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.dto.student.StudentDTO;
import com.smartattendance.model.entity.Course;

public class CourseRepository {

    public Course findCourseById(Integer id) {
        String sql = "SELECT * FROM courses WHERE course_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getInt("course_id"),
                            rs.getString("course_name"),
                            rs.getString("course_code"));

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Course> findAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses";

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_name"),
                        rs.getString("course_code")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Find all students enrolled in a specific course with their full names
     *
     * @param courseId The course ID to filter by
     * @return List of StudentDTO containing studentId, fullName, and courseName
     */
    public List<StudentDTO> findStudentsByCourseId(Integer courseId) {
        List<StudentDTO> students = new ArrayList<>();
        String sql = "SELECT " +
                     "    u.user_id as studentId, " +
                     "    CONCAT(p.first_name, ' ', p.last_name) as fullName, " +
                     "    c.course_name as courseName " +
                     "FROM users u " +
                     "JOIN profile p ON u.user_id = p.user_id " +
                     "JOIN enrollments e ON u.user_id = e.user_id " +
                     "JOIN courses c ON e.course_id = c.course_id " +
                     "WHERE u.role = 'STUDENT' AND c.course_id = ? " +
                     "ORDER BY u.user_id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new StudentDTO(
                            rs.getInt("studentId"),
                            rs.getString("fullName"),
                            rs.getString("courseName")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    /**
     * Find all students enrolled in a course by course name with their full names
     *
     * @param courseName The course name to filter by
     * @return List of StudentDTO containing studentId, fullName, and courseName
     */
    public List<StudentDTO> findStudentsByCourseName(String courseName) {
        List<StudentDTO> students = new ArrayList<>();
        String sql = "SELECT " +
                     "    u.user_id as studentId, " +
                     "    CONCAT(p.first_name, ' ', p.last_name) as fullName, " +
                     "    c.course_name as courseName " +
                     "FROM users u " +
                     "JOIN profile p ON u.user_id = p.user_id " +
                     "JOIN enrollments e ON u.user_id = e.user_id " +
                     "JOIN courses c ON e.course_id = c.course_id " +
                     "WHERE u.role = 'STUDENT' AND c.course_name = ? " +
                     "ORDER BY u.user_id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, courseName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new StudentDTO(
                            rs.getInt("studentId"),
                            rs.getString("fullName"),
                            rs.getString("courseName")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    /**
     * Find all students across all courses with their full names
     *
     * @return List of StudentDTO containing studentId, fullName, and courseName
     */
    public List<StudentDTO> findAllStudents() {
        List<StudentDTO> students = new ArrayList<>();
        String sql = "SELECT " +
                     "    u.user_id as studentId, " +
                     "    CONCAT(p.first_name, ' ', p.last_name) as fullName, " +
                     "    c.course_name as courseName " +
                     "FROM users u " +
                     "JOIN profile p ON u.user_id = p.user_id " +
                     "JOIN enrollments e ON u.user_id = e.user_id " +
                     "JOIN courses c ON e.course_id = c.course_id " +
                     "WHERE u.role = 'STUDENT' " +
                     "ORDER BY u.user_id";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(new StudentDTO(
                        rs.getInt("studentId"),
                        rs.getString("fullName"),
                        rs.getString("courseName")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }
}
