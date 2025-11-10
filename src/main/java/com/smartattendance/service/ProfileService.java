package com.smartattendance.service;

import com.smartattendance.model.entity.Profile;
import com.smartattendance.repository.ProfileRepository;

public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService() {
        this.profileRepository = new ProfileRepository();
    }

    // ==================== Entity Methods ====================

    /**
     * Get profile by user id
     *
     * @param userId The id of the user
     * @return The Profile object by the filtered user id
     */
    public Profile getUserProfile(Integer userId) {
        return profileRepository.getProfileById(userId);
    }

    /**
     * Create user profile
     *
     * @param firstName The first name of the user
     * @param lastName  The last name of the user
     * @param phoneNo   The phone number of the user
     * @param userId    The user id
     * @return Boolean: if created > true. If not > false
     */
    public boolean createUserProfile(String firstName, String lastName, String phoneNo, Integer userId) {
        return profileRepository.createProfile(firstName, lastName, phoneNo, userId);
    }

    /**
     * Update user profile
     *
     * @param firstName The first name of the user
     * @param lastName  The last name of the user
     * @param phoneNo   The phone number of the user
     * @param userId    The user id
     * @return Boolean: if updated > true. If not > false
     */
    public boolean updateUserProfile(String firstName, String lastName, String phoneNo, Integer userId) {
        return profileRepository.updateProfileById(firstName, lastName, phoneNo, userId);
    }

    /**
     * Delete user profile
     *
     * @param userId The user id
     * @return Boolean: if deleted > true. If not > false
     */
    public boolean deleteUserProfile(Integer userId) {
        return profileRepository.deleteProfileById(userId);
    }
}
