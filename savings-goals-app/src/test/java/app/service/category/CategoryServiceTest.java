package app.service.category;

import app.model.entity.category.Category;
import app.repository.category.CategoryRepository;
import app.repository.savingsgoal.SavingsGoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getAllCategories_ShouldReturnCategories() {
        Category category = new Category();

        when(categoryRepository.findAll())
                .thenReturn(List.of(category));

        List<Category> result =
                categoryService.getAllCategories();

        assertEquals(1, result.size());

        verify(categoryRepository).findAll();
    }

    @Test
    void getById_ShouldReturnCategory_WhenCategoryExists() {
        UUID id = UUID.randomUUID();

        Category category = new Category();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));

        Category result =
                categoryService.getById(id);

        assertNotNull(result);
        assertSame(category, result);
    }

    @Test
    void getById_ShouldReturnNull_WhenCategoryDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        Category result =
                categoryService.getById(id);

        assertNull(result);
    }

    @Test
    void save_ShouldSaveCategory() {
        Category category = new Category();

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result =
                categoryService.save(category);

        assertSame(category, result);

        verify(categoryRepository).save(category);
    }

    @Test
    void deleteById_ShouldReturnFalse_WhenCategoryIsUsedByGoal() {
        UUID id = UUID.randomUUID();

        when(savingsGoalRepository.existsByCategory_Id(id))
                .thenReturn(true);

        boolean result =
                categoryService.deleteById(id);

        assertFalse(result);

        verify(categoryRepository, never())
                .deleteById(any());
    }

    @Test
    void deleteById_ShouldDeleteCategory_WhenCategoryIsNotUsed() {
        UUID id = UUID.randomUUID();

        when(savingsGoalRepository.existsByCategory_Id(id))
                .thenReturn(false);

        boolean result =
                categoryService.deleteById(id);

        assertTrue(result);

        verify(categoryRepository).deleteById(id);
    }
}
