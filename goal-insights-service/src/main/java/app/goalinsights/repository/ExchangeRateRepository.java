package app.goalinsights.repository;

import app.goalinsights.model.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {

    Optional<ExchangeRate> findByBaseCurrencyAndTargetCurrency(
            String baseCurrency,
            String targetCurrency
    );
}
