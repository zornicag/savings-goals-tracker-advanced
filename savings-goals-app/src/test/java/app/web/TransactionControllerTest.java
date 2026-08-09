package app.web;

import app.model.dto.transaction.TransactionForm;
import app.model.entity.savingsgoal.SavingsGoal;
import app.model.entity.transaction.Transaction;
import app.model.entity.transaction.TransactionType;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.service.transaction.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void getTransactions_ShouldRedirect_WhenUserIsNotLoggedIn() {
        when(session.getAttribute("currentUserId")).thenReturn(null);

        String result =
                transactionController.getTransactions(model, session);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getTransactions_ShouldReturnTransactionsPage() {
        UUID userId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(UUID.randomUUID());
        goal.setName("Vacation");

        Transaction transaction = new Transaction();
        transaction.setSavingsGoal(goal);

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(transactionService.getAllTransactionsForUser(userId))
                .thenReturn(List.of(transaction));

        String result =
                transactionController.getTransactions(model, session);

        assertEquals("transactions", result);

        verify(model)
                .addAttribute(eq("transactionGroups"), any());
    }

    @Test
    void getAddTransaction_ShouldRedirect_WhenUserIsNotLoggedIn() {
        when(session.getAttribute("currentUserId")).thenReturn(null);

        String result =
                transactionController.getAddTransaction(model, session);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getAddTransaction_ShouldReturnAddPage() {
        UUID userId = UUID.randomUUID();

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(model.containsAttribute("transactionForm"))
                .thenReturn(false);

        when(savingsGoalRepository.findByUser_Id(userId))
                .thenReturn(List.of());

        String result =
                transactionController.getAddTransaction(model, session);

        assertEquals("transaction-add", result);

        verify(model)
                .addAttribute(eq("transactionForm"), any(TransactionForm.class));

        verify(model)
                .addAttribute("transactionTypes", TransactionType.values());
    }

    @Test
    void addTransaction_ShouldRedirect_WhenUserIsNotLoggedIn() {
        TransactionForm form = new TransactionForm();

        when(session.getAttribute("currentUserId"))
                .thenReturn(null);

        String result =
                transactionController.addTransaction(
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("redirect:/login", result);
    }

    @Test
    void addTransaction_ShouldShowError_WhenUserHasNoGoals() {
        UUID userId = UUID.randomUUID();

        TransactionForm form = new TransactionForm();

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(savingsGoalRepository.findByUser_Id(userId))
                .thenReturn(List.of());

        String result =
                transactionController.addTransaction(
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("transaction-add", result);

        verify(model).addAttribute(
                "goalError",
                "Please create a savings goal before adding a transaction."
        );
    }

    @Test
    void addTransaction_ShouldReturnForm_WhenBindingHasErrors() {
        UUID userId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(UUID.randomUUID());

        TransactionForm form = new TransactionForm();

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(savingsGoalRepository.findByUser_Id(userId))
                .thenReturn(List.of(goal));

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result =
                transactionController.addTransaction(
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("transaction-add", result);
    }

    @Test
    void addTransaction_ShouldRejectInvalidGoal() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(UUID.randomUUID());

        TransactionForm form = new TransactionForm();
        form.setSavingsGoalId(goalId);

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(savingsGoalRepository.findByUser_Id(userId))
                .thenReturn(List.of(goal));

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.empty());

        String result =
                transactionController.addTransaction(
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("transaction-add", result);

        verify(bindingResult).rejectValue(
                "savingsGoalId",
                "invalid",
                "Savings goal is required"
        );
    }

    @Test
    void addTransaction_ShouldSaveDeposit() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);

        TransactionForm form = new TransactionForm();
        form.setSavingsGoalId(goalId);
        form.setType(TransactionType.DEPOSIT);
        form.setAmount(new BigDecimal("100.00"));
        form.setDescription("Deposit");

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(savingsGoalRepository.findByUser_Id(userId))
                .thenReturn(List.of(goal));

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.of(goal));

        when(transactionService.applyAndSave(any(Transaction.class)))
                .thenReturn(true);

        String result =
                transactionController.addTransaction(
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("redirect:/transactions", result);

        verify(transactionService)
                .applyAndSave(any(Transaction.class));
    }

    @Test
    void addTransaction_ShouldShowError_WhenWithdrawalFails() {
        UUID userId = UUID.randomUUID();
        UUID goalId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);

        TransactionForm form = new TransactionForm();
        form.setSavingsGoalId(goalId);
        form.setType(TransactionType.WITHDRAW);
        form.setAmount(new BigDecimal("500.00"));
        form.setDescription("Withdraw");

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(savingsGoalRepository.findByUser_Id(userId))
                .thenReturn(List.of(goal));

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.of(goal));

        when(transactionService.applyAndSave(any(Transaction.class)))
                .thenReturn(false);

        String result =
                transactionController.addTransaction(
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("transaction-add", result);

        verify(model).addAttribute(
                "goalError",
                "Not enough money in this goal"
        );
    }
}
