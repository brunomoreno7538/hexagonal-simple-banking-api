package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.CoreUser;

public interface CreateCoreUserUseCase {
    CoreUser create(CoreUser coreUser);
}
