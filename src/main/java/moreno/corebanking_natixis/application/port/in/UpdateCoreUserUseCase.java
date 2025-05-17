package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.CoreUser;

import java.util.UUID;

public interface UpdateCoreUserUseCase {
    CoreUser update(UUID userId, CoreUser coreUser);
}
