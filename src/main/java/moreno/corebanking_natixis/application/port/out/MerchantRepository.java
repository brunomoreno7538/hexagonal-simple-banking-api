package moreno.corebanking_natixis.application.port.out;

import moreno.corebanking_natixis.domain.model.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository {
    Merchant save(Merchant merchant);
    Optional<Merchant> findByIdAndActiveTrue(UUID merchantId);
    Optional<Merchant> findByCnpjAndActiveTrue(String cnpj);
    Page<Merchant> findAllByActiveTrue(Pageable pageable);
    boolean existsByCnpjAndActiveTrue(String cnpj);
    Optional<Merchant> findByIdEvenIfInactive(UUID merchantId);
}