package moreno.corebanking_natixis.application.port.in;

import moreno.corebanking_natixis.application.dto.MerchantProfileData;
import org.springframework.security.core.userdetails.UserDetails;

public interface GetMyMerchantProfileUseCase {
    MerchantProfileData getMyProfile(UserDetails principal);
}