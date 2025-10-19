package com.smartattendance.repository;

import com.smartattendance.model.Student;
import com.smartattendance.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresStudentRepository implements StudentRepository {

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, name, group_name FROM students"; // adjust table column names

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("group_name")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    @Override
    public Student findById(String id) {
        String sql = "SELECT student_id, name, group_name FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getString("student_id"),
                            rs.getString("name"),
                            rs.getString("group_name")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void save(Student s) {
        String sql = "INSERT INTO students (student_id, name, group_name) VALUES (?, ?, ?) " +
                     "ON CONFLICT (student_id) DO UPDATE SET name = ?, group_name = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getStudentId());
            ps.setString(2, s.getName());
            ps.setString(3, s.getGroup());
            ps.setString(4, s.getName());
            ps.setString(5, s.getGroup());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
