package com.portal.controller;

import com.portal.dto.LoginRequest;
import com.portal.dto.LoginResponse;
import com.portal.dto.RegisterDto;
import com.portal.dto.UserDto;
import com.portal.entity.User;
import com.portal.repository.UserRepository;
import com.portal.security.JwtUtil;
import com.portal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          UserRepository userRepository,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // handle student registration
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterDto registerDto) {
        userService.registerStudent(registerDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Registration successful. Please log in.");
        return ResponseEntity.ok(response);
    }

    // handle user login and JWT issuance
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        String normalizedEmail = loginRequest.getEmail().trim().toLowerCase();

        // 1. authenticate user credentials with Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, loginRequest.getPassword())
        );

        // 2. retrieve user details
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3. generate stateless JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());

        // 4. return token and basic profile info
        LoginResponse response = new LoginResponse(
                token,
                user.getRole().getName(),
                user.getFullName(),
                user.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    // get currently authenticated user profile
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return ResponseEntity.ok(userService.toDto(user));
    }
}
