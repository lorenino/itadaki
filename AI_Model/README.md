# Itadaki — AI_Model

Package technique pour **Ahmed** (archi Java) : tout ce qu'il faut pour brancher Spring AI ↔ Ollama dans l'architecture Spring Boot du projet.

L'infra Ollama est validée côté Hugo (voir `VALIDATION-OLLAMA.md`). Ahmed n'a plus qu'à configurer et appeler.

## Fichiers

| Fichier | Contenu |
|---|---|
| `VALIDATION-OLLAMA.md` | **À lire en premier.** Modèle retenu, IP LAN Ollama, prompt système validé, exemple JSON réel, config Spring AI à poser |
| `SCHEMA.md` | JSON de sortie + records Java + entités JPA (`Meal`/`MealAnalysis`) + DTOs + repos |
| `APPLICATION-PROPERTIES.md` | Clés Spring AI / multipart / Swagger à ajouter dans `application.properties` |
| `FLOW.md` | Séquences request/response des 8 endpoints (upload, analyse, correction 2ᵉ passe, historique, image stream) |
| `exemples/AnalyseResult.java` | Record de sortie du LLM, prêt à copier |
| `exemples/AnalyseService.java` | Service Spring AI complet (analyse initiale + 2ᵉ passe + retry) |
| `exemples/response-sample.json` | 5 exemples de réponses JSON valides pour tests |

## Quickstart Ahmed

1. Lire `VALIDATION-OLLAMA.md` → tu as tout : IP, modèle, prompt, config properties.
2. Lire `SCHEMA.md` → nomenclature et types.
3. Copier/adapter `exemples/AnalyseResult.java` dans `src/main/java/fr/esgi/hla/itadaki/...`
4. Copier/adapter `exemples/AnalyseService.java` comme squelette du service Ollama.
5. Ajouter les clés de `APPLICATION-PROPERTIES.md` à `src/main/resources/application.properties` (surtout `spring.ai.ollama.base-url=http://10.213.203.128:11434`).

## Décisions actées (cf. `../DECISIONS.md`)

- **Modèle** : `qwen2.5vl:7b` (test image validé → JSON strict, FR, 6 ingrédients, toutes règles schéma respectées)
- **Stratégie calories** : LLM direct + fourchette `caloriesMin`/`caloriesMax` + disclaimer UX
- **Portion** : catégorie qualitative `petit`/`moyen`/`grand`
- **Langue** : prompt EN + instruction "nomPlat et ingredients en français"
- **Correction 2ᵉ passe (BF3)** : reconstruction stateless — on renvoie au LLM `analyseV1_json + user_correction`, toutes les passes conservées en DB
- **Retry** : 1 retry prompt renforcé si JSON invalide, sinon fallback UX 503

## Avertissement Spring AI 2.0.0-M4

Le pom utilise `spring-ai-starter-model-ollama:2.0.0-M4` (milestone). Les exemples de code sont basés sur la doc 1.x. Vérifier au premier `mvn compile` les signatures `UserMessage.builder().media()` et `Media`. Si ça casse, voir `APPLICATION-PROPERTIES.md` → section rollback.
