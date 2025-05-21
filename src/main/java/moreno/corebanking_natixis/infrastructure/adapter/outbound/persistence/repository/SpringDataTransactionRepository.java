package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository;

import moreno.corebanking_natixis.domain.model.TransactionType;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID>, JpaSpecificationExecutor<TransactionJpaEntity> {

    @Query("SELECT SUM(t.amount) FROM TransactionJpaEntity t " +
            "WHERE t.accountId = :accountId " +
            "AND (:startDateTime IS NULL OR t.timestamp >= :startDateTime) " +
            "AND (:endDateTime IS NULL OR t.timestamp <= :endDateTime) " +
            "AND (:transactionType IS NULL OR t.type = :transactionType)")
    BigDecimal sumAmountByFilters(
            @Param("accountId") UUID accountId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("transactionType") TransactionType transactionType
    );
}