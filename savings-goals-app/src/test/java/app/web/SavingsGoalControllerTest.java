package app.web;

import app.model.dto.savingsgoal.SavingsGoalForm;
import app.model.entity.category.Category;
import app.model.entity.savingsgoal.SavingsGoal;
import app.model.entity.user.User;
import app.repository.category.CategoryRepository;
import app.repository.user.UserRepository;
import app.service.savingsgoal.SavingsGoalService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalControllerTest {

    @Mock
    private SavingsGoalService savingsGoalService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private SavingsGoalController savingsGoalController;

    @Test
    void getGoals_ShouldRedirect_WhenUserIsNotLoggedIn() {
        when(session.getAttribute("currentUserId"))
                .thenReturn(null);

        String result =
                savingsGoalController.getGoals(model, session);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getGoals_ShouldReturnGoalsPage() {
        UUID userId = UUID.randomUUID();

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(savingsGoalService.getGoalsByUserId(userId))
                .thenReturn(List.of());

        String result =
                savingsGoalController.getGoals(model, session);

        assertEquals("goals", result);

        verify(model).addAttribute("goals", List.of());
    }

    @Test
    void getAddGoal_ShouldRedirect_WhenUserIsNotLoggedIn() {
        when(session.getAttribute("currentUserId"))
                .thenReturn(null);

        String result =
                savingsGoalController.getAddGoal(model, session);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getAddGoal_ShouldRedirectToCategories_WhenNoCategoriesExist() {
        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(categoryRepository.count())
                .thenReturn(0L);

        String result =
                savingsGoalController.getAddGoal(model, session);

        assertEquals("redirect:/categories/add", result);
    }

    @Test
    void getAddGoal_ShouldReturnAddPage() {
        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(categoryRepository.count())
                .thenReturn(1L);

        when(model.containsAttribute("savingsGoalForm"))
                .thenReturn(false);

        String result =
                savingsGoalController.getAddGoal(model, session);

        assertEquals("goal-add", result);

        verify(model)
                .addAttribute(eq("savingsGoalForm"), any(SavingsGoalForm.class));
    }

    @Test
    void addGoal_ShouldReturnForm_WhenBindingHasErrors() {
        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result =
                savingsGoalController.addGoal(
                        new SavingsGoalForm(),
                        bindingResult,
                        model,
                        session
                );

        assertEquals("goal-add", result);
    }

    @Test
    void addGoal_ShouldCreateGoal() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        Category category = new Category();

        SavingsGoalForm form = new SavingsGoalForm();
        form.setName("Vacation");
        form.setTargetAmount(new BigDecimal("1000"));
        form.setCurrentAmount(new BigDecimal("100"));
        form.setTargetDate(LocalDate.now().plusMonths(2));

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(categoryRepository.findAll())
                .thenReturn(List.of(category));

        String result =
                savingsGoalController.addGoal(
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("redirect:/goals", result);

        verify(savingsGoalService)
                .save(any(SavingsGoal.class));
    }

    @Test
    void getEditGoal_ShouldRedirect_WhenGoalDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(savingsGoalService.getById(id))
                .thenReturn(null);

        String result =
                savingsGoalController.getEditGoal(id, model, session);

        assertEquals("redirect:/goals", result);
    }

    @Test
    void getEditGoal_ShouldReturnEditPage() {
        UUID id = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();
        goal.setName("Car");
        goal.setTargetAmount(new BigDecimal("5000"));
        goal.setCurrentAmount(new BigDecimal("500"));
        goal.setDeadline(LocalDateTime.now().plusMonths(3));

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(savingsGoalService.getById(id))
                .thenReturn(goal);

        String result =
                savingsGoalController.getEditGoal(id, model, session);

        assertEquals("goal-edit", result);

        verify(model).addAttribute("goalId", id);
    }

    @Test
    void editGoal_ShouldUpdateGoal() {
        UUID id = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();

        SavingsGoalForm form = new SavingsGoalForm();
        form.setName("Updated Goal");
        form.setTargetAmount(new BigDecimal("2000"));
        form.setCurrentAmount(new BigDecimal("300"));

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(savingsGoalService.getById(id))
                .thenReturn(goal);

        String result =
                savingsGoalController.editGoal(
                        id,
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("redirect:/goals", result);

        verify(savingsGoalService).save(goal);
    }

    @Test
    void deleteGoal_ShouldReturnDeleteError_WhenDeleteFails() {
        UUID id = UUID.randomUUID();

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(savingsGoalService.deleteById(id))
                .thenReturn(false);

        String result =
                savingsGoalController.deleteGoal(id, session);

        assertEquals("redirect:/goals?deleteError", result);
    }

    @Test
    void deleteGoal_ShouldRedirectToGoals_WhenDeleteSucceeds() {
        UUID id = UUID.randomUUID();

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(savingsGoalService.deleteById(id))
                .thenReturn(true);

        String result =
                savingsGoalController.deleteGoal(id, session);

        assertEquals("redirect:/goals", result);
    }
}