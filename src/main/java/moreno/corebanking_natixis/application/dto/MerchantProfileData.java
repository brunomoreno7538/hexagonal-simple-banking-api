package moreno.corebanking_natixis.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moreno.corebanking_natixis.domain.model.Merchant;
import moreno.corebanking_natixis.domain.model.MerchantUser;

@Getter
@AllArgsConstructor
public class MerchantProfileData {
    private final MerchantUser merchantUser;
    private final Merchant merchant;
}