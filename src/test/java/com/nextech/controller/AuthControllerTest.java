package com.nextech.controller;

import com.nextech.entity.User;
import com.nextech.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean UserService userService;

    @Test
    void getRegister_returnsOkWithForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registrationDto"));
    }

    @Test
    void postRegister_validData_redirectsToLogin() throws Exception {
        given(userService.existsByUsername("newuser")).willReturn(false);
        given(userService.existsByEmail("new@example.com")).willReturn(false);
        User saved = new User();
        saved.setUsername("newuser");
        given(userService.register(any())).willReturn(saved);

        mockMvc.perform(post("/register")
                .param("username", "newuser")
                .param("fullName", "New User")
                .param("email", "new@example.com")
                .param("password", "secret123")
                .param("confirmPassword", "secret123")
                .param("role", "BUYER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
