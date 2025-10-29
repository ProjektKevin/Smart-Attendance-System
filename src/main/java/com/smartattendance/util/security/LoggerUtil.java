package com.smartattendance.util.security;

import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {
  public static final Logger LOGGER = Logger.getLogger("SmartAttendanceLog");
  static {
    try {
      Handler h = new FileHandler("attendance.log", true);
      LOGGER.addHandler(h);
      LOGGER.setLevel(Level.INFO);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
