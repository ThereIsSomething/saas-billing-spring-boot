package com.project.saasbilling.controller;

import com.project.saasbilling.dto.*;
import com.project.saasbilling.model.User;
import com.project.saasbilling.service.FileStorageService;
import com.project.saasbilling.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File upload and management")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file")
    public ResponseEntity<FileResponse> uploadFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(fileStorageService.storeFile(user.getId(), file));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's files")
    public ResponseEntity<List<FileResponse>> getMyFiles(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(fileStorageService.getUserFiles(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file metadata")
    public ResponseEntity<FileResponse> getFile(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable String id) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(fileStorageService.getFileByIdAndUser(id, user.getId()));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a file")
    public ResponseEntity<byte[]> downloadFile(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable String id) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        FileResponse meta = fileStorageService.getFileByIdAndUser(id, user.getId());
        byte[] content = fileStorageService.getFileContent(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(meta.getContentType()))
                .body(content);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file")
    public ResponseEntity<Void> deleteFile(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable String id) {
        User user = userService.getCurrentUser(userDetails.getUsername());
        fileStorageService.deleteFile(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}