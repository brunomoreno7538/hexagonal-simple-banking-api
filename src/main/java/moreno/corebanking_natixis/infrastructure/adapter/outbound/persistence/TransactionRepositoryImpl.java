package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.out.TransactionRepository;
import moreno.corebanking_natixis.domain.model.Transaction;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.TransactionJpaEntity;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper.TransactionPersistenceMapper;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository.SpringDataTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final SpringDataTransactionRepository springDataTransactionRepository;
    private final TransactionPersistenceMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(springDataTransactionRepository.save(mapper.toJpaEntity(transaction)));
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        return springDataTransactionRepository.findById(transactionId).map(mapper::toDomain);
    }

    @Override
    public Page<Transaction> findByAccountId(UUID accountId, Pageable pageable) {
        Page<TransactionJpaEntity> pageOfEntities = springDataTransactionRepository.findByAccountId(accountId, pageable);
        return pageOfEntities.map(mapper::toDomain);
    }
}