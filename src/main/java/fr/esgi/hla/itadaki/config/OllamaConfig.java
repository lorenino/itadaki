package fr.esgi.hla.itadaki.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Ollama AI integration configuration.
 *
 * Exposes a {@link RestClient} bean pre-configured with:
 * <ul>
 *   <li>the Ollama base URL (from {@code spring.ai.ollama.base-url}, overridable
 *       at runtime via the {@code OLLAMA_URL} environment variable — used to
 *       point the app at a ngrok tunnel for the demo)</li>
 *   <li>the {@code ngrok-skip-browser-warning} header injected on every
 *       outbound request, so the free-tier ngrok warning HTML page never
 *       reaches our JSON parser</li>
 * </ul>
 *
 * Consumed by {@code OllamaServiceImpl} to reach the Ollama {@code /api/chat}
 * endpoint.
 */
@Configuration
public class OllamaConfig {

    @Bean
    public RestClient ollamaRestClient(@Value("${spring.ai.ollama.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("ngrok-skip-browser-warning", "any")
                .build();
    }
}
