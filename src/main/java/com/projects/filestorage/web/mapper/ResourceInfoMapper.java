package com.projects.filestorage.web.mapper;

import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ResourceInfoMapper {

    public ResourceInfoResponseDto toResourceInfo(String relativePath,
                                                  String name,
                                                  Long size,
                                                  ResourceType resourceType) {
        return ResourceInfoResponseDto.builder()
                .path(relativePath)
                .name(name)
                .size(size)
                .resourceType(resourceType)
                .build();
    }
}
