package com.projects.filestorage.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representing a user")
public record UserDto(

        @Schema(description = "Unique username", example = "Username_10")
        String username) {
}
