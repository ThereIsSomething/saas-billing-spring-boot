package com.project.saasbilling.service;

import com.project.saasbilling.dto.FileResponse;
import com.project.saasbilling.dto.PageResponse;
import com.project.saasbilling.exception.BadRequestException;
import com.project.saasbilling.exception.ResourceNotFoundException;
import com.project.saasbilling.model.UploadedFile;
import com.project.saasbilling.model.User;
import com.project.saasbilling.repository.UploadedFileRepository;
import com.project.saasbilling.repository.UserRepository;
import com.project.saasbilling.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for file storage operations.
 * Updated for MongoDB with String IDs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.max-size}")
    private long maxFileSize;

    @Value("${file.upload.allowed-types}")
    private String allowedTypes;

    /**
     * Store a file for a user.
     */
    public FileResponse storeFile(String userId, MultipartFile file) {
        log.info("Storing file for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate file
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Create user-specific subdirectory
            Path userUploadPath = uploadPath.resolve(userId);
            Files.createDirectories(userUploadPath);

            // Store the file
            Path targetLocation = userUploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Create file record with denormalized data
            UploadedFile uploadedFile = UploadedFile.builder()
                    .userId(userId)
                    .userEmail(user.getEmail())
                    .fileName(storedFileName)
                    .originalFileName(originalFileName)
                    .filePath(targetLocation.toString())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileType(determineFileType(file.getContentType()))
                    .storageType("LOCAL")
                    .build();

            uploadedFile = uploadedFileRepository.save(uploadedFile);
            log.info("File stored successfully: {}", uploadedFile.getId());

            return dtoMapper.toFileResponse(uploadedFile);

        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    /**
     * Get file by ID.
     */
    public FileResponse getFileById(String id) {
        UploadedFile file = findFileById(id);
        return dtoMapper.toFileResponse(file);
    }

    /**
     * Get file by ID for a specific user.
     */
    public FileResponse getFileByIdAndUser(String id, String userId) {
        UploadedFile file = uploadedFileRepository.findById(id)
                .filter(f -> f.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));
        return dtoMapper.toFileResponse(file);
    }

    /**
     * Get user's files.
     */
    public List<FileResponse> getUserFiles(String userId) {
        return uploadedFileRepository.findByUserId(userId)
                .stream()
                .map(dtoMapper::toFileResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user's files with pagination.
     */
    public PageResponse<FileResponse> getUserFiles(String userId, Pageable pageable) {
        Page<FileResponse> page = uploadedFileRepository.findByUserId(userId, pageable)
                .map(dtoMapper::toFileResponse);
        return dtoMapper.toPageResponse(page);
    }

    /**
     * Get file content by ID.
     */
    public byte[] getFileContent(String id) {
        UploadedFile file = findFileById(id);

        try {
            Path filePath = Paths.get(file.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to read file: {}", e.getMessage());
            throw new BadRequestException("Failed to read file: " + e.getMessage());
        }
    }

    /**
     * Delete a file.
     */
    public void deleteFile(String id, String userId) {
        UploadedFile file = uploadedFileRepository.findById(id)
                .filter(f -> f.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        try {
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath);
            uploadedFileRepository.delete(file);
            log.info("File deleted: {}", id);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Get total storage used by a user.
     */
    public Long getUserStorageUsed(String userId) {
        return uploadedFileRepository.findByUserId(userId)
                .stream()
                .mapToLong(UploadedFile::getFileSize)
                .sum();
    }

    /**
     * Validate uploaded file.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size of " +
                    (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        List<String> allowedTypesList = Arrays.asList(allowedTypes.split(","));

        if (contentType == null || !allowedTypesList.contains(contentType.trim())) {
            throw new BadRequestException("File type not allowed. Allowed types: " + allowedTypes);
        }
    }

    /**
     * Get file extension from filename.
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1).toLowerCase() : "";
    }

    /**
     * Determine file type from content type.
     */
    private String determineFileType(String contentType) {
        if (contentType == null)
            return "unknown";
        if (contentType.startsWith("image/"))
            return "image";
        if (contentType.startsWith("video/"))
            return "video";
        if (contentType.startsWith("audio/"))
            return "audio";
        if (contentType.equals("application/pdf"))
            return "document";
        return "other";
    }

    /**
     * Find file entity by ID.
     */
    public UploadedFile findFileById(String id) {
        return uploadedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));
    }
}
