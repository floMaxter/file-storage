package com.projects.filestorage.web.dto.internal;

import lombok.Builder;

@Builder
public record UserPathContextDto(String userRootDirectory,
                                 String absolutePath) {
}
