package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper;

import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateMerchantUserRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.UpdateMerchantUserRequest;
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

    public MerchantUser toDomain(UpdateMerchantUserRequest request) {
        return MerchantUser.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .enabled(request.getEnabled())
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
                .enabled(merchantUser.getEnabled())
                .build();
    }
}