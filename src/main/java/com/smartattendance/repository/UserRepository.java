package com.smartattendance.repository;

import com.smartattendance.model.User;

public interface UserRepository {
  User findByUsername(String username);
}
