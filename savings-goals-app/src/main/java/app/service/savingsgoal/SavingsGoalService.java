package app.service.savingsgoal;

import app.model.entity.savingsgoal.SavingsGoal;
import app.repository.savingsgoal.SavingsGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.repository.transaction.TransactionRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository,
                              TransactionRepository transactionRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<SavingsGoal> getGoalsByUserId(UUID userId) {
        return savingsGoalRepository.findByUser_Id(userId);
    }

    public SavingsGoal getById(UUID id) {
        return savingsGoalRepository.findById(id).orElse(null);
    }

    public SavingsGoal save(SavingsGoal savingsGoal) {
        return savingsGoalRepository.save(savingsGoal);
    }

    public boolean deleteById(UUID id) {

        if (transactionRepository.existsBySavingsGoal_Id(id)) {
            return false;
        }

        savingsGoalRepository.deleteById(id);
        return true;
    }
}
