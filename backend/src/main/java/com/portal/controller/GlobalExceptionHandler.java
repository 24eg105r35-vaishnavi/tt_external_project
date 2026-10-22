package com.portal.controller;

import com.portal.util.FileStorageException;
import com.portal.util.ProfileNotFoundException;
import com.portal.util.RegistrationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    // file upload or validation failure redirects back with friendly flash message
    @ExceptionHandler(FileStorageException.class)
    public String handleFileStorage(FileStorageException ex,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.trim().isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/student/achievements/new";
    }

    // missing or private profile shows friendly 404 page
    @ExceptionHandler(ProfileNotFoundException.class)
    public String handleProfileNotFound(ProfileNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    // registration error redirects back to register page with message
    @ExceptionHandler(RegistrationException.class)
    public String handleRegistration(RegistrationException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/register";
    }

    // invalid arguments redirect back to previous page with flash message
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.trim().isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}
