package com.example.priceservice.infrastructure.config;

import com.example.priceservice.application.service.GetApplicablePriceService;
import com.example.priceservice.domain.port.PriceQueryPort;
import com.example.priceservice.domain.service.PriceSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PriceConfiguration {

    @Bean
    PriceSelector priceSelector() {
        return new PriceSelector();
    }

    @Bean
    GetApplicablePriceService getApplicablePriceService(PriceQueryPort priceQueryPort, PriceSelector priceSelector) {
        return new GetApplicablePriceService(priceQueryPort, priceSelector);
    }
}
