package com.example.priceservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    @Query("""
            select price from PriceEntity price
            where price.productId = :productId
              and price.brandId = :brandId
              and price.startDate <= :applicationDate
              and price.endDate >= :applicationDate
            """)
    List<PriceEntity> findApplicablePrices(
            @Param("applicationDate") LocalDateTime applicationDate,
            @Param("productId") long productId,
            @Param("brandId") long brandId);
}
