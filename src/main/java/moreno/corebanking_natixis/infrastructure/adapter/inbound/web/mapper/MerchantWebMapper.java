package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper;

import moreno.corebanking_natixis.domain.model.Merchant;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateMerchantRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.UpdateMerchantRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.MerchantResponse;
import org.springframework.stereotype.Component;

@Component
public class MerchantWebMapper {

    public Merchant toDomain(CreateMerchantRequest request) {
        return Merchant.builder()
                .name(request.getName())
                .cnpj(request.getCnpj())
                .build();
    }

    public MerchantResponse toResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .merchantId(merchant.getId())
                .name(merchant.getName())
                .cnpj(merchant.getCnpj())
                .accountId(merchant.getAccountId())
                .build();
    }

    public Merchant toDomain(UpdateMerchantRequest request) {
        return Merchant.builder()
                .name(request.getName())
                .build();
    }
}