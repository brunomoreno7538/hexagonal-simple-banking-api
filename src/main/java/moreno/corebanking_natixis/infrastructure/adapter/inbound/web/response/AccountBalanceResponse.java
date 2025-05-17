package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceResponse {
    private UUID accountId;
    private BigDecimal balance;
}