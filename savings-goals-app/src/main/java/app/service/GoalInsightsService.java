package app.service;

import app.client.GoalInsightsClient;
import app.client.dto.ExchangeRateResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class GoalInsightsService {

    private final GoalInsightsClient goalInsightsClient;

    public GoalInsightsService(GoalInsightsClient goalInsightsClient) {
        this.goalInsightsClient = goalInsightsClient;
    }

    public List<ExchangeRateResponse> getAllExchangeRates() {
        return goalInsightsClient.getAllExchangeRates();
    }

    public ExchangeRateResponse createExchangeRate(ExchangeRateResponse exchangeRate) {
        return goalInsightsClient.createExchangeRate(exchangeRate);
    }

    public ExchangeRateResponse updateExchangeRate(
            UUID id,
            ExchangeRateResponse exchangeRate) {

        return goalInsightsClient.updateExchangeRate(id, exchangeRate);
    }

    public void deleteExchangeRate(UUID id) {
        goalInsightsClient.deleteExchangeRate(id);
    }

    public BigDecimal calculateExchangeRate(
            String baseCurrency,
            String targetCurrency) {

        return goalInsightsClient.calculateExchangeRate(
                baseCurrency,
                targetCurrency
        );
    }
}