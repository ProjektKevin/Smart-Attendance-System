package com.smartattendance.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry to manage controller instances that need lifecycle management.
 * Allows controllers to register themselves and be accessed by their unique ID.
 *
 * Reloads data when the user directs tp different tabs
 * 
 * @author Thiha Swan Htet
 */
public class ControllerRegistry {
    private static final ControllerRegistry instance = new ControllerRegistry();
    private final Map<String, TabRefreshable> controllers = new HashMap<>();

    private ControllerRegistry() {
    }

    public static ControllerRegistry getInstance() {
        return instance;
    }

    /**
     * Register a controller that implements TabRefreshable
     *
     * @param controllerId Unique identifier for the controller
     * @param controller   The controller instance
     */
    public void register(String controllerId, TabRefreshable controller) {
        controllers.put(controllerId, controller);
    }

    /**
     * Unregister a controller
     *
     * @param controllerId Unique identifier for the controller
     */
    public void unregister(String controllerId) {
        controllers.remove(controllerId);
    }

    /**
     * Get a registered controller
     *
     * @param controllerId Unique identifier for the controller
     * @return The controller or null if not registered
     */
    public TabRefreshable getController(String controllerId) {
        return controllers.get(controllerId);
    }

    /**
     * Refresh a specific controller
     *
     * @param controllerId Unique identifier for the controller
     */
    public void refresh(String controllerId) {
        TabRefreshable controller = controllers.get(controllerId);
        if (controller != null) {
            controller.refresh();
        }
    }
}
