package com.smartattendance.model.enums;

/**
 * Represents the method used to mark a student's attendance.
 *
 * This enum indicates whether attendance was marked automatically by the
 * system, manually by a user, through QR scanning, or not marked at all
 * (default).
 *
 * @author Chue Wan Yan
 *
 * @version 15:11 07 Nov 2025
 */
public enum MarkMethod {
    AUTO, // Marked automatically via face recognition
    MANUAL, // Marked manually via user action
    QR, // Marked through a QR code scan
    NONE // No marking method was applied (default)
}
