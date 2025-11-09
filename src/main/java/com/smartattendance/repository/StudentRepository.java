package com.smartattendance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.smartattendance.config.DatabaseUtil;
import com.smartattendance.model.entity.Student;

public class StudentRepository {

    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT e.user_id, u.username, c.course_code FROM enrollments e JOIN users u ON e.user_id = u.user_id JOIN courses c ON e.course_id = c.course_id;"; 

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("course_code")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public Student findById(int id) {
        String sql = "SELECT e.user_id, u.username, c.course_code FROM enrollments e JOIN users u ON e.user_id = u.user_id JOIN courses c ON e.course_id = c.course_id WHERE e.user_id = ?;";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("course_code")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> findByCourse(String course) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT e.user_id, u.username, c.course_code FROM enrollments e JOIN users u ON e.user_id = u.user_id JOIN courses c ON e.course_id = c.course_id WHERE c.course_code = ?;"; 

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, course);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("course_code")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    public void save(Student s) {
        String sql = "INSERT INTO enrollments (user_id, course_id) VALUES (?, (SELECT course_id FROM courses WHERE course_code = ?));";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getStudentId());
            ps.setString(2, s.getCourse());

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