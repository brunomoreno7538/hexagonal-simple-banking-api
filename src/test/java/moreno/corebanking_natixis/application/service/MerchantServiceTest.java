package moreno.corebanking_natixis.application.service;

import moreno.corebanking_natixis.application.port.out.AccountRepository;
import moreno.corebanking_natixis.application.port.out.MerchantRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.domain.exception.DuplicateResourceException;
import moreno.corebanking_natixis.domain.model.Merchant;
import moreno.corebanking_natixis.domain.model.MerchantUser;
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
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private MerchantUserRepository merchantUserRepository;

    @InjectMocks
    private MerchantService merchantService;

    private Merchant merchantDetailsInput;
    private Merchant existingMerchant;
    private UUID merchantIdValue;
    private UUID accountIdValue;

    @BeforeEach
    void setUp() {
        merchantIdValue = UUID.randomUUID();
        accountIdValue = UUID.randomUUID();

        merchantDetailsInput = Merchant.builder()
                .name("New Test Merchant")
                .cnpj("11122233000155")
                .build();

        existingMerchant = Merchant.builder()
                .id(merchantIdValue)
                .accountId(accountIdValue)
                .name("Existing Merchant")
                .cnpj("98765432000100")
                .active(true)
                .build();
    }

    @Test
    void createMerchant_shouldCreateMerchant_whenCnpjIsUnique() {
        when(merchantRepository.existsByCnpjAndActiveTrue("11122233000155")).thenReturn(false);

        when(merchantRepository.save(any(Merchant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Merchant created = merchantService.createMerchant(merchantDetailsInput);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertNotNull(created.getAccountId());
        assertEquals("New Test Merchant", created.getName());
        assertTrue(created.isActive());
    }

    @Test
    void createMerchant_shouldThrowDuplicateResourceException_whenCnpjExists() {
        when(merchantRepository.existsByCnpjAndActiveTrue("11122233000155")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> merchantService.createMerchant(merchantDetailsInput));
    }

    @Test
    void findById_shouldReturnActiveMerchant_whenFound() {
        when(merchantRepository.findByIdAndActiveTrue(merchantIdValue)).thenReturn(Optional.of(existingMerchant));
        Optional<Merchant> found = merchantService.findById(merchantIdValue);
        assertTrue(found.isPresent());
        assertEquals(existingMerchant.getId(), found.get().getId());
    }

    @Test
    void findAll_shouldReturnPageOfActiveMerchants() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Merchant> merchantPage = new PageImpl<>(Collections.singletonList(existingMerchant), pageable, 1);
        when(merchantRepository.findAllByActiveTrue(pageable)).thenReturn(merchantPage);

        Page<Merchant> result = merchantService.findAll(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(existingMerchant.getId(), result.getContent().get(0).getId());
    }

    @Test
    void updateMerchant_shouldUpdateName_whenMerchantActive() {
        Merchant updateData = Merchant.builder().name("Updated Merchant Name").build();
        when(merchantRepository.findByIdAndActiveTrue(merchantIdValue)).thenReturn(Optional.of(existingMerchant));
        when(merchantRepository.save(any(Merchant.class))).thenReturn(existingMerchant);

        Merchant updated = merchantService.updateMerchant(merchantIdValue, updateData);
        assertEquals("Updated Merchant Name", updated.getName());
        verify(merchantRepository).save(existingMerchant);
    }

    @Test
    void deleteMerchantById_shouldSoftDeleteMerchantAndItsUsers() {
        MerchantUser user1 = MerchantUser.builder().id(UUID.randomUUID()).merchantId(merchantIdValue).active(true).enabled(true).build();
        Page<MerchantUser> usersPage = new PageImpl<>(Collections.singletonList(user1));

        when(merchantRepository.findByIdEvenIfInactive(merchantIdValue)).thenReturn(Optional.of(existingMerchant));
        when(merchantUserRepository.findByMerchantIdAndActiveTrue(merchantIdValue, Pageable.unpaged())).thenReturn(usersPage);

        ArgumentCaptor<Merchant> merchantCaptor = ArgumentCaptor.forClass(Merchant.class);
        ArgumentCaptor<MerchantUser> merchantUserCaptor = ArgumentCaptor.forClass(MerchantUser.class);

        merchantService.deleteMerchantById(merchantIdValue);

        verify(merchantUserRepository).save(merchantUserCaptor.capture());
        assertFalse(merchantUserCaptor.getValue().isActive());
        assertFalse(merchantUserCaptor.getValue().isEnabled());

        verify(merchantRepository).save(merchantCaptor.capture());
        assertFalse(merchantCaptor.getValue().isActive());
    }
    @Test
    void deleteMerchantById_shouldDoNothing_ifMerchantAlreadyInactive() {
        existingMerchant.setActive(false);
        when(merchantRepository.findByIdEvenIfInactive(merchantIdValue)).thenReturn(Optional.of(existingMerchant));

        merchantService.deleteMerchantById(merchantIdValue);

        verify(merchantUserRepository, never()).findByMerchantIdAndActiveTrue(any(), any());
        verify(accountRepository, never()).findById(any());
        verify(merchantRepository, never()).save(any(Merchant.class));
    }
}