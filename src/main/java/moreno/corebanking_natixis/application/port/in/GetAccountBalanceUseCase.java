package moreno.corebanking_natixis.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface GetAccountBalanceUseCase {
    BigDecimal getBalance(UUID merchantUserId, UUID accountId);
}