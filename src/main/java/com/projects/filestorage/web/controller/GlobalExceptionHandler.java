package com.projects.filestorage.web.controller;

import com.projects.filestorage.exception.DirectoryDeletionException;
import com.projects.filestorage.exception.DirectoryNotFoundException;
import com.projects.filestorage.exception.InvalidMultipartFileException;
import com.projects.filestorage.exception.InvalidResourcePathFormatException;
import com.projects.filestorage.exception.InvalidSearchQueryFormatException;
import com.projects.filestorage.exception.MinioAccessException;
import com.projects.filestorage.exception.MinioResourceHandlerNotFound;
import com.projects.filestorage.exception.ResourceAlreadyExistsException;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.exception.UnauthenticatedAccessException;
import com.projects.filestorage.exception.UserAlreadyExistsException;
import com.projects.filestorage.exception.UserNotFoundException;
import com.projects.filestorage.exception.UserRoleNotFoundException;
import com.projects.filestorage.web.dto.response.ErrorResponseDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("[Handle] Validation error (MethodArgumentNotValidException): {}", ex.getMessage());
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return buildValidationErrorResponse(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("[Handle] Validation error (ConstraintViolationException): {}", ex.getMessage());
        var message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        return buildValidationErrorResponse(message);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.warn("[Handle] Registration conflict (UserAlreadyExistsException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("[Handle] Authorization error (BadCredentialsException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(UnauthenticatedAccessException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthenticatedAccessException(UnauthenticatedAccessException ex) {
        log.warn("[Handle] Unauthenticated access (UnauthenticatedAccessException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(UserRoleNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserRoleNotFoundException(UserRoleNotFoundException ex) {
        log.warn("[Handle] User role not found (UserRoleNotFoundException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("[Handle] User not found (UserNotFoundException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("[Handle] Resource not found (NoResourceFoundException): {}", ex.getMessage());
        return handleNotFoundException(ex.getMessage());
    }

    @ExceptionHandler(InvalidResourcePathFormatException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidResourcePathFormatException(
            InvalidResourcePathFormatException ex) {
        log.warn("[Handle] Validation error (InvalidResourcePathFormatException): {}", ex.getMessage());
        return buildValidationErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("[Handle] Resource not found (ResourceNotFoundException): {}", ex.getMessage());
        return handleNotFoundException(ex.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        log.warn("[Handle] Resource conflict (ResourceAlreadyExistsException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(InvalidSearchQueryFormatException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidSearchQueryFormatException(
            InvalidSearchQueryFormatException ex) {
        log.warn("[Handle] Invalid search query format (InvalidSearchQueryFormatException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(InvalidMultipartFileException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidMultipartFileException(InvalidMultipartFileException ex) {
        log.warn("[Handle] Invalid multipart file (InvalidMultipartFileException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("[Handle] The file size is too large when uploading (MaxUploadSizeExceededException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(DirectoryNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleDirectoryNotFoundException(DirectoryNotFoundException ex) {
        log.warn("[Handle] Directory not found (DirectoryNotFoundException): {}", ex.getMessage());
        return handleNotFoundException(ex.getMessage());
    }

    @ExceptionHandler(DirectoryDeletionException.class)
    public ResponseEntity<ErrorResponseDto> handleDirectoryDeletionException(DirectoryDeletionException ex) {
        log.warn("[Handle] Error when deleting a directory (DirectoryDeletionException): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(MinioAccessException.class)
    public ResponseEntity<ErrorResponseDto> handleMinioAccessException(MinioAccessException ex) {
        log.warn("[Handle] Error when working with MinIO: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(MinioResourceHandlerNotFound.class)
    public ResponseEntity<ErrorResponseDto> handleMinioResourceHandlerNotFound(MinioResourceHandlerNotFound ex) {
        log.warn("[Handle] Error when searching for a handler for request to a minio: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        log.error("[Handle] Unexpected error (Exception): {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("Internal error"));
    }

    private ResponseEntity<ErrorResponseDto> handleNotFoundException(String message) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(message));
    }

    private ResponseEntity<ErrorResponseDto> buildValidationErrorResponse(String message) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(message));
    }
}
