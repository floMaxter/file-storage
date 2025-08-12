package com.projects.filestorage.validation;

import com.projects.filestorage.config.properties.FileUploadProperties;
import com.projects.filestorage.exception.InvalidMultipartFileException;
import com.projects.filestorage.exception.InvalidResourcePathFormatException;
import com.projects.filestorage.exception.InvalidSearchQueryFormatException;
import com.projects.filestorage.utils.MinioUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourcePathValidator {

    private final FileUploadProperties fileUploadProperties;

    private static final Pattern VALID_PATH_PATTERN = Pattern.compile(
            "^(?!.*(?:^|/)\\.\\.?(?:/|$))(?:[\\p{L}\\p{N} _.-]+/)*[\\p{L}\\p{N} _.-]+/?$|^$"
    );

    private static final Pattern VALID_DIRECTORY_PATH_PATTERN = Pattern.compile(
            "^(?!.*(?:^|/)(\\.{1,2})(?:/|$))(?!.*(?:^|/)(\\.\\.[^/]*)(?:/|$))([\\p{L}\\p{N} _.-]+/)+$|^$"
    );

    private static final Pattern VALID_SEARCH_QUERY_PATTERN = Pattern.compile(
            "^(?!.*(?:^|/)(?:\\.|\\.\\.)($|/))(?:[\\p{L}\\p{N} _.-]+/)*[\\p{L}\\p{N} _.-]+/?$|^$"
    );

    public void validatePathFormat(String path) {
        if (!isValidPathFormat(path)) {
            log.info("[Validate] Invalid path format: '{}'", path);
            throw new InvalidResourcePathFormatException(String.format("The path '%s' has an invalid format", path));
        }
    }

    public void validateDirectoryPathFormat(String path) {
        if (!isValidDirectoryPathFormat(path)) {
            log.info("[Validate] Invalid format for directory path: '{}'. Expected pattern: 'parentFolderName/newFolderName/'", path);
            throw new InvalidResourcePathFormatException(String.format(
                    "The path '%s' has an invalid format for directory. Expected format: 'parentFolder/.../newFolder/'", path));
        }
    }

    public void validateMovePathsFormat(String sourcePath, String destinationPath) {
        validatePathFormat(sourcePath);
        validatePathFormat(destinationPath);
        validateSamePathType(sourcePath, destinationPath);
    }

    public void validateSearchQueryFormat(String query) {
        if (!isValidSearchQueryFormat(query)) {
            log.info("[Validate] Invalid search query format: '{}'", query);
            throw new InvalidSearchQueryFormatException(String.format("The query '%s' has an invalid format", query));
        }
    }

    public void validateUploadResourcesFormat(String path, List<MultipartFile> objets) {
        validateDirectoryPathFormat(path);
        for (var object : objets) {
            validatePathFormat(object.getOriginalFilename());
            validateMultipartFileSize(object);
        }
    }

    public void validateCreateEmptyDirectoryPathFormat(String path) {
        if (isEmptyPath(path)) {
            log.info("[Validate] Empty path for creation empty directory");
            throw new InvalidResourcePathFormatException("The folder creation path must not be empty.");
        }
        validateDirectoryPathFormat(path);
    }

    private void validateSamePathType(String sourcePath, String destinationPath) {
        boolean isSourceDir = MinioUtils.isPathDirectoryLike(sourcePath);
        boolean isDestinationDir = MinioUtils.isPathDirectoryLike(destinationPath);

        if (isSourceDir != isDestinationDir) {
            log.warn("[Validate] Mismatch in resource types: sourcePath='{}' (directory: {}), destinationPath='{}' (directory: {})",
                    sourcePath, isSourceDir, destinationPath, isDestinationDir);
            throw new InvalidResourcePathFormatException("Source and destination must both be files or both be directories");
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

    private boolean isValidPathFormat(String path) {
        if (path == null) {
            return false;
        }
        return path.matches(String.valueOf(VALID_PATH_PATTERN));
    }

    private boolean isValidSearchQueryFormat(String query) {
        if (query == null) {
            return false;
        }
        return query.matches(String.valueOf(VALID_SEARCH_QUERY_PATTERN));
    }

    public boolean isValidDirectoryPathFormat(String path) {
        if (path == null) {
            return false;
        }
        return path.matches(String.valueOf(VALID_DIRECTORY_PATH_PATTERN));
    }

    public boolean isEmptyPath(String path) {
        return path == null || path.trim().isBlank();
    }
}
