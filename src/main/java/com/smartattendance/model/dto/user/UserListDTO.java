package com.smartattendance.model.dto.user;

import com.smartattendance.model.entity.User;
import com.smartattendance.model.enums.Role;

/**
 * DTO for Student List table display
 */
public class UserListDTO {
    private Integer id;
    private String email;
    private boolean emailVerified;
    private Role role;

    /**
     * Constructor from User entity
     *
     * @param user The User entity
     */
    public UserListDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.emailVerified = user.getIsEmailVerified();
        this.role = user.getRole();
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean getIsEmailVerified() {
        return emailVerified;
    }

    public Role getRole() {
        return role;
    }
}
