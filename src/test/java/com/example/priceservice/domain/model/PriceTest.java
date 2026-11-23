package com.example.priceservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceTest {

    private static final LocalDateTime START = LocalDateTime.of(2020, 6, 14, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

    @Test
    void shouldBeValidWithinDateRange() {
        assertTrue(price().isValidAt(LocalDateTime.of(2020, 6, 14, 10, 0)));
    }

    @Test
    void shouldIncludeStartDate() {
        assertTrue(price().isValidAt(START));
    }

    @Test
    void shouldIncludeEndDate() {
        assertTrue(price().isValidAt(END));
    }

    @Test
    void shouldNotBeValidBeforeStartDate() {
        assertFalse(price().isValidAt(START.minusNanos(1)));
    }

    @Test
    void shouldNotBeValidAfterEndDate() {
        assertFalse(price().isValidAt(END.plusNanos(1)));
    }

    @Test
    void shouldNotMatchAnotherProduct() {
        assertFalse(price().matches(99999, 1));
    }

    @Test
    void shouldNotMatchAnotherBrand() {
        assertFalse(price().matches(35455, 2));
    }

    @Test
    void shouldRejectInvalidIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> price(0, 35455, 1, START, END, 0, "35.50"));
        assertThrows(IllegalArgumentException.class, () -> price(1, 0, 1, START, END, 0, "35.50"));
        assertThrows(IllegalArgumentException.class, () -> price(1, 35455, 0, START, END, 0, "35.50"));
    }

    @Test
    void shouldRejectInvalidDateRange() {
        assertThrows(IllegalArgumentException.class,
                () -> price(1, 35455, 1, END, START, 0, "35.50"));
    }

    @Test
    void shouldRejectNegativePriorityAndAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> price(1, 35455, 1, START, END, -1, "35.50"));
        assertThrows(IllegalArgumentException.class,
                () -> price(1, 35455, 1, START, END, 0, "-0.01"));
    }

    @Test
    void shouldRejectMissingRequiredValues() {
        assertThrows(NullPointerException.class,
                () -> new Price(1, 35455, 1, null, END, 0, new BigDecimal("35.50"), Currency.getInstance("EUR")));
        assertThrows(NullPointerException.class,
                () -> new Price(1, 35455, 1, START, null, 0, new BigDecimal("35.50"), Currency.getInstance("EUR")));
        assertThrows(NullPointerException.class,
                () -> new Price(1, 35455, 1, START, END, 0, null, Currency.getInstance("EUR")));
        assertThrows(NullPointerException.class,
                () -> new Price(1, 35455, 1, START, END, 0, new BigDecimal("35.50"), null));
    }

    private Price price() {
        return price(1, 35455, 1, START, END, 0, "35.50");
    }

    private Price price(long brandId, long productId, long priceListId, LocalDateTime startDate,
                        LocalDateTime endDate, int priority, String amount) {
        return new Price(brandId, productId, priceListId, startDate, endDate, priority,
                new BigDecimal(amount), Currency.getInstance("EUR"));
    }
}
