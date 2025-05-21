package moreno.corebanking_natixis.application.port.in;

import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.UUID;

public interface GetAccountBalanceUseCase {
    BigDecimal getBalance(UserDetails principal, UUID accountId);
}