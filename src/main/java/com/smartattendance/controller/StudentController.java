package com.smartattendance.controller;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.Student;
import com.smartattendance.service.StudentService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Simple controller for displaying and adding students in a table view.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Configure the {@link TableView} columns to bind to {@link Student} properties</li>
 *     <li>Load the initial list of students from {@link StudentService}</li>
 *     <li>Provide a basic "Add student" handler for demo/testing</li>
 * </ul>
 *
 * <p>
 * This controller is intentionally minimal and mainly used as a simple CRUD-style
 * table example in the manager view.
 * </p>
 *
 * @author Ernest Lun
 */
public class StudentController {

    /**
     * Table showing all students.
     */
    @FXML
    private TableView<Student> studentTable;

    /**
     * Column for displaying the student ID.
     */
    @FXML
    private TableColumn<Student, String> colId;

    /**
     * Column for displaying the student name.
     */
    @FXML
    private TableColumn<Student, String> colName;

    /**
     * Column for displaying the student's course.
     */
    @FXML
    private TableColumn<Student, String> colCourse;

    /**
     * Backing service for student data, obtained from the application context.
     */
    private final StudentService svc = ApplicationContext.getStudentService();

    /**
     * Called automatically by JavaFX after FXML fields are injected.
     *
     * <p>
     * Sets up column–property bindings and loads the initial list of students
     * into the table.
     * </p>
     */
    @FXML
    public void initialize() {
        // Bind table columns to Student entity properties by name
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course"));

        // Load student data from the service and display in the table
        ObservableList<Student> data =
                FXCollections.observableArrayList(svc.getAllStudents());
        studentTable.setItems(data);
    }

    /**
     * Handler for the "Add Student" action.
     *
     * <p>
     * Creates a dummy {@link Student} with random ID and fixed name/course,
     * persists it via {@link StudentService}, and appends it to the table.
     * This is primarily for testing/demo purposes and not production logic.
     * </p>
     */
    @FXML
    private void onAddStudent() {
        // Create a simple demo student with a random ID
        Student s = new Student(
                1 + (int) (Math.random() * 10),
                "New Student",
                "CS102"
        );

        // Persist via service and update table
        svc.addStudent(s);
        studentTable.getItems().add(s);
    }
}
