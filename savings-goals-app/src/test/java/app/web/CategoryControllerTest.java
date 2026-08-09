package app.web;

import app.model.dto.category.CategoryForm;
import app.model.entity.category.Category;
import app.service.category.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void getCategories_ShouldRedirect_WhenUserIsNotLoggedIn() {
        when(session.getAttribute("currentUserId"))
                .thenReturn(null);

        String result =
                categoryController.getCategories(model, session);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getCategories_ShouldReturnCategoriesPage() {
        UUID userId = UUID.randomUUID();

        when(session.getAttribute("currentUserId"))
                .thenReturn(userId);

        when(categoryService.getAllCategories())
                .thenReturn(List.of());

        String result =
                categoryController.getCategories(model, session);

        assertEquals("categories", result);

        verify(model).addAttribute("categories", List.of());
    }

    @Test
    void getAddCategory_ShouldRedirect_WhenUserIsNotLoggedIn() {
        when(session.getAttribute("currentUserId"))
                .thenReturn(null);

        String result =
                categoryController.getAddCategory(model, session);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getAddCategory_ShouldReturnAddPage() {
        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(model.containsAttribute("categoryForm"))
                .thenReturn(false);

        String result =
                categoryController.getAddCategory(model, session);

        assertEquals("category-add", result);

        verify(model)
                .addAttribute(eq("categoryForm"), any(CategoryForm.class));
    }

    @Test
    void getEditCategory_ShouldRedirect_WhenCategoryDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(categoryService.getById(id))
                .thenReturn(null);

        String result =
                categoryController.getEditCategory(id, model, session);

        assertEquals("redirect:/categories", result);
    }

    @Test
    void getEditCategory_ShouldReturnEditPage() {
        UUID id = UUID.randomUUID();

        Category category = new Category();
        category.setName("Travel");
        category.setDescription("Travel expenses");

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(categoryService.getById(id))
                .thenReturn(category);

        String result =
                categoryController.getEditCategory(id, model, session);

        assertEquals("category-edit", result);

        verify(model).addAttribute("categoryId", id);
        verify(model)
                .addAttribute(eq("categoryForm"), any(CategoryForm.class));
    }

    @Test
    void addCategory_ShouldReturnForm_WhenBindingHasErrors() {
        CategoryForm form = new CategoryForm();

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result =
                categoryController.addCategory(
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("category-add", result);
    }

    @Test
    void addCategory_ShouldSaveCategory() {
        CategoryForm form = new CategoryForm();
        form.setName("Travel");
        form.setDescription("Travel expenses");

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(bindingResult.hasErrors())
                .thenReturn(false);

        String result =
                categoryController.addCategory(
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("redirect:/categories", result);

        verify(categoryService).save(any(Category.class));
    }

    @Test
    void editCategory_ShouldReturnForm_WhenBindingHasErrors() {
        UUID id = UUID.randomUUID();

        CategoryForm form = new CategoryForm();

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result =
                categoryController.editCategory(
                        id,
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("category-edit", result);
    }

    @Test
    void editCategory_ShouldRedirect_WhenCategoryDoesNotExist() {
        UUID id = UUID.randomUUID();

        CategoryForm form = new CategoryForm();
        form.setName("Travel");

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(categoryService.getById(id))
                .thenReturn(null);

        String result =
                categoryController.editCategory(
                        id,
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("redirect:/categories", result);
    }

    @Test
    void editCategory_ShouldSaveCategory() {
        UUID id = UUID.randomUUID();

        Category category = new Category();

        CategoryForm form = new CategoryForm();
        form.setName("Updated Travel");
        form.setDescription("Updated description");

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(categoryService.getById(id))
                .thenReturn(category);

        String result =
                categoryController.editCategory(
                        id,
                        form,
                        bindingResult,
                        model,
                        session
                );

        assertEquals("redirect:/categories", result);

        verify(categoryService).save(category);
    }

    @Test
    void deleteCategory_ShouldReturnError_WhenDeleteFails() {
        UUID id = UUID.randomUUID();

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(categoryService.deleteById(id))
                .thenReturn(false);

        String result =
                categoryController.deleteCategory(id, session);

        assertEquals(
                "redirect:/categories?deleteError",
                result
        );
    }

    @Test
    void deleteCategory_ShouldRedirect_WhenDeleteSucceeds() {
        UUID id = UUID.randomUUID();

        when(session.getAttribute("currentUserId"))
                .thenReturn(UUID.randomUUID());

        when(categoryService.deleteById(id))
                .thenReturn(true);

        String result =
                categoryController.deleteCategory(id, session);

        assertEquals("redirect:/categories", result);
    }
}
