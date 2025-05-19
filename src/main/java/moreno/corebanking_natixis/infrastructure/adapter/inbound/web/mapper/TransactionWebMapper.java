package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper;

import moreno.corebanking_natixis.domain.model.Transaction;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionWebMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(transaction.getAccountId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .build();
    }
}