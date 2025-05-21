package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.dto.AdminTransactionFiltersDTO;
import moreno.corebanking_natixis.application.dto.TransactionQueryResult;
import moreno.corebanking_natixis.application.port.in.CreateTransactionUseCase;
import moreno.corebanking_natixis.application.port.in.GetAllSystemTransactionsUseCase;
import moreno.corebanking_natixis.application.port.in.GetTransactionsByAccountIdUseCase;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.domain.model.Transaction;
import moreno.corebanking_natixis.domain.model.TransactionType;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.TransactionWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateTransactionRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.PagedTransactionsWithSummaryResponse;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.TransactionResponse;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.TransactionSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetAllSystemTransactionsUseCase getAllSystemTransactionsUseCase;
    private final GetTransactionsByAccountIdUseCase getTransactionsByAccountIdUseCase;
    private final TransactionWebMapper transactionWebMapper;
    private final MerchantUserRepository merchantUserRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_USER')")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        MerchantUser authenticatedMerchantUser = merchantUserRepository.findByUsernameAndActiveTrue(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated active merchant user not found for transaction creation. Username: " + userDetails.getUsername()));

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
    @PreAuthorize("hasRole('ADMIN') or hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_USER')")
    public ResponseEntity<PagedTransactionsWithSummaryResponse> getTransactionsForAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) TransactionType type,
            Pageable pageable) {

        TransactionQueryResult serviceResult = getTransactionsByAccountIdUseCase.getTransactions(
                userDetails,
                accountId,
                startDate,
                endDate,
                type,
                pageable);

        Page<TransactionResponse> transactionResponsePage = serviceResult.getTransactionsPage()
                .map(transactionWebMapper::toResponse);

        TransactionSummaryResponse summaryResponse = TransactionSummaryResponse.builder()
                .quantity(serviceResult.getTransactionsPage().getTotalElements())
                .totalAmount(serviceResult.getTotalAmountFiltered())
                .build();

        PagedTransactionsWithSummaryResponse finalResponse = PagedTransactionsWithSummaryResponse.builder()
                .transactionsPage(transactionResponsePage)
                .summary(summaryResponse)
                .build();

        return ResponseEntity.ok(finalResponse);
    }

    @GetMapping("/system-wide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getAllSystemTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID accountId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        AdminTransactionFiltersDTO filters = AdminTransactionFiltersDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .transactionType(type)
                .accountId(accountId)
                .build();

        Page<Transaction> domainPage =
                getAllSystemTransactionsUseCase.getAll(filters, pageable);

        Page<TransactionResponse> responsePage = domainPage.map(transactionWebMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }
}