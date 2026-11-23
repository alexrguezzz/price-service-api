package com.example.priceservice.infrastructure.persistence;

import com.example.priceservice.domain.model.Price;
import com.example.priceservice.domain.port.PriceQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

@Repository
public class PricePersistenceAdapter implements PriceQueryPort {

    private static final Logger logger = LoggerFactory.getLogger(PricePersistenceAdapter.class);

    private final PriceJpaRepository repository;

    public PricePersistenceAdapter(PriceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Price> findApplicablePrices(LocalDateTime applicationDate, long productId, long brandId) {
        List<Price> candidates = repository.findApplicablePrices(applicationDate, productId, brandId)
                .stream()
                .map(this::toDomain)
                .toList();
        logger.debug("Found {} applicable price candidates for product {} and brand {}",
                candidates.size(), productId, brandId);
        return candidates;
    }

    private Price toDomain(PriceEntity entity) {
        return new Price(
                entity.getBrandId(),
                entity.getProductId(),
                entity.getPriceListId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPriority(),
                entity.getAmount(),
                Currency.getInstance(entity.getCurrencyCode()));
    }
}
