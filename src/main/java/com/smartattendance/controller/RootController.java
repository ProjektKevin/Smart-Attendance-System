package com.smartattendance.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.smartattendance.ApplicationContext;
import com.smartattendance.model.entity.AuthSession;

public class RootController {
    @FXML
    private BorderPane root;
    @FXML
    private ToggleButton themeToggle;
    @FXML
    private Button logoutButton;

    // Admin tabs (may be null on student view)
    @FXML
    private Tab tabDashboard, tabStudents, tabEnrollments, tabSessions, tabLive, tabReports, tabSettings;
    // <!-- F_MA: modified by felicia handling marking attendance ##for testing-->
    @FXML
    private Tab tabTestAutoMark;
    // Student tabs (may be null on admin view)
    @FXML
    private Tab tabAttendance, tabProfile;

    private Label moonGlyph;
    private Label sunGlyph;

    private static final String VS16 = "\uFE0F";

    @FXML
    public void initialize() {
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
        safeSetTabIcon(tabSessions, "\uD83D\uDD53"); // 🕓 (your original)
        safeSetTabIcon(tabLive, "\uD83C\uDFA5"); // 🎥
        // <!-- F_MA: modified by felicia handling marking attendance ##for testing-->
        safeSetTabIcon(tabTestAutoMark, "\uD83C\uDFA5"); // 🎥
        safeSetTabIcon(tabReports, "\uD83D\uDCCA"); // 📊
        safeSetTabIcon(tabSettings, "\u2699"); // ⚙
        safeSetTabIcon(tabProfile, "\uD83D\uDC64");
        safeSetTabIcon(tabEnrollments, "\uD83D\uDCCB");

        // Student icons (null-safe)
        safeSetTabIcon(tabAttendance, "\uD83D\uDCCB"); // 🗋/📋 clipboard (or use calendar "\uD83D\uDCC5" 🗓)
        safeSetTabIcon(tabProfile, "\uD83D\uDC64");
        // If you prefer calendar for attendance, swap to: safeSetTabIcon(tabAttendance,
        // "\uD83D\uDCC5"); // 🗓
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
        try {
            // Get the auth session and clear it
            AuthSession session = ApplicationContext.getAuthSession();
            session.logout();

            // Load the login view
            Parent loginUI = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));

            // Get the current stage and update the scene
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(loginUI));
            stage.setTitle("Smart Attendance System - Login");
            stage.show();
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
