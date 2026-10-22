package com.portal.service;

import com.portal.entity.Notification;
import com.portal.entity.User;
import com.portal.repository.NotificationRepository;
import com.portal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // get unread notification count for a user
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        if (email == null || email.trim().isEmpty() || "anonymousUser".equalsIgnoreCase(email.trim())) {
            return 0;
        }
        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        if (user == null) {
            return 0;
        }
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    // get all notifications for a user, newest first
    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // mark a single notification as read (with cross-user ownership check)
    @Transactional
    public void markAsRead(Long id, String email) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));

        // refuse if notification belongs to another user
        if (!notification.getUser().getEmail().equalsIgnoreCase(email.trim())) {
            throw new SecurityException("You do not have permission to modify this notification.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // mark all notifications as read for the user
    @Transactional
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        List<Notification> unreadList = notificationRepository.findByUserAndIsReadFalse(user);
        for (Notification n : unreadList) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }
}
