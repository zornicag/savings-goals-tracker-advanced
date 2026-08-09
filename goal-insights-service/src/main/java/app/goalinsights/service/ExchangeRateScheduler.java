package app.goalinsights.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateScheduler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ExchangeRateScheduler.class);

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateScheduler(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void logDailyExchangeRates() {

        int ratesCount = exchangeRateService.getAll().size();

        LOGGER.info(
                "Daily exchange rates check completed. Available exchange rates: {}",
                ratesCount
        );
    }

    @Scheduled(fixedRate = 60000)
    public void logExchangeRateServiceStatus() {

        int ratesCount = exchangeRateService.getAll().size();

        LOGGER.info(
                "Exchange rate service status check. Available exchange rates: {}",
                ratesCount
        );
    }
}
