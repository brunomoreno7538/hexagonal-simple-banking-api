package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.CoreUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GetCoreUserUseCase {
    Optional<CoreUser> findById(UUID userId);
    Optional<CoreUser> findByUsername(String username);
    Page<CoreUser> findAll(Pageable pageable);
}