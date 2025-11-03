package com.smartattendance.model.dto.student;

public class StudentDTO {
    private Integer id;
    private String fullName;
    private String courseName;
    private FaceDataDTO faceData;

    /**
     * Constructor for StudentDTO
     *
     * @param id         The user id (student specific)
     * @param fullName   The full name of the student (first_name + last_name)
     * @param courseName The name of the course enrolled in
     */
    public StudentDTO(Integer id, String fullName, String courseName) {
        this.id = id;
        this.fullName = fullName;
        this.courseName = courseName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public FaceDataDTO getFaceData() {
        return faceData;
    }

    public void setFaceData(FaceDataDTO faceData) {
        this.faceData = faceData;
    }
}
