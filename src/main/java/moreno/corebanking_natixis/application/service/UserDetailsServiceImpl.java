package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.out.CoreUserRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.domain.model.CoreUser;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CoreUserRepository coreUserRepository;
    private final MerchantUserRepository merchantUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<CoreUser> coreUserOptional = coreUserRepository.findByUsernameAndActiveTrue(username);

        if (coreUserOptional.isPresent()) {
            CoreUser coreUser = coreUserOptional.get();
            if (!coreUser.isEnabled()) {
                throw new UsernameNotFoundException("User " + username + " is disabled.");
            }
            Set<GrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_" + coreUser.getRole().name())
            );
            return new User(coreUser.getUsername(), coreUser.getPassword(), coreUser.isEnabled(),
                    true, true, true, authorities);
        }

        Optional<MerchantUser> merchantUserOptional = merchantUserRepository.findByUsernameAndActiveTrue(username);

        if (merchantUserOptional.isPresent()) {
            MerchantUser merchantUser = merchantUserOptional.get();
            if (!merchantUser.getEnabled()) {
                throw new UsernameNotFoundException("User " + username + " is disabled.");
            }
            Set<GrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_" + merchantUser.getRole().name())
            );
            return new User(merchantUser.getUsername(), merchantUser.getPassword(), merchantUser.getEnabled(),
                    true, true, true, authorities);
        }

        throw new UsernameNotFoundException("User not found or not active/enabled: " + username);
    }
}