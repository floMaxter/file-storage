package com.projects.filestorage.web.mapper;

import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ResourceInfoMapper {

    public ResourceInfoResponseDto toResourceInfo(String parentPath,
                                                  String name,
                                                  Long size,
                                                  ResourceType resourceType) {
        return ResourceInfoResponseDto.builder()
                .parentPath(parentPath)
                .name(name)
                .size(size)
                .resourceType(resourceType)
                .build();
    }
}
