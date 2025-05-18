package moreno.corebanking_natixis.application.service;

import moreno.corebanking_natixis.application.port.out.MerchantRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.application.port.out.PasswordEncoderPort;
import moreno.corebanking_natixis.domain.exception.DuplicateResourceException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.Merchant;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantUserServiceTest {

    @Mock
    private MerchantUserRepository merchantUserRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private MerchantUserService merchantUserService;

    private MerchantUser merchantUserInput;
    private MerchantUser existingMerchantUser;
    private Merchant parentMerchant;
    private UUID merchantUserIdValue;
    private UUID parentMerchantIdValue;

    @BeforeEach
    void setUp() {
        merchantUserIdValue = UUID.randomUUID();
        parentMerchantIdValue = UUID.randomUUID();

        parentMerchant = Merchant.builder()
                .id(parentMerchantIdValue)
                .name("Parent Corp")
                .active(true)
                .build();

        merchantUserInput = MerchantUser.builder()
                .username("newstoremanager")
                .password("rawPassword123")
                .email("newmanager@store.com")
                .fullName("New Store Manager")
                .role(UserRole.MERCHANT_ADMIN)
                .merchantId(parentMerchantIdValue)
                .build();

        existingMerchantUser = MerchantUser.builder()
                .id(merchantUserIdValue)
                .username("storemanager")
                .password("encodedPassword")
                .email("manager@store.com")
                .fullName("Store Manager")
                .role(UserRole.MERCHANT_ADMIN)
                .merchantId(parentMerchantIdValue)
                .enabled(true)
                .active(true)
                .build();
    }

    @Test
    void create_shouldCreateMerchantUser_whenValidAndParentMerchantActive() {
        MerchantUser inputUser = MerchantUser.builder()
                .username("newstoremanager")
                .email("newmanager@store.com")
                .password("rawPassword123")
                .merchantId(parentMerchantIdValue)
                .role(UserRole.MERCHANT_ADMIN)
                .build();

        when(merchantUserRepository.existsByUsernameAndActiveTrue("newstoremanager")).thenReturn(false);
        when(merchantUserRepository.existsByEmailAndActiveTrue(inputUser.getEmail())).thenReturn(false);

        when(merchantRepository.findByIdAndActiveTrue(parentMerchantIdValue)).thenReturn(Optional.of(parentMerchant));
        when(passwordEncoderPort.encode("rawPassword123")).thenReturn("encodedPass");
        when(merchantUserRepository.save(any(MerchantUser.class))).thenAnswer(inv -> {
            MerchantUser mu = inv.getArgument(0);
            return mu;
        });

        MerchantUser created = merchantUserService.create(inputUser);

        assertNotNull(created);
        assertEquals("newstoremanager", created.getUsername());
        assertEquals("encodedPass", created.getPassword());
        assertTrue(created.isEnabled());
        assertTrue(created.isActive());
        assertEquals(parentMerchantIdValue, created.getMerchantId());
        verify(merchantUserRepository).save(created);
    }

    @Test
    void create_shouldThrowDuplicateResource_whenUsernameExistsAndActive() {
        when(merchantUserRepository.existsByUsernameAndActiveTrue(merchantUserInput.getUsername())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> merchantUserService.create(merchantUserInput));
    }

    @Test
    void create_shouldThrowDuplicateResource_whenEmailExistsAndActive() {
        when(merchantUserRepository.existsByUsernameAndActiveTrue(merchantUserInput.getUsername())).thenReturn(false);
        when(merchantUserRepository.existsByEmailAndActiveTrue(merchantUserInput.getEmail())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> merchantUserService.create(merchantUserInput));
    }

    @Test
    void create_shouldThrowResourceNotFound_whenParentMerchantNotActiveOrFound() {
        when(merchantUserRepository.existsByUsernameAndActiveTrue(merchantUserInput.getUsername())).thenReturn(false);
        when(merchantUserRepository.existsByEmailAndActiveTrue(merchantUserInput.getEmail())).thenReturn(false);
        when(merchantRepository.findByIdAndActiveTrue(parentMerchantIdValue)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> merchantUserService.create(merchantUserInput));
    }

    @Test
    void findById_shouldReturnActiveUser() {
        when(merchantUserRepository.findByIdAndActiveTrue(merchantUserIdValue)).thenReturn(Optional.of(existingMerchantUser));
        Optional<MerchantUser> found = merchantUserService.findById(merchantUserIdValue);
        assertTrue(found.isPresent());
        assertEquals(existingMerchantUser.getId(), found.get().getId());
    }

    @Test
    void findByMerchantId_shouldReturnPageOfActiveUsers_whenParentMerchantActive() {
        Pageable pageable = PageRequest.of(0,5);
        Page<MerchantUser> userPage = new PageImpl<>(Collections.singletonList(existingMerchantUser), pageable, 1);

        when(merchantRepository.findByIdAndActiveTrue(parentMerchantIdValue)).thenReturn(Optional.of(parentMerchant));
        when(merchantUserRepository.findByMerchantIdAndActiveTrue(parentMerchantIdValue, pageable)).thenReturn(userPage);

        Page<MerchantUser> result = merchantUserService.findByMerchantId(parentMerchantIdValue, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(existingMerchantUser.getId(), result.getContent().get(0).getId());
    }

    @Test
    void findByMerchantId_shouldThrowResourceNotFound_whenParentMerchantNotActive() {
        Pageable pageable = PageRequest.of(0,5);
        when(merchantRepository.findByIdAndActiveTrue(parentMerchantIdValue)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> merchantUserService.findByMerchantId(parentMerchantIdValue, pageable));
    }

    @Test
    void deleteById_shouldSoftDeleteMerchantUser() {
        when(merchantUserRepository.findByIdEvenIfInactive(merchantUserIdValue)).thenReturn(Optional.of(existingMerchantUser));
        ArgumentCaptor<MerchantUser> userCaptor = ArgumentCaptor.forClass(MerchantUser.class);

        merchantUserService.deleteById(merchantUserIdValue);

        verify(merchantUserRepository).save(userCaptor.capture());
        MerchantUser savedUser = userCaptor.getValue();
        assertFalse(savedUser.isActive());
        assertFalse(savedUser.isEnabled());
    }

    @Test
    void deleteById_shouldDoNothing_ifUserAlreadyInactive() {
        existingMerchantUser.setActive(false);
        when(merchantUserRepository.findByIdEvenIfInactive(merchantUserIdValue)).thenReturn(Optional.of(existingMerchantUser));
        merchantUserService.deleteById(merchantUserIdValue);
        verify(merchantUserRepository, never()).save(any(MerchantUser.class));
    }
    @Test
    void deleteById_shouldThrowResourceNotFound_ifUserNotFound() {
        when(merchantUserRepository.findByIdEvenIfInactive(merchantUserIdValue)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> merchantUserService.deleteById(merchantUserIdValue));
    }
}