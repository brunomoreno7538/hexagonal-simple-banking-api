package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.dto.MerchantProfileData;
import moreno.corebanking_natixis.application.port.in.*;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.MerchantUserWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.MerchantWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateMerchantUserRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.UpdateMerchantUserRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.MerchantResponse;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.MerchantUserResponse;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.MyMerchantProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant-users")
@RequiredArgsConstructor
public class MerchantUserController {

    private final GetMyMerchantProfileUseCase getMyMerchantProfileUseCase;
    private final GetMerchantUserUseCase getMerchantUserUseCase;
    private final CreateMerchantUserUseCase createMerchantUserUseCase;
    private final UpdateMerchantUserUseCase updateMerchantUserUseCase;
    private final MerchantUserWebMapper merchantUserWebMapper;
    private final MerchantWebMapper merchantWebMapper;
    private final DeleteMerchantUserUseCase deleteMerchantUserUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT_ADMIN')")
    public ResponseEntity<MerchantUserResponse> createMerchantUser(
            @Valid @RequestBody CreateMerchantUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MerchantUser merchantUserToCreate = merchantUserWebMapper.toDomain(request);
        MerchantUser createdMerchantUser = createMerchantUserUseCase.create(merchantUserToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantUserWebMapper.toResponse(createdMerchantUser));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_MERCHANT_ADMIN', 'ADMIN') or @customSecurityService.isSelfMerchantUser(authentication, #userId) or @customSecurityService.isMerchantAdminOfTargetUser(authentication, #userId)")
    public ResponseEntity<MerchantUserResponse> getMerchantUserById(@PathVariable UUID userId, Authentication authentication) {
        return getMerchantUserUseCase.findById(userId)
                .map(merchantUserWebMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantUser not found with id: " + userId));
    }

    @GetMapping("/by-merchant/{merchantId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "(@customSecurityService.isUserFromMerchant(authentication, #merchantId) and hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_USER'))")
    public ResponseEntity<Page<MerchantUserResponse>> getMerchantUsersByMerchantId(@PathVariable UUID merchantId, Pageable pageable,
                                                                                   Authentication authentication) {
        Page<MerchantUser> usersPage = getMerchantUserUseCase.findByMerchantId(merchantId, pageable);
        Page<MerchantUserResponse> responsesPage = usersPage.map(merchantUserWebMapper::toResponse);
        return ResponseEntity.ok(responsesPage);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@customSecurityService.isSelfMerchantUser(authentication, #userId) or " +
            "@customSecurityService.isMerchantAdminOfTargetUser(authentication, #userId)")
    public ResponseEntity<MerchantUserResponse> updateMerchantUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateMerchantUserRequest request, Authentication authentication) {
        MerchantUser merchantUserUpdateData = merchantUserWebMapper.toDomain(request);
        MerchantUser updatedUser = updateMerchantUserUseCase.update(userId, merchantUserUpdateData);
        return ResponseEntity.ok(merchantUserWebMapper.toResponse(updatedUser));
    }

    @DeleteMapping("/{merchantUserId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "@customSecurityService.isSelfMerchantUser(authentication, #userId) or " +
            "@customSecurityService.isMerchantAdminOfTargetUser(authentication, #userId)")
    public ResponseEntity<Void> deleteMerchantById(@PathVariable UUID merchantUserId, Authentication authentication) {
        deleteMerchantUserUseCase.deleteById(merchantUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ROLE_MERCHANT_ADMIN', 'ROLE_USER')")
    public ResponseEntity<MyMerchantProfileResponse> getMyMerchantProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        MerchantProfileData profileData = getMyMerchantProfileUseCase.getMyProfile(userDetails);
        MerchantUserResponse userResponse = merchantUserWebMapper.toResponse(profileData.getMerchantUser());
        MerchantResponse merchantResponse = merchantWebMapper.toResponse(profileData.getMerchant());
        MyMerchantProfileResponse finalResponse = MyMerchantProfileResponse.builder()
                .user(userResponse)
                .merchant(merchantResponse)
                .build();
        return ResponseEntity.ok(finalResponse);
    }
}