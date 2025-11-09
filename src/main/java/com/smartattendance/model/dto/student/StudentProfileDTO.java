package com.smartattendance.model.dto.student;

import com.smartattendance.model.entity.Profile;
import com.smartattendance.model.entity.User;
import com.smartattendance.model.enums.Role;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Student Profile display
 */
public class StudentProfileDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String phoneNo;
    private boolean emailVerified;
    private List<String> enrolledCourses;

    /**
     * Constructor from User and Profile entities
     *
     * @param user    The User entity
     * @param profile The Profile entity
     */
    public StudentProfileDTO(User user, Profile profile) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.emailVerified = user.getIsEmailVerified();
        this.enrolledCourses = new ArrayList<>();

        if (profile != null) {
            this.firstName = profile.getFirstName();
            this.lastName = profile.getLastName();
            this.phoneNo = profile.getPhoneNo();
        }
    }

    /**
     * Constructor from User and Profile entities with courses
     *
     * @param user             The User entity
     * @param profile          The Profile entity
     * @param enrolledCourses  List of enrolled course names/codes
     */
    public StudentProfileDTO(User user, Profile profile, List<String> enrolledCourses) {
        this(user, profile);
        this.enrolledCourses = enrolledCourses != null ? enrolledCourses : new ArrayList<>();
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

    public List<String> getEnrolledCourses() {
        return enrolledCourses;
    }
}
