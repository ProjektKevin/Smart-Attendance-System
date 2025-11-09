package com.smartattendance.model.dto.user;

import com.smartattendance.model.entity.Profile;
import com.smartattendance.model.entity.User;

import com.smartattendance.model.enums.Role;

public class UserDTO {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean emailVerified;
    private String fullName;

    public UserDTO(User user, Profile profile) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = profile.getFirstName();
        this.lastName = profile.getLastName();
        this.role = user.getRole();
        this.emailVerified = user.getIsEmailVerified();
        this.fullName = profile.getFirstName() + " " + profile.getLastName();
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
