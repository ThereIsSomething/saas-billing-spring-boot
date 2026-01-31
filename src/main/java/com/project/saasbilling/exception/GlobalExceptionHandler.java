package com.project.saasbilling.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Global exception handler for REST API.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(ApiException.class)
        public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
                log.error("API Exception: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(ex.getStatus().value())
                                .error(ex.getStatus().getReasonPhrase())
                                .code(ex.getCode())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(error, ex.getStatus());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                log.warn("Validation error on {}: {}", request.getRequestURI(),
                                ex.getBindingResult().getFieldErrors().stream()
                                                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                                .collect(Collectors.joining(", ")));

                List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(this::mapFieldError)
                                .collect(Collectors.toList());

                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Validation Failed")
                                .code("VALIDATION_ERROR")
                                .message("One or more fields have validation errors")
                                .path(request.getRequestURI())
                                .validationErrors(validationErrors)
                                .build();

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex, HttpServletRequest request) {

                log.warn("JSON parsing error on {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .code("INVALID_JSON")
                                .message("Invalid request body: " + ex.getMostSpecificCause().getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex, HttpServletRequest request) {

                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .code("INVALID_CREDENTIALS")
                                .message("Invalid email or password")
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        AuthenticationException ex, HttpServletRequest request) {

                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .code("AUTHENTICATION_FAILED")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(
                        AccessDeniedException ex, HttpServletRequest request) {

                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .code("ACCESS_DENIED")
                                .message("You don't have permission to access this resource")
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
                        MaxUploadSizeExceededException ex, HttpServletRequest request) {

                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
                                .error("Payload Too Large")
                                .code("FILE_TOO_LARGE")
                                .message("File size exceeds the maximum allowed limit")
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {

                log.error("Unexpected error occurred: ", ex);

                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error("Internal Server Error")
                                .code("INTERNAL_ERROR")
                                .message("An unexpected error occurred. Please try again later.")
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private ErrorResponse.ValidationError mapFieldError(FieldError fieldError) {
                return ErrorResponse.ValidationError.builder()
                                .field(fieldError.getField())
                                .message(fieldError.getDefaultMessage())
                                .rejectedValue(fieldError.getRejectedValue())
                                .build();
        }
}
