package app.goalinsights.web;

import app.goalinsights.model.entity.ExchangeRate;
import app.goalinsights.service.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public List<ExchangeRate> getAll() {
        return exchangeRateService.getAll();
    }

    @PostMapping
    public ExchangeRate create(@RequestBody ExchangeRate exchangeRate) {
        return exchangeRateService.save(exchangeRate);
    }

    @PutMapping("/{id}")
    public ExchangeRate update(@PathVariable UUID id,
                               @RequestBody ExchangeRate exchangeRate) {
        return exchangeRateService.update(id, exchangeRate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        exchangeRateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calculate")
    public BigDecimal calculateRate(
            @RequestParam String baseCurrency,
            @RequestParam String targetCurrency) {

        return exchangeRateService.calculateRate(baseCurrency, targetCurrency);
    }
}
