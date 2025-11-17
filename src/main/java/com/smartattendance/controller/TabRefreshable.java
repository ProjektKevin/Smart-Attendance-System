package com.smartattendance.controller;

/**
 * Interface for controllers that need to refresh their data when tab is selected.
 *
 * Implement this interface in any controller that should reload data when the user
 * navigates to its tab.
 * 
 * @author Thiha Swan Htet
 */
public interface TabRefreshable {
    /**
     * Refresh the controller's data.
     * Called when the tab containing this controller becomes visible.
     */
    void refresh();
}
