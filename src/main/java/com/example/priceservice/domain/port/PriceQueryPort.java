package com.example.priceservice.domain.port;

import com.example.priceservice.domain.model.Price;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceQueryPort {

    List<Price> findApplicablePrices(LocalDateTime applicationDate, long productId, long brandId);
}
