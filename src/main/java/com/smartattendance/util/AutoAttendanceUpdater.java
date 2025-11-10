// package com.smartattendance.util;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Timer;
// import java.util.TimerTask;

// import com.smartattendance.controller.AttendanceController;
// import com.smartattendance.repository.SessionRepository;
// import com.smartattendance.service.AttendanceService;
// import com.smartattendance.service.AutoAttendanceMarker;
// import com.smartattendance.service.AttendanceObserver;

// import javafx.application.Platform;

// public class AutoAttendanceUpdater {

//     private final SessionRepository sessionRepository;
//     private final AttendanceService attendanceService;
//     private final List<AttendanceObserver> observers = new ArrayList<>();
//     private final Timer timer;

//     public AutoAttendanceUpdater(AttendanceService attendanceService) {
//         this.sessionRepository = new SessionRepository();
//         this.attendanceService = attendanceService;
//         this.timer = new Timer(true); // daemon thread
//     }

//     // Allow controllers to register for updates
//     public void addObserver(AttendanceObserver observer) {
//         observers.add(observer);
//     }

//     // private void notifyAutoUpdate() {
//     // for (AttendanceObserver obs : observers) {
//     //     obs.onAttendanceAutoUpdated();
//     // }
//     // }

//     /**
//      * Start automatic update every X seconds
//      */
//     public void startAutoUpdate(int intervalSeconds) {
//         timer.scheduleAtFixedRate(new TimerTask() {
//             @Override
//             public void run() {
//                 try {
//                     AutoAttendanceMarker.markPendingAttendanceAsAbsent(sessionRepository, attendanceService);
//                     // // Refresh UI on JavaFX thread
//                     // Platform.runLater(() -> {
//                     //     attendanceController.loadAttendanceRecords();
//                     // });
//                     // notifyAutoUpdate();
                    
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//             }
//         }, 0, intervalSeconds * 1000L);
//     }

//     /**
//      * Stop the timer
//      */
//     public void stopAutoUpdate() {
//         timer.cancel();
//     }

// }
