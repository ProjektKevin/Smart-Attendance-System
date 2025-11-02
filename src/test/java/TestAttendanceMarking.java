
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;

// import com.smartattendance.model.AttendanceRecord;
// import com.smartattendance.model.MarkMethod;
// import com.smartattendance.model.Session;
// import com.smartattendance.model.Student;
// import com.smartattendance.model.AttendanceStatus;
// import com.smartattendance.service.AttendanceMarker;
// import com.smartattendance.service.AttendanceService;
// import com.smartattendance.service.AutoAttendanceMarker;
// import com.smartattendance.service.ManualAttendanceMarker;

// public class TestAttendanceMarking {

//     public static void main(String[] args) {
//         // Setup
//         Student john = new Student("0123456", "John Smith", "G3");
//         Student amy = new Student("0123457", "Amy Cecilia", "G3");

//         Session session = new Session("S001", "CS123", LocalDate.now(), LocalTime.of(12, 0),
//                 LocalTime.of(15, 15), "Sr1-2", 15);

//         session.addStudent(john);
//         session.addStudent(amy);

//         AttendanceService service = new AttendanceService();
//         AttendanceMarker autoMarker = new AutoAttendanceMarker();

//         // Auto mark attendance
//         AttendanceRecord record1 = service.getOrCreateRecord(john, session);
//         autoMarker.markAttendance(session, record1, LocalDateTime.now());

//         // Manual override example
//         AttendanceMarker manualMarker = new ManualAttendanceMarker();
//         AttendanceRecord record2 = service.getOrCreateRecord(amy, session);
//         record2.mark(AttendanceStatus.ABSENT, LocalDateTime.now(), MarkMethod.MANUAL, "Feeling unwell");
//         manualMarker.markAttendance(session, record2, LocalDateTime.now());

//         System.out.println("\nFinal Attendance Records:");
//         service.printAllRecords();
//     }
// }
