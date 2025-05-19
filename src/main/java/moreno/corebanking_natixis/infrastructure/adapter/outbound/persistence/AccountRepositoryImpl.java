package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.out.AccountRepository;
import moreno.corebanking_natixis.domain.model.Account;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.mapper.AccountPersistenceMapper;
import moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.repository.SpringDataAccountRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final SpringDataAccountRepository springDataAccountRepository;
    private final AccountPersistenceMapper mapper;

    @Override
    public Account save(Account account) {
        return mapper.toDomain(springDataAccountRepository.save(mapper.toJpaEntity(account)));
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return springDataAccountRepository.findById(accountId).map(mapper::toDomain);
    }
}