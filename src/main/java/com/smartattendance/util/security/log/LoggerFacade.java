package com.smartattendance.util.security.log;

/**
 * LoggerFacade: Interface for application logging levels
 *
 * @author Thiha Swan Htet
 */
public interface LoggerFacade {

    /**
     * Log an info level message
     * 
     * @param message the message to log
     */
    void info(String message);

    /**
     * Log a warning level message
     * 
     * @param message the message to log
     */
    void warn(String message);

    /**
     * Log an error message with exception details
     * 
     * @param message   the error message
     * @param exception the exception that occurred
     */
    void error(String message, Exception exception);

    /**
     * Log an error message without exception
     * 
     * @param message the error message
     */
    void error(String message);
}
