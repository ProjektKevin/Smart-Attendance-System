package com.smartattendance.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.model.enums.MarkMethod;
import com.smartattendance.model.entity.Session;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.StudentService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AttendanceFormController {

    @FXML
    private ComboBox<String> cmbStudentId;
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

    public void populateStudents() {
        if (cmbStudentId != null && currentSession != null) {
            // 1. Get all students for this session
            List<Student> allStudents = studentService.getStudentsBySessionId(currentSession);

            // 2. Get all attendance records for this session
            List<AttendanceRecord> existingRecords = attendanceService.findBySessionId(currentSession.getSessionId());

            // 3. Extract student IDs that already have attendance records
            Set<Integer> existingStudentIds = existingRecords.stream()
                    .map(record -> record.getStudent().getStudentId())
                    .collect(java.util.stream.Collectors.toSet());

            // 4. Add only students who are not yet marked
            allStudents.stream()
                    .filter(student -> !existingStudentIds.contains(student.getStudentId()))
                    .forEach(student
                            -> cmbStudentId.getItems().add(student.getStudentId() + " - " + student.getName())
                    );
            // studentService.getStudentsBySessionId(currentSession).forEach(student
            //         -> cmbStudentId.getItems().add(String.valueOf(student.getStudentId() + " - " + student.getName()))
            // );
        }
    }

    @FXML
    private void onCreate() {
        try {
            // System.out.println("|" + cmbStatus.getValue().toUpperCase() + "|"); // for testing
            int studentId = 0;
            Student student = null;
            String status = cmbStatus.getValue();
            // int studentId = Integer.parseInt(cmbStudentId.getValue());
            // int trimStudentId;

            if (cmbStudentId.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("No student selected");
                alert.setContentText("Please select a student.");
                alert.showAndWait();
                throw new Exception("No student selected");
            } else {
                studentId = Integer.parseInt(cmbStudentId.getValue().split("-")[0].trim()); // get the part before '-'
                student = studentService.findById(studentId);
            }

            

            // if (student == null) {
            //     Alert alert = new Alert(Alert.AlertType.WARNING);
            //     alert.setTitle("Warning");
            //     alert.setHeaderText("No student found with studentId: " + studentId);
            //     alert.setContentText("Please enter a valid studentId.");
            //     alert.showAndWait();
            //     throw new Exception("No student found with studentId: " + studentId);
            // }

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
            if (txtNote.getText() != "") {
                record.setNote(txtNote.getText());
            } else {
                record.setNote("Manual-created");
            }

            attendanceService.saveRecord(record);
            newRecord = record;

            // Close the dialog
            Stage stage = (Stage) cmbStudentId.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            System.err.println("Invalid student ID.");
        } catch (Exception e) {
            System.err.println("Error creating record: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cmbStudentId.getScene().getWindow();
        stage.close();
    }
}
