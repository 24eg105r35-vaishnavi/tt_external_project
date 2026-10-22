package com.portal.security;

import com.portal.entity.AuditLog;
import com.portal.entity.User;
import com.portal.repository.AuditLogRepository;
import com.portal.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LoginLockoutListener {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public LoginLockoutListener(UserRepository userRepository, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // listen for wrong password attempts
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        Object principal = event.getAuthentication().getName();
        if (principal instanceof String) {
            String email = ((String) principal).trim().toLowerCase();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                // If previous lock has expired, reset counter first
                if (user.getLockUntil() != null && user.getLockUntil().isBefore(LocalDateTime.now())) {
                    user.setFailedAttempts(0);
                    user.setLockUntil(null);
                }

                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);

                // Lock for 15 minutes on 5 consecutive failed attempts
                if (attempts >= 5) {
                    user.setLockUntil(LocalDateTime.now().plusMinutes(15));

                    // Write ACCOUNT_LOCKED audit log row
                    AuditLog log = new AuditLog();
                    log.setAction("ACCOUNT_LOCKED");
                    log.setEntityName("User");
                    log.setEntityId(user.getId());
                    log.setPerformedBy(email);
                    log.setNewValue("Account locked for 15 minutes after 5 failed login attempts");
                    log.setCreatedAt(LocalDateTime.now());
                    auditLogRepository.save(log);
                }

                userRepository.save(user);
            }
        }
    }

    // reset failed attempts counter on successful login
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getName();
        if (principal instanceof String) {
            String email = ((String) principal).trim().toLowerCase();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && (user.getFailedAttempts() > 0 || user.getLockUntil() != null)) {
                user.setFailedAttempts(0);
                user.setLockUntil(null);
                userRepository.save(user);
            }
        }
    }
}
