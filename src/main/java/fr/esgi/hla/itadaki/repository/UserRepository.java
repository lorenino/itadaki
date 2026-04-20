package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO: Repository for User entity.
 *       Custom queries to add:
 *       - findByEmail(String email) → Optional<User>
 *       - findByUsername(String username) → Optional<User>
 *       - existsByEmail(String email) → boolean
 *       - existsByUsername(String username) → boolean
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // TODO: Add custom query methods listed above
}
