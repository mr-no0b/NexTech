package com.nextech.service;

import com.nextech.dto.UserRegistrationDto;
import com.nextech.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(UserRegistrationDto dto);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User findById(Long id);
    List<User> findAll();
    User save(User user);
    void deleteById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
