# Itadaki — Model-IA

Package technique pour **Ahmed** (archi Java) et **Hugo** (data scientist).
Tout ce qu'il faut pour brancher Spring AI ↔ Ollama dans l'architecture Spring Boot du projet.

## Fichiers

| Fichier | Pour qui | Contenu |
|---|---|---|
| `HUGO-SETUP-OLLAMA.md` | **Hugo** | Setup complet step-by-step : install Ollama, pull modèles, tests texte+image, benchmark, expo LAN |
| `MODELE.md` | Hugo | Modèle Ollama choisi, commandes install, pre-pull |
| `SCHEMA.md` | **Ahmed** | JSON de sortie + records Java + entités JPA + DTOs |
| `PROMPTS.md` | Hugo | Prompt système v1 (analyse) et v2 (correction 2ᵉ passe) |
| `APPLICATION-PROPERTIES.md` | Ahmed | Clés de config Spring AI à ajouter à `application.properties` |
| `FLOW.md` | Ahmed | Séquences request/response (upload, analyse, correction, historique) |
| `exemples/` | Ahmed | Code Java prêt à copier : `AnalyseResult.java`, `AnalyseService.java`, sample JSON |
| `scripts/benchmark.ps1` | Hugo | Script PowerShell qui teste une batterie de photos sur les 2 modèles → CSV comparatif |

## Quickstart Ahmed

1. Lire `SCHEMA.md` → tu codes les records + entités JPA
2. Lire `FLOW.md` → tu comprends les 4 endpoints à créer
3. Copier `exemples/AnalyseResult.java` dans `src/main/java/fr/esgi/hla/itadaki/ai/`
4. Copier `exemples/AnalyseService.java` comme squelette
5. Ajouter les clés de `APPLICATION-PROPERTIES.md` à `src/main/resources/application.properties`

## Quickstart Hugo

1. Lire `MODELE.md` → tu pull les 2 modèles ce soir (`qwen2.5vl:7b` + `gemma3:4b`)
2. Lire `PROMPTS.md` → tu itères sur les prompts v1/v2 avec une mini-batterie de photos test
3. Benchmarker les 2 modèles sur la même batterie (latence + taux JSON valide + qualité qualitative)

## Décisions prises (cf. `../DECISIONS.md`)

- **Modèle principal** : `qwen2.5vl:7b` (benchmark avec `gemma3:4b` en fallback)
- **Stratégie calories** : LLM direct + fourchette `caloriesMin`/`caloriesMax` + disclaimer UX
- **Portion** : catégorie qualitative `petit`/`moyen`/`grand` demandée au LLM
- **Langue** : prompt en EN pour fiabilité, sortie FR imposée dans le schema
- **Correction (BF3)** : reconstruction stateless (toutes les passes conservées en DB, flag `active` sur la passe affichée)
- **Retry** : 1 retry avec prompt renforcé si JSON invalide, sinon 503 UX FR

## Avertissement Spring AI 2.0.0-M4

Le pom généré par Ahmed utilise `spring-ai-starter-model-ollama:2.0.0-M4` (milestone, pré-release). Les exemples de code de ce dossier sont **testés sur la série 1.x** qui a la doc la plus stable. En 2.0-M4, certains noms d'API peuvent avoir bougé (`UserMessage.builder().media()` vs `.images()`, noms de propriétés). Les fichiers flaguent les points à vérifier avec la mention `⚠ vérifier 2.0-M4`.

Si blocage 2.0-M4 : rollback vers Spring AI 1.1.4 (GA stable). Voir `APPLICATION-PROPERTIES.md` dernière section.
