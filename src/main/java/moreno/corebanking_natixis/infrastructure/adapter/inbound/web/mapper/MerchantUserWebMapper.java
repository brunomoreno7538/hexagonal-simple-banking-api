package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper;

import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateMerchantUserRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.MerchantUserResponse;
import org.springframework.stereotype.Component;

@Component
public class MerchantUserWebMapper {

    public MerchantUser toDomain(CreateMerchantUserRequest request) {
        return MerchantUser.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(request.getRole())
                .merchantId(request.getMerchantId())
                .build();
    }

    public MerchantUserResponse toResponse(MerchantUser merchantUser) {
        return MerchantUserResponse.builder()
                .userId(merchantUser.getId())
                .username(merchantUser.getUsername())
                .email(merchantUser.getEmail())
                .fullName(merchantUser.getFullName())
                .role(merchantUser.getRole())
                .merchantId(merchantUser.getMerchantId())
                .enabled(merchantUser.isEnabled())
                .build();
    }
}