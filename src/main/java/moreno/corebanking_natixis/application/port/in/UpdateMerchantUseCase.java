package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.Merchant;
import java.util.UUID;

public interface UpdateMerchantUseCase {
    Merchant updateMerchant(UUID merchantId, Merchant merchantUpdateData);
}