package com.example.priceservice.infrastructure.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PriceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ParameterizedTest(name = "Exercise scenario {index}: {0}")
    @MethodSource("exerciseScenarios")
    void shouldReturnExpectedPriceForExerciseScenario(String date, int priceListId, String start,
                                                        String end, double price) throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("applicationDate", date)
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceListId").value(priceListId))
                .andExpect(jsonPath("$.startDate").value(start))
                .andExpect(jsonPath("$.endDate").value(end))
                .andExpect(jsonPath("$.price").value(price))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void shouldRejectMissingDate() throws Exception {
        assertBadRequest(null, "35455", "1");
    }

    @Test
    void shouldRejectInvalidDate() throws Exception {
        assertBadRequest("not-a-date", "35455", "1");
    }

    @Test
    void shouldRejectMissingProduct() throws Exception {
        assertBadRequest("2020-06-14T16:00:00", null, "1");
    }

    @Test
    void shouldRejectNonNumericProduct() throws Exception {
        assertBadRequest("2020-06-14T16:00:00", "abc", "1");
    }

    @Test
    void shouldRejectZeroProduct() throws Exception {
        assertBadRequest("2020-06-14T16:00:00", "0", "1");
    }

    @Test
    void shouldRejectNegativeProduct() throws Exception {
        assertBadRequest("2020-06-14T16:00:00", "-1", "1");
    }

    @Test
    void shouldRejectMissingBrand() throws Exception {
        assertBadRequest("2020-06-14T16:00:00", "35455", null);
    }

    @Test
    void shouldRejectNonNumericBrand() throws Exception {
        assertBadRequest("2020-06-14T16:00:00", "35455", "abc");
    }

    @Test
    void shouldRejectZeroBrand() throws Exception {
        assertBadRequest("2020-06-14T16:00:00", "35455", "0");
    }

    @Test
    void shouldRejectNegativeBrand() throws Exception {
        assertBadRequest("2020-06-14T16:00:00", "35455", "-1");
    }

    @Test
    void shouldReturnNotFoundForUnknownProduct() throws Exception {
        assertNotFound("2020-06-14T16:00:00", "99999", "1");
    }

    @Test
    void shouldReturnNotFoundForUnknownBrand() throws Exception {
        assertNotFound("2020-06-14T16:00:00", "35455", "2");
    }

    @Test
    void shouldReturnNotFoundForDateOutsideAllIntervals() throws Exception {
        assertNotFound("2021-01-01T00:00:00", "35455", "1");
    }

    @Test
    void shouldReturnNotFoundForRootPath() throws Exception {
        assertResourceNotFound("/");
    }

    @Test
    void shouldReturnNotFoundForMissingFavicon() throws Exception {
        assertResourceNotFound("/favicon.ico");
    }

    @Test
    void shouldReturnConflictForAmbiguousHighestPriority() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO PRICES (BRAND_ID, START_DATE, END_DATE, PRICE_LIST, PRODUCT_ID, PRIORITY, PRICE, CURR)
                VALUES (1, '2020-06-14 15:00:00', '2020-06-14 18:30:00', 99, 35455, 1, 20.00, 'EUR')
                """);

        mockMvc.perform(get("/api/v1/prices")
                        .param("applicationDate", "2020-06-14T16:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Multiple applicable prices share the highest priority"));
    }

    private void assertBadRequest(String date, String product, String brand) throws Exception {
        var request = get("/api/v1/prices");
        if (date != null) {
            request.param("applicationDate", date);
        }
        if (product != null) {
            request.param("productId", product);
        }
        if (brand != null) {
            request.param("brandId", brand);
        }
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    private void assertNotFound(String date, String product, String brand) throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("applicationDate", date)
                        .param("productId", product)
                        .param("brandId", brand))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(
                        "No applicable price was found for the requested product, brand, and date"));
    }

    private void assertResourceNotFound(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("No resource was found for the requested path"));
    }

    private static Stream<Arguments> exerciseScenarios() {
        return Stream.of(
                Arguments.of("2020-06-14T10:00:00", 1, "2020-06-14T00:00:00", "2020-12-31T23:59:59", 35.50),
                Arguments.of("2020-06-14T16:00:00", 2, "2020-06-14T15:00:00", "2020-06-14T18:30:00", 25.45),
                Arguments.of("2020-06-14T21:00:00", 1, "2020-06-14T00:00:00", "2020-12-31T23:59:59", 35.50),
                Arguments.of("2020-06-15T10:00:00", 3, "2020-06-15T00:00:00", "2020-06-15T11:00:00", 30.50),
                Arguments.of("2020-06-16T21:00:00", 4, "2020-06-15T16:00:00", "2020-12-31T23:59:59", 38.95));
    }
}
