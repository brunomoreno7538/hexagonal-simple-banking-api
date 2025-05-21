package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.application.dto.AdminTransactionFiltersDTO;
import moreno.corebanking_natixis.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetAllSystemTransactionsUseCase {
    Page<Transaction> getAll(AdminTransactionFiltersDTO filters, Pageable pageable);
}