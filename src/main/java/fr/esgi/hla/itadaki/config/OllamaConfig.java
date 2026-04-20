package fr.esgi.hla.itadaki.config;

import org.springframework.context.annotation.Configuration;

/**
 * TODO: Ollama AI integration configuration.
 *       - Read Ollama base URL from application.properties (spring.ai.ollama.base-url)
 *       - Read model name from application.properties (spring.ai.ollama.chat.model)
 *       - Optionally expose a configured OllamaChatClient or RestTemplate bean
 *         for use in OllamaServiceImpl.
 *
 *       Depends on: Spring AI Ollama starter or manual RestTemplate setup.
 */
@Configuration
public class OllamaConfig {

    // TODO: @Value("${spring.ai.ollama.base-url}") private String ollamaBaseUrl;
    // TODO: @Value("${spring.ai.ollama.chat.model}") private String modelName;

    // TODO: @Bean RestTemplate ollamaRestTemplate() — configured with base URL
    //       or expose an OllamaChatClient bean if using Spring AI starter
}
