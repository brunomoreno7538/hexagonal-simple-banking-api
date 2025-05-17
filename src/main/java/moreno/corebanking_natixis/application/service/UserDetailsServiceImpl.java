package moreno.corebanking_natixis.application.service;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.out.CoreUserRepository;
import moreno.corebanking_natixis.domain.model.CoreUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CoreUserRepository coreUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CoreUser coreUser = coreUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + coreUser.getRole().name())
        );

        return new User(coreUser.getUsername(), coreUser.getPassword(), coreUser.isEnabled(),
                true, true, true, authorities);
    }
}