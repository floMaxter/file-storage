package com.projects.filestorage.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.projects.filestorage.web.dto.internal.enums.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Information about a resource (file or directory) in the fule system")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceInfoResponseDto(

        @Schema(description = "Absolute path to the resource", example = "/home/user/documents/report.pdf")
        String path,

        @Schema(description = "Name of the resource", example = "report.pdf")
        String name,

        @Schema(description = "Size of resource in bytes. Missing for directories", example = "11")
        Long size,

        @Schema(description = "Type of the resource (e.g., FILE or DIRECTORY", example = "FILE")
        @JsonProperty("type")
        ResourceType resourceType) {
}
