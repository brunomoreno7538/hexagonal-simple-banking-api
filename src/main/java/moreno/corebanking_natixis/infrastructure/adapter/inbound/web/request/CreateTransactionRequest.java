package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import moreno.corebanking_natixis.domain.model.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateTransactionRequest {
    @NotNull
    private UUID accountId;

    @NotNull
    private TransactionType type;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;
}