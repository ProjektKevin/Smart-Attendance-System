package com.smartattendance.model;

public class User {
  private String email;
  private String userName;
  private String role;
  private String passwordHash;

  /**
   * Temp constructor (Delete after all compile)
   * 
   * @param email The email of the user
   */
  public User(String userName, String passwordHash, String role) {
    this.userName = userName;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  /**
   * Custom constructor which sets the user (For user registration by admin)
   * 
   * @param email The email of the user
   */
  public User(String email) {
    this.email = email;
  }

  /**
   * Custom default constructor which sets the user (For user registration by
   * admin)
   * 
   * @param email        The email of the user
   * @param userName     The username of the user
   * @param passwordHash The hashed password of the user
   * @param role         The role of the user
   */
  public User(String email, String userName, String role, String passwordHash) {
    this.email = email;
    this.userName = userName;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
