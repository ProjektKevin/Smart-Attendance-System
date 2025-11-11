package com.smartattendance.model.entity;

public class Profile extends AbstractEntity {
    private Integer profileId;
    private String userId;
    private String firstName;
    private String lastName;
    private String phoneNo;

    /**
     * Custom constructor which sets student profile
     * 
     * @param profileId The id of the profile 
     * @param firstName The first name of the user
     * @param lastName  the last name of the user
     * @param phoneNo   The phone number of the user
     */
    public Profile(
            Integer profileId,
            String firstName,
            String lastName,
            String phoneNo) {

        this.profileId = profileId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNo = phoneNo;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
