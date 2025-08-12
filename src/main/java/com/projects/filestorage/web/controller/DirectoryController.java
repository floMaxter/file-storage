package com.projects.filestorage.web.controller;

import com.projects.filestorage.service.UserFileService;
import com.projects.filestorage.validation.ResourcePathValidator;
import com.projects.filestorage.web.dto.response.ErrorResponseDto;
import com.projects.filestorage.web.dto.response.ResourceInfoResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "Directories",
        description = "Operations for working with directories"
)
@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final UserFileService userFileService;
    private final ResourcePathValidator resourcePathValidator;

    @Operation(
            summary = "Get directory content",
            description = "Provides detailed information about the files and folders inside the specified directory path",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful receipt of directory information",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ResourceInfoResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation path exception",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized request",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The directory does not exist",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unknown error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ResourceInfoResponseDto> getDirectoryInfo(@RequestParam("path")
                                                          @Parameter(example = "path/to/dir/", allowEmptyValue = true)
                                                          String path) {
        resourcePathValidator.validateDirectoryPathFormat(path);
        return userFileService.getDirectoryInfo(path);
    }

    @Operation(
            summary = "Create empty directory",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successful creation of an empty directory",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ResourceInfoResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation path exception",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized request",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The parent path does not exist",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The directory already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unknown error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceInfoResponseDto> createEmptyDirectory(@RequestParam("path")
                                                              @Parameter(example = "path/to/dir/")
                                                              String path) {
        resourcePathValidator.validateDirectoryPathFormat(path);
        return userFileService.createEmptyDirectory(path);
    }
}
