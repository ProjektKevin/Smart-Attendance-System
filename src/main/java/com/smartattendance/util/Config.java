package com.smartattendance.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
  private static final Properties props = new Properties();
  static {
    try (FileInputStream fis = new FileInputStream("config.properties")) {
      props.load(fis);
    } catch (IOException e) {
      System.err.println("No config: " + e.getMessage());
    }
  }

  public static String get(String k) {
    return props.getProperty(k);
  }
}
