package com.projects.filestorage.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for user registration response")
public record SignUpResponseDto(

        @Schema(description = "Unique username", example = "Username_10")
        String username) {
}
