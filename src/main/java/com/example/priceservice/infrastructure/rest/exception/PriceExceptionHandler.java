package com.example.priceservice.infrastructure.rest.exception;

import com.example.priceservice.application.exception.PriceNotFoundException;
import com.example.priceservice.domain.exception.AmbiguousPriceSelectionException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class PriceExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PriceExceptionHandler.class);

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Required parameter is missing: " + exception.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleInvalidParameter(MethodArgumentTypeMismatchException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + exception.getName());
    }

    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ProblemDetail handleValidation(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Product and brand identifiers must be positive");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMethod(HttpRequestMethodNotSupportedException exception) {
        return new ResponseEntity<>(
                problem(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method is not supported for this endpoint"),
                exception.getHeaders(),
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ProblemDetail handleUnsupportedResponseType(HttpMediaTypeNotAcceptableException exception) {
        return problem(HttpStatus.NOT_ACCEPTABLE, "Requested response media type is not supported");
    }

    @ExceptionHandler(PriceNotFoundException.class)
    public ProblemDetail handlePriceNotFound(PriceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ProblemDetail handleResourceNotFound(Exception exception) {
        return problem(HttpStatus.NOT_FOUND, "No resource was found for the requested path");
    }

    @ExceptionHandler(AmbiguousPriceSelectionException.class)
    public ProblemDetail handleAmbiguousPrice(AmbiguousPriceSelectionException exception) {
        logger.warn("Ambiguous applicable price selection: {}", exception.getMessage());
        return problem(HttpStatus.CONFLICT, "Multiple applicable prices share the highest priority");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        logger.error("Unexpected error while processing a price request", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
