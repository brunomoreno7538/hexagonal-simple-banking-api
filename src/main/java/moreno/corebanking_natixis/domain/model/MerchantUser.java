package moreno.corebanking_natixis.domain.model;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class MerchantUser {
    private UUID id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private UserRole role;
    private UUID merchantId;
    private boolean enabled;
    @Builder.Default
    private boolean active = true;
}