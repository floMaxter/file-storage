package com.projects.filestorage.service;

import com.projects.filestorage.config.MinioClientProperties;
import com.projects.filestorage.domain.enums.ResourceType;
import com.projects.filestorage.exception.DirectoryDeletionException;
import com.projects.filestorage.exception.InvalidSearchQueryFormatException;
import com.projects.filestorage.exception.MinioAccessException;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.utils.ResponseHeaderUtils;
import com.projects.filestorage.validation.MinioResourceValidator;
import com.projects.filestorage.web.dto.response.ResourceInfoDto;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
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
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioClientService {

    private final MinioClient minioClient;
    private final MinioClientProperties minioClientProperties;
    private final MinioResourceValidator minioResourceValidator;

    public ResourceInfoDto getResourceInfo(String path) {
        log.info("[Start] Resolving resource info for path '{}'", path);

        minioResourceValidator.validateGetResourceInfo(path);

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

        log.info("[Success] Resolved resource info: {}", resourceInfoDto);
        return resourceInfoDto;
    }

    public List<ResourceInfoDto> getDirectoryInfo(String path) {
        log.info("[Start] Resolving directory info for path '{}'", path);

        minioResourceValidator.validateGetDirectoryInfo(path);

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

            log.info("[Success] Resolved directory info: path = {}, resources = {}", path, resourceInfos.size());
            return resourceInfos;
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error while getting information about a directory on the path '{}'", path, ex);
            throw new MinioAccessException(String.format(
                    "Unexpected error while getting information about a directory on the path '%s'", path), ex);
        }
    }

    public List<ResourceInfoDto> searchResources(String query) {
        log.info("[Start] Searching for resources by '{}'", query);

        minioResourceValidator.validateSearchQueryFormat(query);

        try {
            var objectItems = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .prefix(query)
                    .recursive(true)
                    .build());

            var resourceInfos = new ArrayList<ResourceInfoDto>();
            for (var result : objectItems) {
                var objectPath = result.get().objectName();
                var resourceInfo = getResourceInfo(objectPath);
                resourceInfos.add(resourceInfo);
            }

            log.info("[Success] Searched for recourses by '{}': size = {}", query, resourceInfos.size());
            return resourceInfos;
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error while searching for resources by '{}'", query);
            throw new InvalidSearchQueryFormatException(String.format(
                    "Unexpected error while getting information about a directory on the path '%s'", query), ex);
        }
    }

    public List<ResourceInfoDto> createEmptyDirectory(String path) {
        log.info("[Start] Creating empty directory at path '{}'", path);

        minioResourceValidator.validateCreateEmptyDirectoryConstraints(path);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .build());

            log.info("[Success] Created empty directory at path '{}'", path);
            return getDirectoryInfo(path);
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error during creation of an empty directory on the path '{}'", path, ex);
            throw new MinioAccessException(String.format(
                    "Unexpected error during creation of an empty directory on the path '%s'", path), ex);
        }
    }

    public ResourceInfoDto moveResource(String sourcePath, String destinationPath) {
        log.info("[Start] Moving resource from '{}' to '{}'", sourcePath, destinationPath);

        minioResourceValidator.validateMoveResource(sourcePath, destinationPath);

        var resourceType = resolveResourceType(sourcePath);
        switch (resourceType) {
            case FILE -> moveFile(sourcePath, destinationPath);
            case DIRECTORY -> moveDirectory(sourcePath, destinationPath);
        }
        deleteResource(sourcePath);

        log.info("[Success] Moved resource from '{}' to '{}'", sourcePath, destinationPath);
        return getResourceInfo(destinationPath);
    }

    public void downloadResource(HttpServletResponse response, String path) {
        log.info("[Start] Downloading a resource on the path '{}'", path);

        minioResourceValidator.validateDownloadResource(path);

        var resourceType = resolveResourceType(path);
        switch (resourceType) {
            case FILE -> downloadFile(response, path);
            case DIRECTORY -> downloadZipFile(response, path);
        }

        log.info("[Success] Downloaded a resource on the path '{}'", path);
    }

    public void deleteResource(String path) {
        log.info("[Start] Deleting resource at path '{}'", path);

        minioResourceValidator.validateDeleteResource(path);

        var resourceType = resolveResourceType(path);
        switch (resourceType) {
            case FILE -> deleteFile(path);
            case DIRECTORY -> deleteDirectory(path);
        }
        ensureDirectoryPlaceholder(path);

        log.info("[Success] Deleted resource at path '{}'", path);
    }

    private ResourceType resolveResourceType(String path) {
        if (minioResourceValidator.isFile(path)) return ResourceType.FILE;
        if (minioResourceValidator.isDirectory(path)) return ResourceType.DIRECTORY;

        log.warn("[Warn] Resource on path '{}' was not found (not a file or directory)", path);
        throw new ResourceNotFoundException(String.format("The resource on the path '%s' was not found", path));
    }

    private void moveFile(String sourcePath, String destinationPath) {
        log.debug("[Start] Moving file from '{}' to '{}'", sourcePath, destinationPath);

        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(destinationPath)
                    .source(CopySource.builder()
                            .bucket(minioClientProperties.getBucketName())
                            .object(sourcePath)
                            .build())
                    .build());

            log.debug("[Success] Moved file from '{}' to '{}'", sourcePath, destinationPath);
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error during move file from '{}' to '{}'", sourcePath, destinationPath);
            throw new MinioAccessException(String.format(
                    "Unexpected error during move file from '%s' to '%s'", sourcePath, destinationPath));
        }
    }

    private void moveDirectory(String sourcePath, String destinationPath) {
        log.debug("[Start] Moving directory from '{}' to '{}'", sourcePath, destinationPath);

        try {
            var objectItems = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .prefix(sourcePath)
                    .delimiter("/")
                    .recursive(false)
                    .build());

            for (var result : objectItems) {
                var nestedObjectPath = result.get().objectName();
                var nestedResourceName = MinioUtils.extractResourceName(nestedObjectPath);

                var nestedSourcePath = sourcePath + nestedResourceName;
                var nestedDestinationPath = destinationPath + nestedResourceName;
                moveResource(nestedSourcePath, nestedDestinationPath);
            }

            log.debug("[Success] Moved directory from '{}' to '{}'", sourcePath, destinationPath);
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error during directory file from '{}' to '{}'", sourcePath, destinationPath);
            throw new MinioAccessException(String.format(
                    "Unexpected error during directory file from %s to %s", sourcePath, destinationPath));
        }
    }

    private void downloadFile(HttpServletResponse response, String path) {
        log.debug("[Start] Downloading a file on the path '{}'", path);

        ResponseHeaderUtils.setFileDownloadHeader(response, path);
        try {
            var objectResponse = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(path)
                    .build());

            var outputStream = response.getOutputStream();
            StreamUtils.copy(objectResponse, outputStream);

            log.debug("[Success] Downloaded a file on the path '{}'", path);
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error when download a file on the path '{}'", path, ex);
            throw new MinioAccessException(String.format(
                    "Unexpected error when download a file on the path '%s'", path), ex);
        }
    }

    private void downloadZipFile(HttpServletResponse response, String path) {
        log.debug("[Start] Downloading a directory on the path '{}'", path);

        ResponseHeaderUtils.setZipDownloadHeader(response, path);
        try (var zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            var objectItems = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .prefix(path)
                    .recursive(true)
                    .build());

            for (var objectItem : objectItems) {
                var item = objectItem.get();

                var zipEntry = new ZipEntry(item.objectName());
                zipEntry.setSize(item.size());
                zipEntry.setTime(System.currentTimeMillis());

                zipOutputStream.putNextEntry(zipEntry);

                var objectResponse = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(minioClientProperties.getBucketName())
                        .object(item.objectName())
                        .build());
                StreamUtils.copy(objectResponse, zipOutputStream);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();

            log.debug("[Success] Downloaded a directory on the path '{}'", path);
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error when download a directory on the path '{}'", path, ex);
            throw new MinioAccessException(String.format(
                    "Unexpected error when download a directory on the path '%s'", path), ex);
        }
    }

    private void deleteFile(String path) {
        log.debug("[Start] Deleting a file on the '{}'", path);

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(path)
                    .build());

            log.debug("[Success] Deleted a file on the '{}'", path);
        } catch (ErrorResponseException ex) {
            if (MinioUtils.isNoSuchKey(ex)) {
                log.warn("[Warn] Attempted to delete file '{}', but it was not found in MinIO (NoSuchKey)", path);
            } else {
                log.error("[Failure] MinIO responded with error while deleting file '{}'. Error code: {}",
                        path, ex.errorResponse().code(), ex);
                throw new MinioAccessException(String.format("MinIO error occurred while deleting file '%s'. Error code: %s",
                        path, ex.errorResponse().code()), ex);
            }
        } catch (Exception ex) {
            log.error("[Failure] Unexpected exception while deleting file '{}' from MinIO", path, ex);
            throw new MinioAccessException(String.format("Unexpected error occurred while deleting file '%s' from MinIO",
                    path), ex);
        }
    }

    private void deleteDirectory(String path) {
        log.debug("[Start] Deleting a directory on the '{}'", path);

        var objectsIterable = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioClientProperties.getBucketName())
                .prefix(path.endsWith("/") ? path : path + "/")
                .delimiter("/")
                .recursive(true)
                .build());

        var objectsToDelete = createObjectsToDelete(path, objectsIterable);
        removeObjects(path, objectsToDelete);

        log.debug("[Success] Deleted a directory on the '{}'", path);
    }

    private void ensureDirectoryPlaceholder(String path) {
        log.debug("[Start] Recreating placeholder for empty directory on the '{}'", path);

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
                log.debug("[Success] Recreated placeholder for empty directory on the '{}'", prefix);
            }
        } catch (Exception ex) {
            log.warn("[Warn] Failed to recreate placeholder for directory '{}'", prefix, ex);
        }
    }

    private List<DeleteObject> createObjectsToDelete(String path, Iterable<Result<Item>> objects) {
        log.debug("[Start] Collecting objects to delete from directory on the '{}'", path);

        try {
            var objectsToDelete = new LinkedList<DeleteObject>();
            for (var object : objects) {
                var objectName = object.get().objectName();
                objectsToDelete.add(new DeleteObject(objectName));
            }

            log.debug("[Success] Collected {} objects to delete from directory '{}'", objectsToDelete.size(), path);
            return objectsToDelete;
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error when accessing the '{}' resource", path);
            throw new MinioAccessException(String.format("Unexpected error when accessing the '%s' resource",
                    path), ex);
        }
    }

    private void removeObjects(String path, List<DeleteObject> objectsToDelete) {
        log.debug("[Start] Deleting all {} objects from directory '{}'", objectsToDelete.size(), path);

        try {
            var deletionErrors = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .objects(objectsToDelete)
                    .build());

            var failedObjects = new ArrayList<String>();
            for (var errorResult : deletionErrors) {
                var error = errorResult.get();
                failedObjects.add(error.objectName());
                log.warn("[Warn] Failed to delete object '{}' in directory '{}'. Error: {}",
                        error.objectName(), path, error.message());
            }

            if (!failedObjects.isEmpty()) {
                log.error("[Failure] Directory deletion incomplete. {} objects failed to delete in '{}': {}",
                        failedObjects.size(), path, failedObjects);
                throw new DirectoryDeletionException(String.format("Not all files in directory '%s' were deleted: %s",
                        path, failedObjects));
            }

            log.debug("[Success] Deleted all {} objects from directory '{}'", objectsToDelete.size(), path);
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error during deletion of directory '{}'", path, ex);
            throw new MinioAccessException(String.format("Unexpected error during deletion of directory '%s'",
                    path), ex);
        }
    }

    private Long getResourceSize(String path) {
        try {
            log.debug("[Start] Fetching size of resource '{}'", path);

            var statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(path)
                    .build());

            var size = statObjectResponse.size();
            log.debug("[Success] Fetched size of resource '{}': size = {}", path, size);
            return size;
        } catch (ErrorResponseException ex) {
            if (MinioUtils.isNoSuchKey(ex)) {
                log.warn("[Warn] Resource '{}' not found in MinIO (NoSuchKey)", path);
                throw new ResourceNotFoundException(String.format("Resource %s not found", path));
            }
            log.error("[Failure] MinIO returned error when retrieving size of '{}'. Error code: {}",
                    path, ex.errorResponse().code(), ex);
            throw new MinioAccessException("MinIO error while getting size", ex);
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error while getting resource size of '{}'", path, ex);
            throw new MinioAccessException(String.format("Unexpected error when determining the file size '%s'",
                    path), ex);
        }
    }
}
