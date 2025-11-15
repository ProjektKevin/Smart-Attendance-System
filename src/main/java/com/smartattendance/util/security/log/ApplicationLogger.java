package com.smartattendance.util.security.log;

import java.util.logging.Level;

/**
 * ApplicationLogger: Logger for application-level events
 * Logs all service initialization, running services, controllers, system errors
 * and info
 *
 * Extends BaseLogger to inherit common logging functionality
 *
 * Usage: ApplicationLogger.getInstance().info("Application Context Initialized");
 *
 * @author Thiha Swan Htet
 */
public class ApplicationLogger extends BaseLogger {
    private static final ApplicationLogger instance = new ApplicationLogger();

    /**
     * Private constructor to prevent direct instantiation
     * Initializes logger with application-specific configuration
     */
    private ApplicationLogger() {
        super("ApplicationLog");
        // Setup logger: application.log file + console output at INFO level
        setupLogger("application.log", Level.INFO);
    }

    /**
     * Get the instance of ApplicationLogger
     *
     * @return the ApplicationLogger instance
     */
    public static ApplicationLogger getInstance() {
        return instance;
    }
}
