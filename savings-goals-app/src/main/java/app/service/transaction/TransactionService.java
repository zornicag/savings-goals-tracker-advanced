package app.service.transaction;

import app.model.entity.savingsgoal.SavingsGoal;
import app.model.entity.transaction.Transaction;
import app.model.entity.transaction.TransactionType;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.repository.transaction.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              SavingsGoalRepository savingsGoalRepository) {
        this.transactionRepository = transactionRepository;
        this.savingsGoalRepository = savingsGoalRepository;
    }

    public List<Transaction> getAllTransactionsForUser(UUID userId) {
        return transactionRepository.findBySavingsGoal_User_Id(userId);
    }

    public boolean applyAndSave(Transaction transaction) {
        SavingsGoal savingsGoal = savingsGoalRepository.findById(transaction.getSavingsGoal().getId()).orElse(null);
        if (savingsGoal == null) {
            return false;
        }

        BigDecimal oldBalance = savingsGoal.getCurrentAmount() == null
                ? BigDecimal.ZERO
                : savingsGoal.getCurrentAmount();

        BigDecimal amount = transaction.getAmount();

        if (transaction.getType() == TransactionType.WITHDRAW && amount.compareTo(oldBalance) > 0) {
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
        transactionRepository.save(transaction);
        return true;
    }
}
