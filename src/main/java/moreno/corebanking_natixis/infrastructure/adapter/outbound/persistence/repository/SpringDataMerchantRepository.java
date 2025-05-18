package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository;

import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.MerchantJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataMerchantRepository extends JpaRepository<MerchantJpaEntity, UUID> {
    Optional<MerchantJpaEntity> findByCnpjAndActiveTrue(String cnpj);
    boolean existsByCnpjAndActiveTrue(String cnpj);
    Page<MerchantJpaEntity> findAllByActiveTrue(Pageable pageable);
    Optional<MerchantJpaEntity> findByIdAndActiveTrue(UUID merchantId);
}