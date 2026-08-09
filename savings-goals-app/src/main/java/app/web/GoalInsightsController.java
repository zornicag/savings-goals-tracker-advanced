package app.web;

import app.client.dto.ExchangeRateResponse;
import app.service.GoalInsightsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/goal-insights")
public class GoalInsightsController {

    private final GoalInsightsService goalInsightsService;

    public GoalInsightsController(GoalInsightsService goalInsightsService) {
        this.goalInsightsService = goalInsightsService;
    }

    @GetMapping("/exchange-rates")
    public List<ExchangeRateResponse> getExchangeRates() {
        return goalInsightsService.getAllExchangeRates();
    }

    @PostMapping("/exchange-rates")
    public ExchangeRateResponse createExchangeRate(
            @RequestBody ExchangeRateResponse exchangeRate) {

        return goalInsightsService.createExchangeRate(exchangeRate);
    }

    @PutMapping("/exchange-rates/{id}")
    public ExchangeRateResponse updateExchangeRate(
            @PathVariable UUID id,
            @RequestBody ExchangeRateResponse exchangeRate) {

        return goalInsightsService.updateExchangeRate(id, exchangeRate);
    }
}
