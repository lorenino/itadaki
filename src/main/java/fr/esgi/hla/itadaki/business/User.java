package fr.esgi.hla.itadaki.business;

import fr.esgi.hla.itadaki.business.enums.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing an application user.
 *
 * TODO: Add remaining fields: username, email, passwordHash, createdAt, updatedAt
 * TODO: Add @OneToMany relation to Meal
 * TODO: Add @Column constraints (nullable, unique, length)
 * TODO: Implement UserDetails interface for Spring Security integration
 * TODO: Add Lombok @Builder, @NoArgsConstructor, @AllArgsConstructor as needed
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Add String username
    // TODO: Add String email (unique)
    // TODO: Add String passwordHash
    // TODO: Add LocalDateTime createdAt
    // TODO: Add LocalDateTime updatedAt

    @Enumerated(EnumType.STRING)
    private UserRole role;

    // TODO: Add List<Meal> meals relationship
}
