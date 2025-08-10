package com.projects.filestorage.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceInfoResponseDto(
        String path,
        String name,
        Long size,
        @JsonProperty("type") ResourceType resourceType) {
}
