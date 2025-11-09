package com.smartattendance.repository;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, name, course_name FROM students";

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("course_name")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public Student findById(String id) {
        String sql = "SELECT student_id, name, course_name FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getString("student_id"),
                            rs.getString("name"),
                            rs.getString("course_name"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> findByCourse(String course) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, name, course_name FROM students WHERE course_name = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, course);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                            rs.getString("student_id"),
                            rs.getString("name"),
                            rs.getString("course_name")));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    public void save(Student s) {
        String sql = "INSERT INTO students (student_id, name, course_name) VALUES (?, ?, ?) " +
                "ON CONFLICT (student_id) DO UPDATE SET name = ?, course_name = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getStudentId());
            ps.setString(2, s.getUserName());
            ps.setString(3, s.getCourse());
            ps.setString(4, s.getUserName());
            ps.setString(5, s.getCourse());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // public List<Student> findByCourseWithFaceData(String course) {
    //     List<Student> students = new ArrayList<>();
    //     String sql = """
    //             SELECT
    //                 u.user_id,
    //                 u.username,
    //                 fd.avg_histogram
    //             FROM users u
    //             INNER JOIN face_data fd ON u.user_id = fd.student_id
    //             INNER JOIN enrollments e ON u.user_id = e.user_id
    //             INNER JOIN courses c ON e.course_id = c.course_id
    //             WHERE u.role = 'STUDENT' AND c.course_name = ?
    //             ORDER BY u.username
    //             """;

    //     try (Connection conn = DatabaseUtil.getConnection();
    //             PreparedStatement stmt = conn.prepareStatement(sql)) {

    //         stmt.setString(1, course);

    //         try (ResultSet rs = stmt.executeQuery()) {
    //             while (rs.next()) {
    //                 students.add(new Student(
    //                         rs.getInt("user_id"),
    //                         rs.getString("name"),
    //                         rs.getString("course_name")));
    //             }
    //         }

    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }

    //     return students;

    // }

}