package com.example.priceservice.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRICES")
public class PriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BRAND_ID", nullable = false)
    private long brandId;

    @Column(name = "START_DATE", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "PRICE_LIST", nullable = false)
    private long priceListId;

    @Column(name = "PRODUCT_ID", nullable = false)
    private long productId;

    @Column(name = "PRIORITY", nullable = false)
    private int priority;

    @Column(name = "PRICE", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "CURR", nullable = false, length = 3)
    private String currencyCode;

    protected PriceEntity() {
    }

    PriceEntity(long brandId, LocalDateTime startDate, LocalDateTime endDate, long priceListId,
                long productId, int priority, BigDecimal amount, String currencyCode) {
        this.brandId = brandId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priceListId = priceListId;
        this.productId = productId;
        this.priority = priority;
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    public long getBrandId() {
        return brandId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public long getPriceListId() {
        return priceListId;
    }

    public long getProductId() {
        return productId;
    }

    public int getPriority() {
        return priority;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
