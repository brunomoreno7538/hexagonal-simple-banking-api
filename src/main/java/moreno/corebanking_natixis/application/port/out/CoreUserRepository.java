package moreno.corebanking_natixis.application.port.out;

import moreno.corebanking_natixis.domain.model.CoreUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CoreUserRepository {
    CoreUser save(CoreUser coreUser);
    Optional<CoreUser> findByIdAndActiveTrue(UUID userId);
    Optional<CoreUser> findByUsernameAndActiveTrue(String username);
    Page<CoreUser> findAllByActiveTrue(Pageable pageable);
    boolean existsByUsernameAndActiveTrue(String username);
    boolean existsByEmailAndActiveTrue(String email);
    Optional<CoreUser> findByIdEvenIfInactive(UUID userId);
}
