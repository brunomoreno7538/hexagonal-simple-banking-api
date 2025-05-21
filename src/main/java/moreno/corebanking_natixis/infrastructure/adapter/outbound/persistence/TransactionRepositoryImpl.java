package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.dto.AdminTransactionFiltersDTO;
import moreno.corebanking_natixis.application.port.out.TransactionFilterParams;
import moreno.corebanking_natixis.application.port.out.TransactionRepository;
import moreno.corebanking_natixis.domain.model.Transaction;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.TransactionJpaEntity;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper.TransactionPersistenceMapper;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository.SpringDataTransactionRepository;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.specification.TransactionSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    public Page<Transaction> findTransactionsFiltered(TransactionFilterParams filters, Pageable pageable) {
        Specification<TransactionJpaEntity> spec = TransactionSpecifications.withFilters(filters);
        Page<TransactionJpaEntity> pageOfEntities = springDataTransactionRepository.findAll(spec, pageable);
        return pageOfEntities.map(mapper::toDomain);
    }

    @Override
    public BigDecimal sumAmountFiltered(TransactionFilterParams filters) {
        BigDecimal sum = springDataTransactionRepository.sumAmountByFilters(
                filters.getAccountId(),
                filters.getStartDateTime(),
                filters.getEndDateTime(),
                filters.getTransactionType()
        );
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public Page<Transaction> findAllSystemTransactionsFiltered(AdminTransactionFiltersDTO filters, Pageable pageable) {
        Specification<TransactionJpaEntity> spec = TransactionSpecifications.withAdminFilters(filters);
        Page<TransactionJpaEntity> pageOfEntities = springDataTransactionRepository.findAll(spec, pageable);
        return pageOfEntities.map(mapper::toDomain);
    }
}