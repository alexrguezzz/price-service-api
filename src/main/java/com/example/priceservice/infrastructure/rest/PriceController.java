package com.example.priceservice.infrastructure.rest;

import com.example.priceservice.application.service.GetApplicablePriceService;
import com.example.priceservice.infrastructure.rest.dto.PriceResponse;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/prices")
public class PriceController {

    private final GetApplicablePriceService getApplicablePriceService;

    public PriceController(GetApplicablePriceService getApplicablePriceService) {
        this.getApplicablePriceService = getApplicablePriceService;
    }

    @GetMapping
    public PriceResponse getApplicablePrice(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate,
            @RequestParam @Positive long productId,
            @RequestParam @Positive long brandId) {
        return PriceResponse.from(getApplicablePriceService.getApplicablePrice(applicationDate, productId, brandId));
    }
}
