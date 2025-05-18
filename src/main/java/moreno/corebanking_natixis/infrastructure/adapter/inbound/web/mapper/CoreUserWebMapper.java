package moreno.corebanking_natixis.infrastructure.adapter.inbound.web.mapper;

import moreno.corebanking_natixis.domain.model.CoreUser;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.CreateCoreUserRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.request.UpdateCoreUserRequest;
import moreno.corebanking_natixis.infrastructure.adapter.inbound.web.response.CoreUserResponse;
import org.springframework.stereotype.Component;

@Component
public class CoreUserWebMapper {

    public CoreUser toDomain(CreateCoreUserRequest request) {
        return CoreUser.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(request.getRole())
                .build();
    }

    public CoreUser toDomain(UpdateCoreUserRequest request) {
        return CoreUser.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
    }

    public CoreUserResponse toResponse(CoreUser coreUser) {
        return CoreUserResponse.builder()
                .userId(coreUser.getId())
                .username(coreUser.getUsername())
                .email(coreUser.getEmail())
                .fullName(coreUser.getFullName())
                .role(coreUser.getRole())
                .enabled(coreUser.isEnabled())
                .build();
    }
}