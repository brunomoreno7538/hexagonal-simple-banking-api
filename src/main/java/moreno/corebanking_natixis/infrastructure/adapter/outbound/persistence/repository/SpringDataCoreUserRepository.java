package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository;

import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.CoreUserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCoreUserRepository extends JpaRepository<CoreUserJpaEntity, UUID> {
    Optional<CoreUserJpaEntity> findByUsernameAndActiveTrue(String username);
    boolean existsByUsernameAndActiveTrue(String username);
    boolean existsByEmailAndActiveTrue(String email);
    Page<CoreUserJpaEntity> findAllByActiveTrue(Pageable pageable);
    Optional<CoreUserJpaEntity> findByIdAndActiveTrue(UUID userId);
}