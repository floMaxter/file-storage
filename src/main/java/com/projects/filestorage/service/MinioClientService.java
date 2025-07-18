package com.projects.filestorage.service;

import com.projects.filestorage.config.MinioClientProperties;
import com.projects.filestorage.domain.enums.ResourceType;
import com.projects.filestorage.exception.DirectoryDeletionException;
import com.projects.filestorage.exception.MinioAccessException;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.response.ResourceInfoDto;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioClientService {

    private final MinioClient minioClient;
    private final MinioClientProperties minioClientProperties;

    public ResourceInfoDto getResourceInfo(String path) {
        log.info("Resolving resource info for path '{}'", path);

        var pathToResource = MinioUtils.extractParentPath(path);
        var resourceName = MinioUtils.extractResourceName(path);
        var resourceType = resolveResourceType(path);

        Long size = null;
        if (resourceType == ResourceType.FILE) {
            size = getResourceSize(path);
        }

        var resourceInfoDto = ResourceInfoDto.builder()
                .path(pathToResource)
                .name(resourceName)
                .size(size)
                .resourceType(resourceType)
                .build();

        log.debug("Resolved resource: {}", resourceInfoDto);
        return resourceInfoDto;
    }

    public void deleteResource(String path) {
        var resourceType = resolveResourceType(path);
        log.info("Deleting resource '{}' of type {}", path, resourceType);

        switch (resourceType) {
            case FILE -> deleteFile(path);
            case DIRECTORY -> deleteDirectory(path);
        }
    }

    public ResourceType resolveResourceType(String path) {
        if (isFile(path)) return ResourceType.FILE;
        if (isDirectory(path)) return ResourceType.DIRECTORY;

        log.warn("Resource on path '{}' was not found (not a file or directory)", path);
        throw new ResourceNotFoundException(String.format("The resource on the path '%s' was not found", path));
    }

    private boolean isFile(String path) {
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
            log.info("MinIO responded with error while checking if path '{}' is a file: {}",
                    path, ex.errorResponse().code());
            throw new MinioAccessException(String.format("MinIO error when checking for file at path '%s'. Error code: %s",
                    path, ex.errorResponse().code()), ex);
        } catch (Exception ex) {
            log.error("Unexpected error occurred while checking if path '{}' is a file", path, ex);
            throw new MinioAccessException(String.format("Unexpected error while verifying file at path '%s'",
                    path), ex);
        }
    }

    private boolean isDirectory(String path) {
        if (!path.endsWith("/")) {
            return false;
        }

        try {
            var prefix = path.endsWith("/") ? path : path + "/";
            var objectsIterable = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .prefix(prefix)
                    .delimiter("/")
                    .recursive(false)
                    .build());

            return objectsIterable.iterator().hasNext();
        } catch (Exception ex) {
            log.error("Unexpected error occurred while checking if path '{}' is a directory", path, ex);
            throw new MinioAccessException(String.format("Unexpected error while verifying directory at path '%s'",
                    path), ex);
        }
    }

    private void deleteFile(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(path)
                    .build());

            ensureDirectoryPlaceholder(path);

            log.info("File '{}' deleted successfully from MinIO", path);
        } catch (ErrorResponseException ex) {
            if (MinioUtils.isNoSuchKey(ex)) {
                log.warn("Attempted to delete file '{}', but it was not found in MinIO (NoSuchKey)", path);
            } else {
                log.error("MinIO responded with error while deleting file '{}'. Error code: {}",
                        path, ex.errorResponse().code(), ex);
                throw new MinioAccessException(String.format("MinIO error occurred while deleting file '%s'. Error code: %s",
                        path, ex.errorResponse().code()), ex);
            }
        } catch (Exception ex) {
            log.error("Unexpected exception while deleting file '{}' from MinIO", path, ex);
            throw new MinioAccessException(String.format("Unexpected error occurred while deleting file '%s' from MinIO",
                    path), ex);
        }
    }

    private void ensureDirectoryPlaceholder(String path) {
        var prefix = MinioUtils.extractParentPath(path);
        try {
            var objectsIterable = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .prefix(prefix)
                    .delimiter("/")
                    .recursive(false)
                    .build());

            if (!objectsIterable.iterator().hasNext()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioClientProperties.getBucketName())
                        .object(prefix)
                        .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .build());
                log.info("Recreated placeholder for empty directory '{}'", prefix);
            }
        } catch (Exception ex) {
            log.warn("Failed to recreate placeholder for directory '{}'", prefix, ex);
        }
    }

    private void deleteDirectory(String path) {
        var objectsIterable = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioClientProperties.getBucketName())
                .prefix(path.endsWith("/") ? path : path + "/")
                .delimiter("/")
                .recursive(true)
                .build());

        var objectsToDelete = createObjectsToDelete(path, objectsIterable);
        removeObjects(path, objectsToDelete);
        log.debug("Finished deletion of directory '{}'", path);
    }

    private List<DeleteObject> createObjectsToDelete(String path, Iterable<Result<Item>> objects) {
        try {
            var objectsToDelete = new LinkedList<DeleteObject>();
            for (var object : objects) {
                var objectName = object.get().objectName();
                objectsToDelete.add(new DeleteObject(objectName));
            }

            log.debug("Collected {} objects to delete from directory '{}'", objectsToDelete.size(), path);
            return objectsToDelete;
        } catch (Exception ex) {
            throw new MinioAccessException(String.format("Unexpected error when accessing the '%s' resource",
                    path), ex);
        }
    }

    private void removeObjects(String path, List<DeleteObject> objectsToDelete) {
        try {
            log.info("Starting bulk deletion of {} objects from directory '{}'", objectsToDelete.size(), path);

            var deletionErrors = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .objects(objectsToDelete)
                    .build());

            var failedObjects = new ArrayList<String>();
            for (var errorResult : deletionErrors) {
                var error = errorResult.get();
                failedObjects.add(error.objectName());
                log.warn("Failed to delete object '{}' in directory '{}'. Error: {}",
                        error.objectName(), path, error.message());
            }

            if (!failedObjects.isEmpty()) {
                log.error("Directory deletion incomplete. {} objects failed to delete in '{}': {}",
                        failedObjects.size(), path, failedObjects);
                throw new DirectoryDeletionException(String.format("Not all files in directory '%s' were deleted: %s",
                        path, failedObjects));
            }

            log.info("Successfully deleted all {} objects from directory '{}'", objectsToDelete.size(), path);
        } catch (Exception ex) {
            log.error("Unexpected error during deletion of directory '{}'", path, ex);
            throw new MinioAccessException(String.format("Unexpected error during deletion of directory '%s'",
                    path), ex);
        }
    }

    private Long getResourceSize(String path) {
        try {
            log.debug("Fetching size of resource '{}'", path);

            var statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(path)
                    .build());

            var size = statObjectResponse.size();
            log.info("Size of resource '{}' is {} bytes", path, size);
            return size;
        } catch (ErrorResponseException ex) {
            if (MinioUtils.isNoSuchKey(ex)) {
                log.warn("Resource '{}' not found in MinIO (NoSuchKey)", path);
                throw new ResourceNotFoundException(String.format("Resource %s not found", path));
            }
            log.error("MinIO returned error when retrieving size of '{}'. Error code: {}",
                    path, ex.errorResponse().code(), ex);
            throw new MinioAccessException("MinIO error while getting size", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while getting resource size of '{}'", path, ex);
            throw new MinioAccessException(String.format("Unexpected error when determining the file size '%s'",
                    path), ex);
        }
    }
}
