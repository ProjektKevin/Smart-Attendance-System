package com.smartattendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.smartattendance.model.AttendanceRecord;
import com.smartattendance.model.AttendanceStatus;
import com.smartattendance.model.MarkMethod;
import com.smartattendance.model.Session;
import com.smartattendance.model.Student;
import com.smartattendance.util.AppContext;

public class RecognitionService {
  // Use shared attendance service so Reports can see the records
  private final AttendanceService attendanceService = AppContext.getAttendanceService();

  public AttendanceService getAttendanceService() {
    return attendanceService;
  }

  public void startRecognition() {
    new Thread(() -> {
      try {
        Thread.sleep(1200);
      } catch (Exception ignored) {
      }
      Student s = new Student("S101", "Alice Tan", "CS102");
      Session sess = new Session(1, "CS102", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), "Room 1",
          15, "PENDING");
      // AttendanceRecord rec = new AttendanceRecord(s, sess, "Present", "Auto", 0.95, LocalDateTime.now());
      AttendanceRecord rec = new AttendanceRecord(s, sess, AttendanceStatus.PRESENT, 0.95, LocalDateTime.now(), MarkMethod.AUTO);
      attendanceService.markAttendance(rec);
    }).start();
  }

  public void stopRecognition() {
  }
}
// package com.smartattendance.service;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;

// import com.smartattendance.model.AttendanceRecord;
// import com.smartattendance.model.Session;
// import com.smartattendance.model.Student;
// import com.smartattendance.util.AppContext;

// public class RecognitionService {
//   // Use shared attendance service so Reports can see the records
//   private final AttendanceService attendanceService = AppContext.getAttendanceService();
//   public AttendanceService getAttendanceService(){ return attendanceService; }

//   public void startRecognition(){
//     new Thread(() -> {
//       try { Thread.sleep(1200); } catch (Exception ignored) {}
//       Student s = new Student("S101","Alice Tan","G1");
//       Session sess = new Session("SESS1","CS102", LocalDate.now(), LocalTime.of(10,0), LocalTime.of(11,0), "Room 1", 15);
//       AttendanceRecord rec = new AttendanceRecord(s, sess, "Present", "Auto", 0.95, LocalDateTime.now());
//       attendanceService.markAttendance(rec);
//     }).start();
//   }
//   public void stopRecognition(){}
// }


// /*
//  # Done by: Chue Wan Yan
//  # Step: 1 
//  # Date: 13 Oct 2025
// */

// package com.smartattendance.service;  // <-- import AutoMarker, AttendanceRecord, etc.

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.util.HashMap;
// import java.util.Map;

// import com.smartattendance.model.*;
// import com.smartattendance.util.AppContext;


// /**
//  * Handles the real-time recognition process and delegates marking logic
//  * to the AutoMarker class.
//  */
// public class RecognitionService {

//     // Shared attendance service (used by reports and controllers)
//     private final AttendanceService attendanceService = AppContext.getAttendanceService();

//     // Map of studentId -> AttendanceRecord (used by AutoMarker)
//     private final Map<String, AttendanceRecord> attendanceMap = new HashMap<>();

//     // AutoMarker instance (strategy pattern for automatic marking)
//     private AutoAttendanceMarker autoMarker;

//     // Current session (in a real app, set this dynamically)
//     private Session currentSession;

//     /**
//      * Expose shared attendance service for controllers and observers.
//      */
//     public AttendanceService getAttendanceService() {
//         return attendanceService;
//     }

//     /**
//      * Initialize session and AutoMarker before recognition starts.
//      */
//     public void startRecognition() {
//         // Create a mock session
//         currentSession = new Session("CS102", LocalDateTime.now());

//         // Example student list (in real app, load from database/roster)
//         Student student = new Student("S101", "Alice Tan");
//         AttendanceRecord record = new AttendanceRecord(student, currentSession);
//         attendanceMap.put(student.getStudentId(), record);

//         // Create AutoMarker with thresholds, cooldowns, etc.
//         autoMarker = new AutoAttendanceMarker(
//             currentSession, 
//             attendanceMap,
//             80.0f,  // high confidence threshold
//             60.0f,  // low confidence threshold
//             15,     // late threshold (minutes)
//             10,     // cooldown (seconds)
//             (name) -> {
//                 // Low-confidence callback
//                 System.out.println("Confirm identity for " + name + " (y/n)?");
//                 return true; // auto-confirm for testing
//             }
//         );

//         // Add observer to sync with AttendanceService
//         autoMarker.registerObserver(record1 -> attendanceService.markAttendance(record1));

//         // Simulate recognition in a background thread
//         new Thread(() -> {
//             try { Thread.sleep(1000); } catch (Exception ignored) {}
//             // Simulate detection with high confidence
//             autoMarker.markAttendance("S101", 85.0f);
//         }).start();
//     }

//     /**
//      * Stop recognition logic gracefully.
//      */
//     public void stopRecognition() {
//         System.out.println("Recognition stopped.");
//     }
// }


