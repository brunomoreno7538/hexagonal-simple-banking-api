package moreno.corebanking_natixis.application.port.in;

import java.util.UUID;

public interface DeleteMerchantUseCase {
    void deleteMerchantById(UUID merchantId);
}