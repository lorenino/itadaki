package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements OllamaService using Spring AI or a raw HTTP client.
 *       - analyzeImage: encode image to base64, build prompt, call Ollama REST API,
 *                        return raw JSON response string
 *       - buildMealAnalysisPrompt: build the analysis prompt string with optional hint
 *
 *       Ollama base URL and model name should come from OllamaConfig / application.properties.
 *       Inject: OllamaConfig (or @Value for base URL / model name)
 */
@Service
@RequiredArgsConstructor
public class OllamaServiceImpl implements OllamaService {

    // TODO: Inject OllamaConfig or use @Value for base URL and model
    // TODO: Inject RestTemplate or WebClient for HTTP calls to Ollama

    // TODO: Override analyzeImage(String imagePath, String prompt) → String
    // TODO: Override buildMealAnalysisPrompt(String hint) → String
}
