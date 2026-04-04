package com.nextech.controller;

import com.nextech.dto.UserRegistrationDto;
import com.nextech.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationDto", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationDto") UserRegistrationDto dto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        // Extra cross-field validation
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if ("SELLER".equalsIgnoreCase(dto.getRole()) &&
                (dto.getShopName() == null || dto.getShopName().isBlank())) {
            result.rejectValue("shopName", "required", "Shop name is required for sellers");
        }
        if (userService.existsByUsername(dto.getUsername())) {
            result.rejectValue("username", "taken", "Username is already taken");
        }
        if (userService.existsByEmail(dto.getEmail())) {
            result.rejectValue("email", "taken", "Email is already registered");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(dto);
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
