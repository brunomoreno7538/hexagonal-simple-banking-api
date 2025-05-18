package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.domain.model.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface GetMerchantUseCase {
    Optional<Merchant> findById(UUID merchantId);
    Page<Merchant> findAll(Pageable pageable);
}