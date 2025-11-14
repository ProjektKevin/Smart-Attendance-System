package com.smartattendance.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Small utility for attendance related time calculations.
 * @author Chue Wan Yan
 */
public final class AttendanceTimeUtils {

    private AttendanceTimeUtils() {
        // utility, not meant to be created as an object
    }

    public static long minutesBetween(LocalTime sessionStartTime, LocalDateTime markTime) {
        return Duration.between(sessionStartTime, markTime).toMinutes();
    }

    public static long secondsBetween(LocalDateTime older, LocalDateTime newer) {
        return Duration.between(older, newer).getSeconds();
    }

    public static boolean isCooldownExpired(LocalDateTime lastSeen, LocalDateTime now, int cooldownSeconds) {
        return secondsBetween(lastSeen, now) >= cooldownSeconds;
    }
}
