package app.service.transaction;

import app.model.entity.savingsgoal.SavingsGoal;
import app.model.entity.transaction.Transaction;
import app.model.entity.transaction.TransactionType;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.repository.transaction.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              SavingsGoalRepository savingsGoalRepository) {
        this.transactionRepository = transactionRepository;
        this.savingsGoalRepository = savingsGoalRepository;
    }

    public List<Transaction> getAllTransactionsForUser(UUID userId) {

        LOGGER.info(
                "Loading transactions for user with id: {}",
                userId
        );

        return transactionRepository.findBySavingsGoal_User_Id(userId);
    }

    public boolean applyAndSave(Transaction transaction) {

        SavingsGoal savingsGoal =
                savingsGoalRepository
                        .findById(transaction.getSavingsGoal().getId())
                        .orElse(null);

        if (savingsGoal == null) {

            LOGGER.warn(
                    "Transaction cannot be saved because savings goal was not found"
            );

            return false;
        }

        BigDecimal oldBalance =
                savingsGoal.getCurrentAmount() == null
                        ? BigDecimal.ZERO
                        : savingsGoal.getCurrentAmount();

        BigDecimal amount = transaction.getAmount();

        if (transaction.getType() == TransactionType.WITHDRAW
                && amount.compareTo(oldBalance) > 0) {

            LOGGER.warn(
                    "Withdrawal rejected for goal id {}. Requested amount: {}, available balance: {}",
                    savingsGoal.getId(),
                    amount,
                    oldBalance
            );

            return false;
        }

        BigDecimal newBalance;

        if (transaction.getType() == TransactionType.DEPOSIT) {
            newBalance = oldBalance.add(amount);
        } else {
            newBalance = oldBalance.subtract(amount);
        }

        savingsGoal.setCurrentAmount(newBalance);
        transaction.setBalanceAfter(newBalance);

        savingsGoalRepository.save(savingsGoal);
        Transaction savedTransaction =
                transactionRepository.save(transaction);

        LOGGER.info(
                "Transaction saved successfully. Id: {}, type: {}, amount: {}, goal id: {}, new balance: {}",
                savedTransaction.getId(),
                savedTransaction.getType(),
                savedTransaction.getAmount(),
                savingsGoal.getId(),
                newBalance
        );

        return true;
    }

    public boolean deleteById(UUID transactionId) {

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElse(null);

        if (transaction == null) {

            LOGGER.warn(
                    "Transaction with id {} was not found for deletion",
                    transactionId
            );

            return false;
        }

        SavingsGoal savingsGoal = transaction.getSavingsGoal();

        BigDecimal currentBalance =
                savingsGoal.getCurrentAmount() == null
                        ? BigDecimal.ZERO
                        : savingsGoal.getCurrentAmount();

        BigDecimal amount = transaction.getAmount();

        if (transaction.getType() == TransactionType.DEPOSIT) {
            savingsGoal.setCurrentAmount(
                    currentBalance.subtract(amount)
            );
        } else {
            savingsGoal.setCurrentAmount(
                    currentBalance.add(amount)
            );
        }

        savingsGoalRepository.save(savingsGoal);
        transactionRepository.delete(transaction);

        LOGGER.info(
                "Transaction deleted successfully. Id: {}, type: {}, amount: {}, goal id: {}, updated balance: {}",
                transactionId,
                transaction.getType(),
                amount,
                savingsGoal.getId(),
                savingsGoal.getCurrentAmount()
        );

        return true;
    }
}