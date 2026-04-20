package fr.esgi.hla.itadaki.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.esgi.hla.itadaki.annotation.CurrentUser;
import fr.esgi.hla.itadaki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for custom argument resolvers.
 * Enables the @CurrentUser annotation to inject the authenticated user ID into controller methods.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserRepository userRepository;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver(userRepository));
    }

    /**
     * Resolves @CurrentUser annotated parameters to the ID of the currently authenticated user.
     */
    static class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

        private final UserRepository userRepository;

        CurrentUserArgumentResolver(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            // Extract user email from the principal name (in our case, email is the username)
            String userEmail = authentication.getName();

            // Look up the user ID from the database
            return userRepository.findByEmail(userEmail)
                    .map(user -> user.getId())
                    .orElse(null);
        }
    }
}


