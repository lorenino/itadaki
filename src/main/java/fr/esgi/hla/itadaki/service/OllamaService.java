package fr.esgi.hla.itadaki.service;

/**
 * Defines Ollama AI integration operations.
 * Communicates with the local Ollama endpoint configured in OllamaConfig
 * (base URL injected from {@code spring.ai.ollama.base-url},
 * model name from {@code spring.ai.ollama.chat.model}).
 */
public interface OllamaService {

    /**
     * Sends a meal image to the Ollama multimodal model for analysis.
     *
     * @param imagePath absolute or relative filesystem path to the meal image (JPEG/PNG)
     * @param prompt    user-level prompt to send (typically built via {@link #buildMealAnalysisPrompt(String)})
     * @return raw JSON string returned by the AI model (the {@code message.content} field
     *         of the Ollama {@code /api/chat} response). Callers are responsible for parsing
     *         this into a domain object (e.g. in {@code AnalysisServiceImpl}).
     */
    String analyzeImage(String imagePath, String prompt);

    /**
     * Builds the user-facing prompt to send to the model.
     *
     * @param hint optional free-text user correction; when null or blank, returns the
     *             default initial analysis prompt; otherwise embeds the hint so the model
     *             re-analyzes the image taking the correction into account.
     * @return the user prompt string (system prompt is handled internally by the service)
     */
    String buildMealAnalysisPrompt(String hint);
}
