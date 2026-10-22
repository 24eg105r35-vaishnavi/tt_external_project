package com.portal.config;

import com.portal.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    // single password encoder bean for the entire application
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // explicitly wire DaoAuthenticationProvider with CustomUserDetailsService and PasswordEncoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // redirects user to their specific dashboard based on their role
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_ADMIN")) {
                    response.sendRedirect("/admin/dashboard");
                    return;
                } else if (role.equals("ROLE_FACULTY")) {
                    response.sendRedirect("/faculty/dashboard");
                    return;
                } else if (role.equals("ROLE_STUDENT")) {
                    response.sendRedirect("/student/dashboard");
                    return;
                }
            }
            response.sendRedirect("/");
        };
    }

    // custom failure handler to detect locked account vs invalid credentials
    @Bean
    public org.springframework.security.web.authentication.AuthenticationFailureHandler customFailureHandler(com.portal.repository.UserRepository userRepository) {
        return (request, response, exception) -> {
            String email = request.getParameter("email");
            if (email != null && !email.trim().isEmpty()) {
                com.portal.entity.User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
                if (user != null && user.getLockUntil() != null && user.getLockUntil().isAfter(java.time.LocalDateTime.now())) {
                    response.sendRedirect("/login?locked=true");
                    return;
                }
            }
            if (exception instanceof org.springframework.security.authentication.LockedException) {
                response.sendRedirect("/login?locked=true");
                return;
            }
            response.sendRedirect("/login?error=true");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DaoAuthenticationProvider authProvider,
                                                   com.portal.repository.UserRepository userRepository) throws Exception {
        http
            .authenticationProvider(authProvider)
            // security headers
            .headers(headers -> headers
                .contentTypeOptions(contentType -> {}) // X-Content-Type-Options: nosniff
                .frameOptions(frame -> frame.deny())   // X-Frame-Options: DENY
                .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            // role-based endpoint protection
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/error", "/css/**", "/js/**", "/images/**", "/u/**", "/robots.txt", "/sitemap.xml").permitAll()
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/faculty/**").hasRole("FACULTY")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/analytics/**").hasAnyRole("FACULTY", "ADMIN")
                .requestMatchers("/search/**", "/export/**").hasAnyRole("FACULTY", "ADMIN")
                .requestMatchers("/leaderboard/**", "/notifications/**").authenticated()
                .anyRequest().authenticated()
            )
            // custom login page configuration
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(customSuccessHandler())
                .failureHandler(customFailureHandler(userRepository))
                .permitAll()
            )
            // logout configuration
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
