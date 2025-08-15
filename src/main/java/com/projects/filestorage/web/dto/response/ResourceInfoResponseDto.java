package com.projects.filestorage.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Information about a resource (file or directory) in the file system")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceInfoResponseDto(

        @Schema(description = "Path to the parent path", example = "/home/user/documents/")
        @JsonProperty("path")
        String parentPath,

        @Schema(description = "Name of the resource", example = "report.pdf")
        String name,

        @Schema(description = "Size of resource in bytes. Missing for directories", example = "11")
        Long size,

        @Schema(description = "Type of the resource (e.g., FILE or DIRECTORY", example = "FILE")
        @JsonProperty("type")
        ResourceType resourceType) {
}
