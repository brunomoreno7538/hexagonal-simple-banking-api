package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.application.dto.TransactionQueryResult;
import moreno.corebanking_natixis.domain.model.Transaction;
import moreno.corebanking_natixis.domain.model.TransactionType;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.PagedTransactionsWithSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface GetTransactionsByAccountIdUseCase {
    TransactionQueryResult getTransactions(
            UserDetails principal,
            UUID accountId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            TransactionType transactionType,
            Pageable pageable
    );
}