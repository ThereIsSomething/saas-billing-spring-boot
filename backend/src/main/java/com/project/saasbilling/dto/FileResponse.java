package com.project.saasbilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for file upload response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private String id;
    private String userId;
    private String fileName;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String fileType;
    private String storageType;
    private String publicUrl;
    private LocalDateTime createdAt;
}
