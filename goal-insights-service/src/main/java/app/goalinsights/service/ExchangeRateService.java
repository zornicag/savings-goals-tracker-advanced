package app.goalinsights.service;

import app.goalinsights.exception.ExchangeRateNotFoundException;
import app.goalinsights.model.entity.ExchangeRate;
import app.goalinsights.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class ExchangeRateService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Cacheable("exchangeRates")
    public List<ExchangeRate> getAll() {
        LOGGER.info("Loading all exchange rates");
        return exchangeRateRepository.findAll();
    }

    @CacheEvict(value = "exchangeRates", allEntries = true)
    public ExchangeRate save(ExchangeRate exchangeRate) {

        if (!"EUR".equalsIgnoreCase(exchangeRate.getBaseCurrency())) {
            LOGGER.warn(
                    "Invalid base currency received: {}",
                    exchangeRate.getBaseCurrency()
            );

            throw new IllegalArgumentException(
                    "Base currency must be EUR"
            );
        }

        ExchangeRate savedRate =
                exchangeRateRepository.save(exchangeRate);

        LOGGER.info(
                "Exchange rate created: {} -> {}, rate: {}",
                savedRate.getBaseCurrency(),
                savedRate.getTargetCurrency(),
                savedRate.getRate()
        );

        return savedRate;
    }

    @CacheEvict(value = "exchangeRates", allEntries = true)
    public void delete(UUID id) {

        if (!exchangeRateRepository.existsById(id)) {
            LOGGER.warn(
                    "Exchange rate with id {} was not found for deletion",
                    id
            );

            throw new ExchangeRateNotFoundException(
                    "Exchange rate not found"
            );
        }

        exchangeRateRepository.deleteById(id);

        LOGGER.info(
                "Exchange rate deleted successfully. Id: {}",
                id
        );
    }

    @CacheEvict(value = "exchangeRates", allEntries = true)
    public ExchangeRate update(
            UUID id,
            ExchangeRate updatedRate) {

        ExchangeRate existingRate =
                exchangeRateRepository.findById(id)
                        .orElseThrow(() -> {

                            LOGGER.warn(
                                    "Exchange rate with id {} was not found for update",
                                    id
                            );

                            return new ExchangeRateNotFoundException(
                                    "Exchange rate not found"
                            );
                        });

        existingRate.setBaseCurrency(
                updatedRate.getBaseCurrency()
        );

        existingRate.setTargetCurrency(
                updatedRate.getTargetCurrency()
        );

        existingRate.setRate(
                updatedRate.getRate()
        );

        ExchangeRate savedRate =
                exchangeRateRepository.save(existingRate);

        LOGGER.info(
                "Exchange rate updated: {} -> {}, rate: {}",
                savedRate.getBaseCurrency(),
                savedRate.getTargetCurrency(),
                savedRate.getRate()
        );

        return savedRate;
    }

    public BigDecimal calculateRate(
            String baseCurrency,
            String targetCurrency) {

        LOGGER.info(
                "Calculating exchange rate from {} to {}",
                baseCurrency,
                targetCurrency
        );

        if (baseCurrency.equals(targetCurrency)) {
            return BigDecimal.ONE;
        }

        BigDecimal calculatedRate =
                exchangeRateRepository
                        .findByBaseCurrencyAndTargetCurrency(
                                baseCurrency,
                                targetCurrency
                        )
                        .map(ExchangeRate::getRate)
                        .orElseGet(() -> {

                            ExchangeRate baseToEur =
                                    exchangeRateRepository
                                            .findByBaseCurrencyAndTargetCurrency(
                                                    "EUR",
                                                    baseCurrency
                                            )
                                            .orElseThrow(() ->
                                                    new ExchangeRateNotFoundException(
                                                            "Base currency rate not found"
                                                    )
                                            );

                            ExchangeRate eurToTarget =
                                    exchangeRateRepository
                                            .findByBaseCurrencyAndTargetCurrency(
                                                    "EUR",
                                                    targetCurrency
                                            )
                                            .orElseThrow(() ->
                                                    new ExchangeRateNotFoundException(
                                                            "Target currency rate not found"
                                                    )
                                            );

                            return eurToTarget.getRate()
                                    .divide(
                                            baseToEur.getRate(),
                                            5,
                                            RoundingMode.HALF_UP
                                    );
                        });

        LOGGER.info(
                "Calculated exchange rate {} -> {} = {}",
                baseCurrency,
                targetCurrency,
                calculatedRate
        );

        return calculatedRate;
    }
}