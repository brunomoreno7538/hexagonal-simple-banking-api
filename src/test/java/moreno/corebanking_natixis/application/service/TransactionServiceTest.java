package moreno.corebanking_natixis.application.service;

import moreno.corebanking_natixis.application.dto.TransactionQueryResult;
import moreno.corebanking_natixis.application.port.out.AccountRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.application.port.out.TransactionFilterParams;
import moreno.corebanking_natixis.application.port.out.TransactionRepository;
import moreno.corebanking_natixis.domain.exception.BankingBusinessException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
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
    private UUID merchantFkIdValue;
    private MerchantUser merchantUser;
    private Account account;
    private UserDetails adminPrincipal;
    private UserDetails merchantPrincipal;

    @BeforeEach
    void setUp() {
        merchantUserIdValue = UUID.randomUUID();
        accountIdValue = UUID.randomUUID();
        merchantFkIdValue = UUID.randomUUID();

        merchantUser = MerchantUser.builder()
                .id(merchantUserIdValue)
                .merchantId(merchantFkIdValue)
                .username("testMerchantUser")
                .active(true)
                .enabled(true)
                .build();

        account = Account.builder()
                .id(accountIdValue)
                .holderId(merchantFkIdValue)
                .accountHolderType("MERCHANT")
                .balance(new BigDecimal("200.00"))
                .build();

        Collection adminAuthorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        adminPrincipal = mock(UserDetails.class);
        lenient().when(adminPrincipal.getAuthorities()).thenReturn(adminAuthorities);
        lenient().when(adminPrincipal.getUsername()).thenReturn("admin");

        Collection merchantAuthorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MERCHANT_USER"));
        merchantPrincipal = mock(UserDetails.class);
        lenient().when(merchantPrincipal.getAuthorities()).thenReturn(merchantAuthorities);
        lenient().when(merchantPrincipal.getUsername()).thenReturn("testMerchantUser");
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
        assertEquals(0, initialBalance.add(amount).compareTo(accountCaptor.getValue().getBalance()));
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
        assertEquals(0, initialBalance.subtract(amount).compareTo(accountCaptor.getValue().getBalance()));
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
    void createTransaction_shouldThrowResourceNotFound_whenMerchantUserNotFound() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createTransaction(merchantUserIdValue, accountIdValue, TransactionType.PAYIN, new BigDecimal("10.00"), "test"));
    }

    @Test
    void createTransaction_shouldThrowResourceNotFound_whenAccountNotFound() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createTransaction(merchantUserIdValue, accountIdValue, TransactionType.PAYIN, new BigDecimal("10.00"), "test"));
    }

    @Test
    void createTransaction_shouldThrowBankingBusinessException_whenUserNotAuthorizedForAccount() {
        UUID anotherMerchantFkId = UUID.randomUUID();
        Account accountOfAnotherMerchant = Account.builder()
                .id(accountIdValue)
                .holderId(anotherMerchantFkId)
                .accountHolderType("MERCHANT")
                .balance(new BigDecimal("500.00"))
                .build();

        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(accountOfAnotherMerchant));

        assertThrows(BankingBusinessException.class,
                () -> transactionService.createTransaction(merchantUserIdValue, accountIdValue, TransactionType.PAYIN, new BigDecimal("10.00"), "test unauthorized"));
    }

    @Test
    void getTransactions_shouldReturnFilteredPageAndSummary_forAuthorizedMerchantUser() {
        Pageable pageable = PageRequest.of(0, 5);
        LocalDateTime startDate = LocalDate.now().minusDays(5).atStartOfDay();
        LocalDateTime endDate = LocalDate.now().atStartOfDay();
        TransactionType type = TransactionType.PAYIN;

        Transaction sampleTransaction = Transaction.builder().accountId(accountIdValue).type(TransactionType.PAYIN).amount(new BigDecimal("100")).build();
        Page<Transaction> transactionDomainPage = new PageImpl<>(Collections.singletonList(sampleTransaction), pageable, 1);
        BigDecimal totalAmount = new BigDecimal("100.00");

        when(merchantUserRepository.findByUsernameAndActiveTrue("testMerchantUser")).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));
        when(transactionRepository.findTransactionsFiltered(any(TransactionFilterParams.class), eq(pageable))).thenReturn(transactionDomainPage);
        when(transactionRepository.sumAmountFiltered(any(TransactionFilterParams.class))).thenReturn(totalAmount);

        TransactionQueryResult result = transactionService.getTransactions(merchantPrincipal, accountIdValue, startDate, endDate, type, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTransactionsPage().getTotalElements());
        assertEquals(0, totalAmount.compareTo(result.getTotalAmountFiltered()));
        assertEquals(sampleTransaction, result.getTransactionsPage().getContent().get(0));

        verify(merchantUserRepository).findByUsernameAndActiveTrue("testMerchantUser");
        verify(accountRepository).findById(accountIdValue);
        verify(transactionRepository).findTransactionsFiltered(any(TransactionFilterParams.class), eq(pageable));
        verify(transactionRepository).sumAmountFiltered(any(TransactionFilterParams.class));
    }

    @Test
    void getTransactions_shouldReturnFilteredPageAndSummary_forAdminUser() {
        Pageable pageable = PageRequest.of(0, 5);
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDate.now().atStartOfDay();
        TransactionType type = null;

        Transaction sampleTransaction = Transaction.builder().accountId(accountIdValue).amount(new BigDecimal("200")).build();
        Page<Transaction> transactionDomainPage = new PageImpl<>(Collections.singletonList(sampleTransaction), pageable, 1);
        BigDecimal totalAmount = new BigDecimal("200.00");

        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));
        when(transactionRepository.findTransactionsFiltered(any(TransactionFilterParams.class), eq(pageable))).thenReturn(transactionDomainPage);
        when(transactionRepository.sumAmountFiltered(any(TransactionFilterParams.class))).thenReturn(totalAmount);

        TransactionQueryResult result = transactionService.getTransactions(adminPrincipal, accountIdValue, startDate, endDate, type, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTransactionsPage().getTotalElements());
        assertEquals(0, totalAmount.compareTo(result.getTotalAmountFiltered()));

        verify(merchantUserRepository, never()).findByUsernameAndActiveTrue(anyString());
        verify(accountRepository).findById(accountIdValue);
        verify(transactionRepository).findTransactionsFiltered(any(TransactionFilterParams.class), eq(pageable));
        verify(transactionRepository).sumAmountFiltered(any(TransactionFilterParams.class));
    }

    @Test
    void getTransactions_shouldThrowResourceNotFound_whenAccountNotFound() {
        Pageable pageable = PageRequest.of(0, 5);
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactions(adminPrincipal, accountIdValue, null, null, null, pageable));
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactions(merchantPrincipal, accountIdValue, null, null, null, pageable));
    }

    @Test
    void getTransactions_shouldThrowResourceNotFound_whenMerchantUserNotFoundForNonAdmin() {
        Pageable pageable = PageRequest.of(0, 5);
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));
        when(merchantUserRepository.findByUsernameAndActiveTrue("testMerchantUser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactions(merchantPrincipal, accountIdValue, null, null, null, pageable));
    }

    @Test
    void getTransactions_shouldThrowBankingBusinessException_whenMerchantUserNotAuthorized() {
        Pageable pageable = PageRequest.of(0, 5);
        UUID anotherMerchantFkId = UUID.randomUUID();
        Account accountOfAnotherMerchant = Account.builder()
                .id(accountIdValue)
                .holderId(anotherMerchantFkId)
                .accountHolderType("MERCHANT")
                .balance(new BigDecimal("300.00"))
                .build();

        when(merchantUserRepository.findByUsernameAndActiveTrue("testMerchantUser")).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(accountOfAnotherMerchant));

        assertThrows(BankingBusinessException.class,
                () -> transactionService.getTransactions(merchantPrincipal, accountIdValue, null, null, null, pageable));
    }

    @Test
    void getTransactions_handlesNullSumAmountFromRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Transaction> emptyTransactionPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(merchantUserRepository.findByUsernameAndActiveTrue("testMerchantUser")).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));
        when(transactionRepository.findTransactionsFiltered(any(TransactionFilterParams.class), eq(pageable))).thenReturn(emptyTransactionPage);
        when(transactionRepository.sumAmountFiltered(any(TransactionFilterParams.class))).thenReturn(null);

        TransactionQueryResult result = transactionService.getTransactions(merchantPrincipal, accountIdValue, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTransactionsPage().getTotalElements());
        assertEquals(BigDecimal.ZERO, result.getTotalAmountFiltered());
    }
}