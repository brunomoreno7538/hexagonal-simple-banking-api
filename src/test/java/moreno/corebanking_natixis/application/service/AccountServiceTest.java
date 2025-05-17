package moreno.corebanking_natixis.application.service;

import moreno.corebanking_natixis.application.port.out.AccountRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.domain.exception.BankingBusinessException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.Account;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MerchantUserRepository merchantUserRepository;

    @InjectMocks
    private AccountService accountService;

    private UUID merchantUserIdValue;
    private UUID accountIdValue;
    private MerchantUser merchantUser;
    private Account account;
    private UserDetails adminPrincipal;
    private UserDetails merchantPrincipal;

    @BeforeEach
    void setUp() {
        merchantUserIdValue = UUID.randomUUID();
        accountIdValue = UUID.randomUUID();
        UUID merchantFkIdValue = UUID.randomUUID();

        merchantUser = MerchantUser.builder()
                .id(merchantUserIdValue)
                .merchantId(merchantFkIdValue)
                .username("merchantTestUser")
                .active(true)
                .enabled(true)
                .build();

        account = Account.builder()
                .id(accountIdValue)
                .holderId(merchantFkIdValue)
                .accountHolderType("MERCHANT")
                .balance(new BigDecimal("1000.00"))
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
        lenient().when(merchantPrincipal.getUsername()).thenReturn("merchantTestUser");
    }

    @Test
    void getBalance_shouldReturnBalance_whenAuthorized() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance(merchantUserIdValue, accountIdValue);

        assertEquals(new BigDecimal("1000.00"), balance);
        verify(merchantUserRepository).findByIdAndActiveTrue(merchantUserIdValue);
        verify(accountRepository).findById(accountIdValue);
    }

    @Test
    void getBalance_shouldThrowResourceNotFound_whenMerchantUserNotFound() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getBalance(merchantUserIdValue, accountIdValue));
    }

    @Test
    void getBalance_shouldThrowResourceNotFound_whenAccountNotFound() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getBalance(merchantUserIdValue, accountIdValue));
    }

    @Test
    void getBalance_shouldThrowBankingBusinessException_whenNotAuthorized() {
        Account otherAccount = Account.builder().id(accountIdValue).holderId(UUID.randomUUID()).accountHolderType("MERCHANT").build();
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(otherAccount));
        assertThrows(BankingBusinessException.class, () -> accountService.getBalance(merchantUserIdValue, accountIdValue));
    }

    @Test
    void getAccountDetails_shouldReturnAccount_forAdmin() {
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));

        Account result = accountService.getAccountDetails(accountIdValue, adminPrincipal);

        assertEquals(account, result);
        verify(accountRepository).findById(accountIdValue);
        verify(adminPrincipal, atLeastOnce()).getAuthorities();
        verify(merchantUserRepository, never()).findByUsernameAndActiveTrue(anyString());
    }

    @Test
    void getAccountDetails_shouldReturnAccount_forAuthorizedMerchantUser() {
        when(merchantUserRepository.findByUsernameAndActiveTrue("merchantTestUser")).thenReturn(Optional.of(merchantUser));
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));

        Account result = accountService.getAccountDetails(accountIdValue, merchantPrincipal);

        assertEquals(account, result);
        verify(accountRepository).findById(accountIdValue);
        verify(merchantUserRepository).findByUsernameAndActiveTrue("merchantTestUser");
    }

    @Test
    void getAccountDetails_shouldThrowResourceNotFound_whenAccountNotFoundForAdmin() {
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountDetails(accountIdValue, adminPrincipal));
    }

    @Test
    void getAccountDetails_shouldThrowResourceNotFound_whenMerchantUserNotFoundForDetails() {
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(account));
        when(merchantUserRepository.findByUsernameAndActiveTrue("merchantTestUser")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountDetails(accountIdValue, merchantPrincipal));
    }

    @Test
    void getAccountDetails_shouldThrowBankingBusinessException_whenMerchantUserNotAuthorized() {
        Account otherAccount = Account.builder().id(accountIdValue).holderId(UUID.randomUUID()).accountHolderType("MERCHANT").build();
        when(accountRepository.findById(accountIdValue)).thenReturn(Optional.of(otherAccount));
        when(merchantUserRepository.findByUsernameAndActiveTrue("merchantTestUser")).thenReturn(Optional.of(merchantUser));
        assertThrows(BankingBusinessException.class, () -> accountService.getAccountDetails(accountIdValue, merchantPrincipal));
    }
}