package moreno.corebanking_natixis.application.port.out;

import moreno.corebanking_natixis.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID transactionId);
    Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);
}