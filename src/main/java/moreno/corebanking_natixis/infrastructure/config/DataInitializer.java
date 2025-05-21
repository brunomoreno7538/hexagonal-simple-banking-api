package moreno.corebanking_natixis.infrastructure.config;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateCoreUserUseCase;
import moreno.corebanking_natixis.application.port.in.CreateMerchantUseCase;
import moreno.corebanking_natixis.application.port.in.CreateMerchantUserUseCase;
import moreno.corebanking_natixis.application.port.out.CoreUserRepository;
import moreno.corebanking_natixis.application.port.out.MerchantRepository;
import moreno.corebanking_natixis.application.port.out.MerchantUserRepository;
import moreno.corebanking_natixis.domain.model.CoreUser;
import moreno.corebanking_natixis.domain.model.Merchant;
import moreno.corebanking_natixis.domain.model.MerchantUser;
import moreno.corebanking_natixis.domain.model.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CreateCoreUserUseCase createCoreUserUseCase;
    private final CoreUserRepository coreUserRepository;
    private final CreateMerchantUseCase createMerchantUseCase;
    private final MerchantRepository merchantRepository;
    private final CreateMerchantUserUseCase createMerchantUserUseCase;
    private final MerchantUserRepository merchantUserRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!coreUserRepository.findByUsernameAndActiveTrue("initialadmin").isPresent()) {
            CoreUser adminUser = CoreUser.builder()
                    .username("admin")
                    .password("123456")
                    .email("initialadmin@example.com")
                    .fullName("Initial Admin")
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .build();
            try {
                createCoreUserUseCase.create(adminUser);
                System.out.println("***********************************************************");
                System.out.println("Initial admin user 'initialadmin' created with password 'initialpassword'");
                System.out.println("***********************************************************");
            } catch (Exception e) {
                System.err.println("***********************************************************");
                System.err.println("Error creating initial admin user: " + e.getMessage());
                System.err.println("***********************************************************");
            }
        } else {
            System.out.println("Initial admin user 'initialadmin' already exists.");
        }

        String baseMerchantCnpj = "00000000000000";
        Merchant createdBaseMerchant = null;

        if (!merchantRepository.findByCnpjAndActiveTrue(baseMerchantCnpj).isPresent()) {
            Merchant baseMerchantDetails = Merchant.builder()
                    .name("Base Merchant Inc.")
                    .cnpj(baseMerchantCnpj)
                    .build();
            try {
                createdBaseMerchant = createMerchantUseCase.createMerchant(baseMerchantDetails);
                System.out.println("***********************************************************");
                System.out.println("Base merchant 'Base Merchant Inc.' created with CNPJ " + baseMerchantCnpj);
                System.out.println("Merchant ID: " + createdBaseMerchant.getId());
                System.out.println("Account ID: " + createdBaseMerchant.getAccountId());
                System.out.println("***********************************************************");
            } catch (Exception e) {
                System.err.println("***********************************************************");
                System.err.println("Error creating base merchant: " + e.getMessage());
                System.err.println("***********************************************************");
            }
        } else {
            System.out.println("Base merchant with CNPJ " + baseMerchantCnpj + " already exists.");
            Optional<Merchant> existingBaseMerchantOpt = merchantRepository.findByCnpjAndActiveTrue(baseMerchantCnpj);
            if (existingBaseMerchantOpt.isPresent()) {
                createdBaseMerchant = existingBaseMerchantOpt.get();
            }
        }
        
        String baseMerchantUsername = "merchant";
        if (createdBaseMerchant != null && !merchantUserRepository.findByUsernameAndActiveTrue(baseMerchantUsername).isPresent()) {
            MerchantUser baseMerchantUserDetails = MerchantUser.builder()
                    .username(baseMerchantUsername)
                    .password("123456")
                    .email("admin@" + createdBaseMerchant.getName().toLowerCase().replaceAll("\\s+", "") + ".com")
                    .fullName("Base Merchant Admin")
                    .role(UserRole.MERCHANT_ADMIN)
                    .merchantId(createdBaseMerchant.getId())
                    .enabled(true)
                    .build();
            try {
                createMerchantUserUseCase.create(baseMerchantUserDetails);
                System.out.println("***********************************************************");
                System.out.println("Base merchant user '" + baseMerchantUsername + "' created for merchant ID: " + createdBaseMerchant.getId());
                System.out.println("***********************************************************");
            } catch (Exception e) {
                System.err.println("***********************************************************");
                System.err.println("Error creating base merchant user: " + e.getMessage());
                System.err.println("***********************************************************");
            }
        } else if (createdBaseMerchant != null) {
            System.out.println("Base merchant user '" + baseMerchantUsername + "' already exists or base merchant was not found.");
        } else {
            System.out.println("Base merchant was not created or found, skipping base merchant user creation.");
        }
    }
}