package com.smartattendance.controller;

import java.time.LocalDateTime;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.AttendanceStatus;
import com.smartattendance.model.entity.MarkMethod;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.StudentService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AttendanceFormController {

    @FXML
    private TextField txtStudentId;
    @FXML
    private ComboBox<String> cmbStatus;
    @FXML
    private TextField txtNote;

    private AttendanceService attendanceService = new AttendanceService();
    private StudentService studentService = new StudentService();
    private Session currentSession;

    private AttendanceRecord newRecord;

    public void setSession(Session session) {
        this.currentSession = session;
    }

    public AttendanceRecord getNewRecord() {
        return newRecord;
    }

    @FXML
    public void initialize() {
        // Populate ComboBox safely
        if (cmbStatus != null) {
            cmbStatus.getItems().addAll("Present", "Absent", "Late", "Pending");
        }
    }


    @FXML
    private void onCreate() {
        try {
            // System.out.println("|" + cmbStatus.getValue().toUpperCase() + "|"); // for testing

            String status = cmbStatus.getValue();
            int studentId = Integer.parseInt(txtStudentId.getText().trim());
            Student student = studentService.findById(studentId);

            if (student == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("No student found with studentId: " + studentId);
                alert.setContentText("Please enter a valid studentId.");
                alert.showAndWait();
                throw new Exception("No student found with studentId: " + studentId);
            }

            if (status == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("No Status Selected");
                alert.setContentText("Please select a status before submitting.");
                alert.showAndWait();
                throw new Exception("No status selected");
            }

            AttendanceRecord record = new AttendanceRecord(student, currentSession, AttendanceStatus.valueOf(status.toUpperCase()), 0.0, MarkMethod.MANUAL, LocalDateTime.now());
            // record.setStudent(student);
            // record.setSession(currentSession);
            // record.setStatus(cmbStatus.getValue());
            if (txtNote.getText() != null) {
                record.setNote(txtNote.getText());
            } else {
                record.setNote("Manual-created");
            }

            attendanceService.saveRecord(record);
            newRecord = record;

            // Close the dialog
            Stage stage = (Stage) txtStudentId.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            System.err.println("Invalid student ID.");
        } catch (Exception e) {
            System.err.println("Error creating record: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) txtStudentId.getScene().getWindow();
        stage.close();
    }
}
