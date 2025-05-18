package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.Transaction;
import moreno.corebanking_natixis.domain.model.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreateTransactionUseCase {
    Transaction createTransaction(UUID merchantUserId, UUID accountId, TransactionType type, BigDecimal amount, String description);
}