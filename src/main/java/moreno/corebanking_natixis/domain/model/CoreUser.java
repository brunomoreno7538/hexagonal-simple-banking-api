package moreno.corebanking_natixis.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreUser {
    private UUID id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private UserRole role;
    private boolean enabled;
    @Builder.Default
    private boolean active = true;
}
