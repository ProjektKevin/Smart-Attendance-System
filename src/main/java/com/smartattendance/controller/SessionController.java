// package com.smartattendance.controller;

// import com.smartattendance.model.AttendanceRecord;
// import com.smartattendance.model.Session;
// import com.smartattendance.model.Student;
// import com.smartattendance.repository.StudentRepository;
// import com.smartattendance.service.SessionService;

// import javafx.animation.KeyFrame;
// import javafx.animation.Timeline;
// import javafx.util.Duration;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader;
// import javafx.geometry.Insets;
// import javafx.scene.Parent;
// import javafx.scene.Scene;
// import javafx.scene.control.*;
// import javafx.scene.control.cell.PropertyValueFactory;
// import javafx.scene.layout.VBox;
// import javafx.stage.Modality;
// import javafx.stage.Stage;
// import org.controlsfx.control.table.TableRowExpanderColumn;

// import java.time.*;
// import java.util.List;

// public class SessionController {
//     @FXML
//     private Label sessionsInfo;
//     @FXML
//     private TableView<Session> sessionTable;
//     @FXML
//     private TableColumn<Session, String> colId;
//     @FXML
//     private TableColumn<Session, String> colCourse;
//     @FXML
//     private TableColumn<Session, LocalDate> colDate;
//     @FXML
//     private TableColumn<Session, LocalTime> colStart;
//     @FXML
//     private TableColumn<Session, LocalTime> colEnd;
//     @FXML
//     private TableColumn<Session, String> colLoc;
//     @FXML
//     private TableColumn<Session, Integer> colLate;
//     @FXML
//     private TableColumn<Session, String> colStatus;
//     TableColumn<Session, Void> colDelete = new TableColumn<>("Actions");

//     private final SessionService sm = new SessionService();
//     private final StudentRepository studentRepo = new StudentRepository();

//     String sessionId,String courseId, LocalDate sessionDate,
//     LocalTime startTime,LocalTime endTime,String location,
//     int late

//     @FXML
//     public void initialize() {
//         Create expander column for sessions
//         TableRowExpanderColumn<Session> expander = new TableRowExpanderColumn<>(this::createExpanderContent);
//         sessionTable.getColumns().add(0, expander);

//         colId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
//         colCourse.setCellValueFactory(new PropertyValueFactory<>("courseId"));
//         colDate.setCellValueFactory(new PropertyValueFactory<>("sessionDate"));
//         colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
//         colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
//         colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));
//         colLate.setCellValueFactory(new PropertyValueFactory<>("lateThresholdMinutes"));
//         colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
//         colDelete.setCellFactory(param -> new TableCell<>() {
//             private final Button btn = new Button("Delete");

//             {
//                 btn.setOnAction(event -> {
//                     Session s = getTableView().getItems().get(getIndex());
//                     if (s.isOpen()) {
//                         Alert alert = new Alert(Alert.AlertType.WARNING);
//                         alert.setHeaderText("Cannot delete active session");
//                         alert.setContentText("Please close the session before deleting it.");
//                         alert.showAndWait();
//                     } else {
//                         getTableView().getItems().remove(s);
//                         sm.deleteSession(s);
//                         sessionsInfo.setText("Session " + s.getSessionId() + " deleted.");
//                     }
//                 });
//             }

//             @Override
//             protected void updateItem(Void item, boolean empty) {
//                 super.updateItem(item, empty);
//                 if (empty) {
//                     setGraphic(null);
//                 } else {
//                     Session s = getTableView().getItems().get(getIndex());
//                     btn.setDisable(s.isOpen());
//                     setGraphic(btn);
//                 }
//             }
//         });

//         ObservableList<Session> data = FXCollections.observableArrayList(sm.getAllSessions());
//         sessionTable.setItems(data);
//         sessionTable.getColumns().add(colDelete);

//         // Refresh table periodically (optional, for UI updates)
//         Timeline uiRefresher = new Timeline(new KeyFrame(Duration.seconds(30), e -> sessionTable.refresh()));
//         uiRefresher.setCycleCount(Timeline.INDEFINITE);
//         uiRefresher.play();

//     } 
    
//     // change gui so there is checkboxes for users to select one or multiple sessions and then can either click open/stop/delete
//     // if there is error, alert using notification bara instead of SessionInfo
//     // allow clear all
//     // set up supabase

//     private VBox createExpanderContent(TableRowExpanderColumn.TableRowDataFeatures<Session> param) {
//         Session session = param.getValue();

//         TableView<AttendanceTracker> studentTable = new TableView<>();
//         studentTable.setPrefHeight(180);

//         TableColumn<AttendanceTracker, String> idCol = new TableColumn<>("Student ID");
//         idCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudent().getStudentId()));

//         TableColumn<AttendanceTracker, String> nameCol = new TableColumn<>("Name");
//         nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudent().getName()));

//         TableColumn<AttendanceTracker, String> courseCol = new TableColumn<>("Course");
//         courseCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudent().getCourseId()));

//         TableColumn<AttendanceTracker, String> statusCol = new TableColumn<>("Status");
//         statusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().name()));

//         TableColumn<AttendanceTracker, String> methodCol = new TableColumn<>("Method");
//         methodCol.setCellValueFactory(cell -> new SimpleStringProperty(
//                 cell.getValue().getMethod() == null ? "-" : cell.getValue().getMethod()));

//         studentTable.getColumns().addAll(List.of(idCol, nameCol, courseCol, statusCol, methodCol));

//         studentTable.setItems(FXCollections.observableArrayList(session.getAttendanceTrackers()));

//         VBox box = new VBox(studentTable);
//         box.setPadding(new Insets(10));
//         return box;

//     }

//     @FXML
//     private void onCreateSession() {
//         try {
//             FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SessionForm.fxml"));
//             Parent form = loader.load();

//             Stage dialog = new Stage();
//             dialog.setTitle("Create New Session");
//             dialog.setScene(new Scene(form));
//             dialog.initModality(Modality.APPLICATION_MODAL); // block interaction with main window
//             dialog.showAndWait();

//             // After popup closes:
//             SessionFormController formCtrl = loader.getController();
//             Session newSession = formCtrl.getNewSession();

//             if (newSession != null) {
//                 // Populate roster with students of same course
//                 List<Student> enrolledStudents = studentRepo.findAll().stream()
//                         .filter(s -> s.getCourseId().equalsIgnoreCase(newSession.getCourseId()))
//                         .toList();

//                 for (Student s : enrolledStudents) {
//                     newSession.addStudentToRoster(s);
//                 }

//                 sm.createSession(newSession);
//                 sessionTable.getItems().add(newSession);
//                 sessionsInfo.setText("Session " + newSession.getSessionId() + " created successfully!");
//             }

//         } catch (Exception e) {
//             e.printStackTrace();
//             sessionsInfo.setText("Error creating session: " + e.getMessage());
//         }
//     }

//     @FXML
//     private void onStartSession() {
//         Session selected = sessionTable.getSelectionModel().getSelectedItem();
//         if (selected == null) {
//             sessionsInfo.setText("Please select a session to start.");
//             return;
//         }

//         selected.open();
//         sessionTable.refresh(); // update table view
//         sessionsInfo.setText("Session " + selected.getSessionId() + " is now OPEN.");
//     }

//     @FXML
//     private void onStopSession() {
//         Session selected = sessionTable.getSelectionModel().getSelectedItem();
//         if (selected == null) {
//             sessionsInfo.setText("Please select a session to stop.");
//             return;
//         }

//         selected.close();
//         sessionTable.refresh();
//         sessionsInfo.setText("Session " + selected.getSessionId() + " has been CLOSED.");
//     }
// }

package com.smartattendance.controller; import javafx.fxml.FXML; import javafx.scene.control.Label;
public class SessionController { @FXML private Label sessionsInfo; @FXML public void initialize(){ sessionsInfo.setText("No sessions created (scaffold)."); }
  @FXML private void onCreateSession(){ sessionsInfo.setText("Created session CS102 "+System.currentTimeMillis()+" (stub)."); }
  @FXML private void onStartSession(){ sessionsInfo.setText("Session started (stub)."); }
  @FXML private void onStopSession(){ sessionsInfo.setText("Session stopped (stub)."); } }