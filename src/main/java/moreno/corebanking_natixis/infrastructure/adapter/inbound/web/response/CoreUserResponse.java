package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response;

import lombok.Builder;
import lombok.Data;
import moreno.corebanking_natixis.domain.model.UserRole;

import java.util.UUID;

@Data
@Builder
public class CoreUserResponse {
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private boolean enabled;
}