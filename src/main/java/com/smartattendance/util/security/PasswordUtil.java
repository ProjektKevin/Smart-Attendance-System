package com.smartattendance.util.security;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class PasswordUtil {
  public static String hash(String p) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return Base64.getEncoder().encodeToString(md.digest(p.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean matches(String p, String h) {
    return hash(p).equals(h);
  }
}
