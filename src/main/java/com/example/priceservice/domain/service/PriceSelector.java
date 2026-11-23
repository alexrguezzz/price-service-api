package com.example.priceservice.domain.service;

import com.example.priceservice.domain.exception.AmbiguousPriceSelectionException;
import com.example.priceservice.domain.model.Price;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PriceSelector {

    public Optional<Price> selectApplicablePrice(
            Collection<Price> prices,
            LocalDateTime applicationDate,
            long productId,
            long brandId) {
        Objects.requireNonNull(prices, "Prices must not be null");
        Objects.requireNonNull(applicationDate, "Application date must not be null");

        List<Price> applicablePrices = prices.stream()
                .map(price -> Objects.requireNonNull(price, "Prices must not contain null values"))
                .filter(price -> price.isApplicableAt(applicationDate, productId, brandId))
                .toList();

        Optional<Integer> highestPriority = applicablePrices.stream()
                .map(Price::priority)
                .max(Comparator.naturalOrder());

        if (highestPriority.isEmpty()) {
            return Optional.empty();
        }

        List<Price> highestPriorityPrices = applicablePrices.stream()
                .filter(price -> price.priority() == highestPriority.get())
                .toList();

        if (highestPriorityPrices.size() > 1) {
            throw new AmbiguousPriceSelectionException(highestPriority.get());
        }

        return Optional.of(highestPriorityPrices.getFirst());
    }
}
