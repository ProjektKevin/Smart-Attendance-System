package com.smartattendance.model.entity;

import java.time.LocalDateTime;
import com.smartattendance.model.enums.AuthVerification;

/**
 * Verification Entity
 * Reflects DB: verification
 * Entity object with getters and setters for the auth verification
 * Stores temp tokens: Random string from @util/security/RandomUtil
 * 
 * Verification types: FORGOT_PASSWORD, VERIFICATION 
 * @see @models/enums/AuthVerification
 * 
 * @author Thiha Swan Htet
 */
public class Verification {

    private Integer id;
    private AuthVerification identifier;
    private String token;
    private LocalDateTime expTime;
    private Integer userId;

    /**
     * Custom default constructor
     * 
     * @param id         The id of the verification service
     * @param identifier The identifier of the verification from AuthVerification
     *                   ENUM
     * @param token      The random generated string to check against
     * @param expTime    The expiration time of the token
     * @param userId     The user id of the user who requested
     * 
     */
    public Verification(Integer id, AuthVerification identifier, String token, LocalDateTime expTime, Integer userId) {
        this.id = id;
        this.identifier = identifier;
        this.token = token;
        this.expTime = expTime;
        this.userId = userId;
    }

    /**
     * Custom default constructor
     * 
     * @param identifier The identifier of the request from AuthVerification ENUM
     * @param token      The random generated string to check against
     * @param expTime    The expiration time of the token
     * @param userId     The user id of the user who requested
     * 
     */
    public Verification(AuthVerification identifier, String token, LocalDateTime expTime, Integer userId) {
        this.identifier = identifier;
        this.token = token;
        this.expTime = expTime;
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AuthVerification getIdentifier() {
        return identifier;
    }

    public void setIdentifier(AuthVerification identifier) {
        this.identifier = identifier;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpTime() {
        return expTime;
    }

    public void setExpTime(LocalDateTime expTime) {
        this.expTime = expTime;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

}
