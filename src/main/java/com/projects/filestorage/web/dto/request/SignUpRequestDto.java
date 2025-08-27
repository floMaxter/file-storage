package com.projects.filestorage.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO for user registration request")
public record SignUpRequestDto(

        @Schema(description = "Unique username", example = "Username_10")
        @NotBlank(message = "Username should not be empty")
        @Size(min = 5, max = 20, message = "Username should be more than 5 and less than 20 character")
        String username,

        @Schema(example = "test_password")
        @NotBlank(message = "Password should not be empty")
        @Size(min = 5, max = 100, message = "Password should be more than 5 and less than 20 character")
        String password) {
}
