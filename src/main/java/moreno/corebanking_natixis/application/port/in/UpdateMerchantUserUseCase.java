package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.MerchantUser;
import java.util.UUID;

public interface UpdateMerchantUserUseCase {
    MerchantUser update(UUID userId, MerchantUser userUpdateData);
}