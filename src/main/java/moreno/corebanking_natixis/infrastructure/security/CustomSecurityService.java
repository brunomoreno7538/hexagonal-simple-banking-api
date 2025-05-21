package moreno.corebanking_natixis.infrastructure.security;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.domain.model.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("customSecurityService")
@RequiredArgsConstructor
public class CustomSecurityService {

    private final MerchantUserRepository merchantUserRepository;

    public boolean isSelfMerchantUser(Authentication authentication, UUID targetUserId) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            return false;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUsername = userDetails.getUsername();

        Optional<MerchantUser> authenticatedMerchantUserOpt = merchantUserRepository.findByUsernameAndActiveTrue(currentUsername);
        if (authenticatedMerchantUserOpt.isEmpty()) {
            return false;
        }
        return authenticatedMerchantUserOpt.get().getId().equals(targetUserId);
    }

    public boolean isUserFromMerchant(Authentication authentication, UUID targetMerchantId) {
        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return false;
        }
        String currentUsername = userDetails.getUsername();

        Optional<MerchantUser> authenticatedMerchantUserOpt = merchantUserRepository.findByUsernameAndActiveTrue(currentUsername);
        if (authenticatedMerchantUserOpt.isEmpty()) {
            return false;
        }
        return authenticatedMerchantUserOpt.get().getMerchantId().equals(targetMerchantId);
    }

    public boolean isMerchantAdminOfTargetUser(Authentication authentication, UUID targetUserId) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            return false;
        }

        UserDetails callingUserDetails = (UserDetails) authentication.getPrincipal();
        String callingUsername = callingUserDetails.getUsername();

        boolean isCallingUserMerchantAdmin = callingUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_" + UserRole.MERCHANT_ADMIN.name()));

        if (!isCallingUserMerchantAdmin) {
            return false;
        }

        Optional<MerchantUser> callingAdminOpt = merchantUserRepository.findByUsernameAndActiveTrue(callingUsername);
        Optional<MerchantUser> targetUserOpt = merchantUserRepository.findByIdAndActiveTrue(targetUserId);

        if (callingAdminOpt.isEmpty() || targetUserOpt.isEmpty()) {
            return false;
        }

        MerchantUser callingAdmin = callingAdminOpt.get();
        MerchantUser targetUser = targetUserOpt.get();

        return callingAdmin.getMerchantId().equals(targetUser.getMerchantId());
    }
}