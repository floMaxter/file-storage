package com.projects.filestorage.repository;

import com.projects.filestorage.exception.DirectoryDeletionException;
import com.projects.filestorage.exception.MinioAccessException;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MinioRepository {

    private final MinioClient minioClient;

    public StatObjectResponse getObjectMetadata(String bucket, String path) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
        } catch (Exception ex) {
            log.error("[Failed] Unexpected error while receiving metadata about a resource in bucket='{}', path='{}'. Reason: {}",
                    bucket, path, ex.getMessage());
            throw new MinioAccessException(String.format(
                    "Unexpected error while receiving metadata about a resource on the path '%s'", path));
        }
    }

    public List<String> listRecursiveObjectPaths(String bucket, String path) {
        try {
            var objectItems = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucket)
                    .prefix(path)
                    .recursive(true)
                    .build());

            var objectPaths = new ArrayList<String>();
            for (var objectItem : objectItems) {
                var objectPath = objectItem.get().objectName();
                objectPaths.add(objectPath);
            }

            return objectPaths;
        } catch (Exception ex) {
            log.error("[Failure] Failed to list recursive object paths in bucket='{}', path='{}'. Reason: {}",
                    bucket, path, ex.getMessage());
            throw new MinioAccessException(String.format(
                    "Unexpected error while getting information about a directory on the path '%s'", path));
        }
    }

    public List<String> listDirectObjectPaths(String bucket, String path) {
        try {
            var objectItems = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucket)
                    .prefix(path)
                    .delimiter("/")
                    .recursive(false)
                    .build());

            var objectPaths = new ArrayList<String>();
            for (var objectItem : objectItems) {
                var objectPath = objectItem.get().objectName();

                if (objectPath.equals(path)) continue;

                objectPaths.add(objectPath);
            }

            return objectPaths;
        } catch (Exception ex) {
            log.error("[Failure] Failed to list non-recursive object paths in bucket='{}', path='{}'. Reason: {}",
                    bucket, path, ex.getMessage());
            throw new MinioAccessException(String.format(
                    "Unexpected error while getting information about a directory on the path '%s'", path));
        }
    }

    public GetObjectResponse getObject(String bucket, String path) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error when get a object in bucket='{}', path='{}'. Reason: {}",
                    bucket, path, ex.getMessage());
            throw new MinioAccessException(String.format(
                    "Unexpected error when download a file on the path '%s'", path));
        }
    }

    public Long getResourceSize(String bucket, String path) {
        var statObjectResponse = getObjectMetadata(bucket, path);
        return statObjectResponse.size();
    }

    public ResourceType resolveResourceType(String bucket, String path) {
        if (isFile(bucket, path)) return ResourceType.FILE;
        if (isDirectory(bucket, path)) return ResourceType.DIRECTORY;

        log.warn("[Warn] Resource was not found (not a file or directory) in bucket='{}', path='{}'.", bucket, path);
        throw new ResourceNotFoundException(String.format("The resource on the path '%s' was not found", path));
    }

    public void uploadResource(String bucket, String path, MultipartFile file) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error while loading resource on the path in bucket='{}', path='{}'. Reason: {}",
                    bucket, path, ex.getMessage());
            throw new MinioAccessException(String.format(
                    "Unexpected error while loading resource on the path '%s'", path));
        }
    }

    public void copyResource(String bucket, String sourcePath, String destinationPath) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucket)
                    .object(destinationPath)
                    .source(CopySource.builder()
                            .bucket(bucket)
                            .object(sourcePath)
                            .build())
                    .build());
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error during move file in bucket='{}', from='{}' to='{}'. Reason: {}",
                    bucket, sourcePath, destinationPath, ex.getMessage());
            throw new MinioAccessException(String.format(
                    "Unexpected error during move file from '%s' to '%s'", sourcePath, destinationPath));
        }
    }

    public void putEmptyDirectory(String bucket, String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .build());
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error during creation of an empty directory in bucket='{}', path='{}'. Reason: {}",
                    bucket, path, ex.getMessage());
            throw new MinioAccessException(String.format(
                    "Unexpected error during creation of an empty directory on the path '%s'", path));
        }
    }

    public void deleteResource(String bucket, String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
        } catch (Exception ex) {
            log.error("[Failure] Unexpected exception while deleting file in bucket='{}', path='{}'. Reason: {}",
                    bucket, path, ex.getMessage());
            throw new MinioAccessException(String.format(
                    "Unexpected error occurred while deleting file '%s' from MinIO", path));
        }
    }

    public void deleteResources(String bucket, String prefix) {
        var objectPaths = listRecursiveObjectPaths(bucket, prefix);
        deleteResources(bucket, objectPaths);
    }

    public void deleteResources(String bucket, List<String> objectPaths) {
        try {
            var objectsToDelete = objectPaths.stream()
                    .map(DeleteObject::new)
                    .toList();

            var deletionErrors = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucket)
                    .objects(objectsToDelete)
                    .build());

            var failedObjects = new ArrayList<String>();
            for (var errorResult : deletionErrors) {
                var error = errorResult.get();
                failedObjects.add(error.objectName());
            }

            if (!failedObjects.isEmpty()) {
                log.error("[Failure] Delete some objects in bucket='{}', path='{}'", bucket, failedObjects);
                throw new DirectoryDeletionException(String.format("Failed to delete some objects: %s", failedObjects));
            }
        } catch (Exception ex) {
            log.error("[Failure] Unexpected error while deleting resources", ex);
            throw new MinioAccessException("Unexpected error while deleting resources");
        }
    }

    public void ensureDirectoryPlaceholder(String bucket, String path) {
        if (!isDirectoryExists(bucket, path)) {
            putEmptyDirectory(bucket, path);
        }
    }

    public boolean isFile(String bucket, String path) {
        if (!MinioUtils.isPathFileLike(path)) {
            return false;
        }

        return isFileExists(bucket, path);
    }

    public boolean isDirectory(String bucket, String path) {
        if (!MinioUtils.isPathDirectoryLike(path)) {
            return false;
        }

        return isDirectoryExists(bucket, path);
    }

    public boolean isFileExists(String bucket, String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
            return true;
        } catch (ErrorResponseException ex) {
            if (MinioUtils.isNoSuchKey(ex)) {
                return false;
            }
            throw new MinioAccessException(String.format("MinIO error when checking for file at path '%s'. Error code: %s",
                    path, ex.errorResponse().code()));
        } catch (Exception ex) {
            log.error("[Failed] Unexpected error occurred while checking if path is a file in bucket='{}', path='{}'. Reason: {}",
                    bucket, path, ex.getMessage());
            throw new MinioAccessException(String.format("Unexpected error while verifying file at path '%s'", path));
        }
    }

    public boolean isDirectoryExists(String bucket, String path) {
        try {
            var objectItems = listRecursiveObjectPaths(bucket, path);
            return objectItems.iterator().hasNext();
        } catch (Exception ex) {
            log.error("[Failed] Unexpected error occurred while checking if directory exists in bucket='{}', path='{}'. Reason: {}",
                    bucket, path, ex.getMessage());
            throw new MinioAccessException(String.format(
                    "Unexpected error occurred while checking if directory '%s' exists", path));
        }
    }
}
