package com.smartattendance.model.entity;

public class Course {
    private Integer id;
    private String name;
    private String code;

    /**
     * Explicit constructor
     * 
     * @param id   The id of the course
     * @param name The name of the course (eg. Programming Fundamentals)
     * @param role The code of the course (eg.CS102)
     */
    public Course(Integer id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
