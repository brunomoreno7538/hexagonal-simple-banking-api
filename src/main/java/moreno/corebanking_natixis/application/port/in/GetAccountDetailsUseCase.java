package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.Account;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface GetAccountDetailsUseCase {
    Account getAccountDetails(UUID accountId, UserDetails principal);
}