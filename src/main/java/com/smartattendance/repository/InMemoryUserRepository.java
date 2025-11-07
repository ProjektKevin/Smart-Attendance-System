package com.smartattendance.repository;

import com.smartattendance.model.entity.User;
import com.smartattendance.model.enums.Role;
import com.smartattendance.util.security.PasswordUtil;
import java.util.*;

public class InMemoryUserRepository implements UserRepository {
  private final Map<String, User> users = new HashMap<>();

  public InMemoryUserRepository() {
    users.put("admin", new User("admin", PasswordUtil.hash("admin123"), Role.ADMIN));
    users.put("staff", new User("staff", PasswordUtil.hash("staff123"), Role.STUDENT));
  }

  public User findByUsername(String u) {
    return users.get(u);
  }
}
