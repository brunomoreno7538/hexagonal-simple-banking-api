package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.out.CoreUserRepository;
import moreno.corebanking_natixis.domain.model.CoreUser;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.CoreUserJpaEntity;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper.CoreUserPersistenceMapper;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository.SpringDataCoreUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CoreUserRepositoryImpl implements CoreUserRepository {

    private final SpringDataCoreUserRepository springDataCoreUserRepository;
    private final CoreUserPersistenceMapper mapper;

    @Override
    public CoreUser save(CoreUser coreUser) {
        CoreUserJpaEntity jpaEntity = mapper.toJpaEntity(coreUser);
        CoreUserJpaEntity savedEntity = springDataCoreUserRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CoreUser> findByIdAndActiveTrue(UUID userId) {
        return springDataCoreUserRepository.findByIdAndActiveTrue(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<CoreUser> findByIdEvenIfInactive(UUID userId) {
        return springDataCoreUserRepository.findById(userId).map(mapper::toDomain);
    }


    @Override
    public Optional<CoreUser> findByUsernameAndActiveTrue(String username) {
        return springDataCoreUserRepository.findByUsernameAndActiveTrue(username).map(mapper::toDomain);
    }

    @Override
    public Page<CoreUser> findAllByActiveTrue(Pageable pageable) {
        return springDataCoreUserRepository.findAllByActiveTrue(pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsernameAndActiveTrue(String username) {
        return springDataCoreUserRepository.existsByUsernameAndActiveTrue(username);
    }

    @Override
    public boolean existsByEmailAndActiveTrue(String email) {
        return springDataCoreUserRepository.existsByEmailAndActiveTrue(email);
    }
}