package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper;

import moreno.corebanking_natixis.domain.model.Account;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.AccountDetailsResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountWebMapper {
    public AccountDetailsResponse toDetailsResponse(Account account) {
        if (account == null) return null;
        return AccountDetailsResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountHolderType(account.getAccountHolderType())
                .holderId(account.getHolderId())
                .build();
    }
}