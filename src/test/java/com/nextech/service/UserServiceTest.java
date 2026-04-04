package com.nextech.service;

import com.nextech.dto.UserRegistrationDto;
import com.nextech.entity.Role;
import com.nextech.entity.RoleName;
import com.nextech.entity.User;
import com.nextech.repository.RoleRepository;
import com.nextech.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserServiceImpl userService;

    private UserRegistrationDto validDto;
    private Role buyerRole;

    @BeforeEach
    void setUp() {
        validDto = new UserRegistrationDto();
        validDto.setUsername("testuser");
        validDto.setFullName("Test User");
        validDto.setEmail("test@example.com");
        validDto.setPassword("secret123");
        validDto.setConfirmPassword("secret123");
        validDto.setRole("BUYER");

        buyerRole = new Role();
        buyerRole.setName(RoleName.ROLE_BUYER);
    }

    @Test
    void register_success_returnsUser() {
        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(roleRepository.findByName(RoleName.ROLE_BUYER)).willReturn(Optional.of(buyerRole));
        given(passwordEncoder.encode("secret123")).willReturn("$2a$10$hashed");
        User saved = new User();
        saved.setUsername("testuser");
        given(userRepository.save(any(User.class))).willReturn(saved);

        User result = userService.register(validDto);

        assertThat(result.getUsername()).isEqualTo("testuser");
        then(passwordEncoder).should().encode("secret123");
    }

    @Test
    void register_duplicateUsername_throwsException() {
        given(userRepository.existsByUsername("testuser")).willReturn(true);

        assertThatThrownBy(() -> userService.register(validDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void register_duplicateEmail_throwsException() {
        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.register(validDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void findByUsername_returnsUser() {
        User user = new User();
        user.setUsername("testuser");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void register_passwordEncodedWithBCrypt() {
        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(roleRepository.findByName(any())).willReturn(Optional.of(buyerRole));
        given(passwordEncoder.encode("secret123")).willReturn("$2a$10$encoded");
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        User result = userService.register(validDto);

        assertThat(result.getPassword()).isEqualTo("$2a$10$encoded");
    }
}
