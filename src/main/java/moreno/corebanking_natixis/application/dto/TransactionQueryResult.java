package moreno.corebanking_natixis.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moreno.corebanking_natixis.domain.model.Transaction;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TransactionQueryResult {
    private final Page<Transaction> transactionsPage;
    private final BigDecimal totalAmountFiltered;
}