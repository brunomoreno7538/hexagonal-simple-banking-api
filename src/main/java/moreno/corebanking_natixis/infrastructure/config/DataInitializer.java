package moreno.corebanking_natixis.infrastructure.config;

import lombok.RequiredArgsConstructor;
import moreno.corebanking_natixis.application.port.in.CreateCoreUserUseCase;
import moreno.corebanking_natixis.application.port.out.CoreUserRepository;
import moreno.corebanking_natixis.domain.model.CoreUser;
import moreno.corebanking_natixis.domain.model.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CreateCoreUserUseCase createCoreUserUseCase;
    private final CoreUserRepository coreUserRepository;

    @Override
    public void run(String... args) throws Exception {
        if (!coreUserRepository.findByUsernameAndActiveTrue("initialadmin").isPresent()) {
            CoreUser adminUser = CoreUser.builder()
                    .username("initialadmin")
                    .password("initialpassword")
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
    }
}