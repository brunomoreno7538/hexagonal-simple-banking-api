package moreno.corebanking_natixis.application.port.out;

import moreno.corebanking_natixis.domain.model.MerchantUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface MerchantUserRepository {
    MerchantUser save(MerchantUser merchantUser);
    Optional<MerchantUser> findByIdAndActiveTrue(UUID userId);
    Optional<MerchantUser> findByUsernameAndActiveTrue(String username);
    Page<MerchantUser> findByMerchantIdAndActiveTrue(UUID merchantId, Pageable pageable);
    Page<MerchantUser> findAllByActiveTrue(Pageable pageable);
    boolean existsByUsernameAndActiveTrue(String username);
    boolean existsByEmailAndActiveTrue(String email);
    Optional<MerchantUser> findByIdEvenIfInactive(UUID userId);
}