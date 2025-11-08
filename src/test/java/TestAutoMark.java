import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.smartattendance.ApplicationContext;
import com.smartattendance.config.Config;
import com.smartattendance.model.entity.AttendanceRecord;
import com.smartattendance.model.entity.AttendanceStatus;
import com.smartattendance.model.entity.MarkMethod;
import com.smartattendance.model.entity.Session;
import com.smartattendance.model.entity.Student;
import com.smartattendance.service.AttendanceService;

public class TestAutoMark {
  // Use shared attendance service so Reports can see the records
  private final AttendanceService attendanceService = ApplicationContext.getAttendanceService();
  private final double threshold = Double.parseDouble(Config.get("recognition.threshold"));

  public AttendanceService getAttendanceService() {
    return attendanceService;
  }

  public void startRecognition() {
    new Thread(() -> {
      try {
        Thread.sleep(1200);
      } catch (Exception ignored) {
      }
      Student s = new Student(10, "Alice Tan", "CS102");
      Session sess = new Session(1, "CS102", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), "Room 1",
          15, "PENDING");
      // AttendanceRecord rec = new AttendanceRecord(s, sess, "Present", "Auto", 0.95, LocalDateTime.now());
      // AttendanceRecord rec = new AttendanceRecord(s, sess, AttendanceStatus.PRESENT, 0.95, LocalDateTime.now(), MarkMethod.AUTO);
      // attendanceService.markAttendance(rec);
      // AttendanceRecord rec = new AttendanceRecord(s, sess, AttendanceStatus.PRESENT, 0.95, MarkMethod.AUTO, LocalDateTime.now());
      AttendanceRecord rec = new AttendanceRecord(s, sess, AttendanceStatus.PRESENT, 0.95, MarkMethod.AUTO, LocalDateTime.now());
      attendanceService.markAttendance(rec);
    }).start();
  }

  public void stopRecognition() {
  }
}