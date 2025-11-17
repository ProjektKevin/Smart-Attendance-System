package com.smartattendance.model.entity;

import com.smartattendance.model.enums.Role;

public class User {
  private Integer id;
  private String email;
  private String userName;
  private Role role;
  private String passwordHash;
  private boolean isEmailVerified;

  /**
   * Temp constructor (Delete after done)
   * 
   * @param userName     The username of the user
   * @param passwordHash The hashed password of the user
   * @param role         The role of the user
   */
  public User(String userName, String passwordHash, Role role) {
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
  public User(Integer id, String username, String email, String passwordHash, Role role, boolean isEmailVerified) {
    this.id = id;
    this.userName = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
    this.isEmailVerified = isEmailVerified;
  }

  /**
   * Custom default constructor (For Authentication)
   * 
   * @param id              The id of the user
   * @param username        The username of the user
   * @param email           The email of the user
   * @param role            The role of the user
   * @param isEmailVerified The email verification of the user
   * 
   */
  public User(Integer id, String username, String email, Role role, boolean isEmailVerified) {
    this.id = id;
    this.userName = username;
    this.email = email;
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

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public boolean getIsEmailVerified() {
    return isEmailVerified;
  }

  public void setIsEmailVerified(boolean isEmailVerified) {
    this.isEmailVerified = isEmailVerified;
  }
}
