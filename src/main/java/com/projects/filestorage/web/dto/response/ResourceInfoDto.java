package com.projects.filestorage.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.projects.filestorage.domain.enums.ResourceType;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceInfoDto(
        String path,
        String name,
        Long size,
        ResourceType resourceType) {
}
