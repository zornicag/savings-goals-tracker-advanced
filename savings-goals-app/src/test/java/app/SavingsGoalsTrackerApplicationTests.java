package app;

import app.repository.category.CategoryRepository;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.repository.transaction.TransactionRepository;
import app.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
class SavingsGoalsTrackerApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private SavingsGoalRepository savingsGoalRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @Test
    void contextLoads() {
    }

}
