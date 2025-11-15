package com.smartattendance.util;

// import java.util.ArrayList;
// import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.smartattendance.repository.SessionRepository;
// import com.smartattendance.service.AttendanceObserver;
import com.smartattendance.service.AttendanceService;
import com.smartattendance.service.AutoAttendanceMarker;

/**
 * Automatically update attendance records
 *
 * @author Chue Wan Yan 
 * 
 * @version 13:23 15 Nov 2025
 */
/**
 * Handles automatic updating of attendance records.
 *
 * This class uses a {@link Timer} to periodically mark pending attendance
 * records as ABSENT for sessions that are closed. It delegates the actual
 * marking to {@link AutoAttendanceMarker}.
 *
 */
public class AutoAttendanceUpdater {

    private final SessionRepository sessionRepository; // Reference to sessionRepository
    // private final AttendanceService attendanceService;
    // private final List<AttendanceObserver> observers = new ArrayList<>();
    private final Timer timer;
    private final AutoAttendanceMarker autoAttendanceMarker; // Reference to autoAttendanceMarker

    /**
     * Constructs an AutoAttendanceUpdater.
     *
     * @param attendanceService the shared attendance service that provides
     * access to the AutoAttendanceMarker
     */
    public AutoAttendanceUpdater(AttendanceService attendanceService) {
        this.sessionRepository = new SessionRepository();
        this.autoAttendanceMarker = attendanceService.getAutoAttendanceMarker();
        this.timer = new Timer(true); // daemon thread
    }

    // Allow controllers to register for updates
    // public void addObserver(AttendanceObserver observer) {
    //     observers.add(observer);
    // }
    // private void notifyAutoUpdate() {
    // for (AttendanceObserver obs : observers) {
    //     obs.onAttendanceAutoUpdated();
    // }
    // }


    /**
     * Starts the automatic attendance update process.
     * 
     * The updater will run every {@code intervalSeconds} and mark PEMDING
     * attendance records as ABSENT for sessions that are closed.
     * 
     *
     * @param intervalSeconds the interval in seconds between automatic updates
     */
    public void startAutoUpdate(int intervalSeconds) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    autoAttendanceMarker.markPendingAttendanceAsAbsent(sessionRepository);

                    // // Refresh UI on JavaFX thread
                    // Platform.runLater(() -> {
                    //     attendanceController.loadAttendanceRecords();
                    // });
                    // notifyAutoUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, intervalSeconds * 1000L);
    }

     /**
     * Stops the automatic attendance updater.
     * 
     * Cancels the underlying {@link Timer} and prevents further updates.
     * 
     */
    public void stopAutoUpdate() {
        timer.cancel();
    }

}
