package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.CoreUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetCoreUserUseCase {
    Optional<CoreUser> findById(UUID userId);
    Optional<CoreUser> findByUsername(String username);
    List<CoreUser> findAll();
}