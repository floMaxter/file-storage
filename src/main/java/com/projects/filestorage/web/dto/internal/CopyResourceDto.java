package com.projects.filestorage.web.dto.internal;

public record CopyResourceDto(ResourceContextDto sourceContext,
                              String absoluteDestinationPath) {
}
