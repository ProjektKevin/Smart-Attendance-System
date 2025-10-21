package com.smartattendance.model;

public class Profile extends User {
    private String profileId;
    private String firstName;
    private String lastName;
    private String phoneNo;
    private String email;

    // Address (Optional)
    // chore(), Harry: make a separate class if got time later on.
    public Profile(
            String username, String passwordHash, String role,
            String profileId, String firstName, String lastName,
            String phoneNo, String email) {

        // Inherit and create user
        super(username, passwordHash, role);

        // Set profile properties
        this.profileId = profileId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNo = phoneNo;
        this.email = email;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
