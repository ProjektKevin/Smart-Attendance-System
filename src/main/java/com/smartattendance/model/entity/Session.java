package com.smartattendance.model.entity;

import java.time.*;

/**
 * Represents a session in the Smart Attendance System.
 * 
 * A session encapsulates all information related to a specific
 * attendance-taking event,
 * including timing, location, course details, and session lifecycle management.
 * 
 * @author Lim Jia Hui
 * @version 18:18 16 Nov 2025
 */
public class Session {

  // ========== IMMUTABLE FIELDS ==========

  /** The course code associated with this session */
  private final String course;

  /** The physical location where the session takes place */
  private final String location;

  /** The date when the session occurs */
  private final LocalDate sessionDate;

  /** The scheduled start time of the session */
  private final LocalTime startTime;

  /** The scheduled end time of the session */
  private final LocalTime endTime;

  /**
   * The grace period in minutes after start time before attendance is marked as
   * late.
   * Students arriving within this threshold are considered on-time.
   */
  private final int lateThresholdMinutes;

  // ========== MUTABLE FIELDS ==========

  /** Unique identifier for the session, assigned by the database */
  private int sessionId;

  /**
   * Current status of the session. Possible values: "Pending", "Open", "Closed"
   * 
   * @see #open()
   * @see #close()
   */
  private String status;

  /**
   * Flag indicating whether the session should start automatically based on
   * schedule.
   * When true, the system will automatically open the session at the scheduled
   * start time.
   */
  private boolean autoStart;

  /**
   * Flag indicating whether the session should stop automatically based on
   * schedule.
   * When true, the system will automatically close the session at the scheduled
   * end time.
   */
  private boolean autoStop;

  // ========== CONSTRUCTORS ==========

  /**
   * Constructs a new Session with Pending status for session creation via form.
   * This constructor is typically used when creating new sessions through the UI.
   * The sessionId is initialised to 0 and will be assigned by the database upon
   * persistence.
   *
   * @param course               The course code or name associated with this
   *                             session
   * @param sessionDate          The date when the session occurs
   * @param startTime            The scheduled start time of the session
   * @param endTime              The scheduled end time of the session
   * @param location             The physical location where the session takes
   *                             place
   * @param lateThresholdMinutes The grace period in minutes for late attendance
   * @throws IllegalArgumentException if any required parameter is null or invalid
   * @throws DateTimeException        if the time range is invalid (end before
   *                                  start)
   */
  public Session(String course, LocalDate sessionDate,
      LocalTime startTime, LocalTime endTime, String location, int late) {
    this(0, course, sessionDate, startTime, endTime, location, late, "Pending", false, false);
  }

  /**
   * Constructs a fully specified Session, typically used when fetching from
   * database.
   * This constructor provides complete control over all session attributes and is
   * commonly used by data access layers and service classes.
   *
   * @param sessionId            The unique identifier for the session
   * @param course               The course code or name associated with this
   *                             session
   * @param sessionDate          The date when the session occurs
   * @param startTime            The scheduled start time of the session
   * @param endTime              The scheduled end time of the session
   * @param location             The physical location where the session takes
   *                             place
   * @param lateThresholdMinutes The grace period in minutes for late attendance
   * @param status               The initial status of the session ("Pending",
   *                             "Open", "Closed")
   * @param autoStart            Flag for automatic session start
   * @param autoStop             Flag for automatic session stop
   * @throws IllegalArgumentException if any required parameter is null or invalid
   * @throws DateTimeException        if the time range is invalid (end before
   *                                  start)
   */
  public Session(int sessionId, String course, LocalDate sessionDate,
      LocalTime startTime, LocalTime endTime, String location, int late, String status, boolean autoStart,
      boolean autoStop) {
    this.sessionId = sessionId;
    this.course = course;
    this.sessionDate = sessionDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.lateThresholdMinutes = late;
    this.status = status;
    this.autoStart = autoStart;
    this.autoStop = autoStop;
  }

  // ========== GETTER METHODS ==========

  /**
   * Returns the unique session identifier.
   *
   * @return the session ID, never null
   */
  public int getSessionId() {
    return sessionId;
  }

  /**
   * Returns the course associated with this session.
   *
   * @return the course code, never null
   */
  public String getCourse() {
    return course;
  }

  /**
   * Returns the date when this session occurs.
   *
   * @return the session date, never null
   */
  public LocalDate getSessionDate() {
    return sessionDate;
  }

  /**
   * Returns the scheduled start time of the session.
   *
   * @return the start time, never null
   */
  public LocalTime getStartTime() {
    return startTime;
  }

  /**
   * Returns the scheduled end time of the session.
   *
   * @return the end time, never null
   */
  public LocalTime getEndTime() {
    return endTime;
  }

  /**
   * Returns the physical location of the session.
   *
   * @return the session location, never null
   */
  public String getLocation() {
    return location;
  }

  /**
   * Returns the late threshold in minutes.
   * Students arriving within this many minutes after start time are not marked
   * late.
   *
   * @return the late threshold in minutes, always non-negative
   */
  public int getLateThresholdMinutes() {
    return lateThresholdMinutes;
  }

  /**
   * Returns the current status of the session.
   *
   * @return the session status ("Pending", "Open", or "Closed"), never null
   */
  public String getStatus() {
    return status;
  }

  // ========== SETTER METHODS ==========

  /**
   * Sets the session ID, assigned by the database upon persistence.
   * This method should only be called once when the session is first saved.
   *
   * @param id the unique identifier to assign to this session
   * @throws IllegalArgumentException if the ID is negative
   */
  public void setSessionId(int id) {
    this.sessionId = id;
  }

  /**
   * Opens the session, changing its status to "Open".
   * This indicates that attendance can now be taken for this session.
   */
  public void open() {
    this.status = "Open";
  }

  /**
   * Closes the session, changing its status to "Closed".
   * This indicates that attendance is no longer being taken for this session.
   */
  public void close() {
    this.status = "Closed";
  }

  // ========== AUTO-SESSION MANAGEMENT METHODS ==========

  /**
   * Returns whether this session is configured for automatic start.
   *
   * @return true if the session should start automatically at the scheduled time
   */
  public boolean isAutoStart() {
    return autoStart;
  }

  /**
   * Configures automatic start for this session.
   *
   * @param autoStart true to enable automatic session start, false to disable
   */
  public void setAutoStart(boolean autoStart) {
    this.autoStart = autoStart;
  }

  /**
   * Returns whether this session is configured for automatic stop.
   *
   * @return true if the session should stop automatically at the scheduled end
   *         time
   */
  public boolean isAutoStop() {
    return autoStop;
  }

  /**
   * Configures automatic stop for this session.
   *
   * @param autoStop true to enable automatic session stop, false to disable
   */
  public void setAutoStop(boolean autoStop) {
    this.autoStop = autoStop;
  }
}