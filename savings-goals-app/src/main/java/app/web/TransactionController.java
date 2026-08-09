package app.web;

import app.model.dto.transaction.TransactionForm;
import app.model.entity.savingsgoal.SavingsGoal;
import app.model.entity.transaction.Transaction;
import app.model.entity.transaction.TransactionType;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.service.transaction.TransactionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final SavingsGoalRepository savingsGoalRepository;

    public TransactionController(TransactionService transactionService,
                                 SavingsGoalRepository savingsGoalRepository) {
        this.transactionService = transactionService;
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @GetMapping
    public String getTransactions(Model model, HttpSession session) {
        UUID currentUserId = getCurrentUserId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));

        List<Transaction> sortedTransactions =
                new ArrayList<>(transactionService.getAllTransactionsForUser(currentUserId));

        sortedTransactions.sort(
                Comparator.comparing(
                        Transaction::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        Map<UUID, GoalTransactionGroup> groupedByGoal = new LinkedHashMap<>();

        for (Transaction transaction : sortedTransactions) {
            SavingsGoal goal = transaction.getSavingsGoal();

            UUID goalId = goal != null
                    ? goal.getId()
                    : UUID.randomUUID();

            String goalName = goal != null
                    ? goal.getName()
                    : "Unknown goal";

            GoalTransactionGroup group =
                    groupedByGoal.computeIfAbsent(
                            goalId,
                            id -> new GoalTransactionGroup(goalName)
                    );

            group.getTransactions().add(transaction);
        }

        model.addAttribute("transactionGroups", groupedByGoal.values());

        return "transactions";
    }

    @GetMapping("/add")
    public String getAddTransaction(Model model,
                                    HttpSession session) {

        UUID currentUserId = getCurrentUserId(session);

        if (currentUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "currentUsername",
                session.getAttribute("currentUsername")
        );

        if (!model.containsAttribute("transactionForm")) {
            model.addAttribute(
                    "transactionForm",
                    new TransactionForm()
            );
        }

        addFormModelAttributes(model, currentUserId);

        return "transaction-add";
    }

    @PostMapping("/add")
    public String addTransaction(
            @Valid @ModelAttribute("transactionForm")
            TransactionForm transactionForm,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {

        UUID currentUserId = getCurrentUserId(session);

        if (currentUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "currentUsername",
                session.getAttribute("currentUsername")
        );

        List<SavingsGoal> goals =
                savingsGoalRepository.findByUser_Id(currentUserId);

        if (goals.isEmpty()) {
            addFormModelAttributes(model, currentUserId);

            model.addAttribute(
                    "goalError",
                    "Please create a savings goal before adding a transaction."
            );

            return "transaction-add";
        }

        if (bindingResult.hasErrors()) {
            addFormModelAttributes(model, currentUserId);

            return "transaction-add";
        }

        Optional<SavingsGoal> selectedGoal =
                savingsGoalRepository.findById(
                        transactionForm.getSavingsGoalId()
                );

        if (selectedGoal.isEmpty()
                || !goals.stream()
                .anyMatch(goal ->
                        goal.getId()
                                .equals(selectedGoal.get().getId()))) {

            bindingResult.rejectValue(
                    "savingsGoalId",
                    "invalid",
                    "Savings goal is required"
            );

            addFormModelAttributes(model, currentUserId);

            return "transaction-add";
        }

        Transaction transaction = new Transaction();

        transaction.setType(transactionForm.getType());
        transaction.setAmount(transactionForm.getAmount());
        transaction.setDescription(transactionForm.getDescription());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setSavingsGoal(selectedGoal.get());

        boolean isSaved =
                transactionService.applyAndSave(transaction);

        if (!isSaved
                && transactionForm.getType()
                == TransactionType.WITHDRAW) {

            addFormModelAttributes(model, currentUserId);

            model.addAttribute(
                    "goalError",
                    "Not enough money in this goal"
            );

            return "transaction-add";
        }

        return "redirect:/transactions";
    }

    @PostMapping("/{id}/delete")
    public String deleteTransaction(
            @PathVariable UUID id,
            HttpSession session) {

        UUID currentUserId = getCurrentUserId(session);

        if (currentUserId == null) {
            return "redirect:/login";
        }

        boolean belongsToCurrentUser =
                transactionService
                        .getAllTransactionsForUser(currentUserId)
                        .stream()
                        .anyMatch(transaction ->
                                transaction.getId().equals(id));

        if (!belongsToCurrentUser) {
            return "redirect:/transactions";
        }

        transactionService.deleteById(id);

        return "redirect:/transactions?deleted";
    }

    private void addFormModelAttributes(
            Model model,
            UUID currentUserId) {

        model.addAttribute(
                "transactionTypes",
                TransactionType.values()
        );

        model.addAttribute(
                "goals",
                savingsGoalRepository.findByUser_Id(currentUserId)
        );
    }

    private UUID getCurrentUserId(HttpSession session) {
        Object currentUserId =
                session.getAttribute("currentUserId");

        if (currentUserId instanceof UUID uuid) {
            return uuid;
        }

        return null;
    }

    public static class GoalTransactionGroup {

        private final String goalName;

        private final List<Transaction> transactions =
                new ArrayList<>();

        public GoalTransactionGroup(String goalName) {
            this.goalName = goalName;
        }

        public String getGoalName() {
            return goalName;
        }

        public List<Transaction> getTransactions() {
            return transactions;
        }
    }
}