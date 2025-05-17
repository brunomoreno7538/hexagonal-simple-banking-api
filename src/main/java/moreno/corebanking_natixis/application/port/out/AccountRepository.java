package moreno.corebanking_natixis.application.port.out;

import moreno.corebanking_natixis.domain.model.Account;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID accountId);
}