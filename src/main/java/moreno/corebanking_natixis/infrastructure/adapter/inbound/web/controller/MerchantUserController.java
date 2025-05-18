package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateMerchantUserUseCase;
import moreno.corebanking_natixis.application.port.in.GetMerchantUserUseCase;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.MerchantUserWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateMerchantUserRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.MerchantUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant-users")
@RequiredArgsConstructor
public class MerchantUserController {

    private final CreateMerchantUserUseCase createMerchantUserUseCase;
    private final GetMerchantUserUseCase getMerchantUserUseCase;
    private final MerchantUserWebMapper merchantUserWebMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MerchantUserResponse> createMerchantUser(@Valid @RequestBody CreateMerchantUserRequest request) {
        MerchantUser merchantUserToCreate = merchantUserWebMapper.toDomain(request);
        MerchantUser createdMerchantUser = createMerchantUserUseCase.create(merchantUserToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantUserWebMapper.toResponse(createdMerchantUser));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @customSecurityService.isSelfOrAdminMerchantUser(authentication, #userId) or @customSecurityService.isUserFromSameMerchant(authentication, #userId)")
    public ResponseEntity<MerchantUserResponse> getMerchantUserById(@PathVariable UUID userId) {
        return getMerchantUserUseCase.findById(userId)
                .map(merchantUserWebMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantUser not found with id: " + userId));
    }

    @GetMapping("/by-merchant/{merchantId}")
    @PreAuthorize("hasRole('ADMIN') or @customSecurityService.isUserFromMerchant(authentication, #merchantId)")
    public ResponseEntity<Page<MerchantUserResponse>> getMerchantUsersByMerchantId(@PathVariable UUID merchantId, Pageable pageable) {
        Page<MerchantUser> usersPage = getMerchantUserUseCase.findByMerchantId(merchantId, pageable);
        Page<MerchantUserResponse> responsesPage = usersPage.map(merchantUserWebMapper::toResponse);
        return ResponseEntity.ok(responsesPage);
    }
}