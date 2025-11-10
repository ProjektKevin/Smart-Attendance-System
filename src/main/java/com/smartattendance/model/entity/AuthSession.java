package com.smartattendance.model.entity;

public class AuthSession {
    private User currentUser;
    private boolean isActive;
    private Integer activeSessionId;

    public AuthSession() {
        this.isActive = false;
        this.activeSessionId = null;
    }

    public void login(User user) {
        this.currentUser = user;
        this.isActive = true;
    }

    public void logout() {
        this.currentUser = null;
        this.isActive = false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setActiveSessionId(Integer sessionId) {
        this.activeSessionId = sessionId;
    }

    public Integer getActiveSessionId() {
        return activeSessionId;
    }

    public boolean hasActiveSession() {
        return activeSessionId != null;
    }

    public void clearActiveSessionId() {
        this.activeSessionId = null;
    }
}
