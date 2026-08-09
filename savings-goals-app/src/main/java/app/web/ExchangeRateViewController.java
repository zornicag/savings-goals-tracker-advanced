package app.web;

import app.client.dto.ExchangeRateResponse;
import app.service.GoalInsightsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
public class ExchangeRateViewController {

    private final GoalInsightsService goalInsightsService;

    public ExchangeRateViewController(GoalInsightsService goalInsightsService) {
        this.goalInsightsService = goalInsightsService;
    }

    @GetMapping("/exchange-rates")
    public String exchangeRates(Model model) {
        model.addAttribute("exchangeRates",
                goalInsightsService.getAllExchangeRates());

        model.addAttribute("exchangeRate",
                new ExchangeRateResponse());

        return "exchange-rates";
    }

    @PostMapping("/exchange-rates")
    public String createExchangeRate(ExchangeRateResponse exchangeRate) {

        exchangeRate.setBaseCurrency("EUR");

        goalInsightsService.createExchangeRate(exchangeRate);

        return "redirect:/exchange-rates";
    }

    @PutMapping("/exchange-rates/{id}")
    public String updateExchangeRate(
            @PathVariable UUID id,
            ExchangeRateResponse exchangeRate) {

        goalInsightsService.updateExchangeRate(id, exchangeRate);

        return "redirect:/exchange-rates";
    }

    @PostMapping("/exchange-rates/{id}/delete")
    public String deleteExchangeRate(@PathVariable UUID id) {

        goalInsightsService.deleteExchangeRate(id);

        return "redirect:/exchange-rates?deleted";
    }

    @GetMapping("/exchange-rates/calculate")
    public String calculateExchangeRate(
            @RequestParam String baseCurrency,
            @RequestParam String targetCurrency,
            Model model) {

        BigDecimal calculatedRate =
                goalInsightsService.calculateExchangeRate(
                        baseCurrency,
                        targetCurrency
                );

        model.addAttribute("exchangeRates",
                goalInsightsService.getAllExchangeRates());

        model.addAttribute("exchangeRate",
                new ExchangeRateResponse());

        model.addAttribute("calculatedRate", calculatedRate);

        return "exchange-rates";
    }
}