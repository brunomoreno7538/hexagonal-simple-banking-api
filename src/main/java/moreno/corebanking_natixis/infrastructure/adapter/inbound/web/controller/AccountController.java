package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.controller;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.GetAccountBalanceUseCase;
import moreno.corebanking_natixis.application.port.in.GetAccountDetailsUseCase;
import moreno.corebanking_natixis.domain.model.Account;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.AccountWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.AccountBalanceResponse;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.AccountDetailsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final GetAccountBalanceUseCase getAccountBalanceUseCase;
    private final GetAccountDetailsUseCase getAccountDetailsUseCase;
    private final MerchantUserRepository merchantUserRepository;
    private final AccountWebMapper accountWebMapper;


    @GetMapping("/{accountId}/balance")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_USER', 'ADMIN')")
    public ResponseEntity<AccountBalanceResponse> getAccountBalance(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails) {

        MerchantUser authenticatedMerchantUser = merchantUserRepository.findByUsernameAndActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated merchant user not found."));

        BigDecimal balance = getAccountBalanceUseCase.getBalance(authenticatedMerchantUser.getId(), accountId);
        return ResponseEntity.ok(new AccountBalanceResponse(accountId, balance));
    }

    @GetMapping("/{accountId}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT_ADMIN', 'MERCHANT_USER')")
    public ResponseEntity<AccountDetailsResponse> getAccountDetails(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Account account = getAccountDetailsUseCase.getAccountDetails(accountId, userDetails);
        return ResponseEntity.ok(accountWebMapper.toDetailsResponse(account));
    }
}