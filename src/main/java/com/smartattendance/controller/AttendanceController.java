package com.smartattendance.controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.model.enums.AttendanceStatus;
import com.smartattendance.service.AttendanceMarker;
import com.smartattendance.service.AttendanceObserver;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.ManualAttendanceMarker;
import com.smartattendance.service.StudentService;
import com.smartattendance.util.security.log.ApplicationLogger;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for managing attendance records in a session. Implements
 * {@link AttendanceObserver} to show alert messages and update UI when
 * attendance changes. Implements {@link TabRefreshable} to update UI when
 * attendance changes.
 *
 * @author Chue Wan Yan (modify existing methods and create rest of the methods,
 * add comments and organize the codes)
 * @author Lim Jia Hui (setSession, onBack, saveChanges, setBackHandler,
 * initialize, loadAttendanceRecords)
 *
 * @version 14:58 15 Nov 2025
 *
 */
public class AttendanceController implements AttendanceObserver, TabRefreshable {

    // ======= FXML UI Components =======
    @FXML
    private Label attendanceInfo; // Label to display messages to the user
    @FXML
    private Label lblSessionTitle; // Label to display current session title
    @FXML
    private Label lblAttendanceSummary; // Label to display attendance statistics (number of present, late and total students)
    @FXML
    private Button createButton; // Button to create new attendance records
    @FXML
    private Button deleteButton; // Button to delete selected attendance records
    @FXML
    private Button clearEditButton; // Button to clear edits
    @FXML
    private TableView<AttendanceRecord> attendanceTable; // Table showing attendance records
    @FXML
    private TableColumn<AttendanceRecord, String> colStudentId; // Student Id column
    @FXML
    private TableColumn<AttendanceRecord, String> colStudentName; // Student name column
    @FXML
    private TableColumn<AttendanceRecord, String> colStatus; // Attendance status column
    @FXML
    private TableColumn<AttendanceRecord, String> colMethod; // Attendance marking method column
    @FXML
    private TableColumn<AttendanceRecord, String> colMarkedAt; // Attendance marking time
    @FXML
    private TableColumn<AttendanceRecord, String> colLastSeen; // Last seen time for a student (same as MarkerAt if didn't update last seen)
    @FXML
    private TableColumn<AttendanceRecord, String> colNote; // Note column for an attendance record
    @FXML
    private TableColumn<AttendanceRecord, Boolean> colSelect; // Selection checkbox for an attendance record

    // ======= Services =======
    // private final ApplicationLogger appLogger = ApplicationLogger.getInstance();
    private final AttendanceService service = new AttendanceService(); // Local service instance
    private final AttendanceService attendanceService
            = ApplicationContext.getAttendanceService(); // Shared attendance service
    private final StudentService studentService = new StudentService(); // Fetch student info

    // ======= Data =======
    private final ObservableList<AttendanceRecord> attendanceList
            = FXCollections.observableArrayList(); // Observable list of attendance records
    private Session currentSession; // Currently selected session

    // ======= Handlers =======
    private Runnable backHandler; // Custom back button handler

    // ======= Flags =======
    private boolean isEditing = false; // Track edit mode
    private boolean hasUnsavedChanges = false; // Track unsaved changes

    // ======= Track original values for manual marking =======
    private final Map<Integer, String> originalStatuses = new HashMap<>();
    private final Map<Integer, String> originalNotes = new HashMap<>();

    // ======= Formatter for timestamp display =======
    private final DateTimeFormatter formatter
            = DateTimeFormatter.ofPattern(Config.get("datetime.format"));

    // ======= Track selections of attendance records in the table =======
    private final Map<Integer, SimpleBooleanProperty> selectionMap = new HashMap<>();

    // ======= AttendanceObserver Methods =======
    /**
     * Called when an attendance record is marked. Implementation required by
     * {@link AttendanceObserver}.
     *
     * @param message Optional message describing the marking
     */
    @Override
    public void onAttendanceMarked(String message) {
        // Callback when an attendance record is marked to load attendance records
        loadAttendanceRecords();
    }

    /**
     * Called when auto-updated attendance occurs. Reloads the attendance table
     * on the JavaFX thread.
     */
    public void onAttendanceAutoUpdated() {
        // Called when auto-marking updates multiple records
        // Update the table on the JavaFX UI thread
        Platform.runLater(this::loadAttendanceRecords);
    }

    // ======= Message Display Helpers =======
    /**
     * Displays a success message in the info label.
     *
     * @param message The message text
     */
    private void showSuccess(String message) {
        styleInfoLabel("success", "✓ " + message);
    }

    /**
     * Displays an error message in the info label.
     *
     * @param message The message text
     */
    private void showError(String message) {
        styleInfoLabel("error", "✗ " + message);
    }

    /**
     * Displays a warning message in the info label.
     *
     * @param message The message text
     */
    private void showWarning(String message) {
        styleInfoLabel("warning", "⚠ " + message);
    }

    /**
     * Displays a neutral info message in the info label.
     *
     * @param message The message text
     */
    private void showInfo(String message) {
        styleInfoLabel("normal", "ℹ " + message);
    }

    /**
     * Styles the info label based on message type.
     *
     * @param type "success", "error", "warning", or "normal"
     * @param message Text to display
     */
    private void styleInfoLabel(String type, String message) {
        attendanceInfo.setText(message);

        // Reset styles first
        attendanceInfo.setStyle("-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        switch (type.toLowerCase()) {
            case "success":
                attendanceInfo.setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda; -fx-border-color: #c3e6cb; "
                        + "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
            case "error":
                attendanceInfo.setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; -fx-border-color: #f5c6cb; "
                        + "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
            case "warning":
                attendanceInfo.setStyle("-fx-text-fill: #856404; -fx-background-color: #fff3cd; -fx-border-color: #ffeaa7; "
                        + "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
            case "normal":
            default:
                attendanceInfo.setStyle("-fx-text-fill: #383d41; -fx-background-color: #e2e3e5; -fx-border-color: #d6d8db; "
                        + "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-width: 1px;");
                break;
        }

        // Auto-clear after 3 seconds
        // PauseTransition pause = new PauseTransition(Duration.seconds(3));
        // pause.setOnFinished(e -> attendanceInfo.setText(""));
        // pause.play();
    }

    /**
     * Sets the back button handler.
     *
     * @param backHandler Runnable to execute on back
     */
    public void setBackHandler(Runnable backHandler) {
        this.backHandler = backHandler;
    }

    /**
     * Sets the current session for which attendance is displayed.
     *
     * @param session The session
     */
    public void setSession(Session session) {
        this.currentSession = session;
        lblSessionTitle.setText("Attendance for Session ID: " + session.getSessionId());
        loadAttendanceRecords();
    }

    /**
     * Initializes the selection map for checkboxes in the table.
     */
    private void initSelectionMap() {
        selectionMap.clear();
        for (AttendanceRecord record : attendanceList) {
            selectionMap.put(record.getStudent().getStudentId(), new SimpleBooleanProperty(false));
        }
    }

    /**
     * Sets up the checkbox column for selecting attendance records.
     */
    private void setupCheckBoxColumn() {
        colSelect.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    selectionMap.get(record.getStudent().getStudentId())
                            .set(checkBox.isSelected());
                    updateButtonStates();
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    AttendanceRecord record = getTableRow().getItem();
                    checkBox.setSelected(selectionMap.get(record.getStudent().getStudentId()).get());
                    setGraphic(checkBox);
                }
            }
        });
        colSelect.setCellValueFactory(cellData -> {
            int id = cellData.getValue().getStudent().getStudentId();
            return selectionMap.get(id);
        });
    }

    // /**
    //  * Shows a confirmation dialog asynchronously for low-confidence recognition. (being moved to RecognitionController)
    //  *
    //  * @param record   The attendance record requiring confirmation
    //  * @param callback Consumer<Boolean> that receives true if confirmed, false otherwise
    //  */
    // public void requestUserConfirmationAsync(AttendanceRecord record, Consumer<Boolean> callback) {
    //     Platform.runLater(() -> {
    //         Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    //         alert.setTitle("Confirm Student Identity");
    //         alert.setHeaderText("Low Confidence Detection");
    //         alert.setContentText(String.format(
    //                 "Detected student:\n\nName: %s\nID: %d\n\nIs this correct?",
    //                 record.getStudent().getName(),
    //                 record.getStudent().getStudentId()
    //         ));
    //         ButtonType yesButton = new ButtonType("Yes");
    //         ButtonType noButton = new ButtonType("No");
    //         alert.getButtonTypes().setAll(yesButton, noButton);
    //         Optional<ButtonType> result = alert.showAndWait();
    //         callback.accept(result.isPresent() && result.get() == yesButton);
    //     });
    // }
    /**
     * Initializes the controller and sets up table columns, buttons, and
     * observers.
     */
    @FXML
    public void initialize() {
        // Let each row have a selectable checkbox
        setupCheckBoxColumn();

        // Buttons to create or delete records
        createButton.setOnAction(e -> onCreateRecord());
        deleteButton.setOnAction(e -> onDeleteRecord());

        // Configure Student Id column to get from Student object
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
        colMethod.setCellValueFactory(cellData
                -> new SimpleStringProperty(service.capitalize(cellData.getValue().getMethod().name()))
        );

        // Configure note column to be editable
        colNote.setCellValueFactory(cellData
                -> new SimpleStringProperty(cellData.getValue().getNote() != null ? cellData.getValue().getNote() : "")
        );

        colNote.setCellFactory(column -> new TableCell<AttendanceRecord, String>() {
            private final Label label = new Label();
            private final ScrollPane scroll = new ScrollPane();
            private final Button editButton = new Button();
            private final StackPane stack = new StackPane();

            {
                label.setWrapText(true);
                label.setPadding(new Insets(5));
                label.setTooltip(new Tooltip("Click pencil to edit"));

                scroll.setContent(label);
                scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scroll.setFitToWidth(true);
                scroll.setPrefHeight(50);
                scroll.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

                // Pencil icon as button
                ImageView pencilIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/pencil_431.png")));
                pencilIcon.setFitWidth(14);
                pencilIcon.setFitHeight(14);
                editButton.setGraphic(pencilIcon);
                editButton.setText("");
                editButton.setStyle("-fx-background-color: transparent;");
                editButton.setOnAction(e -> {
                    enterEditMode(); // only called when user clicks pencil
                    startEdit();
                });

                // StackPane overlays button on top-right
                StackPane.setAlignment(editButton, Pos.TOP_RIGHT);
                stack.getChildren().addAll(scroll, editButton);
            }

            @Override
            public void startEdit() {
                super.startEdit();

                // notify controller to enter edit mode and show clear edit button
                // enterEditMode();

                TextField textField = new TextField(getItem());
                textField.setOnAction(e -> commitEdit(textField.getText()));
                textField.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV) {
                        commitEdit(textField.getText());
                    }
                });

                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                textField.requestFocus();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                updateDisplay(getItem());
            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    updateDisplay(item);
                }
            }

            private void updateDisplay(String text) {
                label.setText(text);
                setGraphic(stack);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });

        // F_MA: added by felicia handling marking attendance
        // colNote.setCellFactory(TextFieldTableCell.forTableColumn());
        colNote.setOnEditCommit(event -> {
            AttendanceRecord record = event.getRowValue();
            record.setNote(event.getNewValue()); // update in-memory only
            markUnsaved();
        });

        // Configure marked_at column with proper formatting
        colMarkedAt.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTimestamp() != null) {
                return new SimpleStringProperty(formatter.format(cellData.getValue().getTimestamp()));
            } else {
                return new SimpleStringProperty("-");
            }
        });

        // Configure last_seen column with proper formatting
        colLastSeen.setCellValueFactory(cellData -> {
            if (cellData.getValue().getLastSeen() != null) {
                return new SimpleStringProperty(formatter.format(cellData.getValue().getLastSeen()));
            } else {
                return new SimpleStringProperty("-");
            }
        });

        // Custom cell factory for status with dropdown
        // colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        // F_MA: modified by felicia handling marking attendance
        colStatus.setCellValueFactory(cellData
                -> new SimpleStringProperty(service.capitalize(cellData.getValue().getStatus().name()))
        );
        colStatus.setCellFactory(column -> new TableCell<AttendanceRecord, String>() {
            // F_MA: modified by felicia handling marking attendance
            private final ComboBox<String> combo = new ComboBox<>(
                    FXCollections.observableArrayList("Present", "Absent", "Late", "Pending"));

            {
                combo.setOnAction(event -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    if (record != null) {
                        // enterEditMode(); // notify controller to enter edit mode and show clear edit button
                        // // F_MA: modified by felicia handling marking attendance
                        // // String selected = combo.getValue();
                        // // if (selected != null) {
                        // record.setStatus(AttendanceStatus.valueOf(combo.getValue().toUpperCase()));
                        // markUnsaved();
                        
                        if (!record.getStatus().name().equalsIgnoreCase(combo.getValue())) {
                            record.setStatus(AttendanceStatus.valueOf(combo.getValue().toUpperCase()));
                            markUnsaved();
                            enterEditMode(); // notify controller to enter edit mode and show clear edit button
                        }
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

        // F_MA: added by felicia handling marking attendance (change to use externalized parameter)
        // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        colMarkedAt.setCellValueFactory(cellData -> {
            LocalDateTime markedAt = cellData.getValue().getTimestamp();
            String formatted = markedAt != null ? formatter.format(markedAt) : "";
            return new SimpleStringProperty(formatted);
        });

        colLastSeen.setCellValueFactory(cellData -> {
            LocalDateTime lastSeen = cellData.getValue().getLastSeen();
            String formatted = lastSeen != null ? formatter.format(lastSeen) : "";
            return new SimpleStringProperty(formatted);
        });

        clearEditButton.setOnAction(e -> onClearEdit());
        attendanceTable.setFixedCellSize(-1); // allows dynamic height
        attendanceTable.setEditable(true); // Make table editable to edit notes or status
        attendanceService.addObserver(this); // add observers

        // Register this controller for global access
        ControllerRegistry.getInstance().register("attendance", this);

        // Reload attendance records
        loadAttendanceRecords();

        // Hide clear edit button first
        exitEditMode();
    }

    /**
     * Loads attendance records for the current session into the table.
     * Initializes selection map and stores original values for manual edits.
     */
    // F_MA: modified by felicia handling marking attendance
    // private void loadAttendanceRecords() {
    public void loadAttendanceRecords() {
        if (currentSession != null) {
            List<AttendanceRecord> records = service.findBySessionId(currentSession.getSessionId());

            Platform.runLater(() -> {
                attendanceList.setAll(records);
                attendanceTable.setItems(attendanceList);
                updateAttendanceSummary(); // update count of student who present, late and total students
                initSelectionMap(); // initialize selections
                updateButtonStates(); // update button states

                // Store the original statuses for change detection
                originalStatuses.clear();
                originalNotes.clear();

                // Store original values for status and notes
                for (AttendanceRecord record : records) {
                    originalStatuses.put(record.getStudent().getStudentId(), record.getStatus().toString());
                    originalNotes.put(record.getStudent().getStudentId(), record.getNote());
                }
            });
        }
    }

    /**
     * Updates the attendance summary label (Present, Late, Total).
     */
    private void updateAttendanceSummary() {
        if (attendanceTable.getItems() == null) {
            return;
        }

        long total = attendanceTable.getItems().size();
        long present = attendanceTable.getItems().stream()
                .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                .count();
        long late = attendanceTable.getItems().stream()
                .filter(r -> r.getStatus() == AttendanceStatus.LATE)
                .count();

        lblAttendanceSummary.setText(
                String.format("Present: %d | Late: %d | Total: %d", present, late, total)
        );
    }

    /**
     * Handles creating new attendance records via the Attendance Form.
     */
    @FXML
    private void onCreateRecord() {
        if (!getSelectedRecords().isEmpty()) {
            return; // return if selection exists
        }
        try {
            // Get all students in this session
            List<Student> allStudents = studentService.getStudentsBySessionId(currentSession);

            //Get all existing attendance records
            List<AttendanceRecord> existingRecords
                    = attendanceService.findBySessionId(currentSession.getSessionId());

            // Extract student IDs that already have records
            Set<Integer> existingStudentIds = existingRecords.stream()
                    .map(r -> r.getStudent().getStudentId())
                    .collect(java.util.stream.Collectors.toSet());

            // Filter for students not yet added
            List<Student> remainingStudents = allStudents.stream()
                    .filter(s -> !existingStudentIds.contains(s.getStudentId()))
                    .toList();

            // If no remaining students, show info and return
            if (remainingStudents.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No New Records");
                alert.setHeaderText(null);
                alert.setContentText("All students enrolled in this session have attendance records."
                        + "\nNo new record can be created.");
                alert.showAndWait();
                return;
            }

            // Otherwise open the attendance form to for attendance record creation    
            FXMLLoader loader
                    = new FXMLLoader(getClass().getResource("/view/AttendanceFormView.fxml"));

            Parent root = loader.load();

            AttendanceFormController formController = loader.getController();
            formController.setSession(currentSession);
            formController.populateStudents();

            Stage dialog = new Stage();
            dialog.setTitle("Create Attendance Record");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

            AttendanceRecord newRecord = formController.getNewRecord();
            if (newRecord != null) {
                loadAttendanceRecords();
                showSuccess("Added record for student " + newRecord.getStudent().getStudentId()
                        + " - " + newRecord.getStudent().getName() + " successfully.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles deleting selected attendance records.
     */
    @FXML
    private void onDeleteRecord() {
        List<AttendanceRecord> selectedRecords = attendanceList.stream()
                .filter(r -> selectionMap.get(r.getStudent().getStudentId()).get())
                .toList();

        if (selectedRecords.isEmpty()) {
            showWarning("Please select record(s) to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete " + selectedRecords.size() + " record(s)");
        String studentNames = selectedRecords.stream()
                .map(r -> r.getStudent().getName())
                .collect(Collectors.joining(", "));
        alert.setContentText("Are you sure you want to delete the selected record(s)?\n"
                + studentNames);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (AttendanceRecord r : selectedRecords) {
                service.deleteRecord(r);  // implement deleteRecord() in AttendanceService
            }
            loadAttendanceRecords(); // refresh UI page and load attendance record

            // show success message
            showSuccess("Deleted " + selectedRecords.size() + " record(s) successfully.");
        }
    }

    /**
     * Updates the enabled/disabled state of buttons based on selection.
     */
    private void updateButtonStates() {
        boolean hasSelection = !getSelectedRecords().isEmpty();

        deleteButton.setDisable(!hasSelection); // disable delete when no records selected
        createButton.setDisable(hasSelection); // disable create when records selected
    }

    /**
     * Gets a list of selected attendance records in the table.
     *
     * @return List of selected AttendanceRecord objects
     */
    private List<AttendanceRecord> getSelectedRecords() {
        return attendanceList.stream()
                .filter(r -> selectionMap.get(r.getStudent().getStudentId()).get())
                .toList();
    }

    /**
     * Handles saving manual changes to attendance records.
     */
    @FXML
    private void onSaveChanges() {
        try {
            int updatedCount = 0;

            // Create manual attendanace marker (polymorphism)
            AttendanceMarker marker = new ManualAttendanceMarker(service);
            // List<AttendanceObserver> observers = List.of();

            for (AttendanceRecord record : attendanceList) {
                String originalStatus
                        = originalStatuses.get(record.getStudent().getStudentId());
                String currentStatus = record.getStatus().toString();
                String originalNote = originalNotes.get(record.getStudent().getStudentId());
                String currentNote = record.getNote();

                // Set original values
                record.setOriginalStatus(AttendanceStatus.valueOf(originalStatus));
                record.setOriginalNote(originalNote);

                // Call manual marker
                marker.markAttendance(record);
                if (record.isStatusChanged() || record.isNoteChanged()) {
                    updatedCount++; // update count for number of records modified

                    // Update original maps for future edits
                    originalStatuses.put(record.getStudent().getStudentId(),
                            record.getStatus().toString());
                    originalNotes.put(record.getStudent().getStudentId(), record.getNote());
                }

                // boolean statusChanged = !Objects.equals(originalStatus, currentStatus);
                // boolean noteChanged = !Objects.equals(originalNote, currentNote);
                // Only update if status actually changed
                // if (!java.util.Objects.equals(originalStatus, currentStatus)) {
                // if (statusChanged) {
                //     // F_MA: modified by felicia handling marking attendance
                //     record.setTimestamp(LocalDateTime.now());
                //     // record.setLastSeen(LocalDateTime.now());
                //     service.updateStatus(record);
                //     // updatedCount++;
                //     // Update the original status map so subsequent saves work fine
                //     originalStatuses.put(record.getStudent().getStudentId(), currentStatus);
                // }
                // Only update if note actually changed
                // if (noteChanged) {
                //     // F_MA: modified by felicia handling marking attendance
                //     // if update note don't have to update marked_at or last_seen time
                //     // record.setTimestamp(LocalDateTime.now());
                //     // record.setLastSeen(LocalDateTime.now());
                //     service.updateNote(record); // need to change to updateNote
                //     // updatedCount++;
                //     // Update the original notes map so subsequent saves work fine
                //     originalNotes.put(record.getStudent().getStudentId(), currentNote);
                // }
                // if (statusChanged || noteChanged) {
                //     updatedCount++;
                // }
            }

            // Clear unsaved changes flag after save
            clearUnsaved();

            // Exit edit mode (hide clear edit button)
            exitEditMode();

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

    /**
     * Return to SessionView page. 
     */
    @FXML
    private void onBack() {
        // Block leaving if user is editing a cell
        // if (attendanceTable.getEditingCell() != null) {
        //     showError("Please finish or cancel editing before going back.");
        //     exitEditMode();
        // }

        if (backHandler != null) {
            backHandler.run(); // Run backHandler if it is not null
        } else {
            try {
                Parent sessionRoot = FXMLLoader.load(getClass().getResource("/view/SessionView.fxml"));
                attendanceTable.getScene().setRoot(sessionRoot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Hide clear edit button and refresh attendance UI page.
     */
    @FXML
    private void onClearEdit() {
        loadAttendanceRecords(); // Refresh UI page and load attendance from database
        exitEditMode(); // Hide edit button
        showInfo("Edits cleared.");
    }

    /**
     * Show clear edit button.
     */
    private void enterEditMode() {
        if (!isEditing) {
            isEditing = true;
            clearEditButton.setVisible(true);
            clearEditButton.setManaged(true);
        }
    }

    /**
     * Hide clear edit button.
     */
    private void exitEditMode() {
        isEditing = false;
        clearEditButton.setVisible(false);
        clearEditButton.setManaged(false);
    }

    /**
     * Get the hasUnsavedChanges.
     *
     * @return hasUnsavedChanges
     * 
     */
    public boolean getHasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * Set hasUnsavedChanges to true.
     */
    public void markUnsaved() {
        hasUnsavedChanges = true;
    }

    /**
     * Set hasUnsavedChanges to false.
     */
    public void clearUnsaved() {
        hasUnsavedChanges = false;
    }

    /**
     * Override TabRefreshable refresh method
     */
    @Override
    public void refresh() {
        loadAttendanceRecords(); // refresh UI
    }

    /**
     * Returns the TableView that displays attendance records
     *
     * This TableView is used to show the list of AttendanceRecord objects,
     * including student details, selection checkboxes and attendance details.
     *
     * @return the TableView containing AttendanceRecord items
     */
    public TableView<AttendanceRecord> getAttendanceTable() {
        return attendanceTable;
    }

}
