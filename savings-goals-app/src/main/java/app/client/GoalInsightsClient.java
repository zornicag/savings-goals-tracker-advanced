package app.client;

import app.client.dto.ExchangeRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "goal-insights-service", url = "http://localhost:8081")
public interface GoalInsightsClient {

    @GetMapping("/api/exchange-rates")
    List<ExchangeRateResponse> getAllExchangeRates();

    @PostMapping("/api/exchange-rates")
    ExchangeRateResponse createExchangeRate(
            @RequestBody ExchangeRateResponse exchangeRate
    );

    @PutMapping("/api/exchange-rates/{id}")
    ExchangeRateResponse updateExchangeRate(
            @PathVariable UUID id,
            @RequestBody ExchangeRateResponse exchangeRate
    );

    @DeleteMapping("/api/exchange-rates/{id}")
    void deleteExchangeRate(
            @PathVariable UUID id
    );

    @GetMapping("/api/exchange-rates/calculate")
    BigDecimal calculateExchangeRate(
            @RequestParam String baseCurrency,
            @RequestParam String targetCurrency
    );
}