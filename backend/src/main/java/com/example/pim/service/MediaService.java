package com.example.pim.service;

import com.example.pim.domain.Media;
import com.example.pim.domain.Product;
import com.example.pim.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ThumbnailService thumbnailService;
    private final Path rootLocation = Paths.get("upload-dir"); // TODO: Make this configurable
    private static final int MAX_IMAGES_PER_PRODUCT = 12; // Configurable limit
    private static final long STORAGE_QUOTA_BYTES = 100 * 1024 * 1024; // 100MB configurable quota

    @Autowired
    public MediaService(MediaRepository mediaRepository, AuditLogService auditLogService, ThumbnailService thumbnailService) {
        this.mediaRepository = mediaRepository;
        this.auditLogService = auditLogService;
        this.thumbnailService = thumbnailService;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public Media saveMedia(Product product, MultipartFile file) {
        // Check image limit
        long currentImageCount = mediaRepository.findByProductId(product.getId()).size();
        if (currentImageCount >= MAX_IMAGES_PER_PRODUCT) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES_PER_PRODUCT + " images allowed per product.");
        }

        // Validate file type and size
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit for single file
            throw new IllegalArgumentException("File exceeds 10MB limit.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File type not supported. Please upload an image.");
        }

        // Check storage quota
        try {
            long currentStorageUsed = Files.walk(rootLocation)
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
            if (currentStorageUsed + file.getSize() > STORAGE_QUOTA_BYTES) {
                throw new IllegalArgumentException("Storage quota exceeded. Cannot upload more files.");
            }
        } catch (IOException e) {
            System.err.println("Error calculating storage usage: " + e.getMessage());
            // Continue with upload, but log the issue
        }

        try {
            String filename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            Path destinationFile = this.rootLocation.resolve(filename);
            Files.copy(file.getInputStream(), destinationFile);

            // Generate thumbnail
            Path thumbnailPath = thumbnailService.generateThumbnail(destinationFile, rootLocation, filename);

            Media media = new Media();
            media.setProduct(product);
            media.setFilePath(destinationFile.toString());
            media.setThumbnailPath(thumbnailPath.toString());
            media.setFileType(file.getContentType());
            // If this is the first image, make it primary by default
            if (currentImageCount == 0) {
                media.setPrimary(true);
            }
            Media savedMedia = mediaRepository.save(media);

            auditLogService.log("UPLOAD_MEDIA", "Media", savedMedia.getId(), "system");
            return savedMedia;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file or generate thumbnail.", e);
        }
    }

    public List<Media> getMediaForProduct(Long productId) {
        return mediaRepository.findByProductId(productId);
    }

    @Transactional
    public Media setPrimaryImage(Long productId, Long mediaId) {
        List<Media> existingMedia = mediaRepository.findByProductId(productId);
        Media newPrimary = null;

        for (Media media : existingMedia) {
            if (media.getId().equals(mediaId)) {
                newPrimary = media;
            } else if (media.isPrimary()) {
                media.setPrimary(false);
                mediaRepository.save(media);
            }
        }

        if (newPrimary == null) {
            throw new IllegalArgumentException("Media with ID '" + mediaId + "' not found for product '" + productId + "'.");
        }

        if (!newPrimary.isPrimary()) {
            newPrimary.setPrimary(true);
            mediaRepository.save(newPrimary);
            auditLogService.log("SET_PRIMARY_MEDIA", "Media", newPrimary.getId(), "system");
        }
        return newPrimary;
    }

    @Transactional
    public void deleteMedia(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        try {
            Files.deleteIfExists(Paths.get(media.getFilePath()));
            if (media.getThumbnailPath() != null) {
                Files.deleteIfExists(Paths.get(media.getThumbnailPath()));
            }
        } catch (IOException e) {
            // Log the error but don't prevent database deletion if file is already gone or inaccessible
            System.err.println("Could not delete file(s) for media ID " + mediaId + ". Error: " + e.getMessage());
        }

        mediaRepository.delete(media);
        auditLogService.log("DELETE_MEDIA", "Media", mediaId, "system");
    }
}




