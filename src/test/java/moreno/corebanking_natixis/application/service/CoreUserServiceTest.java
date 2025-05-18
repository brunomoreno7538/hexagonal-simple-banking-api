package moreno.corebanking_natixis.application.service;

import moreno.corebanking_natixis.application.port.out.CoreUserRepository;
import moreno.corebanking_natixis.application.port.out.PasswordEncoderPort;
import moreno.corebanking_natixis.domain.exception.BankingBusinessException;
import moreno.corebanking_natixis.domain.exception.DuplicateResourceException;
import moreno.corebanking_natixis.domain.exception.ResourceNotFoundException;
import moreno.corebanking_natixis.domain.model.CoreUser;
import moreno.corebanking_natixis.domain.model.UserRole;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoreUserServiceTest {

    @Mock
    private CoreUserRepository coreUserRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private CoreUserService coreUserService;

    private CoreUser coreUser;
    private UUID coreUserIdValue;
    private String currentAdminUsernameForTest = "callingAdmin";

    @BeforeEach
    void setUp() {
        coreUserIdValue = UUID.randomUUID();
        coreUser = CoreUser.builder()
                .id(coreUserIdValue)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .fullName("Test User")
                .role(UserRole.ADMIN)
                .enabled(true)
                .active(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String username) {
        UserDetails mockUserDetails = mock(UserDetails.class);
        lenient().when(mockUserDetails.getUsername()).thenReturn(username);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(mockUserDetails);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void create_shouldCreateUser_whenUsernameAndEmailAreUnique() {
        CoreUser inputUser = CoreUser.builder().username("newuser").email("new@example.com").password("pass").role(UserRole.ADMIN).build();
        when(coreUserRepository.existsByUsernameAndActiveTrue("newuser")).thenReturn(false);
        when(coreUserRepository.existsByEmailAndActiveTrue("new@example.com")).thenReturn(false);
        when(passwordEncoderPort.encode("pass")).thenReturn("encodedPassword");
        when(coreUserRepository.save(any(CoreUser.class))).thenAnswer(invocation -> {
            CoreUser saved = invocation.getArgument(0);
            return saved;
        });

        CoreUser created = coreUserService.create(inputUser);

        assertNotNull(created);
        assertEquals("newuser", created.getUsername());
        assertEquals("encodedPassword", created.getPassword());
        assertTrue(created.isEnabled());
        assertTrue(created.isActive());
        verify(coreUserRepository).save(created);
    }

    @Test
    void create_shouldThrowDuplicateResourceException_whenUsernameExists() {
        CoreUser inputUser = CoreUser.builder().username("existingUser").email("new@example.com").build();
        when(coreUserRepository.existsByUsernameAndActiveTrue("existingUser")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> coreUserService.create(inputUser));
    }

    @Test
    void create_shouldThrowDuplicateResourceException_whenEmailExists() {
        CoreUser inputUser = CoreUser.builder().username("newuser").email("existing@example.com").build();
        when(coreUserRepository.existsByUsernameAndActiveTrue("newuser")).thenReturn(false);
        when(coreUserRepository.existsByEmailAndActiveTrue("existing@example.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> coreUserService.create(inputUser));
    }

    @Test
    void findById_shouldReturnUser_whenFoundAndActive() {
        when(coreUserRepository.findByIdAndActiveTrue(coreUserIdValue)).thenReturn(Optional.of(coreUser));
        Optional<CoreUser> found = coreUserService.findById(coreUserIdValue);
        assertTrue(found.isPresent());
        assertEquals(coreUser, found.get());
    }

    @Test
    void findByUsername_shouldReturnUser_whenFoundAndActive() {
        when(coreUserRepository.findByUsernameAndActiveTrue("testuser")).thenReturn(Optional.of(coreUser));
        Optional<CoreUser> found = coreUserService.findByUsername("testuser");
        assertTrue(found.isPresent());
        assertEquals(coreUser, found.get());
    }

    @Test
    void findAll_shouldReturnPageOfActiveUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CoreUser> userPage = new PageImpl<>(Collections.singletonList(coreUser), pageable, 1);
        when(coreUserRepository.findAllByActiveTrue(pageable)).thenReturn(userPage);

        Page<CoreUser> result = coreUserService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(coreUser, result.getContent().get(0));
    }

    @Test
    void update_shouldUpdateUser_whenUserExistsAndDataIsValid() {
        CoreUser updateData = CoreUser.builder().fullName("Test User").email("newemail@example.com").build();
        when(coreUserRepository.findByIdAndActiveTrue(coreUserIdValue)).thenReturn(Optional.of(coreUser));
        when(coreUserRepository.existsByEmailAndActiveTrue("newemail@example.com")).thenReturn(false);
        when(coreUserRepository.save(any(CoreUser.class))).thenReturn(coreUser);

        CoreUser updated = coreUserService.update(coreUserIdValue, updateData);

        assertEquals("Test User", updated.getFullName());
        assertEquals("newemail@example.com", updated.getEmail());
        verify(coreUserRepository).save(coreUser);
    }

    @Test
    void update_shouldThrowResourceNotFound_whenUserToUpdateNotFoundOrInactive() {
        CoreUser updateData = CoreUser.builder().fullName("Updated Name").build();
        when(coreUserRepository.findByIdAndActiveTrue(coreUserIdValue)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> coreUserService.update(coreUserIdValue, updateData));
    }

    @Test
    void deleteById_shouldSoftDeleteUser_whenUserExistsAndNotSelf() {
        mockSecurityContext(currentAdminUsernameForTest);

        when(coreUserRepository.findByIdEvenIfInactive(coreUserIdValue)).thenReturn(Optional.of(coreUser));
        ArgumentCaptor<CoreUser> userCaptor = ArgumentCaptor.forClass(CoreUser.class);

        coreUserService.deleteById(coreUserIdValue);

        verify(coreUserRepository).save(userCaptor.capture());
        CoreUser savedUser = userCaptor.getValue();
        assertFalse(savedUser.isActive());
        assertFalse(savedUser.isEnabled());
    }

    @Test
    void deleteById_shouldThrowBankingBusinessException_whenSelfDelete() {
        mockSecurityContext(coreUser.getUsername());

        when(coreUserRepository.findByIdEvenIfInactive(coreUserIdValue)).thenReturn(Optional.of(coreUser));

        assertThrows(BankingBusinessException.class, () -> coreUserService.deleteById(coreUserIdValue));
        verify(coreUserRepository, never()).save(any(CoreUser.class));
    }

    @Test
    void deleteById_shouldDoNothing_ifUserAlreadyInactive() {
        mockSecurityContext(currentAdminUsernameForTest);
        CoreUser inactiveUser = CoreUser.builder().id(coreUserIdValue).username("testuser").active(false).enabled(false).build();
        when(coreUserRepository.findByIdEvenIfInactive(coreUserIdValue)).thenReturn(Optional.of(inactiveUser));

        coreUserService.deleteById(coreUserIdValue);

        verify(coreUserRepository, never()).save(any(CoreUser.class));
    }
}