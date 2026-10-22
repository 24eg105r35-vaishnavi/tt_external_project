package com.portal.controller;

import com.portal.dto.RegisterDto;
import com.portal.service.UserService;
import com.portal.util.RegistrationException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // show custom login page
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // show student registration form
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    // handle student registration submission
    @PostMapping("/register")
    public String registerStudent(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                                  BindingResult bindingResult,
                                  Model model) {
        // if bean validation fails, show form with field errors
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerStudent(registerDto);
            // redirect to login with success alert
            return "redirect:/login?registered=true";
        } catch (RegistrationException e) {
            // show custom error message (e.g. wrong domain, duplicate roll number)
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            // show unexpected error message
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }
}
