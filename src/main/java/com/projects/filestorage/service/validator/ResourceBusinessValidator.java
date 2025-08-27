package com.projects.filestorage.service.validator;

import com.projects.filestorage.exception.DirectoryNotFoundException;
import com.projects.filestorage.exception.ResourceAlreadyExistsException;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.repository.MinioRepository;
import com.projects.filestorage.utils.MinioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceBusinessValidator {

    private final MinioRepository minioRepository;

    public void validateFileCopyPreconditions(String bucket, String sourcePath, String targetPath) {
        validateFileExists(bucket, sourcePath);
        validateFileDoesNotExits(bucket, targetPath);
    }

    public void validateDirectoryCopyPreconditions(String bucket, String sourcePath, String targetPath) {
        validateDirectoryExists(bucket, sourcePath);
        validateDirectoryDoesNotExits(bucket, targetPath);
    }

    public void validateDirectoryCreationPreconditions(String bucket, String path) {
        validateParentExists(bucket, path);
        validateDirectoryDoesNotExits(bucket, path);
    }

    public void validateFileExists(String bucket, String path) {
        if (!minioRepository.isFileExists(bucket, path)) {
            log.error("[Validate] File not exits in bucket='{}', path='{}'", bucket, path);
            throw new ResourceNotFoundException(String.format("The file on the path '%s' was not found", path));
        }
    }

    public void validateDirectoryExists(String bucket, String path) {
        if (!minioRepository.isDirectoryExists(bucket, path)) {
            log.error("[Validate] Directory not exits in bucket='{}', path='{}'", bucket, path);
            throw new DirectoryNotFoundException(String.format("The directory on the path '%s' was not found", path));
        }
    }

    public void validateFileDoesNotExits(String bucket, String path) {
        if (minioRepository.isFileExists(bucket, path)) {
            log.error("[Validate] File already exits in bucket='{}', path='{}'", bucket, path);
            throw new ResourceAlreadyExistsException(String.format("The file on the path '%s' already exits", path));
        }
    }

    public void validateDirectoryDoesNotExits(String bucket, String path) {
        if (minioRepository.isDirectoryExists(bucket, path)) {
            log.error("[Validate] Directory already exits in bucket='{}', path='{}'", bucket, path);
            throw new ResourceAlreadyExistsException(String.format("The directory on the path '%s' already exits", path));
        }
    }

    public void validateParentExists(String bucket, String path) {
        var parentPath = MinioUtils.extractParentPath(path);
        if (!minioRepository.isDirectoryExists(bucket, parentPath)) {
            log.info("[Validate] An attempt to create an empty directory using a non-existent path '{}'", parentPath);
            throw new DirectoryNotFoundException(String.format("Parent directory does not exist: %s", parentPath));
        }
    }
}
