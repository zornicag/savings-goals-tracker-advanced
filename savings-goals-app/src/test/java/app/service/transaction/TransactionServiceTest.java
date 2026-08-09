package app.service.transaction;

import app.model.entity.savingsgoal.SavingsGoal;
import app.model.entity.transaction.Transaction;
import app.model.entity.transaction.TransactionType;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.repository.transaction.TransactionRepository;
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
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void getAllTransactionsForUser_ShouldReturnTransactions() {
        UUID userId = UUID.randomUUID();

        Transaction transaction = new Transaction();

        when(transactionRepository.findBySavingsGoal_User_Id(userId))
                .thenReturn(List.of(transaction));

        List<Transaction> result =
                transactionService.getAllTransactionsForUser(userId);

        assertEquals(1, result.size());

        verify(transactionRepository)
                .findBySavingsGoal_User_Id(userId);
    }

    @Test
    void applyAndSave_ShouldReturnFalse_WhenGoalDoesNotExist() {
        UUID goalId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);

        Transaction transaction = new Transaction();
        transaction.setSavingsGoal(goal);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.empty());

        boolean result =
                transactionService.applyAndSave(transaction);

        assertFalse(result);

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void applyAndSave_ShouldDepositSuccessfully() {
        UUID goalId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setCurrentAmount(new BigDecimal("100.00"));

        Transaction transaction = new Transaction();
        transaction.setSavingsGoal(goal);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("50.00"));

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.of(goal));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        boolean result =
                transactionService.applyAndSave(transaction);

        assertTrue(result);
        assertEquals(new BigDecimal("150.00"), goal.getCurrentAmount());
        assertEquals(new BigDecimal("150.00"), transaction.getBalanceAfter());

        verify(savingsGoalRepository).save(goal);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void applyAndSave_ShouldUseZero_WhenCurrentAmountIsNull() {
        UUID goalId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setCurrentAmount(null);

        Transaction transaction = new Transaction();
        transaction.setSavingsGoal(goal);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("25.00"));

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.of(goal));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        boolean result =
                transactionService.applyAndSave(transaction);

        assertTrue(result);
        assertEquals(new BigDecimal("25.00"), goal.getCurrentAmount());
    }

    @Test
    void applyAndSave_ShouldWithdrawSuccessfully() {
        UUID goalId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setCurrentAmount(new BigDecimal("100.00"));

        Transaction transaction = new Transaction();
        transaction.setSavingsGoal(goal);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(new BigDecimal("40.00"));

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.of(goal));

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        boolean result =
                transactionService.applyAndSave(transaction);

        assertTrue(result);
        assertEquals(new BigDecimal("60.00"), goal.getCurrentAmount());
        assertEquals(new BigDecimal("60.00"), transaction.getBalanceAfter());
    }

    @Test
    void applyAndSave_ShouldRejectWithdrawal_WhenAmountIsGreaterThanBalance() {
        UUID goalId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setCurrentAmount(new BigDecimal("50.00"));

        Transaction transaction = new Transaction();
        transaction.setSavingsGoal(goal);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(new BigDecimal("100.00"));

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.of(goal));

        boolean result =
                transactionService.applyAndSave(transaction);

        assertFalse(result);

        verify(savingsGoalRepository, never())
                .save(any());

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void deleteById_ShouldReturnFalse_WhenTransactionDoesNotExist() {
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        boolean result =
                transactionService.deleteById(transactionId);

        assertFalse(result);

        verify(transactionRepository, never())
                .delete(any());
    }

    @Test
    void deleteById_ShouldReverseDeposit() {
        UUID transactionId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setCurrentAmount(new BigDecimal("150.00"));

        Transaction transaction = new Transaction();
        transaction.setSavingsGoal(goal);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("50.00"));

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        boolean result =
                transactionService.deleteById(transactionId);

        assertTrue(result);
        assertEquals(new BigDecimal("100.00"), goal.getCurrentAmount());

        verify(savingsGoalRepository).save(goal);
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteById_ShouldReverseWithdrawal() {
        UUID transactionId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setCurrentAmount(new BigDecimal("60.00"));

        Transaction transaction = new Transaction();
        transaction.setSavingsGoal(goal);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(new BigDecimal("40.00"));

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        boolean result =
                transactionService.deleteById(transactionId);

        assertTrue(result);
        assertEquals(new BigDecimal("100.00"), goal.getCurrentAmount());

        verify(savingsGoalRepository).save(goal);
        verify(transactionRepository).delete(transaction);
    }
}
