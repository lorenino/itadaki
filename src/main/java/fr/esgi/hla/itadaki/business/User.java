package fr.esgi.hla.itadaki.business;

import fr.esgi.hla.itadaki.business.enums.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * JPA entity representing an application user.
 *
 * TODO: Add fields: username, email, passwordHash
 * TODO: Add @CreationTimestamp LocalDateTime createdAt
 * TODO: Add @UpdateTimestamp LocalDateTime updatedAt
 * TODO: Add @Column(unique = true) on email and username
 * TODO: Add @Column(nullable = false) on required fields
 * TODO: Add @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE) List<Meal> meals
 * TODO: Add @NotBlank / @Email / @Size validation annotations on fields
 * TODO: Implement UserDetails for Spring Security integration
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: @Column(nullable = false, unique = true) String username
    // TODO: @Column(nullable = false, unique = true) @Email String email
    // TODO: @Column(nullable = false) String passwordHash

    @Enumerated(EnumType.STRING)
    private UserRole role;

    // TODO: @CreationTimestamp LocalDateTime createdAt
    // TODO: @UpdateTimestamp LocalDateTime updatedAt
    // TODO: @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE) @ToString.Exclude List<Meal> meals
}
