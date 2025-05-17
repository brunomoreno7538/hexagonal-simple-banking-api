package moreno.corebanking_natixis.application.port.out;

import moreno.corebanking_natixis.domain.model.CoreUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoreUserRepository {
    CoreUser save(CoreUser coreUser);
    Optional<CoreUser> findById(UUID userId);
    Optional<CoreUser> findByUsername(String username);
    Optional<CoreUser> findByEmail(String email);
    List<CoreUser> findAll();
    void deleteById(UUID userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
