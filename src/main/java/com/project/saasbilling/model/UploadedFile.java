package com.project.saasbilling.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Uploaded file document representing a file stored in the system.
 */
@Document(collection = "uploaded_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedFile {

    @Id
    private String id;

    @Indexed
    private String userId;

    // Denormalized for easy access
    private String userEmail;

    @Indexed(unique = true)
    private String fileName;

    private String originalFileName;

    private String contentType;

    private Long fileSize;

    private String fileType;

    @Builder.Default
    private String storageType = "LOCAL";

    private String filePath;

    private String publicUrl;

    @CreatedDate
    private LocalDateTime createdAt;
}
