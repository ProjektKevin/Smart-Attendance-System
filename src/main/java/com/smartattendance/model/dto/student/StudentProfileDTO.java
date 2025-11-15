package com.smartattendance.model.dto.student;

import com.smartattendance.model.dto.user.UserProfileDTO;
import com.smartattendance.model.entity.Course;
import com.smartattendance.model.entity.Profile;
import com.smartattendance.model.entity.User;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Student Profile display
 * 
 * @author Thiha Swan Htet
 */
public class StudentProfileDTO extends UserProfileDTO {
    private List<Course> enrolledCourses;

    /**
     * Constructor from User and Profile entities
     *
     * @param user    The User entity
     * @param profile The Profile entity
     */
    public StudentProfileDTO(User user, Profile profile) {
        super(user, profile);
        this.enrolledCourses = new ArrayList<>();
    }

    /**
     * Constructor from User and Profile entities with courses (as Course objects)
     *
     * @param user            The User entity
     * @param profile         The Profile entity
     * @param enrolledCourses List of enrolled Course entities
     */
    public StudentProfileDTO(User user, Profile profile, List<Course> enrolledCourses) {
        super(user, profile);
        // Convert Course objects to course codes
        this.enrolledCourses = new ArrayList<>();
        this.enrolledCourses = enrolledCourses != null ? enrolledCourses : new ArrayList<>();
    }

    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }
}
