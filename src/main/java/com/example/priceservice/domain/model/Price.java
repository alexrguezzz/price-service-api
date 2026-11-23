package com.example.priceservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Objects;

public record Price(
        long brandId,
        long productId,
        long priceListId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int priority,
        BigDecimal amount,
        Currency currency) {

    public Price {
        if (brandId <= 0) {
            throw new IllegalArgumentException("Brand identifier must be positive");
        }
        if (productId <= 0) {
            throw new IllegalArgumentException("Product identifier must be positive");
        }
        if (priceListId <= 0) {
            throw new IllegalArgumentException("Price list identifier must be positive");
        }
        Objects.requireNonNull(startDate, "Start date must not be null");
        Objects.requireNonNull(endDate, "End date must not be null");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must not be after end date");
        }
        if (priority < 0) {
            throw new IllegalArgumentException("Priority must not be negative");
        }
        Objects.requireNonNull(amount, "Price amount must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Price amount must not be negative");
        }
        Objects.requireNonNull(currency, "Currency must not be null");
    }

    public boolean matches(long requestedProductId, long requestedBrandId) {
        return productId == requestedProductId && brandId == requestedBrandId;
    }

    public boolean isValidAt(LocalDateTime applicationDate) {
        Objects.requireNonNull(applicationDate, "Application date must not be null");
        return !applicationDate.isBefore(startDate) && !applicationDate.isAfter(endDate);
    }

    public boolean isApplicableAt(LocalDateTime applicationDate, long requestedProductId, long requestedBrandId) {
        return matches(requestedProductId, requestedBrandId) && isValidAt(applicationDate);
    }
}
