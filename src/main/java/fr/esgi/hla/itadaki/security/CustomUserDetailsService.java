package fr.esgi.hla.itadaki.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * TODO: Spring Security UserDetailsService implementation.
 *       - loadUserByUsername: load User entity from UserRepository by email (or username),
 *                             map to Spring Security UserDetails with roles and password
 *
 *       Inject: UserRepository
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // TODO: Inject UserRepository

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: Fetch user from UserRepository.findByEmail(username)
        // TODO: Map User entity to org.springframework.security.core.userdetails.User
        throw new UsernameNotFoundException("Not yet implemented — TODO");
    }
}
