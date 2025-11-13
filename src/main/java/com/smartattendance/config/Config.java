package com.smartattendance.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Central configuration manager for the application.
 *
 * This class loads key–value settings from a local properties file (config.properties)
 * and allows reading and writing configuration values at runtime.
 *
 * The purpose of this class is to keep configuration externalized,
 * matching the project brief’s requirement that settings should not
 * be hard-coded inside source files.
 *
 * All values loaded or updated through this class persist immediately
 * to disk so that the configuration survives application restarts.
 */
public class Config {

    /** Singleton-like shared Properties object holding all loaded settings. */
    private static final Properties props = new Properties();

    /** Path to the properties file stored in the application working directory. */
    private static final String CONFIG_FILE = "config.properties";

    /**
     * Static initializer block.
     *
     * This runs once when the class is first loaded. 
     * It attempts to load existing configuration values from config.properties.
     * If the file does not exist, the application continues with empty/default properties.
     */
    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("No config file found or unable to load: " + e.getMessage());
        }
    }

    /**
     * Retrieves a configuration value by key.
     *
     * @param k The property key.
     * @return The stored value, or null if the key does not exist.
     */
    public static String get(String k) {
        return props.getProperty(k);
    }

    /**
     * Updates a configuration value and persists the change immediately.
     *
     * This method not only updates the in-memory Properties object, 
     * but also writes the entire properties map back to disk to ensure
     * that the update is durable.
     *
     * @param k The property key.
     * @param v The new value to store.
     */
    public static void set(String k, String v) {
        props.setProperty(k, v);

        // Persist updated configuration to disk.
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, null);
            System.out.println("Config updated: " + k + " = " + v);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
            throw new RuntimeException("Could not persist configuration change", e);
        }
    }
}
