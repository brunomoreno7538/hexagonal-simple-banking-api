package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response;

import lombok.Builder;
import lombok.Data;
import moreno.corebanking_natixis.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID transactionId;
    private UUID accountId;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String description;
    private String status;
}