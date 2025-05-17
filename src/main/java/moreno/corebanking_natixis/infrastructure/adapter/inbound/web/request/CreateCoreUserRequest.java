package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import moreno.corebanking_natixis.domain.model.UserRole;

@Data
public class CreateCoreUserRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String fullName;

    @NotNull
    private UserRole role;
}