package com.smartattendance.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
  private static final Properties props = new Properties();
  private static final String CONFIG_FILE = "config.properties";

  static {
    try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
      props.load(fis);
    } catch (IOException e) {
      System.err.println("No config: " + e.getMessage());
    }
  }

  public static String get(String k) {
    return props.getProperty(k);
  }

  public static void set(String k, String v) {
    props.setProperty(k, v);

    // Make it persistent even when program ends
    try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
      props.store(fos, null);
      System.out.println("Config updated: " + k + " = " + v);
    } catch (IOException e) {
      System.err.println("Failed to save config: " + e.getMessage());
      throw new RuntimeException("Could not persist configuration change", e);
    }
  }
}
