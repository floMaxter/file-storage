package com.projects.filestorage.service;

import com.projects.filestorage.config.MinioClientProperties;
import com.projects.filestorage.domain.enums.ResourceType;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.response.ResourceInfoDto;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioClientService {

    private final MinioClient minioClient;
    private final MinioClientProperties minioClientProperties;

    @SneakyThrows
    public ResourceInfoDto getResourceInfo(String path) {
        var pathToResource = MinioUtils.extractPath(path);
        var resourceName = MinioUtils.extractResourceName(path);
        var resourceType = MinioUtils.isFile(path) ? ResourceType.FILE : ResourceType.DIRECTORY;

        Long size = null;
        if (resourceType == ResourceType.FILE) {
            size = getResourceSize(path);
        }

        return ResourceInfoDto.builder()
                .path(pathToResource)
                .name(resourceName)
                .size(size)
                .resourceType(resourceType)
                .build();
    }

    @SneakyThrows
    private Long getResourceSize(String path) {
        try {
            var statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioClientProperties.getBucketName())
                    .object(path)
                    .build());
            return statObjectResponse.size();
        } catch (ErrorResponseException ex) {
            if (ex.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException(String.format("Resource %s not found", path));
            }
            throw new RuntimeException("Unexpected error", ex);
        }
    }
}
