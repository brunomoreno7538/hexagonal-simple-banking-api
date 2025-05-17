package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.GetAccountBalanceUseCase;
import moreno.corebanking_natixis.application.port.in.GetAccountDetailsUseCase;
import moreno.corebanking_natixis.application.port.out.AccountRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.domain.exception.BankingBusinessException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.Account;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService implements GetAccountBalanceUseCase, GetAccountDetailsUseCase {

    private final AccountRepository accountRepository;
    private final MerchantUserRepository merchantUserRepository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID merchantUserId, UUID accountId) {
        MerchantUser merchantUser = merchantUserRepository.findByIdAndActiveTrue(merchantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantUser not found with ID: " + merchantUserId));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (!"MERCHANT".equals(account.getAccountHolderType()) || !account.getHolderId().equals(merchantUser.getMerchantId())) {
            throw new BankingBusinessException("MerchantUser not authorized to view this account's balance.");
        }
        return account.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountDetails(UUID accountId, UserDetails principal) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return account;
        }

        MerchantUser merchantUser = merchantUserRepository.findByUsernameAndActiveTrue(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("MerchantUser not found with username: " + principal.getUsername()));

        if (!"MERCHANT".equals(account.getAccountHolderType()) || !account.getHolderId().equals(merchantUser.getMerchantId())) {
            throw new BankingBusinessException("MerchantUser not authorized to view this account's details.");
        }
        return account;
    }
}