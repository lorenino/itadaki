package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for User entity. Spring Data detects this automatically — no @Repository needed.
 * TODO: boolean existsByEmail(String email);
 * TODO: boolean existsByUsername(String username);
 * TODO: Optional<User> findByEmail(String email);
 * TODO: Optional<User> findByUsername(String username);
 */
public interface UserRepository extends JpaRepository<User, Long> {
    // TODO: Add query methods listed above
}
