package com.smartattendance.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.AttendanceStatus;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.repository.AttendanceRecordRepository;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AttendanceController {
    @FXML private Label lblSessionTitle;
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> colStudentId;
    @FXML private TableColumn<AttendanceRecord, String> colStudentName;
    @FXML private TableColumn<AttendanceRecord, String> colStatus;
    @FXML private TableColumn<AttendanceRecord, String> colMethod;
    @FXML private TableColumn<AttendanceRecord, String> colMarkedAt;
    @FXML private TableColumn<AttendanceRecord, String> colLastSeen;
    @FXML private TableColumn<AttendanceRecord, String> colNote;

    private final AttendanceRecordRepository repo = new AttendanceRecordRepository();
    private final ObservableList<AttendanceRecord> attendanceList = FXCollections.observableArrayList();
    private Session currentSession;
    private Runnable backHandler;

    public void setBackHandler(Runnable backHandler) {
        this.backHandler = backHandler;
    }

    public void setSession(Session session) {
        this.currentSession = session;
        lblSessionTitle.setText("Attendance for Session ID: " + session.getSessionId());
        loadAttendanceRecords();
    }

    @FXML
    public void initialize() {
        // Configure Student ID column to get from Student object
        colStudentId.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            // Convert studentId to String 
            String studentId = String.valueOf(student.getStudentId());
            return new SimpleStringProperty(studentId);
        });

        // Configure Student Name column to get from Student object
        colStudentName.setCellValueFactory(cellData -> {
            Student student = cellData.getValue().getStudent();
            return new SimpleStringProperty(student.getName());
        });

        // Configure other columns
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Custom cell factory for status with dropdown
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<AttendanceRecord, String>() {
            // F_MA: modified by felicia handling marking attendance
            private final ComboBox<String> combo = new ComboBox<>(
                    FXCollections.observableArrayList("Present", "Absent", "Late"));

            {
                combo.setOnAction(event -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    if (record != null) {
                        // F_MA: modified by felicia handling marking attendance
                        record.setStatus(AttendanceStatus.valueOf(combo.getValue().toUpperCase()));
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    combo.setValue(item);
                    setGraphic(combo);
                    setText(null);
                }
            }
        });

        // F_MA: added by felicia handling marking attendance //
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        colMarkedAt.setCellValueFactory(cellData -> {
            LocalDateTime markedAt = cellData.getValue().getTimestamp();
            String formatted = markedAt != null ? dtf.format(markedAt) : "";
            return new SimpleStringProperty(formatted);
        });

        colLastSeen.setCellValueFactory(cellData -> {
            LocalDateTime lastSeen = cellData.getValue().getLastSeen();
            String formatted = lastSeen != null ? dtf.format(lastSeen) : "";
            return new SimpleStringProperty(formatted);
        });

        // Make table editable if you want to edit notes or status
        attendanceTable.setEditable(true);
    }

    private void loadAttendanceRecords() {
        if (currentSession != null) {
            List<AttendanceRecord> records = repo.findBySessionId(currentSession.getSessionId());
            attendanceList.setAll(records);
            attendanceTable.setItems(attendanceList);
        }
    }

    @FXML
    private void onSaveChanges() {
        try {
            int updatedCount = 0;
            for (AttendanceRecord record : attendanceList) {
                if (record != null) {
                    repo.updateStatus(record);
                    updatedCount++;
                }
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION, 
                String.format("Successfully updated %d attendance records!", updatedCount));
            alert.showAndWait();
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                "Error saving attendance records: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    // @FXML
    // private void onBack() {
    //     try {
    //         Parent root = FXMLLoader.load(getClass().getResource("/view/SessionView.fxml"));
    //         Scene currentScene = attendanceTable.getScene();
    //         currentScene.setRoot(root);
    //     } catch (Exception e) {
    //         Alert alert = new Alert(Alert.AlertType.ERROR, 
    //             "Error navigating back: " + e.getMessage());
    //         alert.showAndWait();
    //         e.printStackTrace();
    //     }
    // }

    @FXML
    private void onBack() {
        if (backHandler != null) {
            backHandler.run();
        } else {
            // fallback: try to use scene navigation (optional)
            try {
                Parent sessionRoot = FXMLLoader.load(getClass().getResource("/view/SessionView.fxml"));
                attendanceTable.getScene().setRoot(sessionRoot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}