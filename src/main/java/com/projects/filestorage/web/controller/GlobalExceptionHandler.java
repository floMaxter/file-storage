package com.projects.filestorage.web.controller;

import com.projects.filestorage.exception.InvalidResourcePathFormatException;
import com.projects.filestorage.exception.ResourceNotFoundException;
import com.projects.filestorage.exception.UnauthenticatedAccessException;
import com.projects.filestorage.exception.UserAlreadyExistsException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return buildValidationErrorResponse(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        var message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        return buildValidationErrorResponse(message);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.warn("Registration conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(409)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Authorization error: {}", ex.getMessage());
        return ResponseEntity
                .status(401)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UnauthenticatedAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthenticatedAccessException(UnauthenticatedAccessException ex) {
        log.warn("Unauthenticated access: {}", ex.getMessage());
        return ResponseEntity
                .status(401)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFoundException(NoResourceFoundException ex) {
        return handleNotFoundException(ex.getMessage());
    }

    @ExceptionHandler(InvalidResourcePathFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidResourcePathFormatException(
            InvalidResourcePathFormatException ex) {
        return buildValidationErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return handleNotFoundException(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage());
        return ResponseEntity
                .internalServerError()
                .body(Map.of("message", "Internal error"));
    }

    private ResponseEntity<Map<String, String>> handleNotFoundException(String message) {
        log.warn("Resource not fount: {}", message);
        return ResponseEntity
                .status(404)
                .body(Map.of("message", message));
    }

    private ResponseEntity<Map<String, String>> buildValidationErrorResponse(String message) {
        log.warn("Validation failed: {}", message);
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", message));
    }
}
