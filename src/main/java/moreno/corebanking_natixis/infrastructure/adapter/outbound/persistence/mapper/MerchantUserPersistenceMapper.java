package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper;

import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.MerchantUserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class MerchantUserPersistenceMapper {

    public MerchantUserJpaEntity toJpaEntity(MerchantUser merchantUser) {
        if (merchantUser == null) {
            return null;
        }
        return new MerchantUserJpaEntity(
                merchantUser.getId(),
                merchantUser.getUsername(),
                merchantUser.getPassword(),
                merchantUser.getEmail(),
                merchantUser.getFullName(),
                merchantUser.getRole(),
                merchantUser.getMerchantId(),
                merchantUser.getEnabled(),
                merchantUser.isActive()
        );
    }

    public MerchantUser toDomain(MerchantUserJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return MerchantUser.builder()
                .id(jpaEntity.getId())
                .username(jpaEntity.getUsername())
                .password(jpaEntity.getPassword())
                .email(jpaEntity.getEmail())
                .fullName(jpaEntity.getFullName())
                .role(jpaEntity.getRole())
                .merchantId(jpaEntity.getMerchantId())
                .enabled(jpaEntity.isEnabled())
                .active(jpaEntity.isActive())
                .build();
    }
}