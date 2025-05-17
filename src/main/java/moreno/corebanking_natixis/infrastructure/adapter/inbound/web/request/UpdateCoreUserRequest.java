package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import moreno.corebanking_natixis.domain.model.UserRole;

@Data
public class UpdateCoreUserRequest {
    @Email
    private String email;

    private String fullName;

    @Size(min = 8, max = 100)
    private String password;

    private UserRole role;
}