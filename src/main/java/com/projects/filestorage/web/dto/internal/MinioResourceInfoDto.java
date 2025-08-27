package com.projects.filestorage.web.dto.internal;

import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import lombok.Builder;

@Builder
public record MinioResourceInfoDto(
        String path,
        String name,
        Long size,
        ResourceType resourceType) {
}
