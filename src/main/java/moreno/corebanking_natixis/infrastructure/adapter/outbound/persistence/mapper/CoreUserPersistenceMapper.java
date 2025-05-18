package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper;

import moreno.corebanking_natixis.domain.model.CoreUser;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.CoreUserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CoreUserPersistenceMapper {

    public CoreUserJpaEntity toJpaEntity(CoreUser coreUser) {
        if (coreUser == null) {
            return null;
        }
        return new CoreUserJpaEntity(
                coreUser.getId(),
                coreUser.getUsername(),
                coreUser.getPassword(),
                coreUser.getEmail(),
                coreUser.getFullName(),
                coreUser.getRole(),
                coreUser.isEnabled(),
                coreUser.isActive()
        );
    }

    public CoreUser toDomain(CoreUserJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return CoreUser.builder()
                .id(jpaEntity.getId())
                .username(jpaEntity.getUsername())
                .password(jpaEntity.getPassword())
                .email(jpaEntity.getEmail())
                .fullName(jpaEntity.getFullName())
                .role(jpaEntity.getRole())
                .enabled(jpaEntity.isEnabled())
                .build();
    }
}