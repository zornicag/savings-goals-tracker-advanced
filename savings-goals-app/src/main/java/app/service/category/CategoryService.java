package app.service.category;

import app.model.entity.category.Category;
import app.repository.category.CategoryRepository;
import app.repository.savingsgoal.SavingsGoalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoryService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final SavingsGoalRepository savingsGoalRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           SavingsGoalRepository savingsGoalRepository) {
        this.categoryRepository = categoryRepository;
        this.savingsGoalRepository = savingsGoalRepository;
    }

    public List<Category> getAllCategories() {

        LOGGER.info("Loading all categories");

        return categoryRepository.findAll();
    }

    public Category getById(UUID id) {

        LOGGER.info(
                "Loading category with id: {}",
                id
        );

        return categoryRepository.findById(id).orElse(null);
    }

    public Category save(Category category) {

        Category savedCategory =
                categoryRepository.save(category);

        LOGGER.info(
                "Category saved successfully. Id: {}, name: {}",
                savedCategory.getId(),
                savedCategory.getName()
        );

        return savedCategory;
    }

    public boolean deleteById(UUID id) {

        if (savingsGoalRepository.existsByCategory_Id(id)) {

            LOGGER.warn(
                    "Category with id {} cannot be deleted because it is used by an existing savings goal",
                    id
            );

            return false;
        }

        categoryRepository.deleteById(id);

        LOGGER.info(
                "Category deleted successfully. Id: {}",
                id
        );

        return true;
    }
}