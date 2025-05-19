package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateTransactionUseCase;
import moreno.corebanking_natixis.application.port.in.GetTransactionsByAccountIdUseCase;
import moreno.corebanking_natixis.application.port.out.AccountRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.application.port.out.TransactionRepository;
import moreno.corebanking_natixis.domain.exception.BankingBusinessException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService implements CreateTransactionUseCase, GetTransactionsByAccountIdUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MerchantUserRepository merchantUserRepository;


    @Override
    @Transactional
    public Transaction createTransaction(UUID merchantUserId, UUID targetAccountId, TransactionType type, BigDecimal amount, String description) {
        MerchantUser merchantUser = merchantUserRepository.findByIdAndActiveTrue(merchantUserId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantUser not found with ID: " + merchantUserId));

        Account account = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + targetAccountId));

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
    public Page<Transaction> getTransactions(UUID merchantUserId, UUID targetAccountId, Pageable pageable) {
        Account account = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + targetAccountId));
        if (merchantUserId != null) {
            MerchantUser merchantUser = merchantUserRepository.findByIdAndActiveTrue(merchantUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("MerchantUser not found with ID: " + merchantUserId));

            if (!"MERCHANT".equals(account.getAccountHolderType()) || !account.getHolderId().equals(merchantUser.getMerchantId())) {
                throw new BankingBusinessException("MerchantUser not authorized to view transactions for this account.");
            }
        }
        return transactionRepository.findByAccountId(targetAccountId, pageable);
    }
}