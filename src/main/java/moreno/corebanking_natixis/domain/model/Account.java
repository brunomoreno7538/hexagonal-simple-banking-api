package moreno.corebanking_natixis.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class Account {
    private UUID id;
    private String accountNumber;
    private BigDecimal balance;
    private String accountHolderType;
    private UUID holderId;
}