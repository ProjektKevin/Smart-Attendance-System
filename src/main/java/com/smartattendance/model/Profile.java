package com.smartattendance.model;

public class Profile {
    private String profileId;
    private String studentId;
    private String firstName;
    private String lastName;
    private String phoneNo;
    private String email;

    // Address (Optional)
    // chore(), Harry: make a separate class if got time later on.

    /**
     * Custom constructor which sets student profile
     * 
     * @param profileId The id of the profile
     * @param student   The student object
     * @param firstName The first name of the user
     * @param lastName  the last name of the user
     * @param phoneNo   The phone number of the user
     */
    public Profile(
            String profileId,
            String studentId,
            String firstName,
            String lastName,
            String phoneNo,
            String email) {

        this.profileId = profileId;
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNo = phoneNo;
        this.email = email;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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
