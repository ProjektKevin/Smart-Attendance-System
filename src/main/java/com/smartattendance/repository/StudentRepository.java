package com.smartattendance.repository;

import com.smartattendance.model.Student;
import java.util.List;

public interface StudentRepository {
  List<Student> findAll();

  Student findById(String id);

  void save(Student s);
}
