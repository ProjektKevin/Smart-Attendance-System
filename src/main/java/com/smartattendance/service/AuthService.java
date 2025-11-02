package com.smartattendance.service;

import com.smartattendance.model.entity.User;
import com.smartattendance.repository.AuthRepository;

public class AuthService {
  private final AuthRepository authRepo;

  public AuthService(AuthRepository authRepo){
    this.authRepo = authRepo;
  }

  // public User authenticate(String u, String p) {
  // User user = authRepo.findByUsername(u);
  // return (user != null && PasswordUtil.matches(p, user.getPasswordHash())) ?
  // user : null;
  // }

  public User authenticate(String userName) {
    return authRepo.findUserByUsername(userName);
  }

}
