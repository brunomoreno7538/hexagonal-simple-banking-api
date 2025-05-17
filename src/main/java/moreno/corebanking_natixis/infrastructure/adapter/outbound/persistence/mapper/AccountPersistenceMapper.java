package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper;

import moreno.corebanking_natixis.domain.model.Account;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity.AccountJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountPersistenceMapper {

    public AccountJpaEntity toJpaEntity(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountJpaEntity(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getAccountHolderType(),
                account.getHolderId()
        );
    }

    public Account toDomain(AccountJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        return Account.builder()
                .id(jpaEntity.getId())
                .accountNumber(jpaEntity.getAccountNumber())
                .balance(jpaEntity.getBalance())
                .accountHolderType(jpaEntity.getAccountHolderType())
                .holderId(jpaEntity.getHolderId())
                .build();
    }
}