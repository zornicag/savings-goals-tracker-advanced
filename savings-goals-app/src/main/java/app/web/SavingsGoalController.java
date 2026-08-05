package app.web;

import app.model.dto.savingsgoal.SavingsGoalForm;
import app.model.entity.category.Category;
import app.model.entity.savingsgoal.SavingsGoal;
import app.model.entity.user.User;
import app.repository.category.CategoryRepository;
import app.repository.user.UserRepository;
import app.service.savingsgoal.SavingsGoalService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public SavingsGoalController(SavingsGoalService savingsGoalService,
                                 UserRepository userRepository,
                                 CategoryRepository categoryRepository) {
        this.savingsGoalService = savingsGoalService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String getGoals(Model model, HttpSession session) {
        UUID currentUserId = getCurrentUserId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));
        model.addAttribute("goals", savingsGoalService.getGoalsByUserId(currentUserId));
        return "goals";
    }

    @GetMapping("/add")
    public String getAddGoal(Model model, HttpSession session) {
        if (getCurrentUserId(session) == null) {
            return "redirect:/login";
        }

        if (categoryRepository.count() == 0) {
            return "redirect:/categories/add";
        }

        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));
        if (!model.containsAttribute("savingsGoalForm")) {
            model.addAttribute("savingsGoalForm", new SavingsGoalForm());
        }
        return "goal-add";
    }

    @PostMapping("/add")
    public String addGoal(@Valid @ModelAttribute("savingsGoalForm") SavingsGoalForm savingsGoalForm,
                          BindingResult bindingResult,
                          Model model,
                          HttpSession session) {
        UUID currentUserId = getCurrentUserId(session);
        if (currentUserId == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));

        if (bindingResult.hasErrors()) {
            return "goal-add";
        }

        Optional<User> currentUser = userRepository.findById(currentUserId);
        if (currentUser.isEmpty()) {
            model.addAttribute("userError", "Please login again.");
            return "goal-add";
        }

        Optional<Category> firstCategory = categoryRepository.findAll().stream().findFirst();
        if (firstCategory.isEmpty()) {
            model.addAttribute("userError", "Please create a category before creating a goal.");
            return "goal-add";
        }

        SavingsGoal savingsGoal = new SavingsGoal();
        savingsGoal.setName(savingsGoalForm.getName().trim());
        savingsGoal.setTargetAmount(savingsGoalForm.getTargetAmount());
        savingsGoal.setCurrentAmount(savingsGoalForm.getCurrentAmount());
        savingsGoal.setDeadline(
                savingsGoalForm.getTargetDate() == null
                        ? null
                        : savingsGoalForm.getTargetDate().atStartOfDay()
        );
        savingsGoal.setCreatedAt(LocalDateTime.now());
        savingsGoal.setUser(currentUser.get());
        savingsGoal.setCategory(firstCategory.get());

        savingsGoalService.save(savingsGoal);
        return "redirect:/goals";
    }

    @GetMapping("/edit/{id}")
    public String getEditGoal(@PathVariable UUID id,
                              Model model,
                              HttpSession session) {

        if (getCurrentUserId(session) == null) {
            return "redirect:/login";
        }

        SavingsGoal goal = savingsGoalService.getById(id);

        if (goal == null) {
            return "redirect:/goals";
        }

        SavingsGoalForm form = new SavingsGoalForm();
        form.setName(goal.getName());
        form.setTargetAmount(goal.getTargetAmount());
        form.setCurrentAmount(goal.getCurrentAmount());
        form.setTargetDate(
                goal.getDeadline() == null
                        ? null
                        : goal.getDeadline().toLocalDate()
        );

        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));
        model.addAttribute("goalId", id);
        model.addAttribute("savingsGoalForm", form);

        return "goal-edit";
    }

    @PostMapping("/edit/{id}")
    public String editGoal(@PathVariable UUID id,
                           @Valid @ModelAttribute("savingsGoalForm") SavingsGoalForm savingsGoalForm,
                           BindingResult bindingResult,
                           Model model,
                           HttpSession session) {

        if (getCurrentUserId(session) == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUsername", session.getAttribute("currentUsername"));
            model.addAttribute("goalId", id);
            return "goal-edit";
        }

        SavingsGoal goal = savingsGoalService.getById(id);

        if (goal == null) {
            return "redirect:/goals";
        }

        goal.setName(savingsGoalForm.getName().trim());
        goal.setTargetAmount(savingsGoalForm.getTargetAmount());
        goal.setCurrentAmount(savingsGoalForm.getCurrentAmount());
        goal.setDeadline(
                savingsGoalForm.getTargetDate() == null
                        ? null
                        : savingsGoalForm.getTargetDate().atStartOfDay()
        );

        savingsGoalService.save(goal);

        return "redirect:/goals";
    }

    @PostMapping("/delete/{id}")
    public String deleteGoal(@PathVariable UUID id,
                             HttpSession session) {

        if (getCurrentUserId(session) == null) {
            return "redirect:/login";
        }

        boolean deleted = savingsGoalService.deleteById(id);

        if (!deleted) {
            return "redirect:/goals?deleteError";
        }

        return "redirect:/goals";
    }

    private UUID getCurrentUserId(HttpSession session) {
        Object currentUserId = session.getAttribute("currentUserId");
        if (currentUserId instanceof UUID uuid) {
            return uuid;
        }
        return null;
    }
}
