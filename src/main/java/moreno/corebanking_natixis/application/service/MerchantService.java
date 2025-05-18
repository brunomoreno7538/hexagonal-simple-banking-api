package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.*;
import moreno.corebanking_natixis.application.port.out.MerchantRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.domain.exception.DuplicateResourceException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.Merchant;
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
public class MerchantService implements CreateMerchantUseCase, GetMerchantUseCase, UpdateMerchantUseCase, DeleteMerchantUseCase {

    private final MerchantRepository merchantRepository;
    private final MerchantUserRepository merchantUserRepository;

    @Override
    public Merchant createMerchant(Merchant merchantDetails) {
        if (merchantRepository.existsByCnpjAndActiveTrue(merchantDetails.getCnpj())) {
            throw new DuplicateResourceException("Merchant with CNPJ " + merchantDetails.getCnpj() + " already exists and is active.");
        }

        UUID newMerchantId = UUID.randomUUID();
        UUID newAccountId = UUID.randomUUID();

        merchantDetails.setId(newMerchantId);
        merchantDetails.setAccountId(newAccountId);
        merchantDetails.setActive(true);

        return merchantRepository.save(merchantDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Merchant> findById(UUID merchantId) {
        return merchantRepository.findByIdAndActiveTrue(merchantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Merchant> findAll(Pageable pageable) {
        return merchantRepository.findAllByActiveTrue(pageable);
    }

    @Override
    public Merchant updateMerchant(UUID merchantId, Merchant merchantUpdateData) {
        Merchant existingMerchant = merchantRepository.findByIdAndActiveTrue(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Active merchant not found with id: " + merchantId));

        if (merchantUpdateData.getName() != null && !merchantUpdateData.getName().isBlank()) {
            existingMerchant.setName(merchantUpdateData.getName());
        }
        return merchantRepository.save(existingMerchant);
    }

    @Override
    public void deleteMerchantById(UUID merchantId) {
        Merchant merchant = merchantRepository.findByIdEvenIfInactive(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found with id: " + merchantId));

        if (!merchant.isActive()) {
            return;
        }

        Page<MerchantUser> merchantUsersPage = merchantUserRepository.findByMerchantIdAndActiveTrue(merchantId, Pageable.unpaged());
        for (MerchantUser user : merchantUsersPage.getContent()) {
            user.setActive(false);
            user.setEnabled(false);
            merchantUserRepository.save(user);
        }

        merchant.setActive(false);
        merchantRepository.save(merchant);
    }
}