package moreno.corebanking_natixis.application.dto;

import lombok.Builder;
import lombok.Getter;
import moreno.corebanking_natixis.domain.model.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AdminTransactionFiltersDTO {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TransactionType transactionType;
    private UUID accountId;
}