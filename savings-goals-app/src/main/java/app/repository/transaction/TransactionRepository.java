package app.repository.transaction;

import app.model.entity.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findBySavingsGoal_User_Id(UUID userId);

    boolean existsBySavingsGoal_Id(UUID savingsGoalId);

    void deleteBySavingsGoal_User_Id(UUID userId);
}