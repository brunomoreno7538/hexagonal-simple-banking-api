package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.out.MerchantRepository;
import moreno.corebanking_natixis.domain.model.Merchant;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper.MerchantPersistenceMapper;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository.SpringDataMerchantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MerchantRepositoryImpl implements MerchantRepository {

    private final SpringDataMerchantRepository springDataRepo;
    private final MerchantPersistenceMapper mapper;

    @Override
    public Merchant save(Merchant merchant) {
        return mapper.toDomain(springDataRepo.save(mapper.toJpaEntity(merchant)));
    }

    @Override
    public Optional<Merchant> findByIdAndActiveTrue(UUID merchantId) {
        return springDataRepo.findByIdAndActiveTrue(merchantId).map(mapper::toDomain);
    }

    @Override
    public Optional<Merchant> findByCnpjAndActiveTrue(String cnpj) {
        return springDataRepo.findByCnpjAndActiveTrue(cnpj).map(mapper::toDomain);
    }

    @Override
    public Page<Merchant> findAllByActiveTrue(Pageable pageable) {
        return springDataRepo.findAllByActiveTrue(pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCnpjAndActiveTrue(String cnpj) {
        return springDataRepo.existsByCnpjAndActiveTrue(cnpj);
    }

    @Override
    public Optional<Merchant> findByIdEvenIfInactive(UUID merchantId) {
        return springDataRepo.findById(merchantId).map(mapper::toDomain);
    }
}