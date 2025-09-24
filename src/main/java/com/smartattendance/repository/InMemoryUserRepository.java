package com.smartattendance.repository;

import com.smartattendance.model.User;
import com.smartattendance.util.PasswordUtil;
import java.util.*;

public class InMemoryUserRepository implements UserRepository {
  private final Map<String, User> users = new HashMap<>();

  public InMemoryUserRepository() {
    users.put("admin", new User("admin", PasswordUtil.hash("admin123"), "ADMIN"));
    users.put("staff", new User("staff", PasswordUtil.hash("staff123"), "STAFF"));
  }

  public User findByUsername(String u) {
    return users.get(u);
  }
}
