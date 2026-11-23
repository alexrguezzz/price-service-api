package com.example.priceservice.infrastructure.persistence;

import com.example.priceservice.domain.model.Price;
import com.example.priceservice.domain.port.PriceQueryPort;
import com.example.priceservice.domain.service.PriceSelector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(PricePersistenceAdapter.class)
class PricePersistenceIntegrationTest {

    @Autowired
    private PriceJpaRepository repository;

    @Autowired
    private PriceQueryPort priceQueryPort;

    @Test
    void shouldLoadAllFourOriginalPrices() {
        List<PriceEntity> prices = repository.findAll().stream()
                .sorted(Comparator.comparingLong(PriceEntity::getPriceListId))
                .toList();

        assertEquals(4, prices.size());
        assertEquals(List.of(1L, 2L, 3L, 4L), prices.stream().map(PriceEntity::getPriceListId).toList());
        assertEquals(List.of(new BigDecimal("35.50"), new BigDecimal("25.45"),
                new BigDecimal("30.50"), new BigDecimal("38.95")),
                prices.stream().map(PriceEntity::getAmount).toList());
        assertEquals(List.of(0, 1, 1, 1), prices.stream().map(PriceEntity::getPriority).toList());
        assertEquals(List.of(at("2020-06-14T00:00:00"), at("2020-06-14T15:00:00"),
                at("2020-06-15T00:00:00"), at("2020-06-15T16:00:00")),
                prices.stream().map(PriceEntity::getStartDate).toList());
        assertEquals(List.of(at("2020-12-31T23:59:59"), at("2020-06-14T18:30:00"),
                at("2020-06-15T11:00:00"), at("2020-12-31T23:59:59")),
                prices.stream().map(PriceEntity::getEndDate).toList());
        assertTrue(prices.stream().allMatch(price -> price.getBrandId() == 1
                && price.getProductId() == 35455 && price.getCurrencyCode().equals("EUR")));
    }

    @Test
    void shouldFindOnlyPricesForRequestedProductBrandAndDate() {
        assertEquals(List.of(1L, 2L), priceListIds(at("2020-06-14T16:00:00"), 35455, 1));
    }

    @Test
    void shouldIncludeExactStartDate() {
        assertEquals(List.of(1L, 2L), priceListIds(at("2020-06-14T15:00:00"), 35455, 1));
    }

    @Test
    void shouldIncludeExactEndDate() {
        assertEquals(List.of(1L, 2L), priceListIds(at("2020-06-14T18:30:00"), 35455, 1));
    }

    @Test
    void shouldExcludePricesOutsideTheirDateRanges() {
        assertEquals(List.of(1L), priceListIds(at("2020-06-14T14:59:59"), 35455, 1));
        assertEquals(List.of(1L), priceListIds(at("2020-06-14T18:30:01"), 35455, 1));
        assertTrue(priceListIds(at("2021-01-01T00:00:00"), 35455, 1).isEmpty());
    }

    @Test
    void shouldExcludeOtherProducts() {
        repository.saveAndFlush(otherPrice(1, 99999, 1));

        assertEquals(List.of(1L, 2L), priceListIds(at("2020-06-14T16:00:00"), 35455, 1));
        assertEquals(List.of(1L), priceListIds(at("2020-06-14T16:00:00"), 99999, 1));
    }

    @Test
    void shouldExcludeOtherBrandsAndAllowReusedPriceListIdentifiers() {
        repository.saveAndFlush(otherPrice(2, 35455, 1));

        assertEquals(List.of(1L, 2L), priceListIds(at("2020-06-14T16:00:00"), 35455, 1));
        assertEquals(List.of(1L), priceListIds(at("2020-06-14T16:00:00"), 35455, 2));
    }

    @Test
    void shouldMapEveryEntityFieldToDomain() {
        Price price = priceQueryPort.findApplicablePrices(at("2020-06-14T16:00:00"), 35455, 1)
                .stream()
                .filter(candidate -> candidate.priceListId() == 2)
                .findFirst()
                .orElseThrow();

        assertEquals(1, price.brandId());
        assertEquals(35455, price.productId());
        assertEquals(2, price.priceListId());
        assertEquals(at("2020-06-14T15:00:00"), price.startDate());
        assertEquals(at("2020-06-14T18:30:00"), price.endDate());
        assertEquals(1, price.priority());
        assertEquals(new BigDecimal("25.45"), price.amount());
        assertEquals(Currency.getInstance("EUR"), price.currency());
    }

    @Test
    void shouldPreserveExactMonetaryAmountForAdditionalPrice() {
        repository.saveAndFlush(new PriceEntity(2, at("2020-06-14T00:00:00"),
                at("2020-06-14T23:59:59"), 1, 99999, 0, new BigDecimal("123456.78"), "USD"));

        Price price = priceQueryPort.findApplicablePrices(at("2020-06-14T16:00:00"), 99999, 2)
                .getFirst();
        assertEquals(new BigDecimal("123456.78"), price.amount());
        assertEquals(Currency.getInstance("USD"), price.currency());
    }

    @Test
    void shouldReturnEveryOverlappingCandidateWithoutApplyingPriority() {
        List<Price> candidates = priceQueryPort.findApplicablePrices(at("2020-06-14T16:00:00"), 35455, 1);

        assertEquals(List.of(1L, 2L), candidates.stream().map(Price::priceListId).sorted().toList());
        assertEquals(List.of(0, 1), candidates.stream().map(Price::priority).sorted().toList());
    }

    @ParameterizedTest(name = "At {0}, price list {1} applies from persistence")
    @MethodSource("exerciseScenarios")
    void shouldSelectExpectedPriceFromPersistedCandidates(
            LocalDateTime applicationDate, long expectedPriceListId, String expectedAmount) {
        List<Price> candidates = priceQueryPort.findApplicablePrices(applicationDate, 35455, 1);

        Price selected = new PriceSelector()
                .selectApplicablePrice(candidates, applicationDate, 35455, 1)
                .orElseThrow();
        assertEquals(expectedPriceListId, selected.priceListId());
        assertEquals(new BigDecimal(expectedAmount), selected.amount());
        assertEquals(Currency.getInstance("EUR"), selected.currency());
    }

    private List<Long> priceListIds(LocalDateTime date, long productId, long brandId) {
        return repository.findApplicablePrices(date, productId, brandId).stream()
                .map(PriceEntity::getPriceListId)
                .sorted()
                .toList();
    }

    private PriceEntity otherPrice(long brandId, long productId, long priceListId) {
        return new PriceEntity(brandId, at("2020-06-14T00:00:00"), at("2020-06-14T23:59:59"),
                priceListId, productId, 0, new BigDecimal("10.00"), "EUR");
    }

    private static LocalDateTime at(String dateTime) {
        return LocalDateTime.parse(dateTime);
    }

    private static Stream<Arguments> exerciseScenarios() {
        return Stream.of(
                Arguments.of(at("2020-06-14T10:00:00"), 1L, "35.50"),
                Arguments.of(at("2020-06-14T16:00:00"), 2L, "25.45"),
                Arguments.of(at("2020-06-14T21:00:00"), 1L, "35.50"),
                Arguments.of(at("2020-06-15T10:00:00"), 3L, "30.50"),
                Arguments.of(at("2020-06-16T21:00:00"), 4L, "38.95"));
    }
}
