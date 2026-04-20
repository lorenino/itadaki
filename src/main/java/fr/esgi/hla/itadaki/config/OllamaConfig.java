package fr.esgi.hla.itadaki.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

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
 *   <li>a 3-minute response timeout to tolerate Ollama cold starts (~50 s
 *       loading the model into VRAM the first time) plus the analysis latency
 *       (5-15 s warm)</li>
 * </ul>
 *
 * Consumed by {@code OllamaServiceImpl} to reach the Ollama {@code /api/chat}
 * endpoint.
 */
@Configuration
public class OllamaConfig {

    @Bean
    public RestClient ollamaRestClient(@Value("${spring.ai.ollama.base-url}") String baseUrl) {
        // 5 min pour absorber cold start (~50s) + latence ngrok + analyse image
        // complexe (jusqu'a 2-3 min sur qwen2.5vl:7b pour une photo dense).
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMinutes(5));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("ngrok-skip-browser-warning", "any")
                .requestFactory(new ReactorClientHttpRequestFactory(httpClient))
                .build();
    }
}
