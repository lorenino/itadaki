# Validation Ollama — 2026-04-20 12:30

Document de référence de l'équipe pour la partie IA. **Tout ce qui est ci-dessous est validé et actif.**

## Décisions actées

### Modèle retenu

- **Principal** : `qwen2.5vl:7b`
- **Fallback** : `gemma3:4b` (pullé, non benchmarké — on s'en servira si qwen cale)
- **Justification** : JSON strict parfait du 1er coup, français natif, ingrédients pertinents (6 détectés sur la photo test), schéma respecté intégralement (enums + `caloriesMin < caloriesMax`)

### Machine Ollama

- **Host** : PC de Hugo (RTX 5060, 32 Go RAM)
- **IP LAN** : `10.213.203.128`
- **Port** : `11434` (défaut)
- **URL complète** : `http://10.213.203.128:11434`
- **Exposition** : Ollama ouvert sur `0.0.0.0:11434`, firewall Windows ouvert

### Latence constatée (non-bloquante POC)

~64s sur le 1er appel image (probable cold start). Non ré-testée — on s'en fout pour le POC, la démo tournera sur plan B si trop lent (vidéo pré-enregistrée, cf. K7).

## Prompt système validé

```text
You are a nutritionist analyzing a photo of a plate/dish.

Return ONLY valid JSON matching this exact schema (no prose, no markdown, no code fences):
{
  "nomPlat": string,
  "ingredients": string[],
  "portion": string,
  "caloriesMin": integer,
  "caloriesMax": integer,
  "confiance": string
}

Rules:
- JSON only. No explanation.
- nomPlat and ingredients MUST be in French.
- portion ∈ {"petit","moyen","grand"}.
- confiance ∈ {"haute","moyenne","basse"}.
- caloriesMin < caloriesMax, positive integers.
- If no dish: {"nomPlat":"inconnu","ingredients":[],"portion":"moyen","caloriesMin":0,"caloriesMax":0,"confiance":"basse"}.
```

**User prompt** (v1, analyse initiale) :

```text
Identifie ce plat, liste les ingrédients visibles et estime les calories.
```

## Options Ollama validées

```json
{
  "stream": false,
  "format": "json",
  "options": {
    "temperature": 0.2,
    "num_ctx": 4096
  }
}
```

## Exemple de réponse réelle — photo `itadaki-test/assiette1.jpg`

**Input** : photo d'une assiette (poulet + riz + légumes variés).

**Output brut Ollama** (parsing JSON strict OK, pas de prose, pas de markdown) :

```json
{
  "nomPlat": "Assiette de légumes et de poulet",
  "ingredients": ["poulet", "riz", "asperges", "tomates cerises", "carottes", "haricots verts"],
  "portion": "moyen",
  "caloriesMin": 350,
  "caloriesMax": 450,
  "confiance": "haute"
}
```

## Configuration Spring Boot à poser (pour Ahmed)

Dans `src/main/resources/application.properties` :

```properties
# Spring AI Ollama — validé 2026-04-20
spring.ai.ollama.base-url=http://10.213.203.128:11434
spring.ai.ollama.chat.options.model=qwen2.5vl:7b
spring.ai.ollama.chat.options.temperature=0.2
spring.ai.ollama.chat.options.num-ctx=4096
# ⚠ vérifier les noms exacts des clés en Spring AI 2.0.0-M4 (cf. AI_Model/APPLICATION-PROPERTIES.md)
```

## Dans le code Java

Le record `AnalyseResult` de `AI_Model/exemples/AnalyseResult.java` est **aligné** sur le JSON validé (mêmes champs, mêmes types). Il peut être copié tel quel dans `src/main/java/fr/esgi/hla/itadaki/...` (ou renommé pour matcher la nomenclature Ahmed, ex. `OllamaAnalysisResult` dans `service/impl/`).

Pour le parsing, 2 options :

1. **Spring AI `.entity(Class)`** (recommandé) — `BeanOutputConverter` injecte le schema dans le prompt et parse tout seul.
2. **Jackson manuel** — `objectMapper.readValue(rawJson, AnalyseResult.class)` si Spring AI 2.0-M4 coince sur `.entity()`.

## État de l'infra IA

| Item | État |
|---|---|
| Ollama installé sur PC Hugo | ✅ |
| `qwen2.5vl:7b` pullé (6 GB) | ✅ |
| `gemma3:4b` pullé (3.3 GB) | ✅ |
| Test FR texte | ✅ |
| Test multimodal (image → JSON) | ✅ |
| Ollama exposé LAN `0.0.0.0:11434` | ✅ |
| Firewall Windows ouvert port 11434 | ✅ |
| IP LAN communiquée | ✅ `10.213.203.128` |

**On ne touche plus à l'infra IA.** Équipe IA (Lorenzo + Hugo) en standby : on attend la partie Java d'Ahmed pour brancher.
