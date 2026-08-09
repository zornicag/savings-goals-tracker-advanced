package app.goalinsights.service;

import app.goalinsights.exception.ExchangeRateNotFoundException;
import app.goalinsights.model.entity.ExchangeRate;
import app.goalinsights.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void getAll_ShouldReturnAllExchangeRates() {
        ExchangeRate rate = createRate("EUR", "USD", new BigDecimal("1.16"));

        when(exchangeRateRepository.findAll())
                .thenReturn(List.of(rate));

        List<ExchangeRate> result = exchangeRateService.getAll();

        assertEquals(1, result.size());
        assertEquals("USD", result.get(0).getTargetCurrency());

        verify(exchangeRateRepository).findAll();
    }

    @Test
    void save_ShouldSaveExchangeRate_WhenBaseCurrencyIsEur() {
        ExchangeRate rate = createRate("EUR", "USD", new BigDecimal("1.16"));

        when(exchangeRateRepository.save(rate))
                .thenReturn(rate);

        ExchangeRate result = exchangeRateService.save(rate);

        assertEquals("EUR", result.getBaseCurrency());
        assertEquals("USD", result.getTargetCurrency());
        assertEquals(new BigDecimal("1.16"), result.getRate());

        verify(exchangeRateRepository).save(rate);
    }

    @Test
    void save_ShouldThrowException_WhenBaseCurrencyIsNotEur() {
        ExchangeRate rate = createRate("USD", "BGN", new BigDecimal("1.70"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> exchangeRateService.save(rate)
        );

        assertEquals("Base currency must be EUR", exception.getMessage());

        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void delete_ShouldDeleteExchangeRate_WhenIdExists() {
        UUID id = UUID.randomUUID();

        when(exchangeRateRepository.existsById(id))
                .thenReturn(true);

        exchangeRateService.delete(id);

        verify(exchangeRateRepository).deleteById(id);
    }

    @Test
    void delete_ShouldThrowException_WhenIdDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(exchangeRateRepository.existsById(id))
                .thenReturn(false);

        ExchangeRateNotFoundException exception = assertThrows(
                ExchangeRateNotFoundException.class,
                () -> exchangeRateService.delete(id)
        );

        assertEquals("Exchange rate not found", exception.getMessage());

        verify(exchangeRateRepository, never()).deleteById(any());
    }

    @Test
    void update_ShouldUpdateExchangeRate_WhenRateExists() {
        UUID id = UUID.randomUUID();

        ExchangeRate existingRate = createRate(
                "EUR",
                "USD",
                new BigDecimal("1.15")
        );

        ExchangeRate updatedRate = createRate(
                "EUR",
                "USD",
                new BigDecimal("1.16")
        );

        when(exchangeRateRepository.findById(id))
                .thenReturn(Optional.of(existingRate));

        when(exchangeRateRepository.save(existingRate))
                .thenReturn(existingRate);

        ExchangeRate result = exchangeRateService.update(id, updatedRate);

        assertEquals(new BigDecimal("1.16"), result.getRate());

        verify(exchangeRateRepository).save(existingRate);
    }

    @Test
    void update_ShouldThrowException_WhenRateDoesNotExist() {
        UUID id = UUID.randomUUID();

        ExchangeRate updatedRate = createRate(
                "EUR",
                "USD",
                new BigDecimal("1.16")
        );

        when(exchangeRateRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ExchangeRateNotFoundException.class,
                () -> exchangeRateService.update(id, updatedRate)
        );

        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void calculateRate_ShouldReturnOne_WhenCurrenciesAreEqual() {
        BigDecimal result = exchangeRateService.calculateRate(
                "EUR",
                "EUR"
        );

        assertEquals(BigDecimal.ONE, result);

        verifyNoInteractions(exchangeRateRepository);
    }

    @Test
    void calculateRate_ShouldReturnDirectRate_WhenDirectRateExists() {
        ExchangeRate rate = createRate(
                "EUR",
                "USD",
                new BigDecimal("1.16")
        );

        when(exchangeRateRepository.findByBaseCurrencyAndTargetCurrency(
                "EUR",
                "USD"
        )).thenReturn(Optional.of(rate));

        BigDecimal result = exchangeRateService.calculateRate(
                "EUR",
                "USD"
        );

        assertEquals(new BigDecimal("1.16"), result);
    }

    @Test
    void calculateRate_ShouldCalculateCrossRate_WhenDirectRateDoesNotExist() {
        ExchangeRate eurToUsd = createRate(
                "EUR",
                "USD",
                new BigDecimal("1.20")
        );

        ExchangeRate eurToGbp = createRate(
                "EUR",
                "GBP",
                new BigDecimal("0.90")
        );

        when(exchangeRateRepository.findByBaseCurrencyAndTargetCurrency(
                "USD",
                "GBP"
        )).thenReturn(Optional.empty());

        when(exchangeRateRepository.findByBaseCurrencyAndTargetCurrency(
                "EUR",
                "USD"
        )).thenReturn(Optional.of(eurToUsd));

        when(exchangeRateRepository.findByBaseCurrencyAndTargetCurrency(
                "EUR",
                "GBP"
        )).thenReturn(Optional.of(eurToGbp));

        BigDecimal result = exchangeRateService.calculateRate(
                "USD",
                "GBP"
        );

        assertEquals(new BigDecimal("0.75000"), result);
    }

    @Test
    void calculateRate_ShouldThrowException_WhenBaseCurrencyRateDoesNotExist() {
        when(exchangeRateRepository.findByBaseCurrencyAndTargetCurrency(
                "XYZ",
                "USD"
        )).thenReturn(Optional.empty());

        when(exchangeRateRepository.findByBaseCurrencyAndTargetCurrency(
                "EUR",
                "XYZ"
        )).thenReturn(Optional.empty());

        assertThrows(
                ExchangeRateNotFoundException.class,
                () -> exchangeRateService.calculateRate(
                        "XYZ",
                        "USD"
                )
        );
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