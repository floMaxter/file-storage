package com.projects.filestorage.testdata.data.dto;

import com.projects.filestorage.web.dto.internal.enums.ResourceType;

public record UploadedTestResource(
        String parentPath,
        String name,
        Long size,
        ResourceType resourceType) {
}
