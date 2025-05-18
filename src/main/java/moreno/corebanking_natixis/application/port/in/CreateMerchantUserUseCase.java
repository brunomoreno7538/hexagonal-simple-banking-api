package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.MerchantUser;

public interface CreateMerchantUserUseCase {
    MerchantUser create(MerchantUser merchantUser);
}