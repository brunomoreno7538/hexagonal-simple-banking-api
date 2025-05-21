package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedTransactionsWithSummaryResponse {
    private Page<TransactionResponse> transactionsPage;
    private TransactionSummaryResponse summary;
}