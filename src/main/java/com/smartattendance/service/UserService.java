package com.smartattendance.service;

import com.smartattendance.model.dto.user.UserListDTO;
import com.smartattendance.model.dto.user.UserProfileDTO;
import com.smartattendance.model.entity.Profile;
import com.smartattendance.model.entity.User;
import com.smartattendance.repository.PostgresUserRepository;
import com.smartattendance.repository.ProfileRepository;

import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private final PostgresUserRepository userRepository;
    private final ProfileRepository profileRepository;

    public UserService(PostgresUserRepository userRepository) {
        this.userRepository = userRepository;
        this.profileRepository = new ProfileRepository();
    }

    public UserService(PostgresUserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    // ==================== Entity Methods ====================

    /**
     * Get all users by role
     *
     * @param role The role filtered
     * @return List of User objects by the filtered role
     */
    public List<User> getUsersByRole(String role) {
        return userRepository.findUsersByRole(role);
    }

    /**
     * Get user by the id
     *
     * @param userid The id of the user
     * @return User object found by id
     */
    public User getUserById(Integer userId) {
        return userRepository.findUserById(userId);
    }

    /**
     * Delete user
     *
     * @param userId The user id
     * @return Boolean: if deleted > true. If not > false
     */
    public boolean deleteUser(Integer userId) {
        return userRepository.deleteUserById(userId);
    }

    // ==================== DTO Methods ====================

    /**
     * Get all students as StudentListDTO for table display
     *
     * @return List of StudentListDTO objects
     */
    public List<UserListDTO> getStudentListDTOs() {
        return getUsersByRole("STUDENT")
                .stream()
                .map(UserListDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Get student profile DTO with full details for profile dialog
     *
     * @param userId The user ID
     * @return UserProfileDTO with user and profile information
     */
    public UserProfileDTO getUserProfileDTO(Integer userId) {
        User user = getUserById(userId);
        Profile profile = profileRepository.getProfileById(userId);
        return new UserProfileDTO(user, profile);
    }
}
