package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository;

import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.CoreUserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCoreUserRepository extends JpaRepository<CoreUserJpaEntity, UUID> {
    Optional<CoreUserJpaEntity> findByUsername(String username);
    Optional<CoreUserJpaEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}