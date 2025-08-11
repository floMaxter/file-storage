package com.projects.filestorage.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserDto(

        @Schema(description = "Unique username", example = "Username_10")
        String username) {
}
