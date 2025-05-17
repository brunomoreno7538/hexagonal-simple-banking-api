package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.out.CoreUserRepository;
import moreno.corebanking_natixis.domain.model.CoreUser;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.CoreUserJpaEntity;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper.CoreUserPersistenceMapper;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository.SpringDataCoreUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Optional<CoreUser> findById(UUID userId) {
        return springDataCoreUserRepository.findById(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<CoreUser> findByUsername(String username) {
        return springDataCoreUserRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<CoreUser> findByEmail(String email) {
        return springDataCoreUserRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public List<CoreUser> findAll() {
        return springDataCoreUserRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID userId) {
        springDataCoreUserRepository.deleteById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataCoreUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataCoreUserRepository.existsByEmail(email);
    }
}