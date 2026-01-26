package com.borsibaar.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String ERROR_TYPE_BASE_URI = "https://api.borsibaar.com/errors/";

    private ProblemDetail buildProblemDetail(HttpStatus status,
                                              String title,
                                              BorsibaarBusinessException exception,
                                              String path) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(exception.getMessage());
        problemDetail.setType(URI.create(ERROR_TYPE_BASE_URI + exception.getCode().toLowerCase()));
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        problemDetail.setProperty("errorCode", exception.getCode());
        problemDetail.setProperty("path", path);

        if (exception.getParameters() != null && exception.getParameters().length > 0) {
            problemDetail.setProperty("parameters", exception.getParameters());
        }
        
        return problemDetail;
    }

    private ProblemDetail buildProblemDetail(HttpStatus status,
                                              String title,
                                              String detail,
                                              String path,
                                              String errorCode) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setType(URI.create(ERROR_TYPE_BASE_URI + errorCode.toLowerCase()));
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        problemDetail.setProperty("errorCode", errorCode);
        problemDetail.setProperty("path", path);
        return problemDetail;
    }

    @Hidden
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", exception, request.getRequestURI());
    }

    @Hidden
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicate(DuplicateResourceException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Duplicate Resource", exception, request.getRequestURI());
    }

    @Hidden
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBadRequest(BadRequestException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Bad Request", exception, request.getRequestURI());
    }

    @Hidden
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail handleForbidden(ForbiddenException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.FORBIDDEN, "Access Denied", exception, request.getRequestURI());
    }

    @Hidden
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleUnauthorized(UnauthorizedException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.UNAUTHORIZED, "Unauthorized", exception, request.getRequestURI());
    }

    @Hidden
    @ExceptionHandler(GoneException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ProblemDetail handleGone(GoneException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.GONE, "Resource Deleted", exception, request.getRequestURI());
    }

    @Hidden
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(ValidationException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation Failed", exception, request.getRequestURI());
    }

    @Hidden
    @ExceptionHandler(BorsibaarServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleServerException(BorsibaarServerException exception, HttpServletRequest request) {
        log.error("Server error at {}: {}", request.getRequestURI(), exception.getMessage(), exception);
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", 
                exception, request.getRequestURI());
    }

    @Hidden
    @ExceptionHandler(BorsibaarBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleBusinessException(BorsibaarBusinessException exception, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Business Error", exception, request.getRequestURI());
    }

    @Hidden
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
                                                      HttpServletRequest request) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                detail,
                request.getRequestURI(),
                FaultReason.VALIDATION_ERROR.toString());

        Map<String, String> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() != null 
                                ? fieldError.getDefaultMessage() 
                                : "Invalid value",
                        (first, second) -> first));
        
        problemDetail.setProperty("errors", fieldErrors);

        return problemDetail;
    }

    @Hidden
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception,
                                                   HttpServletRequest request) {
        String detail = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Constraint Violation",
                detail,
                request.getRequestURI(),
                FaultReason.CONSTRAINT_VIOLATION.toString());
    }

    @Hidden
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        return buildProblemDetail(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "You do not have permission to access this resource",
                request.getRequestURI(),
                FaultReason.FORBIDDEN.toString());
    }

    @Hidden
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleAuthenticationException(AuthenticationException exception,
                                                       HttpServletRequest request) {
        return buildProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication Failed",
                exception.getMessage(),
                request.getRequestURI(),
                FaultReason.UNAUTHORIZED.toString());
    }


    @Hidden
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException exception,
                                             HttpServletRequest request) {
        log.warn("Data integrity violation at {}: {}", request.getRequestURI(), exception.getMessage());
        return buildProblemDetail(
                HttpStatus.CONFLICT,
                "Data Integrity Violation",
                "Resource already exists or constraint violated",
                request.getRequestURI(),
                FaultReason.DATA_INTEGRITY_VIOLATION.toString());
    }


    @Hidden
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setDetail(exception.getReason());
        problemDetail.setType(URI.create(ERROR_TYPE_BASE_URI + status.name().toLowerCase()));
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        problemDetail.setProperty("errorCode", status.name());
        problemDetail.setProperty("path", request.getRequestURI());
        return problemDetail;
    }

    @Hidden
    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponse(ErrorResponseException exception, HttpServletRequest request) {
        ProblemDetail problemDetail = exception.getBody();
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        problemDetail.setProperty("path", request.getRequestURI());
        return problemDetail;
    }


    @Hidden
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleOther(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), exception.getMessage(), exception);

        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                FaultReason.INTERNAL_SERVER_ERROR.toString());
    }
}
