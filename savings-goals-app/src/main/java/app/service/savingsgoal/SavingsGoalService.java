package app.service.savingsgoal;

import app.model.entity.savingsgoal.SavingsGoal;
import app.repository.savingsgoal.SavingsGoalRepository;
import app.repository.transaction.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SavingsGoalService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SavingsGoalService.class);

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository,
                              TransactionRepository transactionRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<SavingsGoal> getGoalsByUserId(UUID userId) {

        LOGGER.info(
                "Loading savings goals for user with id: {}",
                userId
        );

        return savingsGoalRepository.findByUser_Id(userId);
    }

    public SavingsGoal getById(UUID id) {

        LOGGER.info(
                "Loading savings goal with id: {}",
                id
        );

        return savingsGoalRepository.findById(id).orElse(null);
    }

    public SavingsGoal save(SavingsGoal savingsGoal) {

        SavingsGoal savedGoal =
                savingsGoalRepository.save(savingsGoal);

        LOGGER.info(
                "Savings goal saved successfully. Id: {}, name: {}",
                savedGoal.getId(),
                savedGoal.getName()
        );

        return savedGoal;
    }

    public boolean deleteById(UUID id) {

        if (transactionRepository.existsBySavingsGoal_Id(id)) {

            LOGGER.warn(
                    "Savings goal with id {} cannot be deleted because it has related transactions",
                    id
            );

            return false;
        }

        savingsGoalRepository.deleteById(id);

        LOGGER.info(
                "Savings goal deleted successfully. Id: {}",
                id
        );

        return true;
    }
}