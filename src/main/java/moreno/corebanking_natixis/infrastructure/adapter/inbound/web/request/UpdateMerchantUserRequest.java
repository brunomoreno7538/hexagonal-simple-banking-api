package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import moreno.corebanking_natixis.domain.model.UserRole;

@Data
public class UpdateMerchantUserRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    private UserRole role;

    private Boolean enabled;
}