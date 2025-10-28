package com.smartattendance.model;

public class AuthSession {
    private User currentUser;
    private boolean isActive;

    public AuthSession() {
        this.isActive = false;
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
}
