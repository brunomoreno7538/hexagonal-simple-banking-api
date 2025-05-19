package moreno.corebanking_natixis.application.service;

import moreno.corebanking_natixis.application.port.out.AccountRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.application.port.out.TransactionRepository;
import moreno.corebanking_natixis.domain.exception.BankingBusinessException;
import moreno.corebanking_natixis.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private MerchantUserRepository merchantUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UUID merchantUserIdValue;
    private UUID accountIdValue;
    private MerchantUser merchantUser;
    private Account account;

    @BeforeEach
    void setUp() {
        merchantUserIdValue = UUID.randomUUID();
        accountIdValue = UUID.randomUUID();
        UUID merchantFkIdValue = UUID.randomUUID();

        merchantUser = MerchantUser.builder()
                .id(merchantUserIdValue) 
                .merchantId(merchantFkIdValue)
                .active(true)
                .enabled(true)
                .build();

        account = Account.builder()
                .id(accountIdValue)
                .holderId(merchantFkIdValue)
                .accountHolderType("MERCHANT")
                .balance(new BigDecimal("200.00"))
                .build();
    }

    @Test
    void createTransaction_shouldCreatePayIn_andIncreaseBalance() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            return tx;
        });
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        BigDecimal initialBalance = account.getBalance();
        BigDecimal amount = new BigDecimal("50.00");

        Transaction createdTx = transactionService.createTransaction(merchantUserIdValue, accountIdValue, TransactionType.PAYIN, amount, "Test PayIn");

        assertNotNull(createdTx);
        assertEquals(TransactionType.PAYIN, createdTx.getType());
        assertEquals(amount, createdTx.getAmount());
        assertEquals("COMPLETED", createdTx.getStatus());
        assertEquals(account.getId(), createdTx.getAccountId());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertEquals(initialBalance.add(amount), accountCaptor.getValue().getBalance());
    }

    @Test
    void createTransaction_shouldCreatePayOut_andDecreaseBalance() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        BigDecimal initialBalance = account.getBalance();
        BigDecimal amount = new BigDecimal("50.00");

        Transaction createdTx = transactionService.createTransaction(merchantUserIdValue, accountIdValue, TransactionType.PAYOUT, amount, "Test PayOut");

        assertNotNull(createdTx);
        assertEquals(TransactionType.PAYOUT, createdTx.getType());
        assertEquals(amount, createdTx.getAmount());
        assertEquals(account.getId(), createdTx.getAccountId());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertEquals(initialBalance.subtract(amount), accountCaptor.getValue().getBalance());
    }

    @Test
    void createTransaction_shouldThrowBankingBusinessException_forPayoutWithInsufficientFunds() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));

        BigDecimal amount = new BigDecimal("300.00");

        assertThrows(BankingBusinessException.class,
                () -> transactionService.createTransaction(merchantUserIdValue, accountIdValue, TransactionType.PAYOUT, amount, "Test Insufficient PayOut"));
    }

    @Test
    void createTransaction_shouldThrowBankingBusinessException_forNonPositiveAmount() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));

        assertThrows(BankingBusinessException.class,
                () -> transactionService.createTransaction(merchantUserIdValue, accountIdValue, TransactionType.PAYIN, BigDecimal.ZERO, "Zero amount"));
        assertThrows(BankingBusinessException.class,
                () -> transactionService.createTransaction(merchantUserIdValue, accountIdValue, TransactionType.PAYIN, new BigDecimal("-10"), "Negative amount"));
    }

    @Test
    void getTransactions_shouldReturnPage_forAuthorizedMerchantUser() {
        Pageable pageable = PageRequest.of(0, 5);
        Transaction sampleTransaction = Transaction.builder().accountId(accountIdValue).build();
        Page<Transaction> transactionPage = new PageImpl<>(Collections.singletonList(sampleTransaction), pageable, 1);

        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountId(accountIdValue, pageable)).thenReturn(transactionPage);

        Page<Transaction> result = transactionService.getTransactions(merchantUserIdValue, accountIdValue, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findByAccountId(accountIdValue, pageable);
    }

    @Test
    void getTransactions_shouldReturnPage_forAdminUser_whenMerchantUserIdIsNull() {
        Pageable pageable = PageRequest.of(0, 5);
        Transaction sampleTransaction = Transaction.builder().accountId(accountIdValue).build();
        Page<Transaction> transactionPage = new PageImpl<>(Collections.singletonList(sampleTransaction), pageable, 1);

        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountId(accountIdValue, pageable)).thenReturn(transactionPage);

        Page<Transaction> result = transactionService.getTransactions(null, accountIdValue, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(merchantUserRepository, never()).findByIdAndActiveTrue(any());
        verify(transactionRepository).findByAccountId(accountIdValue, pageable);
    }
}