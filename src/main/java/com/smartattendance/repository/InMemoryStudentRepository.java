package com.smartattendance.repository;

import com.smartattendance.model.Student;
import java.util.*;

public class InMemoryStudentRepository implements StudentRepository {
  private final List<Student> students = new ArrayList<>();

  public InMemoryStudentRepository() {
    students.add(new Student("S101", "Alice Tan", "G1"));
    students.add(new Student("S102", "Bob Lee", "G1"));
    students.add(new Student("S103", "Cheryl Ong", "G2"));
  }

  public List<Student> findAll() {
    return new ArrayList<>(students);
  }

  public Student findById(String id) {
    return students.stream().filter(s -> s.getStudentId().equals(id)).findFirst().orElse(null);
  }

  public void save(Student s) {
    students.add(s);
  }
}
