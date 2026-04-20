# Prompts Spring AI

**Lecture Hugo en priorité.** Tu itères sur ces prompts avec la mini-batterie de photos test.

## Prompt système (partagé v1 + v2)

```text
You are a nutritionist analyzing a photo of a plate/dish.

Return ONLY valid JSON matching this exact schema (no prose, no markdown, no code fences):
{
  "nomPlat": string,          // dish name, IN FRENCH
  "ingredients": string[],    // detected ingredients, IN FRENCH
  "portion": string,          // "petit" | "moyen" | "grand"
  "caloriesMin": integer,     // lower bound, positive
  "caloriesMax": integer,     // upper bound, > caloriesMin
  "confiance": string         // "haute" | "moyenne" | "basse"
}

Rules:
- Respond with JSON only. No explanation, no comments, no markdown fences.
- nomPlat and ingredients MUST be in French (use French cuisine terms when applicable).
- portion values MUST be lowercase French: "petit", "moyen", "grand".
- caloriesMin must be strictly less than caloriesMax, both positive integers.
- Default "confiance" to "moyenne" when unsure.
- If no dish is visible, return:
  {"nomPlat":"inconnu","ingredients":[],"portion":"moyen","caloriesMin":0,"caloriesMax":0,"confiance":"basse"}
```

## Prompt user v1 — analyse initiale

```text
Identifie ce plat, liste les ingrédients visibles et estime les calories.
```

(Court volontairement : le prompt système fait déjà tout le travail de cadrage.)

## Prompt user v2 — correction 2ᵉ passe

Construction côté Java par concaténation. Template (Java text block, {0} et {1} remplacés par `String.format` ou `formatted`) :

```text
You previously produced this analysis of the same photo:
<previous_analysis>
{previousAnalyseJson}
</previous_analysis>

The user corrects your analysis with this indication:
<user_correction>
{userCorrectionText}
</user_correction>

Re-analyze the photo taking the correction into account. Return ONLY valid JSON with the exact same schema (nomPlat, ingredients, portion, caloriesMin, caloriesMax, confiance). No prose.
```

## Prompt de retry (si JSON invalide)

Si `BeanOutputConverter` plante à la première passe, retry avec :

```text
STRICT MODE: respond with ONLY valid JSON matching the schema. No markdown, no code fences, no prose. Your previous reply was invalid.

Identifie ce plat, liste les ingrédients visibles et estime les calories.
```

Si la 2ᵉ tentative plante aussi → fallback `AnalyseResult.inconnu()` + log warning + HTTP 503 côté client avec message FR « analyse impossible, réessayez ».

## Paramètres Spring AI recommandés

- `temperature: 0.2` (bas, pour JSON stable et répétable)
- `num-ctx: 4096` (suffisant pour une photo + prompt)
- `keep-alive: 10m` (garde le modèle en RAM entre 2 requêtes)
- Timeout HTTP read : `180s` (CPU-only peut prendre ~90 s)

Ces paramètres sont dans `APPLICATION-PROPERTIES.md`.

## Astuces

1. **Format JSON natif Ollama** : Ollama supporte `format: json` (ou un schéma JSON) en paramètre API. Spring AI peut injecter ça via `OllamaOptions.format(...)`. Le `BeanOutputConverter` le fait automatiquement avec `.entity(Class)`.

2. **OCR** : `qwen2.5vl` lit le texte visible (emballages, menus). Si besoin, ajouter au prompt : `"If text is visible on packaging or menu, include it in the analysis."`

3. **Few-shot** (si qualité insuffisante) : ajouter 1-2 exemples d'entrée/sortie dans le prompt système.

4. **Portion fine** : si `"petit/moyen/grand"` est trop grossier, on peut demander en plus `"grammesApprox": integer` mais le LLM hallucine souvent — à mesurer.

## Batterie de test (à constituer par Hugo)

Construire ~15 photos variées (photos persos + Google Images) couvrant :

- Plats FR simples (baguette, steak-frites, salade verte)
- Plats composés (lasagnes, couscous, sushi)
- Portions (entrée vs plat principal)
- Cas limites (assiette vide, photo floue, plat non identifiable)

Pour chaque : noter latence (ms), JSON valide (oui/non), nomPlat plausible (oui/non), calories plausibles (±30%). Tableau CSV dans `../test-samples/results.csv` (optionnel).
