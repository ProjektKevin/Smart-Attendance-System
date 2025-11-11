package com.smartattendance.model.dto.user;

import com.smartattendance.model.entity.Profile;
import com.smartattendance.model.entity.User;
import com.smartattendance.model.enums.Role;

public class UserProfileDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String username;
    private String phoneNo;
    private boolean emailVerified;

    public UserProfileDTO(User user, Profile profile) {
        this.id = user.getId();
        this.firstName = profile.getFirstName();
        this.lastName = profile.getLastName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.username = user.getUserName();
        this.phoneNo = profile.getPhoneNo();
        this.emailVerified = user.getIsEmailVerified();
    }

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

    public String getUsername() {
        return username;
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
