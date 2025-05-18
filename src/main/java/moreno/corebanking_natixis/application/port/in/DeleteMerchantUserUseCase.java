package moreno.corebanking_natixis.application.port.in;

import java.util.UUID;

public interface DeleteMerchantUserUseCase {
    void deleteById(UUID userId);
}