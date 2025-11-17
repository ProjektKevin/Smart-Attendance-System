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
import com.smartattendance.util.security.log.ApplicationLogger;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for attendance records creation in a session. Show alert messages
 * when user input is invalid.
 *
 * @author Chue Wan Yan
 *
 * @version 22:44 09 Nov 2025
 *
 */
public class AttendanceFormController {

    // ======= FXML UI Components =======
    @FXML
    private ComboBox<String> cmbStudentId; // ComboBox to select a student
    @FXML
    private ComboBox<String> cmbStatus; // ComboBox to select attendance status
    @FXML
    private TextField txtNote; // TextField to enter optional note

    // ======= Services and Data =======
    private AttendanceService attendanceService = new AttendanceService(); // Service to handle attendance records
    private StudentService studentService = new StudentService(); // Service to handle student info
    private Session currentSession; // The session for which the record is being created
    private AttendanceRecord newRecord; // The newly created attendance record
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance(); // App logger to show message in terminal

    /**
     * Sets the session for the attendance record that will be created.
     *
     * @param session the Session object
     */
    public void setSession(Session session) {
        this.currentSession = session;
    }

    /**
     * Get newRecord.
     *
     * @return the new AttendanceRecord, or null if none created
     */
    public AttendanceRecord getNewRecord() {
        return newRecord;
    }

    /**
     * Initializes the controller after its root element has been completely
     * processed. Populates the status ComboBox with predefined values.
     */
    @FXML
    public void initialize() {
        // Populate ComboBox safely
        if (cmbStatus != null) {
            cmbStatus.getItems().addAll("Present", "Absent",
                    "Late", "Pending");
        }
    }

    /**
     * Populates the student ComboBox with students from the current session who
     * haven't have an attendance record yet.
     */
    public void populateStudents() {
        if (cmbStudentId != null && currentSession != null) {
            // Get all students for this session
            List<Student> allStudents
                    = studentService.getStudentsBySessionId(currentSession);

            // Get all attendance records for this session
            List<AttendanceRecord> existingRecords
                    = attendanceService.findBySessionId(currentSession.getSessionId());

            // Extract student IDs that already have attendance records
            Set<Integer> existingStudentIds = existingRecords.stream()
                    .map(record -> record.getStudent().getStudentId())
                    .collect(java.util.stream.Collectors.toSet());

            // Add only students who haven't have record yet
            allStudents.stream()
                    .filter(student -> !existingStudentIds.contains(student.getStudentId()))
                    .forEach(student
                            -> cmbStudentId.getItems().add(student.getStudentId()
                            + " - " + student.getName())
                    );
            // studentService.getStudentsBySessionId(currentSession).forEach(student
            // -> cmbStudentId.getItems().add(String.valueOf(student.getStudentId() + " - "
            // + student.getName()))
            // );
        }
    }

    /**
     * Handles creating a new attendance record when the user clicks the
     * "Create" button. Validates user input and saves the record using
     * AttendanceService.
     */
    @FXML
    private void onCreate() {
        try {
            // System.out.println("|" + cmbStatus.getValue().toUpperCase() + "|"); // for
            // testing
            int studentId = 0;
            Student student = null;
            String status = cmbStatus.getValue();
            // int studentId = Integer.parseInt(cmbStudentId.getValue());
            // int trimStudentId;

            // Validate student selection
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
            // Alert alert = new Alert(Alert.AlertType.WARNING);
            // alert.setTitle("Warning");
            // alert.setHeaderText("No student found with studentId: " + studentId);
            // alert.setContentText("Please enter a valid studentId.");
            // alert.showAndWait();
            // throw new Exception("No student found with studentId: " + studentId);
            // }
            // Validate status selection
            if (status == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("No Status Selected");
                alert.setContentText("Please select a status before submitting.");
                alert.showAndWait();
                throw new Exception("No status selected");
            }

            // Create new AttendanceRecord
            AttendanceRecord record = new AttendanceRecord(student, currentSession,
                    AttendanceStatus.valueOf(status.toUpperCase()), 0.0, 
                    MarkMethod.MANUAL, LocalDateTime.now());

            // record.setStudent(student);
            // record.setSession(currentSession);
            // record.setStatus(cmbStatus.getValue());

            // Set note if not nulls
            if (txtNote.getText() != "") {
                record.setNote(txtNote.getText());
            } else {
                record.setNote("Manual-created");
            }

            // Save record
            attendanceService.saveRecord(record);
            newRecord = record;

            // Close the form dialog
            Stage stage = (Stage) cmbStudentId.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            appLogger.error("Invalid student ID.");
        } catch (Exception e) {
            appLogger.error("Error creating record: " + e.getMessage());
        }
    }

    /**
     * Closes the attendance form without saving when the user clicks the "Cancel" button.
     */
    @FXML
    private void onCancel() {
        Stage stage = (Stage) cmbStudentId.getScene().getWindow();
        stage.close();
    }
}
