package com.smartattendance.controller;

import com.smartattendance.model.Student;
import com.smartattendance.service.StudentService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class StudentController {
  @FXML
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
}
