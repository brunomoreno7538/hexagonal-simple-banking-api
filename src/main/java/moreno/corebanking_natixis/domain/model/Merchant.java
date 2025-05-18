package moreno.corebanking_natixis.domain.model;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class Merchant {
    private UUID id;
    private String name;
    private String cnpj;
    private UUID accountId;
    @Builder.Default
    private boolean active = true;
}