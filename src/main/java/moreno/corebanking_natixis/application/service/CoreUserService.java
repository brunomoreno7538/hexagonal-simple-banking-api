package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.DeleteCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.GetCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.UpdateCoreUserUseCase;
import moreno.corebanking_natixis.application.port.out.CoreUserRepository;
import moreno.corebanking_natixis.application.port.out.PasswordEncoderPort;
import moreno.corebanking_natixis.domain.exception.BankingBusinessException;
import moreno.corebanking_natixis.domain.exception.DuplicateResourceException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.CoreUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CoreUserService implements CreateCoreUserUseCase, GetCoreUserUseCase, UpdateCoreUserUseCase, DeleteCoreUserUseCase {

    private final CoreUserRepository coreUserRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    public CoreUser create(CoreUser coreUser) {
        if (coreUserRepository.existsByUsernameAndActiveTrue(coreUser.getUsername())) {
            throw new DuplicateResourceException("Username " + coreUser.getUsername() + " already exists and is active.");
        }
        if (coreUserRepository.existsByEmailAndActiveTrue(coreUser.getEmail())) {
            throw new DuplicateResourceException("Email " + coreUser.getEmail() + " already exists and is active.");
        }
        coreUser.setPassword(passwordEncoderPort.encode(coreUser.getPassword()));
        coreUser.setEnabled(true);
        coreUser.setActive(true);
        return coreUserRepository.save(coreUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CoreUser> findById(UUID userId) {
        return coreUserRepository.findByIdAndActiveTrue(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CoreUser> findByUsername(String username) {
        return coreUserRepository.findByUsernameAndActiveTrue(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CoreUser> findAll(Pageable pageable) {
        return coreUserRepository.findAllByActiveTrue(pageable);
    }

    @Override
    public CoreUser update(UUID userId, CoreUser coreUserUpdateData) {
        CoreUser existingUser = coreUserRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active CoreUser not found with id: " + userId));

        if (coreUserUpdateData.getEmail() != null && !coreUserUpdateData.getEmail().equals(existingUser.getEmail())) {
            if (coreUserRepository.existsByEmailAndActiveTrue(coreUserUpdateData.getEmail())) {
                throw new DuplicateResourceException("Email " + coreUserUpdateData.getEmail() + " already exists.");
            }
            existingUser.setEmail(coreUserUpdateData.getEmail());
        }
        return coreUserRepository.save(existingUser);
    }

    @Override
    public void deleteById(UUID userIdToDelete) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();

        CoreUser userToDelete = coreUserRepository.findByIdEvenIfInactive(userIdToDelete)
                .orElseThrow(() -> new ResourceNotFoundException("CoreUser not found with id: " + userIdToDelete));

        if (!userToDelete.isActive()) {
            return;
        }

        if (userToDelete.getUsername().equals(currentUsername)) {
            throw new BankingBusinessException("Admin users cannot delete themselves.");
        }

        userToDelete.setActive(false);
        userToDelete.setEnabled(false);
        coreUserRepository.save(userToDelete);
    }
}