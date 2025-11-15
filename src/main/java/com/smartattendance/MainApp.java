package com.smartattendance;

import com.smartattendance.util.security.log.ApplicationLogger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the Smart Attendance System JavaFX application.
 *
 * <p>This class is responsible for:
 * <ul>
 *     <li>Bootstrapping the application lifecycle ({@link #init()}, {@link #start(Stage)}, {@link #stop()})</li>
 *     <li>Initializing and shutting down the shared {@link ApplicationContext}</li>
 *     <li>Loading the initial UI (login screen) from FXML</li>
 *     <li>Writing basic lifecycle events to the {@link ApplicationLogger}</li>
 * </ul>
 *
 * <p>JavaFX calls:
 * <ul>
 *     <li>{@link #init()} once on the JavaFX launcher thread, before the UI is created</li>
 *     <li>{@link #start(Stage)} once with the primary stage</li>
 *     <li>{@link #stop()} when the application is about to exit</li>
 * </ul>
 * 
 * @author Chue Wan Yan, Ernest Lun, Lim Jia Hui, Min Thet Khine, Thiha Swan Htet
 */
public class MainApp extends Application {

  /**
   * Singleton application-level logger used to record lifecycle events
   * and important actions during startup and shutdown.
   */
  private final ApplicationLogger appLogger = ApplicationLogger.getInstance();

  /**
   * Called by the JavaFX runtime before {@link #start(Stage)}.
   *
   * <p>Responsibilities:
   * <ul>
   *     <li>Initialize the {@link ApplicationContext} so that shared services
   *         (e.g. database connections, configuration) are ready before any UI loads</li>
   *     <li>Log that the application context has been successfully initialized</li>
   * </ul>
   *
   * @throws Exception if initialization fails; JavaFX will handle uncaught exceptions
   */
  @Override
  public void init() throws Exception {
    // Initialize ApplicationContext before the UI is created so that controllers
    // can safely access shared services during or immediately after FXML loading.
    ApplicationContext.initialize();
    appLogger.info("Application Context loaded");
  }

  /**
   * Called by the JavaFX runtime to start the UI.
   *
   * <p>Responsibilities:
   * <ul>
   *     <li>Load the login view from {@code /view/LoginView.fxml}</li>
   *     <li>Set the window title and scene on the primary {@link Stage}</li>
   *     <li>Show the stage to the user</li>
   *     <li>Log that the application has started</li>
   * </ul>
   *
   * @param stage the primary stage provided by the JavaFX runtime
   * @throws Exception if loading the FXML or setting up the stage fails
   */
  @Override
  public void start(Stage stage) throws Exception {
    // Load the initial UI from FXML. The FXML file should define the login screen
    // and its associated controller.
    Parent loginUI = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));

    // Configure the main window (title and root scene).
    stage.setTitle("Smart Attendance System - Login");
    stage.setScene(new Scene(loginUI));

    // Display the primary stage.
    stage.show();

    // Record that the JavaFX application has started successfully.
    appLogger.info("App started");
  }

  /**
   * Called by the JavaFX runtime when the application is about to stop.
   *
   * <p>Responsibilities:
   * <ul>
   *     <li>Shut down the {@link ApplicationContext} to release resources
   *         (e.g. closing database connections, stopping background threads)</li>
   *     <li>Log that the application has been stopped</li>
   * </ul>
   *
   * @throws Exception if shutdown logic fails; errors are not rethrown
   *                   to the JavaFX runtime here
   */
  @Override
  public void stop() throws Exception {
    // Perform application-level cleanup so that all shared resources are
    // closed gracefully when the window is closed or the app exits.
    ApplicationContext.shutdown();
    appLogger.info("App stopped");
  }

  /**
   * Standard Java entry point that delegates to {@link Application#launch(String...)}.
   *
   * <p>The {@code launch} call will:
   * <ul>
   *     <li>Initialize the JavaFX runtime</li>
   *     <li>Create an instance of {@link MainApp}</li>
   *     <li>Invoke {@link #init()}, {@link #start(Stage)}, and later {@link #stop()}</li>
   * </ul>
   *
   * @param args command-line arguments passed to the application
   */
  public static void main(String[] args) {
    launch(args);
  }
}
