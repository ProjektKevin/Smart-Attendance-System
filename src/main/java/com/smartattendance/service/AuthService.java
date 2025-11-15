package com.smartattendance.service;

import com.smartattendance.model.entity.User;
import com.smartattendance.model.entity.Verification;
import com.smartattendance.model.enums.AuthVerification;
import com.smartattendance.repository.AuthRepository;
import com.smartattendance.repository.VerificationRepository;

/**
 * Auth Service
 * Controls auth business logic and interact with db repository
 * 
 * @author Thiha Swan Htet
 */
public class AuthService {
  private final AuthRepository authRepo;
  private final VerificationRepository verificationRepo;

  public AuthService() {
    this.authRepo = new AuthRepository();
    this.verificationRepo = new VerificationRepository();
  }

  /**
   * Get user by username
   *
   * @param userName The username of the user
   * @return User object
   */
  public User getUserByUsername(String userName) {
    return authRepo.findUserByUsername(userName);
  }

  /**
   * Get user by email
   *
   * @param email The email of the user
   * @return User object
   */
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
    return authRepo.createEmailRole(email, role);
  }

  /**
   * Create a verification token for password reset or email verification
   *
   * @param verification The verification entity containing token, expiration, and
   *                     user ID
   * @return The created verification, or null if creation failed
   */
  public Verification createVerification(Verification verification) {
    return verificationRepo.createVerification(verification);
  }

  /**
   * Verify if a token is valid and not expired
   *
   * @param userId The user ID
   * @param token  The token to verify
   * @param type   The verification type (FORGOT_PASSWORD or VERIFICATION)
   * @return true if token is valid and not expired, false otherwise
   */
  public boolean verifyToken(Integer userId, String token, AuthVerification type) {
    return verificationRepo.isTokenValid(userId, token, type);
  }

  /**
   * Delete a specific verification record
   *
   * @param userId The user ID
   * @param type   The verification type
   * @return true if deletion was successful
   */
  public boolean deleteVerification(Integer userId, AuthVerification type) {
    return verificationRepo.deleteVerification(userId, type);
  }

  /**
   * Delete all expired verification tokens from the database
   *
   * @return true if deletion was successful
   */
  public boolean deleteExpiredVerifications() {
    return verificationRepo.deleteExpiredVerifications();
  }

  /**
   * Update user's password in the database
   *
   * @param userId         The user ID
   * @param hashedPassword The new hashed password
   * @return true if update was successful
   */
  public boolean resetPassword(Integer userId, String hashedPassword) {
    return authRepo.updatePassword(userId, hashedPassword);
  }

  /**
   * Complete user registration in a transaction
   * Updates users table with username and password
   * Inserts into profile table with firstName and lastName
   *
   * @param userId         The user ID
   * @param username       The chosen username
   * @param hashedPassword The hashed password
   * @param firstName      The user's first name
   * @param lastName       The user's last name
   * @return true if registration is completed successfully
   */
  public boolean registerUser(Integer userId, String username, String hashedPassword,
      String firstName, String lastName) {
    return authRepo.createUser(userId, username, hashedPassword, firstName, lastName);
  }

}
