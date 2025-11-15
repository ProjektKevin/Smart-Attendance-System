package com.smartattendance.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
 
/**
 * Small utility for for calculating time differences and cooldown checks
 * related to attendance marking.
 * 
 * @author Chue Wan Yan
 * 
 * @version 22:14 14 Nov 2025 
 */
public final class AttendanceTimeUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private AttendanceTimeUtils() {
        // utility, not meant to be created as an object
    }

    /**
     * Calculates the number of minutes between a session start time and
     * a given attendance mark time.
     *
     * @param sessionStartTime the start time of the session
     * @param markTime the timestamp when the attendance was marked
     * @return the difference in minutes between the session start and mark time
     */
    public static long minutesBetween(LocalTime sessionStartTime, LocalDateTime markTime) {
        return Duration.between(sessionStartTime, markTime).toMinutes();
    }

    /**
     * Calculates the number of seconds between two LocalDateTime instances.
     *
     * @param older the older timestamp
     * @param newer the newer timestamp
     * @return the difference in seconds between the two timestamps
     */
    public static long secondsBetween(LocalDateTime older, LocalDateTime newer) {
        return Duration.between(older, newer).getSeconds();
    }

    /**
     * Determines whether the cooldown period has expired.
     *
     * @param lastSeen the last seen timestamp
     * @param now the current timestamp
     * @param cooldownSeconds the cooldown duration in seconds
     * @return true if the cooldown period has expired, false otherwise
     */
    public static boolean isCooldownExpired(LocalDateTime lastSeen, 
            LocalDateTime now, int cooldownSeconds) {
        return secondsBetween(lastSeen, now) >= cooldownSeconds;
    }
}
