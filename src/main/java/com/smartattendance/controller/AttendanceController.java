package com.smartattendance.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.AttendanceStatus;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.util.AttendanceObserver;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AttendanceController implements AttendanceObserver{
    @FXML private Label lblSessionTitle;
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> colStudentId;
    @FXML private TableColumn<AttendanceRecord, String> colStudentName;
    @FXML private TableColumn<AttendanceRecord, String> colStatus;
    @FXML private TableColumn<AttendanceRecord, String> colMethod;
    @FXML private TableColumn<AttendanceRecord, String> colMarkedAt;
    @FXML private TableColumn<AttendanceRecord, String> colLastSeen;
    @FXML private TableColumn<AttendanceRecord, String> colNote;

    private final AttendanceService service = new AttendanceService();
    private final ObservableList<AttendanceRecord> attendanceList = FXCollections.observableArrayList();
    private Session currentSession;
    private Runnable backHandler;
    private final Map<Integer, String> originalStatuses = new HashMap<>();
    
    // Formatter for timestamp display
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onAttendanceMarked(AttendanceRecord record) {
        // single record marked (existing)
    }

    @Override
    public void onAttendanceAutoUpdated() {
        Platform.runLater(this::loadAttendanceRecords);
    }

    public void setBackHandler(Runnable backHandler) {
        this.backHandler = backHandler;
    }

    public void setSession(Session session) {
        this.currentSession = session;
        lblSessionTitle.setText("Attendance for Session ID: " + session.getSessionId());
        loadAttendanceRecords();
    }

    public static void requestUserConfirmation(AttendanceRecord record) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Student Identity");
            alert.setHeaderText("Low Confidence Detection");
            alert.setContentText(String.format(
                    // "Detected student:\n\nName: %s\nID: %d\nConfidence: %.2f\n\nIs this correct?",
                    "Detected student:\n\nName: %s\nID: %d\n\nIs this correct?",
                    record.getStudent().getName(),
                    record.getStudent().getStudentId()
                    // record.getConfidence()
            ));

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // user confirmed → mark attendance
                try {
                    record.mark();
                } catch (Exception e) {
                    System.err.println("Failed to save attendance record");
                    e.printStackTrace();
                }
            } else {
                // user declined → skip
                System.out.println("Skipped marking for " + record.getStudent().getName());
            }
        });
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
        // colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        // F_MA: modified by felicia handling marking attendance
        colMethod.setCellValueFactory(cellData ->
            new SimpleStringProperty(service.capitalize(cellData.getValue().getMethod().name()))
        );
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        
        // Configure marked_at column with proper formatting
        colMarkedAt.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTimestamp() != null) {
                return new SimpleStringProperty(cellData.getValue().getTimestamp().format(formatter));
            } else {
                return new SimpleStringProperty("-");
            }
        });
        
        // Configure last_seen column with proper formatting
        colLastSeen.setCellValueFactory(cellData -> {
            if (cellData.getValue().getLastSeen() != null) {
                return new SimpleStringProperty(cellData.getValue().getLastSeen().format(formatter));
            } else {
                return new SimpleStringProperty("-");
            }
        });

        // Custom cell factory for status with dropdown
        // colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        // F_MA: modified by felicia handling marking attendance
        colStatus.setCellValueFactory(cellData ->
            new SimpleStringProperty(service.capitalize(cellData.getValue().getStatus().name()))
        );
        colStatus.setCellFactory(column -> new TableCell<AttendanceRecord, String>() {
            // F_MA: modified by felicia handling marking attendance
            private final ComboBox<String> combo = new ComboBox<>(
                    FXCollections.observableArrayList("Present", "Absent", "Late", "Pending"));

            {
                combo.setOnAction(event -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    if (record != null) {
                        // F_MA: modified by felicia handling marking attendance
                        // String selected = combo.getValue();
                        // if (selected != null) {
                        record.setStatus(AttendanceStatus.valueOf(combo.getValue().toUpperCase()));
                        // }
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

    // F_MA: modified by felicia handling marking attendance
    // private void loadAttendanceRecords() {
    public void loadAttendanceRecords() {
        if (currentSession != null) {
            List<AttendanceRecord> records = service.findBySessionId(currentSession.getSessionId());
            attendanceList.setAll(records);
            attendanceTable.setItems(attendanceList);

            // Store the original statuses for change detection
            originalStatuses.clear();
            for (AttendanceRecord record : records) {
                originalStatuses.put(record.getStudent().getStudentId(), record.getStatus().toString());
            }
        }
    }

    @FXML
    private void onSaveChanges() {
        try {
            int updatedCount = 0;
            for (AttendanceRecord record : attendanceList) {
                String originalStatus = originalStatuses.get(record.getStudent().getStudentId());
                String currentStatus = record.getStatus().toString();

                // Only update if status actually changed
                if (!java.util.Objects.equals(originalStatus, currentStatus)) {
                    // F_MA: modified by felicia handling marking attendance
                    record.setTimestamp(LocalDateTime.now());
                    // record.setLastSeen(LocalDateTime.now());
                    service.updateStatus(record);
                    updatedCount++;

                    // Update the original status map so subsequent saves work fine
                    originalStatuses.put(record.getStudent().getStudentId(), currentStatus);
                }
            }

            // Reload data from database after saving
            loadAttendanceRecords();
            
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

    @FXML
    private void onBack() {
        if (backHandler != null) {
            backHandler.run();
        } else {
            try {
                Parent sessionRoot = FXMLLoader.load(getClass().getResource("/view/SessionView.fxml"));
                attendanceTable.getScene().setRoot(sessionRoot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}