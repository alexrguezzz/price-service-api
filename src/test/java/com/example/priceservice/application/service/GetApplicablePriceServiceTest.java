package com.example.priceservice.application.service;

import com.example.priceservice.application.exception.PriceNotFoundException;
import com.example.priceservice.domain.exception.AmbiguousPriceSelectionException;
import com.example.priceservice.domain.model.Price;
import com.example.priceservice.domain.port.PriceQueryPort;
import com.example.priceservice.domain.service.PriceSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetApplicablePriceServiceTest {

    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.parse("2020-06-14T16:00:00");

    private PriceQueryPort priceQueryPort;
    private PriceSelector priceSelector;
    private GetApplicablePriceService service;

    @BeforeEach
    void setUp() {
        priceQueryPort = mock(PriceQueryPort.class);
        priceSelector = mock(PriceSelector.class);
        service = new GetApplicablePriceService(priceQueryPort, priceSelector);
    }

    @Test
    void shouldQueryPortAndDelegateSelectionToDomainService() {
        Price selected = price(2, 1, "25.45");
        List<Price> candidates = List.of(selected);
        when(priceQueryPort.findApplicablePrices(APPLICATION_DATE, 35455, 1)).thenReturn(candidates);
        when(priceSelector.selectApplicablePrice(candidates, APPLICATION_DATE, 35455, 1))
                .thenReturn(Optional.of(selected));

        assertSame(selected, service.getApplicablePrice(APPLICATION_DATE, 35455, 1));
        verify(priceQueryPort).findApplicablePrices(APPLICATION_DATE, 35455, 1);
        verify(priceSelector).selectApplicablePrice(candidates, APPLICATION_DATE, 35455, 1);
    }

    @Test
    void shouldReturnHighestPriorityPriceForOverlappingCandidates() {
        Price lower = price(1, 0, "35.50");
        Price higher = price(2, 1, "25.45");
        List<Price> candidates = List.of(lower, higher);
        when(priceQueryPort.findApplicablePrices(APPLICATION_DATE, 35455, 1)).thenReturn(candidates);
        when(priceSelector.selectApplicablePrice(candidates, APPLICATION_DATE, 35455, 1))
                .thenAnswer(invocation -> new PriceSelector().selectApplicablePrice(
                        invocation.getArgument(0), invocation.getArgument(1),
                        invocation.getArgument(2), invocation.getArgument(3)));

        assertEquals(2, service.getApplicablePrice(APPLICATION_DATE, 35455, 1).priceListId());
        verify(priceSelector).selectApplicablePrice(candidates, APPLICATION_DATE, 35455, 1);
    }

    @Test
    void shouldReportMissingPriceWhenPortReturnsNoCandidates() {
        when(priceQueryPort.findApplicablePrices(APPLICATION_DATE, 35455, 1)).thenReturn(List.of());
        when(priceSelector.selectApplicablePrice(List.of(), APPLICATION_DATE, 35455, 1))
                .thenReturn(Optional.empty());

        assertThrows(PriceNotFoundException.class,
                () -> service.getApplicablePrice(APPLICATION_DATE, 35455, 1));
    }

    @Test
    void shouldReportMissingPriceWhenSelectorFindsNoApplicableCandidate() {
        List<Price> candidates = List.of(price(1, 0, "35.50"));
        when(priceQueryPort.findApplicablePrices(APPLICATION_DATE, 35455, 1)).thenReturn(candidates);
        when(priceSelector.selectApplicablePrice(candidates, APPLICATION_DATE, 35455, 1))
                .thenReturn(Optional.empty());

        assertThrows(PriceNotFoundException.class,
                () -> service.getApplicablePrice(APPLICATION_DATE, 35455, 1));
    }

    @Test
    void shouldPropagateAmbiguousSelection() {
        List<Price> candidates = List.of(price(2, 1, "25.45"), price(3, 1, "30.50"));
        when(priceQueryPort.findApplicablePrices(APPLICATION_DATE, 35455, 1)).thenReturn(candidates);
        when(priceSelector.selectApplicablePrice(candidates, APPLICATION_DATE, 35455, 1))
                .thenThrow(new AmbiguousPriceSelectionException(1));

        assertThrows(AmbiguousPriceSelectionException.class,
                () -> service.getApplicablePrice(APPLICATION_DATE, 35455, 1));
    }

    private static Price price(long priceListId, int priority, String amount) {
        return new Price(1, 35455, priceListId, LocalDateTime.parse("2020-06-14T00:00:00"),
                LocalDateTime.parse("2020-06-14T23:59:59"), priority,
                new BigDecimal(amount), Currency.getInstance("EUR"));
    }
}
