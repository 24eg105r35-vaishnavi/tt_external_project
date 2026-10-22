package com.portal.controller;

import com.portal.entity.Document;
import com.portal.entity.User;
import com.portal.repository.DocumentRepository;
import com.portal.repository.UserRepository;
import com.portal.service.AchievementService;
import com.portal.service.FileStorageService;
import com.portal.service.VerificationService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;
    private final VerificationService verificationService;
    private final FileStorageService fileStorageService;

    public DocumentController(DocumentRepository documentRepository,
                              UserRepository userRepository,
                              AchievementService achievementService,
                              VerificationService verificationService,
                              FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
        this.verificationService = verificationService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getDocument(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        String role = (user.getRole() != null) ? user.getRole().getName() : "";
        Document document;

        if ("ADMIN".equalsIgnoreCase(role)) {
            document = documentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Document not found with id: " + id));
        } else if ("FACULTY".equalsIgnoreCase(role)) {
            document = verificationService.getDocumentForFaculty(id, email);
        } else {
            // Student owner
            document = achievementService.getDocumentById(id, email);
        }

        Resource resource = fileStorageService.loadAsResource(document.getStoredName());

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(document.getFileType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getOriginalName() + "\"")
                .body(resource);
    }
}
