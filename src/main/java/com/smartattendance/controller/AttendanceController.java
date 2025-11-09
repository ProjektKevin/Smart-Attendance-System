package com.smartattendance.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.AttendanceStatus;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.service.AttendanceObserver;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.StudentService;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class AttendanceController implements AttendanceObserver {

    @FXML
    private Label attendanceInfo;
    @FXML
    private Label lblSessionTitle;
    @FXML
    private Button createButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TableView<AttendanceRecord> attendanceTable;
    @FXML
    private TableColumn<AttendanceRecord, String> colStudentId;
    @FXML
    private TableColumn<AttendanceRecord, String> colStudentName;
    @FXML
    private TableColumn<AttendanceRecord, String> colStatus;
    @FXML
    private TableColumn<AttendanceRecord, String> colMethod;
    @FXML
    private TableColumn<AttendanceRecord, String> colMarkedAt;
    @FXML
    private TableColumn<AttendanceRecord, String> colLastSeen;
    @FXML
    private TableColumn<AttendanceRecord, String> colNote;
    @FXML
    private TableColumn<AttendanceRecord, Boolean> colSelect;

    private final AttendanceService service = new AttendanceService();
    private final StudentService studentService = new StudentService();
    private final ObservableList<AttendanceRecord> attendanceList = FXCollections.observableArrayList();
    private final AttendanceService attendanceService = ApplicationContext.getAttendanceService();
    private Session currentSession;
    private Runnable backHandler;

    // Track original valuees for comparison
    private final Map<Integer, String> originalStatuses = new HashMap<>();
    private final Map<Integer, String> originalNotes = new HashMap<>();

    // Formatter for timestamp display
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Track selections of attendance records
    private final Map<Integer, SimpleBooleanProperty> selectionMap = new HashMap<>();

    @Override
    public void onAttendanceMarked(AttendanceRecord record, String message) {
        // single record marked (existing)
    }

    @Override
    public void onAttendanceAutoUpdated() {
        Platform.runLater(this::loadAttendanceRecords);
    }

    // Helper methods for different message types
    private void showSuccess(String message) {
        styleInfoLabel("success", "✓ " + message);
    }

    private void showError(String message) {
        styleInfoLabel("error", "✗ " + message);
    }

    private void showWarning(String message) {
        styleInfoLabel("warning", "⚠ " + message);
    }

    private void showInfo(String message) {
        styleInfoLabel("normal", "ℹ " + message);
    }

    // Styles the info label based on message type
    // @param type "success", "error", "warning", or "normal"
    // @param message The text to display
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
    }

    public void setBackHandler(Runnable backHandler) {
        this.backHandler = backHandler;
    }

    public void setSession(Session session) {
        this.currentSession = session;
        lblSessionTitle.setText("Attendance for Session ID: " + session.getSessionId());
        loadAttendanceRecords();
    }

    private void initSelectionMap() {
        selectionMap.clear();
        for (AttendanceRecord record : attendanceList) {
            selectionMap.put(record.getStudent().getStudentId(), new SimpleBooleanProperty(false));
        }
    }

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

    // public static void requestUserConfirmation(AttendanceRecord record) {
    //     Platform.runLater(() -> {
    //         Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    //         alert.setTitle("Confirm Student Identity");
    //         alert.setHeaderText("Low Confidence Detection");
    //         alert.setContentText(String.format(
    //                 // "Detected student:\n\nName: %s\nID: %d\nConfidence: %.2f\n\nIs this correct?",
    //                 "Detected student:\n\nName: %s\nID: %d\n\nIs this correct?",
    //                 record.getStudent().getName(),
    //                 record.getStudent().getStudentId()
    //                 // record.getConfidence()
    //         ));
    //         Optional<ButtonType> result = alert.showAndWait();
    //         if (result.isPresent() && result.get() == ButtonType.OK) {
    //             // user confirmed → mark attendance
    //             try {
    //                 record.mark(observers);
    //             } catch (Exception e) {
    //                 System.err.println("Failed to save attendance record");
    //                 e.printStackTrace();
    //             }
    //         } else {
    //             // user declined → skip
    //             System.out.println("Skipped marking for " + record.getStudent().getName());
    //         }
    //     });
    // }
    // public static boolean requestUserConfirmation(AttendanceRecord record, Consumer<Boolean> callback) {
    //     Platform.runLater(() -> {
    //         Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    //         alert.setTitle("Confirm Student Identity");
    //         alert.setHeaderText("Low Confidence Detection");
    //         alert.setContentText(String.format(
    //                 "Detected student:\n\nName: %s\nID: %d\n\nIs this correct?",
    //                 record.getStudent().getName(),
    //                 record.getStudent().getStudentId()
    //         ));
    //         // Use Yes/No buttons
    //         ButtonType yesButton = new ButtonType("Yes");
    //         ButtonType noButton = new ButtonType("No");
    //         alert.getButtonTypes().setAll(yesButton, noButton);
    //         Optional<ButtonType> result = alert.showAndWait();
    //         boolean confirmed = result.isPresent() && result.get() == yesButton;
    //         callback.accept(confirmed); // notify caller asynchronously
    //     });
    // }
    public static void requestUserConfirmationAsync(AttendanceRecord record, Consumer<Boolean> callback) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Student Identity");
            alert.setHeaderText("Low Confidence Detection");
            alert.setContentText(String.format(
                    "Detected student:\n\nName: %s\nID: %d\n\nIs this correct?",
                    record.getStudent().getName(),
                    record.getStudent().getStudentId()
            ));

            ButtonType yesButton = new ButtonType("Yes");
            ButtonType noButton = new ButtonType("No");
            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            callback.accept(result.isPresent() && result.get() == yesButton);
        });
    }

    @FXML
    public void initialize() {
        // Let each row have a selectable checkbox
        setupCheckBoxColumn();

        // Buttons to create or delete records
        createButton.setOnAction(e -> onCreateRecord());
        deleteButton.setOnAction(e -> onDeleteRecord());

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
                editButton.setOnAction(e -> startEdit());

                // StackPane overlays button on top-right
                StackPane.setAlignment(editButton, Pos.TOP_RIGHT);
                stack.getChildren().addAll(scroll, editButton);
            }

            @Override
            public void startEdit() {
                super.startEdit();

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

        // colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        // colNote.setCellFactory(column -> {
        //     // TableCell<AttendanceRecord, String> cell = new TableCell<>() {
        //     return new TextFieldTableCell<AttendanceRecord, String>() {
        //         private final Label label = new Label();
        //         @Override
        //         public void startEdit() {
        //             super.startEdit();
        //             setGraphic(getTextField());
        //             setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        //         }
        //         @Override
        //         public void cancelEdit() {
        //             super.cancelEdit();
        //             label.setText(getItem());
        //             setGraphic(label);
        //             setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        //         }
        //         @Override
        //         public void updateItem(String item, boolean empty) {
        //             super.updateItem(item, empty);
        //             if (empty || item == null) {
        //                 setGraphic(null);
        //             } else {
        //                 label.setText(item);
        //                 label.setWrapText(true);
        //                 label.setMaxWidth(column.getWidth() - 10);
        //                 label.setTooltip(new Tooltip("Double-click to edit"));
        //                 HBox box = new HBox(5, label, new Label("✏️"));
        //                 box.setAlignment(Pos.TOP_LEFT);
        //                 setGraphic(box);
        //                 setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        //             }
        //         }
        //     };
        //     // return cell;
        // });
        // Configure note column
        // colNote.setCellValueFactory(cellData
        //         -> new SimpleStringProperty(cellData.getValue().getNote())
        // );
        // colNote.setCellFactory(column -> new TextFieldTableCell<AttendanceRecord, String>() {
        //     private final Label label = new Label();
        //     private final Label editIcon = new Label("✏️");
        //     {
        //         label.setWrapText(true);
        //         label.setTooltip(new Tooltip("Double-click to edit"));
        //         editIcon.setStyle("-fx-opacity: 0.6; -fx-font-size: 12;"); // subtle hint
        //     }
        //     @Override
        //     public void startEdit() {
        //         super.startEdit();
        //         // create editable TextField manually since getTextField() is protected
        //         javafx.scene.control.TextField textField = new javafx.scene.control.TextField(getItem());
        //         textField.setOnAction(e -> {
        //             commitEdit(textField.getText());
        //         });
        //         textField.focusedProperty().addListener((obs, oldV, newV) -> {
        //             if (!newV) {
        //                 commitEdit(textField.getText());
        //             }
        //         });
        //         setGraphic(textField);
        //         setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        //         textField.requestFocus();
        //     }
        //     @Override
        //     public void cancelEdit() {
        //         super.cancelEdit();
        //         updateDisplay(getItem());
        //     }
        //     @Override
        //     public void updateItem(String item, boolean empty) {
        //         super.updateItem(item, empty);
        //         if (empty || item == null) {
        //             setGraphic(null);
        //         } else {
        //             updateDisplay(item);
        //         }
        //     }
        //     private void updateDisplay(String text) {
        //         label.setText(text);
        //         label.setMaxWidth(colNote.getWidth() - 30);
        //         HBox box = new HBox(5, label, editIcon);
        //         box.setAlignment(Pos.TOP_LEFT);
        //         setGraphic(box);
        //         setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        //     }
        // });
        // F_MA: added by felicia handling marking attendance
        // colNote.setCellFactory(TextFieldTableCell.forTableColumn());
        colNote.setOnEditCommit(event -> {
            AttendanceRecord record = event.getRowValue();
            record.setNote(event.getNewValue()); // update in-memory only
        });

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

        attendanceTable.setFixedCellSize(-1); // allows dynamic height

        // Make table editable if you want to edit notes or status
        attendanceTable.setEditable(true);
        attendanceService.addObserver(this);
    }

    // F_MA: modified by felicia handling marking attendance
    // private void loadAttendanceRecords() {
    public void loadAttendanceRecords() {
        if (currentSession != null) {
            List<AttendanceRecord> records = service.findBySessionId(currentSession.getSessionId());
            attendanceList.setAll(records);
            attendanceTable.setItems(attendanceList);
            initSelectionMap();
            updateButtonStates();

            // Store the original statuses for change detection
            originalStatuses.clear();
            originalNotes.clear();

            for (AttendanceRecord record : records) {
                originalStatuses.put(record.getStudent().getStudentId(), record.getStatus().toString());
                originalNotes.put(record.getStudent().getStudentId(), record.getNote());
            }
        }
    }

    @FXML
    private void onCreateRecord() {
        if (getSelectedRecords().size() > 0) {
            return; // disable if selection exists
        }
        try {
            // 1️. Get all students in this session
            List<Student> allStudents = studentService.getStudentsBySessionId(currentSession);

            // 2. Get all existing attendance records
            List<AttendanceRecord> existingRecords = attendanceService.findBySessionId(currentSession.getSessionId());

            // 3. Extract student IDs that already have records
            Set<Integer> existingStudentIds = existingRecords.stream()
                    .map(r -> r.getStudent().getStudentId())
                    .collect(java.util.stream.Collectors.toSet());

            // 4. Filter for students not yet added
            List<Student> remainingStudents = allStudents.stream()
                    .filter(s -> !existingStudentIds.contains(s.getStudentId()))
                    .toList();

            // 5. If no remaining students, show info and return
            if (remainingStudents.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No New Records");
                alert.setHeaderText(null);
                alert.setContentText("All students enrolled in this session have attendance records.\nNo new record can be created.");
                alert.showAndWait();
                return; // stop here
            }

            // 6. Otherwise open the form    
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AttendanceFormView.fxml"));

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
                showSuccess("Added record successfully.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        alert.setContentText("Are you sure you want to delete the selected record(s)?\n" + studentNames);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (AttendanceRecord r : selectedRecords) {
                service.deleteRecord(r);  // implement deleteRecord() in AttendanceService
            }
            loadAttendanceRecords();
            showSuccess("Deleted " + selectedRecords.size() + " record(s) successfully.");
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = getSelectedRecords().size() > 0;

        deleteButton.setDisable(!hasSelection);
        createButton.setDisable(hasSelection); // disable create when records selected
    }

    private List<AttendanceRecord> getSelectedRecords() {
        return attendanceList.stream()
                .filter(r -> selectionMap.get(r.getStudent().getStudentId()).get())
                .toList();
    }

    @FXML
    private void onSaveChanges() {
        try {
            int updatedCount = 0;
            for (AttendanceRecord record : attendanceList) {
                String originalStatus = originalStatuses.get(record.getStudent().getStudentId());
                String currentStatus = record.getStatus().toString();
                String originalNote = originalNotes.get(record.getStudent().getStudentId());
                String currentNote = record.getNote();

                boolean statusChanged = !Objects.equals(originalStatus, currentStatus);
                boolean noteChanged = !Objects.equals(originalNote, currentNote);

                // Only update if status actually changed
                // if (!java.util.Objects.equals(originalStatus, currentStatus)) {
                if (statusChanged) {
                    // F_MA: modified by felicia handling marking attendance
                    record.setTimestamp(LocalDateTime.now());
                    // record.setLastSeen(LocalDateTime.now());
                    service.updateStatus(record);
                    // updatedCount++;

                    // Update the original status map so subsequent saves work fine
                    originalStatuses.put(record.getStudent().getStudentId(), currentStatus);
                }

                // Only update if note actually changed
                if (noteChanged) {
                    // F_MA: modified by felicia handling marking attendance
                    // if update note don't have to update marked_at or last_seen time
                    // record.setTimestamp(LocalDateTime.now());
                    // record.setLastSeen(LocalDateTime.now());
                    service.updateNote(record); // need to change to updateNote
                    // updatedCount++;

                    // Update the original notes map so subsequent saves work fine
                    originalNotes.put(record.getStudent().getStudentId(), currentNote);
                }

                if (statusChanged || noteChanged) {
                    updatedCount++;
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
