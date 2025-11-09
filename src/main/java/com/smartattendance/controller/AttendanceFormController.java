package com.smartattendance.controller;

import java.time.LocalDateTime;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.AttendanceStatus;
import com.smartattendance.model.entity.MarkMethod;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.entity.Session;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.StudentService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private void onCreate() {
        try {
            int studentId = Integer.parseInt(txtStudentId.getText().trim());
            Student student = studentService.findById(studentId);

            if (student == null) {
                throw new Exception("No student found with ID " + studentId);
            }

            AttendanceRecord record = new AttendanceRecord(student, currentSession, AttendanceStatus.valueOf(cmbStatus.getValue()), 0.0, MarkMethod.MANUAL, LocalDateTime.now());
            // record.setStudent(student);
            // record.setSession(currentSession);
            // record.setStatus(cmbStatus.getValue());
            record.setNote(txtNote.getText());

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
