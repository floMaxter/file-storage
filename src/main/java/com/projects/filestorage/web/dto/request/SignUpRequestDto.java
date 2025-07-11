package com.projects.filestorage.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
        @NotBlank(message = "Username should not be empty")
        @Size(min = 5, max = 20, message = "Username should be more than 5 and less than 20 character")
        String username,
        @NotBlank(message = "Password should not be empty")
        @Size(min = 5, max = 100, message = "Password should be more than 5 and less than 20 character")
        String password) {
}
