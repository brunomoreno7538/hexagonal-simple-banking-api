package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateTransactionUseCase;
import moreno.corebanking_natixis.application.port.in.GetTransactionsByAccountIdUseCase;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.domain.model.Transaction;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.TransactionWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateTransactionRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetTransactionsByAccountIdUseCase getTransactionsByAccountIdUseCase;
    private final TransactionWebMapper transactionWebMapper;
    private final MerchantUserRepository merchantUserRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_USER')")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        MerchantUser authenticatedMerchantUser = merchantUserRepository.findByUsernameAndActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated merchant user not found."));

        Transaction createdTransaction = createTransactionUseCase.createTransaction(
                authenticatedMerchantUser.getId(),
                request.getAccountId(),
                request.getType(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionWebMapper.toResponse(createdTransaction));
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT_ADMIN', 'MERCHANT_USER')")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsForAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        MerchantUser authenticatedMerchantUser = merchantUserRepository.findByUsernameAndActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated merchant user not found."));

        Page<Transaction> transactionsPage = getTransactionsByAccountIdUseCase.getTransactions(
                authenticatedMerchantUser.getId(),
                accountId,
                pageable);

        Page<TransactionResponse> responsePage = transactionsPage.map(transactionWebMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }
}