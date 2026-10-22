package com.portal.controller;

import com.portal.service.NotificationService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class NotificationAdvice {

    private final NotificationService notificationService;

    public NotificationAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // supplies unreadCount to all Thymeleaf templates automatically
    @ModelAttribute("unreadCount")
    public long populateUnreadCount(Principal principal) {
        if (principal == null || principal.getName() == null || "anonymousUser".equalsIgnoreCase(principal.getName())) {
            return 0;
        }
        try {
            return notificationService.getUnreadCount(principal.getName());
        } catch (Exception e) {
            return 0;
        }
    }
}
