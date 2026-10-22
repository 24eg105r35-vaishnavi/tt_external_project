package com.portal.service;

import com.portal.dto.StoredFileInfo;
import com.portal.entity.Student;
import com.portal.repository.DocumentRepository;
import com.portal.util.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private final DocumentRepository documentRepository;
    private Path rootLocation;

    public FileStorageService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    // initialize upload folder outside resources
    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(this.rootLocation)) {
                Files.createDirectories(this.rootLocation);
            }
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory at: " + uploadDir, e);
        }
    }

    // validate, store file, and verify duplicate checksum for the student
    public StoredFileInfo storeFile(MultipartFile file, Student student) {
        // 1. check not empty
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Please select a file to upload.");
        }

        // 2. check size (max 10MB)
        long maxSizeBytes = 10L * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new FileStorageException("File size exceeds 10MB limit. Please upload a smaller file.");
        }

        // 3. check original filename and extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new FileStorageException("File must have a valid extension (.pdf, .jpg, .jpeg, or .png).");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!extension.equals(".pdf") && !extension.equals(".jpg") && !extension.equals(".jpeg") && !extension.equals(".png")) {
            throw new FileStorageException("Invalid file extension. Only PDF, JPG, and PNG files are allowed.");
        }

        // 4. check Content-Type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equalsIgnoreCase("application/pdf")
                && !contentType.equalsIgnoreCase("image/jpeg")
                && !contentType.equalsIgnoreCase("image/png")
                && !contentType.equalsIgnoreCase("image/jpg"))) {
            throw new FileStorageException("Invalid content type. Only PDF and image files are allowed.");
        }

        // 5. verify file header magic bytes to detect spoofed files (e.g. .exe renamed to .pdf)
        try {
            byte[] headerBytes = new byte[8];
            try (InputStream in = file.getInputStream()) {
                int bytesRead = in.read(headerBytes);
                if (bytesRead < 4) {
                    throw new FileStorageException("File is corrupted or too small.");
                }
            }

            if (extension.equals(".pdf")) {
                // PDF header starts with %PDF (0x25, 0x50, 0x44, 0x46)
                if (headerBytes[0] != 0x25 || headerBytes[1] != 0x50 || headerBytes[2] != 0x44 || headerBytes[3] != 0x46) {
                    throw new FileStorageException("Invalid PDF file content. The file does not appear to be a real PDF.");
                }
            } else if (extension.equals(".png")) {
                // PNG header starts with 0x89 0x50 0x4E 0x47
                if ((headerBytes[0] & 0xFF) != 0x89 || headerBytes[1] != 0x50 || headerBytes[2] != 0x4E || headerBytes[3] != 0x47) {
                    throw new FileStorageException("Invalid PNG file content.");
                }
            } else if (extension.equals(".jpg") || extension.equals(".jpeg")) {
                // JPEG header starts with 0xFF 0xD8 0xFF
                if ((headerBytes[0] & 0xFF) != 0xFF || (headerBytes[1] & 0xFF) != 0xD8 || (headerBytes[2] & 0xFF) != 0xFF) {
                    throw new FileStorageException("Invalid JPEG file content.");
                }
            }
        } catch (IOException e) {
            throw new FileStorageException("Could not read file header.", e);
        }

        // 6. compute SHA-256 checksum and check duplicate for this student
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new FileStorageException("Could not read file data.", e);
        }

        String checksum = calculateSha256(fileBytes);
        if (documentRepository.existsByChecksumAndStudent(checksum, student)) {
            throw new FileStorageException("You already uploaded this file.");
        }

        // 7. generate unique UUID stored name (never use original filename on disk)
        String storedName = UUID.randomUUID().toString() + extension;
        Path targetPath = this.rootLocation.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageException("Could not save file to disk.", e);
        }

        return new StoredFileInfo(
                originalFilename,
                storedName,
                targetPath.toString(),
                contentType,
                file.getSize(),
                checksum
        );
    }

    // helper to delete file if transaction fails
    public void deleteFile(String filePath) {
        if (filePath != null) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
            } catch (IOException ignored) {
            }
        }
    }

    // load file as a Resource for secure download
    public Resource loadAsResource(String storedName) {
        try {
            Path file = this.rootLocation.resolve(storedName).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found or not readable: " + storedName);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Could not read file: " + storedName, e);
        }
    }

    // helper to compute SHA-256 hash string
    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new FileStorageException("SHA-256 algorithm not available.", e);
        }
    }
}
