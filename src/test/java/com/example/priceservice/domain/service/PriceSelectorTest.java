package com.example.priceservice.domain.service;

import com.example.priceservice.domain.exception.AmbiguousPriceSelectionException;
import com.example.priceservice.domain.model.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceSelectorTest {

    private final PriceSelector selector = new PriceSelector();

    @Test
    void shouldSelectHighestPriorityApplicablePrice() {
        Optional<Price> result = selector.selectApplicablePrice(
                exercisePrices(), LocalDateTime.of(2020, 6, 14, 16, 0), 35455, 1);

        assertEquals(2, result.orElseThrow().priceListId());
    }

    @Test
    void shouldSelectOnlyApplicablePrice() {
        Price candidate = exercisePrices().getFirst();

        Optional<Price> result = selector.selectApplicablePrice(
                List.of(candidate), LocalDateTime.of(2020, 6, 14, 10, 0), 35455, 1);

        assertEquals(candidate, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenNoPriceIsApplicable() {
        Optional<Price> result = selector.selectApplicablePrice(
                exercisePrices(), LocalDateTime.of(2020, 6, 14, 10, 0), 99999, 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectAmbiguousHighestPriorityPrices() {
        Price first = price(1, "2020-06-14T00:00:00", "2020-06-14T23:59:59", 1, "35.50");
        Price second = price(2, "2020-06-14T10:00:00", "2020-06-14T18:00:00", 1, "25.45");

        assertThrows(AmbiguousPriceSelectionException.class,
                () -> selector.selectApplicablePrice(
                        List.of(first, second), LocalDateTime.of(2020, 6, 14, 12, 0), 35455, 1));
    }

    @ParameterizedTest(name = "At {0}, price list {1} applies")
    @MethodSource("exerciseScenarios")
    void shouldSelectExpectedPriceForExerciseScenarios(
            LocalDateTime applicationDate, long expectedPriceListId, String expectedAmount) {
        Price result = selector.selectApplicablePrice(exercisePrices(), applicationDate, 35455, 1)
                .orElseThrow();

        assertEquals(expectedPriceListId, result.priceListId());
        assertEquals(0, new BigDecimal(expectedAmount).compareTo(result.amount()));
        assertEquals(Currency.getInstance("EUR"), result.currency());
    }

    private static Stream<Arguments> exerciseScenarios() {
        return Stream.of(
                Arguments.of(LocalDateTime.of(2020, 6, 14, 10, 0), 1L, "35.50"),
                Arguments.of(LocalDateTime.of(2020, 6, 14, 16, 0), 2L, "25.45"),
                Arguments.of(LocalDateTime.of(2020, 6, 14, 21, 0), 1L, "35.50"),
                Arguments.of(LocalDateTime.of(2020, 6, 15, 10, 0), 3L, "30.50"),
                Arguments.of(LocalDateTime.of(2020, 6, 16, 21, 0), 4L, "38.95"));
    }

    private static List<Price> exercisePrices() {
        return List.of(
                price(1, "2020-06-14T00:00:00", "2020-12-31T23:59:59", 0, "35.50"),
                price(2, "2020-06-14T15:00:00", "2020-06-14T18:30:00", 1, "25.45"),
                price(3, "2020-06-15T00:00:00", "2020-06-15T11:00:00", 1, "30.50"),
                price(4, "2020-06-15T16:00:00", "2020-12-31T23:59:59", 1, "38.95"));
    }

    private static Price price(long priceListId, String startDate, String endDate, int priority, String amount) {
        return new Price(1, 35455, priceListId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate),
                priority, new BigDecimal(amount), Currency.getInstance("EUR"));
    }
}
