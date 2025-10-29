package com.smartattendance.service;

import com.smartattendance.model.entity.User;
import com.smartattendance.repository.PostgresUserRepository;

public class AuthService {
  private final PostgresUserRepository userRepo;

  public AuthService(PostgresUserRepository userRepo){
    this.userRepo = userRepo;
  }

  // public User authenticate(String u, String p) {
  // User user = userRepo.findByUsername(u);
  // return (user != null && PasswordUtil.matches(p, user.getPasswordHash())) ?
  // user : null;
  // }

  public User authenticate(String userName) {
    return userRepo.findUserByUsername(userName);
  }

}
