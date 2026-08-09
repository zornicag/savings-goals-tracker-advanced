package app.web;

import app.model.dto.category.CategoryForm;
import app.model.entity.category.Category;
import app.service.category.CategoryService;
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

import java.util.UUID;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String getCategories(Model model, HttpSession session) {
        if (session.getAttribute("currentUserId") == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @GetMapping("/add")
    public String getAddCategory(Model model, HttpSession session) {
        if (session.getAttribute("currentUserId") == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }
        return "category-add";
    }

    @GetMapping("/edit/{id}")
    public String getEditCategory(@PathVariable UUID id,
                                  Model model,
                                  HttpSession session) {
        if (session.getAttribute("currentUserId") == null) {
            return "redirect:/login";
        }

        Category category = categoryService.getById(id);

        if (category == null) {
            return "redirect:/categories";
        }

        CategoryForm categoryForm = new CategoryForm();
        categoryForm.setName(category.getName());
        categoryForm.setDescription(category.getDescription());

        model.addAttribute("categoryId", id);
        model.addAttribute("categoryForm", categoryForm);
        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));

        return "category-edit";
    }

    @PostMapping("/add")
    public String addCategory(@Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
                              BindingResult bindingResult,
                              Model model,
                              HttpSession session) {
        if (session.getAttribute("currentUserId") == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUsername", session.getAttribute("currentUsername"));
        if (bindingResult.hasErrors()) {
            return "category-add";
        }

        Category category = new Category();
        category.setName(categoryForm.getName().trim());
        category.setDescription(categoryForm.getDescription());

        categoryService.save(category);
        return "redirect:/categories";
    }

    @PostMapping("/edit/{id}")
    public String editCategory(@PathVariable UUID id,
                               @Valid @ModelAttribute("categoryForm") CategoryForm categoryForm,
                               BindingResult bindingResult,
                               Model model,
                               HttpSession session) {
        if (session.getAttribute("currentUserId") == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            model.addAttribute("currentUsername", session.getAttribute("currentUsername"));
            return "category-edit";
        }

        Category category = categoryService.getById(id);

        if (category == null) {
            return "redirect:/categories";
        }

        category.setName(categoryForm.getName().trim());
        category.setDescription(categoryForm.getDescription());

        categoryService.save(category);

        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable UUID id,
                                 HttpSession session) {
        if (session.getAttribute("currentUserId") == null) {
            return "redirect:/login";
        }

        boolean deleted = categoryService.deleteById(id);

        if (!deleted) {
            return "redirect:/categories?deleteError";
        }

        return "redirect:/categories";
    }
}
