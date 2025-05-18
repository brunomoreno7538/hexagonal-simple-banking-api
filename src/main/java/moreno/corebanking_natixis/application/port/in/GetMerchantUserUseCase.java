package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.MerchantUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GetMerchantUserUseCase {
    Optional<MerchantUser> findById(UUID userId);
    Page<MerchantUser> findByMerchantId(UUID merchantId, Pageable pageable);
}