package com.projects.filestorage.service;

import com.projects.filestorage.config.MinioClientProperties;
import com.projects.filestorage.domain.enums.ResourceType;
import com.projects.filestorage.exception.DirectoryDeletionException;
import com.projects.filestorage.exception.MinioAccessException;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.validation.MinioResourceValidator;
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
    private final MinioResourceValidator minioResourceValidator;

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

    public List<ResourceInfoDto> getDirectoryInfo(String path) {
        log.info("Resolving directory info for path '{}'", path);

        minioResourceValidator.validateIsDirectory(path);
        try {
            var objectItems = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .prefix(path)
                    .delimiter("/")
                    .recursive(false)
                    .build());

            var resourceInfos = new ArrayList<ResourceInfoDto>();
            for (var result : objectItems) {
                var objectPath = result.get().objectName();

                if (objectPath.equals(path)) continue;

                var resourceInfo = getResourceInfo(objectPath);
                resourceInfos.add(resourceInfo);
            }

            log.debug("Resolved directory: path = {}, size = {}", path, resourceInfos);
            return resourceInfos;
        } catch (Exception ex) {
            log.error("Failed to get directory info for path '{}'", path, ex);
            throw new MinioAccessException(String.format(
                    "Unexpected error while getting information about a directory on the path '%s'", path), ex);
        }
    }

    public List<ResourceInfoDto> createEmptyDirectory(String path) {
        minioResourceValidator.validateCreateEmptyDirectoryConstraints(path);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .build());

            return getDirectoryInfo(path);
        } catch (Exception ex) {
            log.error("Unexpected error during creation of an empty directory on the path {}", path, ex);
            throw new MinioAccessException(String.format(
                    "Unexpected error during creation of an empty directory on the path '%s'", path), ex);
        }
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
        if (minioResourceValidator.isFile(path)) return ResourceType.FILE;
        if (minioResourceValidator.isDirectory(path)) return ResourceType.DIRECTORY;

        log.warn("Resource on path '{}' was not found (not a file or directory)", path);
        throw new ResourceNotFoundException(String.format("The resource on the path '%s' was not found", path));
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
