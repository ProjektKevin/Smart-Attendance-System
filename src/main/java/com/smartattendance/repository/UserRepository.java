package com.smartattendance.repository;

import com.smartattendance.model.entity.User;

public interface UserRepository {
  User findByUsername(String username);
}
