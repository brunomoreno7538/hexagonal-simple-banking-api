package moreno.corebanking_natixis.application.port.in;

import java.util.UUID;

public interface DeleteCoreUserUseCase {
    void deleteById(UUID userId);
}
