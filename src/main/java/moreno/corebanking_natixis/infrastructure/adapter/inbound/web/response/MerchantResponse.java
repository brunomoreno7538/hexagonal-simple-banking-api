package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class MerchantResponse {
    private UUID merchantId;
    private String name;
    private String cnpj;
    private UUID accountId;
}