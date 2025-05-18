package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository;

import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.MerchantUserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataMerchantUserRepository extends JpaRepository<MerchantUserJpaEntity, UUID> {
    Optional<MerchantUserJpaEntity> findByUsernameAndActiveTrue(String username);
    Optional<MerchantUserJpaEntity> findByEmailAndActiveTrue(String email);
    Page<MerchantUserJpaEntity> findByMerchantIdAndActiveTrue(UUID merchantId, Pageable pageable);
    Page<MerchantUserJpaEntity> findAllByActiveTrue(Pageable pageable);
    Optional<MerchantUserJpaEntity> findByIdAndActiveTrue(UUID userId);
    boolean existsByUsernameAndActiveTrue(String username);
    boolean existsByEmailAndActiveTrue(String email);
}