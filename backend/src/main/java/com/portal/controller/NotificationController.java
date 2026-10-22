package com.portal.controller;

import com.portal.entity.Notification;
import com.portal.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // List notifications for the logged in user (any role)
    @GetMapping
    public String listNotifications(Principal principal, Model model) {
        List<Notification> notifications = notificationService.getMyNotifications(principal.getName());
        model.addAttribute("notifications", notifications);
        return "notifications/list";
    }

    // Mark single notification as read
    @PostMapping("/{id}/read")
    public String markNotificationAsRead(@PathVariable("id") Long id,
                                         Principal principal,
                                         RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(id, principal.getName());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/notifications";
    }

    // Mark all notifications as read
    @PostMapping("/read-all")
    public String markAllAsRead(Principal principal,
                                RedirectAttributes redirectAttributes) {
        notificationService.markAllAsRead(principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/notifications";
    }
}
