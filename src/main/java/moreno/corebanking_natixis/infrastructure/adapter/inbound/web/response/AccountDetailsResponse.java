package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AccountDetailsResponse {
    private UUID accountId;
    private String accountNumber;
    private BigDecimal balance;
    private String accountHolderType;
    private UUID holderId;
}