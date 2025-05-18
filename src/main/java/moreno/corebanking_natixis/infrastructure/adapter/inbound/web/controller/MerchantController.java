package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateMerchantUseCase;
import moreno.corebanking_natixis.application.port.in.GetMerchantUseCase;
import moreno.corebanking_natixis.application.port.in.UpdateMerchantUseCase;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.Merchant;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.MerchantWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateMerchantRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.UpdateMerchantRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.MerchantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final CreateMerchantUseCase createMerchantUseCase;
    private final GetMerchantUseCase getMerchantUseCase;
    private final MerchantWebMapper merchantWebMapper;
    private final UpdateMerchantUseCase updateMerchantUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantResponse> createMerchant(@Valid @RequestBody CreateMerchantRequest request) {
        Merchant merchantToCreate = merchantWebMapper.toDomain(request);
        Merchant createdMerchant = createMerchantUseCase.createMerchant(merchantToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantWebMapper.toResponse(createdMerchant));
    }

    @GetMapping("/{merchantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT_ADMIN', 'MERCHANT_USER')")
    public ResponseEntity<MerchantResponse> getMerchantById(@PathVariable UUID merchantId) {
        return getMerchantUseCase.findById(merchantId)
                .map(merchantWebMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + merchantId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MerchantResponse>> getAllMerchants(Pageable pageable) {
        Page<Merchant> merchantsPage = getMerchantUseCase.findAll(pageable);
        Page<MerchantResponse> responsesPage = merchantsPage.map(merchantWebMapper::toResponse);
        return ResponseEntity.ok(responsesPage);
    }

    @PutMapping("/{merchantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantResponse> updateMerchant(@PathVariable UUID merchantId, @Valid @RequestBody UpdateMerchantRequest request) {
        Merchant merchantToUpdate = merchantWebMapper.toDomain(request);
        Merchant updatedMerchant = updateMerchantUseCase.updateMerchant(merchantId, merchantToUpdate);
        return ResponseEntity.ok(merchantWebMapper.toResponse(updatedMerchant));
    }
}