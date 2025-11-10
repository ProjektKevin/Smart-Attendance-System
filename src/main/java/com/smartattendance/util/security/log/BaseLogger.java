package com.smartattendance.util.security.log;

import java.io.IOException;
import java.util.logging.*;

/**
 * BaseLogger: Abstract base class for all logger implementations
 * Provides functionality for file and console logging
 * Implements LoggerFacade to for different levels of logging
 *
 * If there are any sub-loggers, call any setUpLogger functions: console only,
 * file only or both and return the instance and use various levels of logging
 * 
 * Created since there are two loggers: Attendance and Application
 * On app expansion, add more loggers and extends
 *
 * @author Thiha Swan Htet
 */
public abstract class BaseLogger implements LoggerFacade {
    protected final Logger logger;

    /**
     * Constructor for subclasses
     * Creates a logger with the specified name
     *
     * @param loggerName The name of the logger (e.g., "AttendanceLog")
     */
    protected BaseLogger(String loggerName) {
        this.logger = Logger.getLogger(loggerName);
    }

    /**
     * Setup the logger with file and console handlers
     * Call this in the constructor of subclasses
     *
     * @param logFileName The name of the log file (e.g., "attendance.log")
     * @param logLevel    The minimum log level to display
     */
    protected void setupLogger(String logFileName, Level logLevel) {
        addFileHandler(logFileName, logLevel);
        addConsoleHandler(logLevel);
    }

    /**
     * Production ENV follows file logging without console
     *
     * @param logFileName The name of the log file
     * @param logLevel    The minimum log level to display
     */
    protected void setupLoggerFileOnly(String logFileName, Level logLevel) {
        addFileHandler(logFileName, logLevel);
    }

    /**
     * Console Logging Only (Might use for dev)
     * Depends on developer preference
     *
     * @param logLevel The minimum log level to display
     */
    protected void setupLoggerConsoleOnly(Level logLevel) {
        addConsoleHandler(logLevel);
    }

    /**
     * Add a file handler to the logger
     * Logs will be written to the specified file
     *
     * @param logFileName The name of the log file
     * @param logLevel    The minimum log level
     */
    private void addFileHandler(String logFileName, Level logLevel) {
        try {
            FileHandler fileHandler = new FileHandler(logFileName, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(logLevel);
        } catch (IOException e) {
            System.err.println("Failed to add file handler for: " + logFileName);
            e.printStackTrace();
        }
    }

    /**
     * Add a console handler to the logger
     * Logs will be printed to the console/terminal
     *
     * @param logLevel the minimum log level
     */
    private void addConsoleHandler(Level logLevel) {
        try {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);
            logger.setLevel(logLevel);
        } catch (Exception e) {
            System.err.println("Failed to add console handler");
            e.printStackTrace();
        }
    }

    /**
     * Log an info level message
     * Implements LoggerFacade
     *
     * @param message the message to log
     */
    @Override
    public void info(String message) {
        logger.info(message);
    }

    /**
     * Log a warning level message
     * Implements LoggerFacade
     *
     * @param message the message to log
     */
    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    /**
     * Log an error message with exception details
     * Implements LoggerFacade
     *
     * @param message   the error message
     * @param exception the exception that occurred
     */
    @Override
    public void error(String message, Exception exception) {
        logger.log(Level.SEVERE, message, exception);
    }

    /**
     * Log an error message without exception
     * Implements LoggerFacade
     *
     * @param message the error message
     */
    @Override
    public void error(String message) {
        logger.log(Level.SEVERE, message);
    }

    /**
     * Get the underlying Logger instance for advanced logging needs
     *
     * @return the Logger instance
     */
    public Logger getLogger() {
        return logger;
    }
}
