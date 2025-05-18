package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.MerchantUserJpaEntity;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper.MerchantUserPersistenceMapper;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository.SpringDataMerchantUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MerchantUserRepositoryImpl implements MerchantUserRepository {

    private final SpringDataMerchantUserRepository springDataRepo;
    private final MerchantUserPersistenceMapper mapper;

    @Override
    public MerchantUser save(MerchantUser merchantUser) {
        MerchantUserJpaEntity entity = mapper.toJpaEntity(merchantUser);
        return mapper.toDomain(springDataRepo.save(entity));
    }

    @Override
    public Optional<MerchantUser> findByIdAndActiveTrue(UUID userId) {
        return springDataRepo.findByIdAndActiveTrue(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<MerchantUser> findByUsernameAndActiveTrue(String username) {
        return springDataRepo.findByUsernameAndActiveTrue(username).map(mapper::toDomain);
    }

    @Override
    public Page<MerchantUser> findByMerchantIdAndActiveTrue(UUID merchantId, Pageable pageable) {
        return springDataRepo.findByMerchantIdAndActiveTrue(merchantId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<MerchantUser> findAllByActiveTrue(Pageable pageable) {
        return springDataRepo.findAllByActiveTrue(pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsernameAndActiveTrue(String username) {
        return springDataRepo.existsByUsernameAndActiveTrue(username);
    }

    @Override
    public boolean existsByEmailAndActiveTrue(String email) {
        return springDataRepo.existsByEmailAndActiveTrue(email);
    }

    @Override
    public Optional<MerchantUser> findByIdEvenIfInactive(UUID userId) {
        return springDataRepo.findById(userId).map(mapper::toDomain);
    }
}