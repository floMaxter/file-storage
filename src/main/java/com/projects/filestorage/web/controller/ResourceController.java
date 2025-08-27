package com.projects.filestorage.web.controller;

import com.projects.filestorage.security.CustomUserDetails;
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
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(
        name = "Resources",
        description = "Operations for working with resources"
)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceController {

    private final UserFileService userFileService;
    private final ResourcePathValidator resourcePathValidator;

    @Operation(
            summary = "Get resource info",
            description = "Returns detailed information about the resource (file or folder) at the specified path",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful receipt of resource information",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResourceInfoResponseDto.class)
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
                    description = "The resource does not found",
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
    @GetMapping("/resource")
    @ResponseStatus(HttpStatus.OK)
    public ResourceInfoResponseDto getResourceInfo(@RequestParam("path")
                                                   @Parameter(example = "home/resource.txt", allowEmptyValue = true)
                                                   String path,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        resourcePathValidator.validatePathFormat(path);
        return userFileService.getResourceInfo(userDetails.getId(), path);
    }

    @Operation(
            summary = "Delete resource",
            description = "Delete a resource (file or directory) at the specified path",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successful resource deletion",
                    content = @Content(schema = @Schema(hidden = true))),
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
                    description = "The resource does not found",
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
    @DeleteMapping("/resource")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@RequestParam("path")
                               @Parameter(example = "home/resource.txt", allowEmptyValue = true)
                               String path,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        resourcePathValidator.validatePathFormat(path);
        userFileService.deleteResource(userDetails.getId(), path);
    }

    @Operation(
            summary = "Download resource",
            description = "Download a resource (file or directory) at the specified path",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful resource download",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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
                    description = "The resource does not found",
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
    @GetMapping("/resource/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam("path")
                                                                  @Parameter(example = "home/resource.txt", allowEmptyValue = true)
                                                                  String path,
                                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        resourcePathValidator.validatePathFormat(path);

        var resourceDownloadDto = userFileService.downloadResource(userDetails.getId(), path);
        var contentDisposition = ContentDisposition.attachment()
                .filename(resourceDownloadDto.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resourceDownloadDto.responseBody());
    }

    @Operation(
            summary = "Move resource",
            description = "Move a resource (file or directory) from the source path to the destination path",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful resource transfer",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
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
                    description = "The resource does not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The resource on the destination path already exists",
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
    @GetMapping("/resource/move")
    @ResponseStatus(HttpStatus.OK)
    public ResourceInfoResponseDto moveResource(@RequestParam("from")
                                                @Parameter(example = "folder1/resource.txt", allowEmptyValue = true)
                                                String sourcePath,

                                                @RequestParam("to")
                                                @Parameter(example = "folder2/resource.txt", allowEmptyValue = true)
                                                String destinationPath,

                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        resourcePathValidator.validateMovePathsFormat(sourcePath, destinationPath);
        return userFileService.moveResource(userDetails.getId(), sourcePath, destinationPath);
    }

    @Operation(
            summary = "Search for resources by query",
            description = "Finds all resources (files and directories) whose name matches the specified query",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully found resources",
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
                    responseCode = "500",
                    description = "Unknown error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/resource/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ResourceInfoResponseDto> searchResources(@RequestParam("query")
                                                         @Parameter(example = "folder1/resource", allowEmptyValue = true)
                                                         String query,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        resourcePathValidator.validateSearchQueryFormat(query);
        return userFileService.searchResources(userDetails.getId(), query);
    }

    @Operation(
            summary = "Upload resources",
            description = "Uploads multiple files to the given directory path",
            security = @SecurityRequirement(name = "sessionAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Resources uploaded successfully",
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
                    responseCode = "409",
                    description = "The resource on the destination path already exists",
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
    @PostMapping(value = "/resource", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceInfoResponseDto> uploadResource(@RequestParam("path")
                                                        @Parameter(example = "folder1/", allowEmptyValue = true)
                                                        String path,

                                                        @AuthenticationPrincipal CustomUserDetails userDetails,

                                                        @RequestPart("object")
                                                        @Parameter(
                                                                description = "List of files to upload. Example: select multiple files in the form-data field named 'object'"
                                                        ) List<MultipartFile> objects) {
        resourcePathValidator.validateUploadResourcesFormat(path, objects);
        return userFileService.uploadResources(userDetails.getId(), path, objects);
    }
}
