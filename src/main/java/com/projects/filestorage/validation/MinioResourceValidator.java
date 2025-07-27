package com.projects.filestorage.validation;

import com.projects.filestorage.config.FileUploadProperties;
import com.projects.filestorage.config.MinioClientProperties;
import com.projects.filestorage.exception.DirectoryNotFoundException;
import com.projects.filestorage.exception.InvalidMultipartFileException;
import com.projects.filestorage.exception.InvalidResourcePathFormatException;
import com.projects.filestorage.exception.InvalidSearchQueryFormatException;
import com.projects.filestorage.exception.MinioAccessException;
import com.projects.filestorage.exception.ResourceAlreadyExistsException;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.utils.MinioUtils;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioResourceValidator {

    private final MinioClient minioClient;
    private final MinioClientProperties minioClientProperties;
    private final FileUploadProperties fileUploadProperties;

    public void validateGetResourceInfo(String path) {
        validatePathFormat(path);
        validateResourceExists(path);
    }

    public void validateGetDirectoryInfo(String path) {
        validateDirectoryPathFormat(path);
        validateResourceExists(path);
        validateIsDirectory(path);
    }

    public void validateMoveResource(String sourcePath, String destinationPath) {
        validatePathFormat(sourcePath);
        validatePathFormat(destinationPath);

        validateSamePathType(sourcePath, destinationPath);

        validateResourceExists(sourcePath);
        validateNotExists(destinationPath);
    }

    public void validateDownloadResource(String path) {
        validatePathFormat(path);
        validateResourceExists(path);
    }

    public void validateUploadResources(String path, List<MultipartFile> files) {
        validatePathFormat(path);

        for (var file : files) {
            validateUploadResource(path + file.getOriginalFilename(), file);
        }
    }

    public void validateUploadResource(String path, MultipartFile file) {
        validatePathFormat(path);
        validateNotExists(path);
        validateMultipartFile(file);
    }

    public void validateDeleteResource(String path) {
        validatePathFormat(path);
        validateResourceExists(path);
    }

    public void validateCreateEmptyDirectoryConstraints(String path) {
        validateDirectoryPathFormat(path);
        validateParentExists(MinioUtils.extractParentPath(path));
        validateNotExists(path);
    }

    public void validateSamePathType(String sourcePath, String destinationPath) {
        boolean isSourceDir = MinioUtils.isPathDirectoryLike(sourcePath);
        boolean isDestinationDir = MinioUtils.isPathDirectoryLike(destinationPath);

        if (isSourceDir != isDestinationDir) {
            log.warn("[Validate] Mismatch in resource types: sourcePath='{}' (directory: {}), destinationPath='{}' (directory: {})",
                    sourcePath, isSourceDir, destinationPath, isDestinationDir);
            throw new InvalidResourcePathFormatException("Source and destination must both be files or both be directories");
        }
    }

    public void validateIsDirectory(String path) {
        if (!isDirectory(path)) {
            log.info("[Validate] The folder on the path '{}' was not found", path);
            throw new DirectoryNotFoundException(String.format("The folder on the path '%s' was not found", path));
        }
    }

    public void validatePathFormat(String path) {
        if (path == null || path.isBlank()) {
            log.info("[Validate] Path must not be null or blank");
            throw new InvalidResourcePathFormatException("The path most not be null or blank");
        }

        if (!MinioUtils.isValidPathFormat(path)) {
            log.info("[Validate] Invalid path format: '{}'", path);
            throw new InvalidResourcePathFormatException(String.format("The path '%s' has an invalid format", path));
        }
    }

    public void validateDirectoryPathFormat(String path) {
        if (!MinioUtils.isValidDirectoryPathFormat(path)) {
            log.info("[Validate] Invalid format for directory path: '{}'. Expected pattern: 'parentFolderName/newFolderName/'", path);
            throw new InvalidResourcePathFormatException(String.format(
                    "The path '%s' has an invalid format for directory. Expected format: 'parentFolder/.../newFolder/'", path));
        }
    }

    public void validateSearchQueryFormat(String query) {
        if (!MinioUtils.isValidSearchQueryFormat(query)) {
            log.info("[Validate] Invalid search query format: '{}'", query);
            throw new InvalidSearchQueryFormatException(String.format("The query '%s' has an invalid format", query));
        }
    }

    public void validateParentExists(String parentPath) {
        if (!isDirectoryExists(parentPath)) {
            log.info("[Validate] An attempt to create an empty file using a non-existent path '{}'", parentPath);
            throw new ResourceNotFoundException(String.format("Parent directory does not exist: %s", parentPath));
        }
    }

    public void validateResourceExists(String path) {
        if (!isResourceExists(path)) {
            log.warn("[Validate] Resource on path '{}' was not found (not a file or directory)", path);
            throw new ResourceNotFoundException(String.format("The resource on the path '%s' was not found", path));
        }
    }

    public void validateNotExists(String path) {
        if (isResourceExists(path)) {
            log.info("[Validate] The resource on the path '{}' already exists", path);
            throw new ResourceAlreadyExistsException(String.format("The resource on the path '%s' already exists", path));
        }
    }

    public void validateMultipartFile(MultipartFile file) {
        validateMultipartFileNotEmpty(file);
        validateMultipartFileSize(file);
    }

    public void validateMultipartFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.debug("[Validate] File is empty");
            throw new InvalidMultipartFileException("File is empty");
        }
    }

    private void validateMultipartFileSize(MultipartFile file) {
        long maxFileSizeBytes = fileUploadProperties.getMaxFileSize().toBytes();
        if (file.getSize() > maxFileSizeBytes) {
            log.debug("[Validate] File size ({} bytes) exceeds the maximum allowed size ({} bytes)",
                    file.getSize(), maxFileSizeBytes);
            throw new InvalidMultipartFileException(String.format(
                    "File size (%d bytes) exceeds the maximum allowed size (%d bytes)",
                    file.getSize(), maxFileSizeBytes));
        }
    }

    public boolean isFile(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(path)
                    .build());
            return !path.endsWith("/");
        } catch (ErrorResponseException ex) {
            if (MinioUtils.isNoSuchKey(ex)) {
                return false;
            }
            log.info("[Validate] MinIO responded with error while checking if path '{}' is a file: {}",
                    path, ex.errorResponse().code());
            throw new MinioAccessException(String.format("MinIO error when checking for file at path '%s'. Error code: %s",
                    path, ex.errorResponse().code()), ex);
        } catch (Exception ex) {
            log.error("[Failed] Unexpected error occurred while checking if path '{}' is a file", path, ex);
            throw new MinioAccessException(String.format("Unexpected error while verifying file at path '%s'",
                    path), ex);
        }
    }

    public boolean isDirectory(String path) {
        if (!path.endsWith("/")) {
            return false;
        }

        try {
            var objectItems = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .prefix(path)
                    .delimiter("/")
                    .recursive(false)
                    .build());

            return objectItems.iterator().hasNext();
        } catch (Exception ex) {
            log.error("[Failed] Unexpected error occurred while checking if path '{}' is a directory", path, ex);
            throw new MinioAccessException(String.format("Unexpected error while verifying directory at path '%s'",
                    path), ex);
        }
    }

    public boolean isResourceExists(String path) {
        if (!path.endsWith("/")) {
            try {
                minioClient.statObject(StatObjectArgs.builder()
                        .bucket(minioClientProperties.getBucketName())
                        .object(path)
                        .build());
                return true;
            } catch (ErrorResponseException erEx) {
                if (!MinioUtils.isNoSuchKey(erEx)) {
                    log.warn("[Validate] Unexpected MinIO error for statObject '{}': {}", path, erEx.errorResponse().code());
                    throw new MinioAccessException("Error checking resource existence", erEx);
                }
                return false;
            } catch (Exception ex) {
                log.error("[Failed] Unexpected error during statObject for '{}'", path, ex);
                throw new MinioAccessException("Unexpected error checking existence", ex);
            }
        }

        try {
            var objects = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .prefix(path)
                    .delimiter("/")
                    .recursive(false)
                    .build());

            return objects.iterator().hasNext();
        } catch (Exception ex) {
            log.error("[Failed] Unexpected error during listObjects for '{}'", path, ex);
            throw new MinioAccessException("Unexpected error checking existence (listObjects)", ex);
        }
    }

    public boolean isDirectoryExists(String path) {
        if (!path.endsWith("/")) {
            return false;
        }

        try {
            var objectItems = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .prefix(path)
                    .delimiter("/")
                    .recursive(false)
                    .build());
            return objectItems.iterator().hasNext();
        } catch (Exception ex) {
            return false;
        }
    }
}
