package com.smartattendance.model;

public class User extends AbstractEntity {
  private Integer id;
  private String email;
  private String userName;
  private String role;
  private String passwordHash;
  private boolean isEmailVerified;

  /**
   * Temp constructor (Delete after done)
   * 
   * @param userName     The username of the user
   * @param passwordHash The hashed password of the user
   * @param role         The role of the user
   */
  public User(String userName, String passwordHash, String role) {
    this.userName = userName;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  /**
   * Custom default constructor
   * 
   * @param id              The id of the user
   * @param username        The username of the user
   * @param email           The email of the user
   * @param passwordHash    The hashed password of the user
   * @param role            The role of the user
   * @param isEmailVerified The email verification of the user
   * 
   */
  public User(Integer id, String username, String email, String passwordHash, String role, boolean isEmailVerified) {
    this.id = id;
    this.userName = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
    this.isEmailVerified = isEmailVerified;
  }

  /**
   * Custom constructor which sets the user (For user registration by admin)
   * 
   * @param email The email of the user
   */
  public User(String email) {
    this.email = email;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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

  public boolean getIsEmailVerified() {
    return isEmailVerified;
  }

  public void setIsEmailVerified(boolean isEmailVerified) {
    this.isEmailVerified = isEmailVerified;
  }
}
