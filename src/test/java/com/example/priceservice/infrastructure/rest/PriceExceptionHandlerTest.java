package com.example.priceservice.infrastructure.rest;

import com.example.priceservice.infrastructure.rest.exception.PriceExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PriceExceptionHandlerTest {

    @Test
    void shouldHideUnexpectedErrorDetails() {
        ProblemDetail problem = new PriceExceptionHandler()
                .handleUnexpected(new IllegalStateException("internal database details"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Internal Server Error", problem.getTitle());
        assertEquals("An unexpected error occurred", problem.getDetail());
        assertFalse(problem.getDetail().contains("database"));
    }
}
