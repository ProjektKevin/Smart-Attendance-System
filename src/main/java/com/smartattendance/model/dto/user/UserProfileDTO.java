package com.smartattendance.model.dto.user;

import com.smartattendance.model.entity.Profile;
import com.smartattendance.model.entity.User;
import com.smartattendance.model.enums.Role;

/**
 * DTO for Student Profile display
 */
public class UserProfileDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String phoneNo;
    private boolean emailVerified;

    /**
     * Constructor from User and Profile entities
     *
     * @param user    The User entity
     * @param profile The Profile entity
     */
    public UserProfileDTO(User user, Profile profile) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.emailVerified = user.getIsEmailVerified();

        if (profile != null) {
            this.firstName = profile.getFirstName();
            this.lastName = profile.getLastName();
            this.phoneNo = profile.getPhoneNo();
        }
    }

    // Getters (read-only)
    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
