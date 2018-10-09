package com.example.pim.service;

import com.example.pim.domain.Media;
import com.example.pim.domain.Product;
import com.example.pim.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final AuditLogService auditLogService;
    private final Path rootLocation = Paths.get("upload-dir"); // TODO: Make this configurable

    @Autowired
    public MediaService(MediaRepository mediaRepository, AuditLogService auditLogService) {
        this.mediaRepository = mediaRepository;
        this.auditLogService = auditLogService;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public Media saveMedia(Product product, MultipartFile file) {
        // Validate file type and size
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File exceeds 10MB limit.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File type not supported. Please upload an image.");
        }

        try {
            String filename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));

            Media media = new Media();
            media.setProduct(product);
            media.setFilePath(this.rootLocation.resolve(filename).toString());
            media.setFileType(file.getContentType());
            Media savedMedia = mediaRepository.save(media);

            auditLogService.log("UPLOAD_MEDIA", "Media", savedMedia.getId(), "system");
            return savedMedia;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    public List<Media> getMediaForProduct(Long productId) {
        return mediaRepository.findByProductId(productId);
    }
}
