package com.example.priceservice.infrastructure.rest.dto;

import com.example.priceservice.domain.model.Price;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceResponse(
        long productId,
        long brandId,
        long priceListId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String currency) {

    public static PriceResponse from(Price price) {
        return new PriceResponse(price.productId(), price.brandId(), price.priceListId(),
                price.startDate(), price.endDate(), price.amount(), price.currency().getCurrencyCode());
    }
}
