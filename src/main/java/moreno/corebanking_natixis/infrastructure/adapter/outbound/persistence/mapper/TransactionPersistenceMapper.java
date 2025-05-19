package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper;

import moreno.corebanking_natixis.domain.model.Transaction;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.TransactionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {

    public TransactionJpaEntity toJpaEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new TransactionJpaEntity(
                transaction.getTransactionId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                transaction.getDescription(),
                transaction.getStatus()
        );
    }

    public Transaction toDomain(TransactionJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Transaction.builder()
                .transactionId(jpaEntity.getTransactionId())
                .accountId(jpaEntity.getAccountId())
                .type(jpaEntity.getType())
                .amount(jpaEntity.getAmount())
                .timestamp(jpaEntity.getTimestamp())
                .description(jpaEntity.getDescription())
                .status(jpaEntity.getStatus())
                .build();
    }
}