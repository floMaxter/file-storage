package com.projects.filestorage.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
        @NotBlank(message = "Username should not be empty")
        @Size(max = 50, message = "Username should be less than 50 character")
        String username,
        @NotBlank(message = "Password should not be empty")
        @Size(max = 50, message = "Password should be less than 50 character")
        String password) {
}
