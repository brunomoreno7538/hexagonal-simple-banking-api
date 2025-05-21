package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.dto.MerchantProfileData;
import moreno.corebanking_natixis.application.port.in.*;
import moreno.corebanking_natixis.application.port.out.MerchantRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.application.port.out.PasswordEncoderPort;
import moreno.corebanking_natixis.domain.exception.BankingBusinessException;
import moreno.corebanking_natixis.domain.exception.DuplicateResourceException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.Merchant;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.domain.model.UserRole;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.MerchantUserWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper.MerchantWebMapper;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.MyMerchantProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MerchantUserService implements CreateMerchantUserUseCase, GetMerchantUserUseCase, UpdateMerchantUserUseCase, DeleteMerchantUserUseCase, GetMyMerchantProfileUseCase {

    private final MerchantUserRepository merchantUserRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final MerchantUserWebMapper merchantUserWebMapper;
    private final MerchantWebMapper merchantWebMapper;

    @Override
    public MerchantUser create(MerchantUser merchantUser) {
        if (merchantUserRepository.existsByUsernameAndActiveTrue(merchantUser.getUsername())) {
            throw new DuplicateResourceException("Username " + merchantUser.getUsername() + " already exists and is active.");
        }
        if (merchantUser.getEmail() != null && merchantUserRepository.existsByEmailAndActiveTrue(merchantUser.getEmail())) {
            throw new DuplicateResourceException("Email " + merchantUser.getEmail() + " already exists and is active.");
        }
        merchantRepository.findByIdAndActiveTrue(merchantUser.getMerchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Active merchant not found with id: " + merchantUser.getMerchantId()));

        merchantUser.setPassword(passwordEncoderPort.encode(merchantUser.getPassword()));
        merchantUser.setEnabled(true);
        merchantUser.setActive(true);
        return merchantUserRepository.save(merchantUser);
    }

    @Override
    public MerchantUser update(UUID userId, MerchantUser userUpdateData) {
        MerchantUser existingUser = merchantUserRepository.findByIdEvenIfInactive(userId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantUser not found with id: " + userId));

        if (userUpdateData.getEmail() != null && !userUpdateData.getEmail().isBlank() &&
                !userUpdateData.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            if (merchantUserRepository.existsByEmailAndActiveTrue(userUpdateData.getEmail())) {
                throw new DuplicateResourceException("Email " + userUpdateData.getEmail() + " already in use by an active user.");
            }
            existingUser.setEmail(userUpdateData.getEmail());
        }

        if (userUpdateData.getFullName() != null && !userUpdateData.getFullName().isBlank()) {
            existingUser.setFullName(userUpdateData.getFullName());
        }

        if (userUpdateData.getPassword() != null && !userUpdateData.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoderPort.encode(userUpdateData.getPassword()));
        }

        if (userUpdateData.getRole() != null) {
            if (userUpdateData.getRole() == UserRole.MERCHANT_USER || userUpdateData.getRole() == UserRole.MERCHANT_ADMIN) {
                existingUser.setRole(userUpdateData.getRole());
            } else {
                throw new BankingBusinessException("Invalid role assignment for MerchantUser: " + userUpdateData.getRole());
            }
        }

        if (userUpdateData.getEnabled() != null) {
            existingUser.setEnabled(userUpdateData.getEnabled());
        }

        return merchantUserRepository.save(existingUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MerchantUser> findById(UUID userId) {
        return merchantUserRepository.findByIdAndActiveTrue(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MerchantUser> findByMerchantId(UUID merchantId, Pageable pageable) {
        merchantRepository.findByIdAndActiveTrue(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Active merchant not found with id: " + merchantId + " when fetching users."));
        return merchantUserRepository.findByMerchantIdAndActiveTrue(merchantId, pageable);
    }

    @Override
    public void deleteById(UUID userId) {
        MerchantUser userToDelete = merchantUserRepository.findByIdEvenIfInactive(userId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantUser not found with id: " + userId));

        if (!userToDelete.isActive()) {
            return;
        }

        userToDelete.setActive(false);
        userToDelete.setEnabled(false);
        merchantUserRepository.save(userToDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantProfileData getMyProfile(UserDetails principal) {
        String username = principal.getUsername();
        MerchantUser merchantUser = merchantUserRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated active merchant user not found. Username: " + username));

        Merchant merchant = merchantRepository.findByIdAndActiveTrue(merchantUser.getMerchantId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated active merchant not found with ID: " + merchantUser.getMerchantId()));

        return new MerchantProfileData(merchantUser, merchant);
    }
}