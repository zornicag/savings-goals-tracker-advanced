package app.goalinsights.web;

import app.goalinsights.model.entity.ExchangeRate;
import app.goalinsights.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateControllerTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ExchangeRateController exchangeRateController;

    @Test
    void getAll_ShouldReturnExchangeRates() {
        ExchangeRate rate = createRate(
                "EUR",
                "USD",
                new BigDecimal("1.16")
        );

        when(exchangeRateService.getAll())
                .thenReturn(List.of(rate));

        List<ExchangeRate> result =
                exchangeRateController.getAll();

        assertEquals(1, result.size());
        assertEquals("USD", result.get(0).getTargetCurrency());

        verify(exchangeRateService).getAll();
    }

    @Test
    void create_ShouldCreateExchangeRate() {
        ExchangeRate rate = createRate(
                "EUR",
                "USD",
                new BigDecimal("1.16")
        );

        when(exchangeRateService.save(rate))
                .thenReturn(rate);

        ExchangeRate result =
                exchangeRateController.create(rate);

        assertEquals("EUR", result.getBaseCurrency());
        assertEquals("USD", result.getTargetCurrency());

        verify(exchangeRateService).save(rate);
    }

    @Test
    void update_ShouldUpdateExchangeRate() {
        UUID id = UUID.randomUUID();

        ExchangeRate rate = createRate(
                "EUR",
                "GBP",
                new BigDecimal("0.87")
        );

        when(exchangeRateService.update(id, rate))
                .thenReturn(rate);

        ExchangeRate result =
                exchangeRateController.update(id, rate);

        assertEquals("GBP", result.getTargetCurrency());

        verify(exchangeRateService).update(id, rate);
    }

    @Test
    void delete_ShouldReturnNoContent() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response =
                exchangeRateController.delete(id);

        assertEquals(204, response.getStatusCode().value());

        verify(exchangeRateService).delete(id);
    }

    @Test
    void calculateRate_ShouldReturnCalculatedRate() {
        BigDecimal expected =
                new BigDecimal("1.16");

        when(exchangeRateService.calculateRate(
                "EUR",
                "USD"
        )).thenReturn(expected);

        BigDecimal result =
                exchangeRateController.calculateRate(
                        "EUR",
                        "USD"
                );

        assertEquals(expected, result);

        verify(exchangeRateService)
                .calculateRate("EUR", "USD");
    }

    private ExchangeRate createRate(
            String baseCurrency,
            String targetCurrency,
            BigDecimal rateValue) {

        ExchangeRate rate = new ExchangeRate();

        rate.setBaseCurrency(baseCurrency);
        rate.setTargetCurrency(targetCurrency);
        rate.setRate(rateValue);

        return rate;
    }
}
