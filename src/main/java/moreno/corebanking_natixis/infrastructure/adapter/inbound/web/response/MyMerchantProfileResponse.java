package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyMerchantProfileResponse {
    private MerchantUserResponse user;
    private MerchantResponse merchant;
}