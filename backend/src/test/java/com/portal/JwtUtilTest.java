package com.portal;

import com.portal.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setup() {
        String testSecret = "studentachievementportaljwtsecretkeymustbelongenough256bits";
        long expiryMs = 7200000; // 2 hours
        jwtUtil = new JwtUtil(testSecret, expiryMs);
    }

    @Test
    public void testTokenGenerationAndExtraction() {
        String email = "student@anurag.edu.in";
        String role = "STUDENT";

        String token = jwtUtil.generateToken(email, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        assertEquals(email, jwtUtil.extractUsername(token));
        assertEquals(role, jwtUtil.extractRole(token));
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    public void testTokenValidation() {
        String email = "faculty@anurag.edu.in";
        String token = jwtUtil.generateToken(email, "FACULTY");

        UserDetails userDetails = new User(email, "password", Collections.emptyList());
        assertTrue(jwtUtil.validateToken(token, userDetails));

        UserDetails differentUser = new User("other@anurag.edu.in", "password", Collections.emptyList());
        assertFalse(jwtUtil.validateToken(token, differentUser));
    }
}
