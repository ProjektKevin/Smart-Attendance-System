package com.smartattendance.model;

public class User {
  private final String username, passwordHash, role;

  public User(String u, String p, String r) {
    this.username = u;
    this.passwordHash = p;
    this.role = r;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getRole() {
    return role;
  }
}
