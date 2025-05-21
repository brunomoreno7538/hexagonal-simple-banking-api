package moreno.corebanking_natixis.application.port.out;

import lombok.Builder;
import lombok.Getter;
import moreno.corebanking_natixis.domain.model.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TransactionFilterParams {
    private UUID accountId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private TransactionType transactionType;
}