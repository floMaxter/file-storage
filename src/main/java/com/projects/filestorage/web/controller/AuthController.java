package com.projects.filestorage.web.controller;

import com.projects.filestorage.service.AuthService;
import com.projects.filestorage.web.dto.request.SignInRequestDto;
import com.projects.filestorage.web.dto.request.SignUpRequestDto;
import com.projects.filestorage.web.dto.response.ErrorResponseDto;
import com.projects.filestorage.web.dto.response.SignInResponseDto;
import com.projects.filestorage.web.dto.response.SignUpResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Authentication",
        description = "Registers, authorizes and logs the user out of the application"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User registration")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successful user registration",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignUpResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation exception",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Non-unique username",
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
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public SignUpResponseDto signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto,
                                    @Parameter(hidden = true) HttpServletRequest request,
                                    @Parameter(hidden = true) HttpServletResponse response) {
        return authService.signUp(signUpRequestDto, request, response);
    }

    @Operation(summary = "User authorization")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful user authorization",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignInResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation exception",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid authorization data",
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
    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.OK)
    public SignInResponseDto signIn(@Valid @RequestBody SignInRequestDto signInRequestDto,
                                    @Parameter(hidden = true) HttpServletRequest request,
                                    @Parameter(hidden = true) HttpServletResponse response) {
        return authService.signIn(signInRequestDto, request, response);
    }

    @Operation(summary = "User logout")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successful user logout",
                    content = @Content(schema = @Schema(hidden = true))),
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
    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut(@Parameter(hidden = true) HttpServletRequest request,
                        @Parameter(hidden = true) HttpServletResponse response) {
        authService.signOut(request, response);
    }
}
