package com.projects.filestorage.service.handler;

import com.projects.filestorage.web.dto.internal.CopyResourceDto;
import com.projects.filestorage.web.dto.internal.ResourceContextDto;
import com.projects.filestorage.web.dto.internal.ResourceDownloadDto;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;

public interface MinioResourceHandler {

    ResourceType getSupportedType();

    ResourceInfoResponseDto getResourceInfo(ResourceContextDto resourceContextDto);

    void copyResource(CopyResourceDto copyResourceDto);

    void deleteResource(ResourceContextDto resourceContextDto);

    ResourceDownloadDto downloadResource(ResourceContextDto resourceContextDto);
}
