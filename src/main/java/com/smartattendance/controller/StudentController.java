package com.smartattendance.controller;

<<<<<<< HEAD
import com.smartattendance.model.Student;
import com.smartattendance.service.StudentService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
=======
import com.smartattendance.ApplicationContext;
import com.smartattendance.model.dto.student.StudentDTO;
import com.smartattendance.service.CourseService;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
>>>>>>> origin/dev
import javafx.scene.control.cell.PropertyValueFactory;

public class StudentController {
  @FXML
<<<<<<< HEAD
  private TableView<Student> studentTable;
  @FXML
  private TableColumn<Student, String> colId;
  @FXML
  private TableColumn<Student, String> colName;
  @FXML
  private TableColumn<Student, String> colGroup;
  private final StudentService svc = new StudentService();

  @FXML
  public void initialize() {
    colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
    colName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colGroup.setCellValueFactory(new PropertyValueFactory<>("course"));
    ObservableList<Student> data = FXCollections.observableArrayList(svc.getAllStudents());
    studentTable.setItems(data);
  }

  @FXML
  private void onAddStudent() {
    Student s = new Student("S" + (100 + (int) (Math.random() * 900)), "New Student", "CS102");
    svc.addStudent(s);
    studentTable.getItems().add(s);
  }
=======
  private TableView<StudentDTO> studentTable;
  @FXML
  private TableColumn<StudentDTO, String> colId;
  @FXML
  private TableColumn<StudentDTO, String> colName;
  @FXML
  private TableColumn<StudentDTO, String> colCourse;
  private final CourseService svc = ApplicationContext.getCourseService();

  @FXML
  public void initialize() {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
    colCourse.setCellValueFactory(new PropertyValueFactory<>("courseName"));
    ObservableList<StudentDTO> data = FXCollections.observableArrayList(svc.getAllStudents());
    studentTable.setItems(data);
  }

  /*
   * chore(): Update this back
   * Search for students, get id
   * Search for course, get id
   * Call database function to insert into enrollments table?
   * And reload the function to call all students again?
   */

  // @FXML
  // private void onAddStudent() {
  // StudentDTO s = new StudentDTO((100 + (int) (Math.random() * 900)), "New
  // Student", "CS102");
  // svc.addStudent(s);
  // studentTable.getItems().add(s);
  // }
>>>>>>> origin/dev
}
