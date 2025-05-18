package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper;

import moreno.corebanking_natixis.domain.model.Merchant;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.MerchantJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantPersistenceMapper {

    public MerchantJpaEntity toJpaEntity(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        return new MerchantJpaEntity(
                merchant.getId(),
                merchant.getName(),
                merchant.getCnpj(),
                merchant.getAccountId(),
                merchant.isActive()
        );
    }

    public Merchant toDomain(MerchantJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Merchant.builder()
                .id(jpaEntity.getId())
                .name(jpaEntity.getName())
                .cnpj(jpaEntity.getCnpj())
                .accountId(jpaEntity.getAccountId())
                .active(jpaEntity.isActive())
                .build();
    }
}