package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.specification;

import jakarta.persistence.criteria.Predicate;
import moreno.corebanking_natixis.application.dto.AdminTransactionFiltersDTO;
import moreno.corebanking_natixis.application.port.out.TransactionFilterParams;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecifications {
    public static Specification<TransactionJpaEntity> withFilters(TransactionFilterParams filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("accountId"), filters.getAccountId()));

            if (filters.getStartDateTime() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), filters.getStartDateTime()));
            }
            if (filters.getEndDateTime() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), filters.getEndDateTime()));
            }
            if (filters.getTransactionType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filters.getTransactionType()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<TransactionJpaEntity> withAdminFilters(AdminTransactionFiltersDTO filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.getAccountId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountId"), filters.getAccountId()));
            }
            if (filters.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), filters.getStartDate()));
            }
            if (filters.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), filters.getEndDate()));
            }
            if (filters.getTransactionType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filters.getTransactionType()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}