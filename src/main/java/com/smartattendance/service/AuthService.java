package com.smartattendance.service;

import com.smartattendance.model.dto.user.UserDTO;
import com.smartattendance.model.entity.User;
import com.smartattendance.repository.AuthRepository;

public class AuthService {
  private final AuthRepository authRepo;

  public AuthService(AuthRepository authRepo) {
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

  public User getUserByEmail(String email) {
    return authRepo.findUserByEmail(email);
  }

  /**
   * Invite user (Admin)
   *
   * @param email The email of the user
   * @param role  The role of the user 
   * @return Boolean: if created > true. If not > false
   */
  public boolean inviteUser(String email, String role) {
    return authRepo.registerEmailRole(email, role);
  }

}
