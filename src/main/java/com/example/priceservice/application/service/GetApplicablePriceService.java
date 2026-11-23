package com.example.priceservice.application.service;

import com.example.priceservice.application.exception.PriceNotFoundException;
import com.example.priceservice.domain.model.Price;
import com.example.priceservice.domain.port.PriceQueryPort;
import com.example.priceservice.domain.service.PriceSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class GetApplicablePriceService {

    private static final Logger logger = LoggerFactory.getLogger(GetApplicablePriceService.class);

    private final PriceQueryPort priceQueryPort;
    private final PriceSelector priceSelector;

    public GetApplicablePriceService(PriceQueryPort priceQueryPort, PriceSelector priceSelector) {
        this.priceQueryPort = priceQueryPort;
        this.priceSelector = priceSelector;
    }

    public Price getApplicablePrice(LocalDateTime applicationDate, long productId, long brandId) {
        logger.debug("Looking up price for product {} and brand {} at {}", productId, brandId, applicationDate);
        var candidates = priceQueryPort.findApplicablePrices(applicationDate, productId, brandId);
        return priceSelector.selectApplicablePrice(candidates, applicationDate, productId, brandId)
                .orElseThrow(() -> {
                    logger.info("No applicable price found for product {} and brand {} at {}",
                            productId, brandId, applicationDate);
                    return new PriceNotFoundException();
                });
    }
}
