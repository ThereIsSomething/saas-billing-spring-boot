package com.project.saasbilling.repository;

import com.project.saasbilling.model.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for UploadedFile documents.
 */
@Repository
public interface UploadedFileRepository extends MongoRepository<UploadedFile, String> {

    List<UploadedFile> findByUserId(String userId);

    Page<UploadedFile> findByUserId(String userId, Pageable pageable);

    Optional<UploadedFile> findByFileName(String fileName);

    List<UploadedFile> findByUserIdAndFileType(String userId, String fileType);

    boolean existsByFileName(String fileName);
}
