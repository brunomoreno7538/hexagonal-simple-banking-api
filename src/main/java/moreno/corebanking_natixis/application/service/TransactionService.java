package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.dto.AdminTransactionFiltersDTO;
import moreno.corebanking_natixis.application.dto.TransactionQueryResult;
import moreno.corebanking_natixis.application.port.in.CreateTransactionUseCase;
import moreno.corebanking_natixis.application.port.in.GetAllSystemTransactionsUseCase;
import moreno.corebanking_natixis.application.port.in.GetTransactionsByAccountIdUseCase;
import moreno.corebanking_natixis.application.port.out.AccountRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.application.port.out.TransactionFilterParams;
import moreno.corebanking_natixis.application.port.out.TransactionRepository;
import moreno.corebanking_natixis.domain.exception.BankingBusinessException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements CreateTransactionUseCase, GetTransactionsByAccountIdUseCase, GetAllSystemTransactionsUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantUserRepository merchantUserRepository;


    @Override
    @Transactional
    public Transaction createTransaction(UUID merchantUserId, UUID targetAccountId, TransactionType type, BigDecimal amount, String description) {
        MerchantUser merchantUser = merchantUserRepository.findByIdAndActiveTrue(merchantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Active MerchantUser not found with ID: " + merchantUserId));

        Account account = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Account not found with ID: " + targetAccountId));

        if (!"MERCHANT".equals(account.getAccountHolderType()) || !account.getHolderId().equals(merchantUser.getMerchantId())) {
            throw new BankingBusinessException("MerchantUser not authorized to transact on this account.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingBusinessException("Transaction amount must be positive.");
        }

        if (type == TransactionType.PAYOUT) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new BankingBusinessException("Insufficient funds for payout.");
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else if (type == TransactionType.PAYIN) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            throw new BankingBusinessException("Invalid transaction type.");
        }

        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .accountId(account.getId())
                .type(type)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .description(description)
                .status("COMPLETED")
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionQueryResult getTransactions(
            UserDetails principal,
            UUID accountId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            TransactionType transactionType,
            Pageable pageable) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Account not found with ID: " + accountId));

        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            MerchantUser merchantUser = merchantUserRepository.findByUsernameAndActiveTrue(principal.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated active merchant user not found. Username: " + principal.getUsername()));
            if (!"MERCHANT".equals(account.getAccountHolderType()) || !account.getHolderId().equals(merchantUser.getMerchantId())) {
                throw new BankingBusinessException("MerchantUser not authorized to view transactions for this account.");
            }
        }

        TransactionFilterParams filters = TransactionFilterParams.builder()
                .accountId(accountId)
                .startDateTime(startDate)
                .endDateTime(endDate)
                .transactionType(transactionType)
                .build();

        Page<Transaction> transactionsDomainPage = transactionRepository.findTransactionsFiltered(filters, pageable);
        BigDecimal totalAmountFiltered = transactionRepository.sumAmountFiltered(filters);

        BigDecimal finalTotalAmount = (totalAmountFiltered != null) ? totalAmountFiltered : BigDecimal.ZERO;

        return new TransactionQueryResult(transactionsDomainPage, finalTotalAmount);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getAll(AdminTransactionFiltersDTO filters, Pageable pageable) {
        return transactionRepository.findAllSystemTransactionsFiltered(filters, pageable);
    }
}