package com.projects.filestorage.web.dto.internal;

import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import lombok.Builder;

@Builder
public record ResourceContextDto(String bucket,
                                 String absolutePath,
                                 String relativePath,
                                 ResourceType resourceType) {
}
