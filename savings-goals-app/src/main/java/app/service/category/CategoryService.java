package app.service.category;

import app.model.entity.category.Category;
import app.repository.category.CategoryRepository;
import app.repository.savingsgoal.SavingsGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           SavingsGoalRepository savingsGoalRepository) {
        this.categoryRepository = categoryRepository;
        this.savingsGoalRepository = savingsGoalRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getById(UUID id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public boolean deleteById(UUID id) {
        if (savingsGoalRepository.existsByCategory_Id(id)) {
            return false;
        }

        categoryRepository.deleteById(id);
        return true;
    }
}
