package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import moreno.corebanking_natixis.domain.model.UserRole;
import moreno.corebanking_natixis.infrastructure.validation.ValidEnumValue;

import java.util.UUID;

@Data
public class CreateMerchantUserRequest {
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
    @ValidEnumValue(
            enumClass = UserRole.class,
            allowedValues = {"MERCHANT_ADMIN", "MERCHANT_USER"},
            message = "Role for merchant user must be MERCHANT_ADMIN or MERCHANT_USER."
    )
    private UserRole role;

    @NotNull
    private UUID merchantId;
}