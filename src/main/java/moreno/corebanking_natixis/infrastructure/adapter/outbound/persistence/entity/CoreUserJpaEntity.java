package moreno.corebanking_natixis.infrastructure.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moreno.corebanking_natixis.domain.model.UserRole;


import java.util.UUID;

@Entity
@Table(name = "core_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoreUserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean active = true;
}