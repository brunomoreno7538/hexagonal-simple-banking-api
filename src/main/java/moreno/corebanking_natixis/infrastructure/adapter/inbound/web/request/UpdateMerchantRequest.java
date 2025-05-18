package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMerchantRequest {
    @Size(min = 2, max = 100)
    private String name;
}