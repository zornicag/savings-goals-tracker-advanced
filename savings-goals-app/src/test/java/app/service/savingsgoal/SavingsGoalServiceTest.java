package app.service.savingsgoal;

import app.model.entity.savingsgoal.SavingsGoal;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.repository.transaction.TransactionRepository;
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
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    @Test
    void getGoalsByUserId_ShouldReturnUserGoals() {
        UUID userId = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();

        when(savingsGoalRepository.findByUser_Id(userId))
                .thenReturn(List.of(goal));

        List<SavingsGoal> result =
                savingsGoalService.getGoalsByUserId(userId);

        assertEquals(1, result.size());

        verify(savingsGoalRepository)
                .findByUser_Id(userId);
    }

    @Test
    void getById_ShouldReturnGoal_WhenGoalExists() {
        UUID id = UUID.randomUUID();

        SavingsGoal goal = new SavingsGoal();

        when(savingsGoalRepository.findById(id))
                .thenReturn(Optional.of(goal));

        SavingsGoal result =
                savingsGoalService.getById(id);

        assertNotNull(result);
        assertSame(goal, result);
    }

    @Test
    void getById_ShouldReturnNull_WhenGoalDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(savingsGoalRepository.findById(id))
                .thenReturn(Optional.empty());

        SavingsGoal result =
                savingsGoalService.getById(id);

        assertNull(result);
    }

    @Test
    void save_ShouldSaveSavingsGoal() {
        SavingsGoal goal = new SavingsGoal();

        when(savingsGoalRepository.save(goal))
                .thenReturn(goal);

        SavingsGoal result =
                savingsGoalService.save(goal);

        assertSame(goal, result);

        verify(savingsGoalRepository)
                .save(goal);
    }

    @Test
    void deleteById_ShouldReturnFalse_WhenGoalHasTransactions() {
        UUID id = UUID.randomUUID();

        when(transactionRepository.existsBySavingsGoal_Id(id))
                .thenReturn(true);

        boolean result =
                savingsGoalService.deleteById(id);

        assertFalse(result);

        verify(savingsGoalRepository, never())
                .deleteById(any());
    }

    @Test
    void deleteById_ShouldDeleteGoal_WhenNoTransactionsExist() {
        UUID id = UUID.randomUUID();

        when(transactionRepository.existsBySavingsGoal_Id(id))
                .thenReturn(false);

        boolean result =
                savingsGoalService.deleteById(id);

        assertTrue(result);

        verify(savingsGoalRepository)
                .deleteById(id);
    }
}
