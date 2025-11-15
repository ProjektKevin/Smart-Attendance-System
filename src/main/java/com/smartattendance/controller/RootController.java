package com.smartattendance.controller;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;
import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.AuthSession;
import com.smartattendance.service.SessionService;
import com.smartattendance.util.security.log.ApplicationLogger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Root controller that controls the view of both the admin and the students
 * 
 * @author Ernest Lun
 */
public class RootController {
    @FXML
    private BorderPane root;
    @FXML
    private ToggleButton themeToggle;
    @FXML
    private Button logoutButton;
    @FXML
    private TabPane tabPane;

    // Admin tabs (may be null on student view)
    @FXML
    private Tab tabDashboard, tabStudents, tabEnrollments, tabSessions, tabLive, tabReports, tabSettings;
    // F_MA: modified by felicia handling marking attendance ##for testing
    // private Tab tabRecognition;
    @FXML
    private RecognitionController recognitionViewController;

    // Student tabs (may be null on admin view)
    @FXML
    private Tab tabAttendance, tabProfile;

    private Label moonGlyph;
    private Label sunGlyph;

    private static final String VS16 = "\uFE0F";

    private SessionService sessionService;

    /**
     * Application Logger to show on terminal and write file to refer back to
     */
    private final ApplicationLogger appLogger = ApplicationLogger.getInstance();
    private final AuthSession authSession = ApplicationContext.getAuthSession();

    @FXML
    public void initialize() {
        if (authSession.getCurrentUser() == null) {
            setInfoDialog(javafx.scene.control.Alert.AlertType.ERROR, "Invalid Session", "Authentication Error",
                    "User Is Not Logged In");
            redirectLogin();
            return;
        }

        if (!root.getStyleClass().contains("dark") && !root.getStyleClass().contains("light")) {
            root.getStyleClass().add("light");
        }

        moonGlyph = makeGlyph("\uD83C\uDF19"); // 🌙
        sunGlyph = makeGlyph("\u2600"); // ☀

        boolean isDark = root.getStyleClass().contains("dark");
        themeToggle.setSelected(isDark);
        themeToggle.setText(isDark ? "Light mode" : "Dark mode");
        themeToggle.setGraphic(isDark ? sunGlyph : moonGlyph);

        // Admin icons (null-safe)
        safeSetTabIcon(tabDashboard, "\uD83C\uDFE0"); // 🏠
        safeSetTabIcon(tabStudents, "\uD83D\uDC65"); // 👥
        safeSetTabIcon(tabSessions, "\uD83D\uDD53"); // 🕓
        safeSetTabIcon(tabLive, "\uD83C\uDFA5"); // 🎥
        // F_MA: modified by felicia handling marking attendance ##for testing
        // safeSetTabIcon(tabRecognition, "\uD83C\uDFA5");
        safeSetTabIcon(tabReports, "\uD83D\uDCCA"); // 📊
        safeSetTabIcon(tabSettings, "\u2699"); // ⚙
        safeSetTabIcon(tabProfile, "\uD83D\uDC64");
        safeSetTabIcon(tabEnrollments, "\uD83D\uDCCB");

        // Student icons (null-safe)
        safeSetTabIcon(tabAttendance, "\uD83D\uDCCB"); // 🗋/📋 clipboard (or use calendar "\uD83D\uDCC5" 🗓)
        safeSetTabIcon(tabProfile, "\uD83D\uDC64");

        initializeTabBlocking(); // for recognition tab

        // Setup tab selection listeners for controllers that need to refresh data
        setupTabRefreshListeners();
    }

    /**
     * Setup listeners for tabs that have controllers implementing TabRefreshable.
     * When a tab is selected, it calls the refresh() method on the registered
     * controller.
     */
    private void setupTabRefreshListeners() {
        ControllerRegistry registry = ControllerRegistry.getInstance();

        // Setup listener for Students tab
        if (tabStudents != null) {
            tabStudents.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) { // When tab becomes selected
                    registry.refresh("studentList");
                    appLogger.info("StudentList tab selected - data refreshed");
                }
            });
        }

        // Setup listener for Profile tab
        if (tabProfile != null) {
            tabProfile.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) { // When tab becomes selected
                    registry.refresh("userProfile");
                    appLogger.info("Profile tab selected - data refreshed");
                }
            });
        }
    }

    @FXML
    private void onThemeToggle() {
        boolean toDark = themeToggle.isSelected();
        root.getStyleClass().removeAll("light", "dark");
        root.getStyleClass().add(toDark ? "dark" : "light");
        themeToggle.setText(toDark ? "Light mode" : "Dark mode");
        themeToggle.setGraphic(toDark ? sunGlyph : moonGlyph);
    }

    private void safeSetTabIcon(Tab tab, String emoji) {
        if (tab != null)
            tab.setGraphic(makeGlyph(emoji));
    }

    private Label makeGlyph(String s) {
        Label l = new Label(stripVS16(s));
        l.getStyleClass().add("glyph");
        return l;
    }

    private static String stripVS16(String s) {
        return (s == null) ? "" : s.replace(VS16, "");
    }

    /**
     * Handle logout button click.
     * Clears the session and navigates back to login page.
     */
    @FXML
    private void handleLogout() {
        // Get the auth session and clear it
        authSession.logout();
        redirectLogin();
    }

    /**
     * Return to login screen
     */
    private void redirectLogin() {
        try {
            // Load the login view
            Parent loginUI = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));

            // Get the current stage and update the scene
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loginUI));
            stage.setTitle("Smart Attendance System - Login");
        } catch (Exception e) {
            setInfoDialog(javafx.scene.control.Alert.AlertType.ERROR, "Redirect Error", "System Error", e.getMessage());
            appLogger.error("Error Redirecting Login", e);
            return;
        }
    }

    /**
     * Show an alert dialog to the user
     */
    public void setInfoDialog(javafx.scene.control.Alert.AlertType alertType, String title, String headerText,
            String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * ADDED: Set up the Live Recognition tab access control
     */
    private void initializeTabBlocking() {
        // Only set up if we're in admin view (tabLive exists)
        if (tabLive == null || tabPane == null) {
            return;
        }

        // Get session service
        sessionService = ApplicationContext.getSessionService();

        // Listen for tab changes
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            // Check if user is trying to access Live Recognition
            if (newTab == tabLive) {
                // Check if there's an open session
                if (!sessionService.isSessionOpen()) {
                    // No session open - block access!

                    // Use Platform.runLater to avoid issues with listener timing
                    Platform.runLater(() -> {
                        // Show alert
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("No Active Session");
                        alert.setHeaderText("Live Recognition Unavailable");
                        alert.setContentText(
                                "Live Recognition requires an active session.\n\n" +
                                        "Please go to the Sessions tab and create/open a session first.");

                        // Add button to go to Sessions tab
                        ButtonType goToSessionsButton = new ButtonType("Go to Sessions", ButtonBar.ButtonData.OK_DONE);
                        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(goToSessionsButton, cancelButton);

                        alert.showAndWait().ifPresent(response -> {
                            if (response == goToSessionsButton) {
                                // Redirect to Sessions tab
                                tabPane.getSelectionModel().select(tabSessions);
                            } else {
                                // Go back to previous tab (or Dashboard if no previous)
                                if (oldTab != null && oldTab != tabLive) {
                                    tabPane.getSelectionModel().select(oldTab);
                                } else if (tabDashboard != null) {
                                    tabPane.getSelectionModel().select(tabDashboard);
                                }
                            }
                        });
                    });

                    appLogger.info("Live Recognition access blocked - no active session");
                } else {
                    appLogger.info("Live Recognition access granted - session is open");
                }
            }
        });
    }
}
