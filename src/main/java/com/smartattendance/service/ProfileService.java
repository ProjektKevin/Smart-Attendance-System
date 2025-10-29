package com.smartattendance.service;

import com.smartattendance.model.Profile;
import com.smartattendance.repository.ProfileRepository;

public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Profile getUserProfile(Integer userId) {
        return profileRepository.getProfileById(userId);
    }

    public boolean createUserProfile(String firstName, String lastName, String phoneNo, Integer userId) {
        return profileRepository.createProfile(firstName, lastName, phoneNo, userId);
    }

    public boolean updateUserProfile(String firstName, String lastName, String phoneNo, Integer userId) {
        return profileRepository.updateProfileById(firstName, lastName, phoneNo, userId);
    }

    public boolean deleteUserProfile(Integer userId) {
        return profileRepository.deleteProfileById(userId);
    }
}
