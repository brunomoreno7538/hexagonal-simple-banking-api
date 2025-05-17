package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.DeleteCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.GetCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.UpdateCoreUserUseCase;
import moreno.corebanking_natixis.application.port.out.CoreUserRepository;
import moreno.corebanking_natixis.application.port.out.PasswordEncoderPort;
import moreno.corebanking_natixis.domain.exception.DuplicateResourceException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.CoreUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        if (coreUserRepository.existsByUsername(coreUser.getUsername())) {
            throw new DuplicateResourceException("Username " + coreUser.getUsername() + " already exists.");
        }
        if (coreUserRepository.existsByEmail(coreUser.getEmail())) {
            throw new DuplicateResourceException("Email " + coreUser.getEmail() + " already exists.");
        }
        coreUser.setPassword(passwordEncoderPort.encode(coreUser.getPassword()));
        coreUser.setEnabled(true);
        return coreUserRepository.save(coreUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CoreUser> findById(UUID userId) {
        return coreUserRepository.findById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CoreUser> findByUsername(String username) {
        return coreUserRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoreUser> findAll() {
        return coreUserRepository.findAll();
    }

    @Override
    public CoreUser update(UUID userId, CoreUser coreUserUpdateData) {
        CoreUser existingUser = coreUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("CoreUser not found with id: " + userId));

        if (coreUserUpdateData.getEmail() != null && !coreUserUpdateData.getEmail().equals(existingUser.getEmail())) {
            if (coreUserRepository.existsByEmail(coreUserUpdateData.getEmail())) {
                throw new DuplicateResourceException("Email " + coreUserUpdateData.getEmail() + " already exists.");
            }
            existingUser.setEmail(coreUserUpdateData.getEmail());
        }
        if (coreUserUpdateData.getFullName() != null) {
            existingUser.setFullName(coreUserUpdateData.getFullName());
        }
        if (coreUserUpdateData.getRole() != null) {
            existingUser.setRole(coreUserUpdateData.getRole());
        }
        if (coreUserUpdateData.getPassword() != null && !coreUserUpdateData.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoderPort.encode(coreUserUpdateData.getPassword()));
        }

        return coreUserRepository.save(existingUser);
    }

    @Override
    public void deleteById(UUID userId) {
        if (!coreUserRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("CoreUser not found with id: " + userId);
        }
        coreUserRepository.deleteById(userId);
    }
}