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

    /**
     * Appel texte pur (pas d'image) au modele Ollama. Utilise pour le bilan hebdo
     * et les suggestions de plats. Garde stream=false pour une reponse atomique.
     *
     * @param systemPrompt instruction systeme (ex. persona coach, format attendu)
     * @param userPrompt   message utilisateur (ex. stats a resumer)
     * @param jsonMode     true pour forcer format=json (reponses structurees),
     *                     false pour prose libre
     * @return contenu textuel brut retourne par le modele
     */
    String chatText(String systemPrompt, String userPrompt, boolean jsonMode);

    /**
     * Variante streaming de {@link #analyzeImage(String, String)}.
     * Lit les chunks NDJSON d'Ollama au fur et a mesure et appelle onToken
     * pour chaque fragment de content. Retourne le contenu concatene complet
     * a la fin pour parsing/persistence.
     *
     * @param imagePath chemin local de l'image
     * @param prompt    prompt user
     * @param onToken   callback invoque pour chaque fragment recu (peut etre
     *                  appele un grand nombre de fois, ~1 par token)
     * @return contenu complet (concatenation de tous les tokens)
     */
    String streamAnalyzeImage(String imagePath, String prompt, java.util.function.Consumer<String> onToken);
}
