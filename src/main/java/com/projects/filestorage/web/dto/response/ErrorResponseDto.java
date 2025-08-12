package com.projects.filestorage.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for error response")
public record ErrorResponseDto(

        @Schema(description = "Detailed error message")
        String message
) {
}
