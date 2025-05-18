package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateMerchantUserUseCase;
import moreno.corebanking_natixis.application.port.in.DeleteMerchantUserUseCase;
import moreno.corebanking_natixis.application.port.in.GetMerchantUserUseCase;
import moreno.corebanking_natixis.application.port.out.MerchantRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.application.port.out.PasswordEncoderPort;
import moreno.corebanking_natixis.domain.exception.DuplicateResourceException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MerchantUserService implements CreateMerchantUserUseCase, GetMerchantUserUseCase, DeleteMerchantUserUseCase {

    private final MerchantUserRepository merchantUserRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoderPort passwordEncoderPort;

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
}