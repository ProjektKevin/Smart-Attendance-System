package com.smartattendance.service;

import com.smartattendance.model.User;
import com.smartattendance.repository.UserRepository;
import com.smartattendance.util.PasswordUtil;

public class AuthService {
  private final UserRepository userRepo;

  public AuthService(UserRepository r) {
    this.userRepo = r;
  }

  public User authenticate(String u, String p) {
    User user = userRepo.findByUsername(u);
    return (user != null && PasswordUtil.matches(p, user.getPasswordHash())) ? user : null;
  }
}
