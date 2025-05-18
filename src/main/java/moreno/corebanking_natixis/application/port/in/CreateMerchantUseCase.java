package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.Merchant;

public interface CreateMerchantUseCase {
    Merchant createMerchant(Merchant merchant);
}