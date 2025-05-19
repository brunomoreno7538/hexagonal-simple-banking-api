package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface GetTransactionsByAccountIdUseCase {
    Page<Transaction> getTransactions(UUID merchantUserId, UUID accountId, Pageable pageable);
}