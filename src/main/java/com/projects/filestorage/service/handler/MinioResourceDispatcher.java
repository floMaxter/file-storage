package com.projects.filestorage.service.handler;

import com.projects.filestorage.exception.MinioResourceHandlerNotFound;
import com.projects.filestorage.web.dto.internal.CopyResourceDto;
import com.projects.filestorage.web.dto.internal.ResourceContextDto;
import com.projects.filestorage.web.dto.internal.ResourceDownloadDto;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class MinioResourceDispatcher {

    private final Map<ResourceType, MinioResourceHandler> handlers;

    public ResourceInfoResponseDto getResourceInfo(ResourceContextDto resourceContextDto) {
        var minioResourceHandler = getMinioResourceHandlerOrElseThrow(resourceContextDto.resourceType());
        return minioResourceHandler.getResourceInfo(resourceContextDto);
    }

    public void copyResource(CopyResourceDto copyResourceDto) {
        var sourceContextDto = copyResourceDto.sourceContext();
        var minioResourceHandler = getMinioResourceHandlerOrElseThrow(sourceContextDto.resourceType());
        minioResourceHandler.copyResource(copyResourceDto);
    }

    public void deleteResource(ResourceContextDto resourceContextDto) {
        var minioResourceHandler = getMinioResourceHandlerOrElseThrow(resourceContextDto.resourceType());
        minioResourceHandler.deleteResource(resourceContextDto);
    }

    public ResourceDownloadDto downloadResource(ResourceContextDto resourceContextDto) {
        var minioResourceHandler = getMinioResourceHandlerOrElseThrow(resourceContextDto.resourceType());
        return minioResourceHandler.downloadResource(resourceContextDto);
    }

    private MinioResourceHandler getMinioResourceHandlerOrElseThrow(ResourceType resourceType) {
        return Optional.ofNullable(handlers.get(resourceType))
                .orElseThrow(() -> new MinioResourceHandlerNotFound(String.format("No handler found for %s", resourceType)));
    }
}
