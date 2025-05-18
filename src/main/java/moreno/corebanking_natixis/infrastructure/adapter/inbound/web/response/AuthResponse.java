package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private long expiresIn;
}