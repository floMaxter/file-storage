package com.projects.filestorage.service.handler.impl;

import com.projects.filestorage.repository.MinioRepository;
import com.projects.filestorage.service.handler.MinioResourceHandler;
import com.projects.filestorage.utils.MinioUtils;
import com.projects.filestorage.web.dto.internal.CopyResourceDto;
import com.projects.filestorage.web.dto.internal.ResourceContextDto;
import com.projects.filestorage.web.dto.internal.ResourceDownloadDto;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;
import com.projects.filestorage.web.mapper.ResourceInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileResourceHandler implements MinioResourceHandler {

    private final MinioRepository minioRepository;
    private final ResourceInfoMapper resourceInfoMapper;

    @Override
    public ResourceType getSupportedType() {
        return ResourceType.FILE;
    }

    @Override
    public ResourceInfoResponseDto getResourceInfo(ResourceContextDto resourceContextDto) {
        var relativePath = MinioUtils.extractParentPath(resourceContextDto.relativePath());
        var resourceName = MinioUtils.extractResourceName(resourceContextDto.relativePath());
        var size = minioRepository.getResourceSize(resourceContextDto.bucket(), resourceContextDto.absolutePath());
        var resourceType = resourceContextDto.resourceType();

        return resourceInfoMapper.toResourceInfo(relativePath, resourceName, size, resourceType);
    }

    @Override
    public void copyResource(CopyResourceDto copyResourceDto) {
        minioRepository.copyResource(
                copyResourceDto.sourceContext().bucket(),
                copyResourceDto.sourceContext().absolutePath(),
                copyResourceDto.absoluteDestinationPath()
        );
    }

    @Override
    public void deleteResource(ResourceContextDto resourceContextDto) {
        minioRepository.deleteResource(resourceContextDto.bucket(), resourceContextDto.absolutePath());
    }

    @Override
    public ResourceDownloadDto downloadResource(ResourceContextDto resourceContextDto) {
        var object = minioRepository.getObject(resourceContextDto.bucket(), resourceContextDto.absolutePath());
        var resourceName = MinioUtils.extractResourceName(resourceContextDto.absolutePath());

        return ResourceDownloadDto.builder()
                .fileName(resourceName)
                .responseBody(object::transferTo)
                .build();
    }
}