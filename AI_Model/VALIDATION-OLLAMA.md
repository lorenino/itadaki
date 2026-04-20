# Validation Ollama — 2026-04-20 12:30

Document de référence de l'équipe pour la partie IA. **Tout ce qui est ci-dessous est validé et actif.**

## Décisions actées

### Modèle retenu

- **Principal** : `qwen2.5vl:7b`
- **Fallback** : `gemma3:4b` (pullé, non benchmarké — on s'en servira si qwen cale)
- **Justification** : JSON strict parfait du 1er coup, français natif, ingrédients pertinents (6 détectés sur la photo test), schéma respecté intégralement (enums + `caloriesMin < caloriesMax`)

### Machine Ollama

- **Host** : PC de Hugo (RTX 5060, 32 Go RAM)
- **Port** : `11434` (défaut)
- **IP LAN initiale (WiFi ESGI)** : `10.213.203.128` — ⚠ **WiFi Campus-Sciences-U fait de l'AP isolation**, trafic inter-clients bloqué par le routeur. Inutilisable en l'état.
- **IP LAN hotspot (test validé)** : `172.20.10.3` — partage de co téléphone Lorenzo, tous sur le même subnet `172.20.10.0/28`. Pipeline validé bout en bout.
- **Règle firewall Hugo** : `New-NetFirewallRule -DisplayName "Ollama LAN" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 11434 -Profile Any -RemoteAddress Any -Enabled True` (la règle "Ollama" initiale `Profile: Any` ne matchait pas en profil Public, il a fallu la recréer)
- **Exposition Ollama** : `OLLAMA_HOST=0.0.0.0:11434` (machine-level), vérifié via `netstat -an | findstr :11434` → `0.0.0.0:11434 LISTENING`
- **Pour la démo** : prévoir hotspot téléphone de quelqu'un de l'équipe (WiFi école ne marchera pas). L'IP va changer à chaque session hotspot → à mettre à jour dans `spring.ai.ollama.base-url` avant de lancer.

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

## Tests distants à chaud (2026-04-20 ~14h via hotspot)

Depuis machine Lorenzo (`172.20.10.2`) vers Ollama Hugo (`172.20.10.3:11434`), modèle `qwen2.5vl:7b` chaud en RAM.

### Cold start vs chaud

- **Chargement initial modèle** : ~50 s (une fois par session, après 5 min d'inactivité `keep_alive` expire)
- **Appels chaud** : 1.5 – 10 s selon la complexité du prompt

### Test 1 — Appel JSON simple (validation connectivité + format)

- Prompt texte trivial « Retourne {"status":"ok","test":"chaud"} »
- **Latence : 1.65 s** (load 77 ms + prompt_eval 378 ms + eval 912 ms pour 11 tokens)
- Retour : `{"status":"ok","test":"chaud"}` — JSON strict respecté ✅

### Test 2 — 2ᵉ passe stateless (BF3 validation pattern E15)

Prompt user construit comme prévu dans `FLOW.md` :
- **Input** : analyse V1 JSON + texte correction utilisateur
- **V1** : `{"nomPlat":"Poulet au curry avec riz","ingredients":["poulet","riz basmati","sauce curry","oignons"],"portion":"moyen","caloriesMin":480,"caloriesMax":620,"confiance":"moyenne"}`
- **Correction user** : « Ce n'est pas du poulet mais du tofu. Il n'y a pas d'oignons dans le plat. »
- **Latence : 9.7 s** (prompt 247 tokens, réponse 58 tokens, ~11 tok/s)
- **V2 obtenue** : `{"nomPlat":"Tofu au curry avec riz","ingredients":["tofu","riz basmati","sauce curry"],"portion":"moyen","caloriesMin":450,"caloriesMax":580,"confiance":"moyenne"}`

**Ce qui est validé** : le modèle comprend sémantiquement la correction (remplace poulet→tofu partout, retire "oignons", recalcule les calories à la baisse puisque le tofu est moins calorique). Pattern reconstruction stateless **fonctionne au 1er jet** sans tuning.

### Test 3 — Fallback « inconnu » (robustesse prompt)

- **Input** : « Cette image montre juste un mur blanc vide, aucune nourriture visible. »
- **Latence : 6.1 s**
- **Retour** : `{"nomPlat":"inconnu","ingredients":[],"portion":"moyen","caloriesMin":0,"caloriesMax":0,"confiance":"basse"}`
- Respecte exactement la règle du prompt système pour le cas « no dish visible » ✅

### Bilan performance

| Métrique | Valeur |
|---|---|
| Cold start modèle | ~50 s (once per session) |
| Appel texte chaud simple | 1.5 – 2 s |
| Appel texte chaud complexe (2ᵉ passe) | 8 – 10 s |
| Appel multimodal chaud (mesuré le matin par Hugo) | ~64 s (première image, cold start compris) |
| Prompt strict + `format: json` + `temperature: 0.2` | **100% JSON valide** sur les 4 tests (ce matin + 3 cet AM) |

Pour la démo : **warm-up obligatoire au démarrage de l'app** (1 appel texte bidon au `@PostConstruct` pour charger le modèle en RAM). Ensuite les appels user seront rapides.

---

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
# IMPORTANT : l'IP ci-dessous doit être mise à jour avant chaque démo
# (réseau école fait de l'AP isolation, il faut passer par un hotspot téléphone).
# L'IP change à chaque session hotspot — demander à Hugo avant de lancer.
spring.ai.ollama.base-url=${OLLAMA_HOST:http://172.20.10.3:11434}
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
| IP LAN initiale ESGI (échec AP isolation) | ❌ `10.213.203.128` |
| IP LAN hotspot validée | ✅ `172.20.10.3` |
| Règle firewall Hugo recréée ("Ollama LAN" explicite) | ✅ |
| Test connectivité distante (curl) | ✅ |
| Test chat chaud simple (1.65 s) | ✅ |
| Test 2ᵉ passe stateless (9.7 s) | ✅ |
| Test fallback inconnu (6.1 s) | ✅ |
| Test multimodal (matin, 64 s cold) | ✅ |

**On ne touche plus à l'infra IA.** Équipe IA (Lorenzo + Hugo) en standby : on attend la partie Java d'Ahmed pour brancher.

## Solution réseau démo : ngrok tunnel HTTPS

Le WiFi ESGI (`Campus-Sciences-U`) fait de l'**AP isolation** (bloque le trafic entre clients d'un même AP) — inutilisable pour parler à l'Ollama de Hugo en direct. **Solution retenue : ngrok** qui contourne le problème en sortant en HTTPS vers l'extérieur puis revient vers le PC Hugo.

### Setup une fois (Hugo, ~10 min)

```powershell
# 1. Install
winget install ngrok.ngrok

# 2. Compte gratuit
#    https://dashboard.ngrok.com/signup (Google OAuth OK)

# 3. Authtoken (une seule fois, persiste)
#    https://dashboard.ngrok.com/get-started/your-authtoken
ngrok config add-authtoken <TOKEN>
```

### Avant chaque démo (Hugo, 2 min)

```powershell
# 1. Warm-up Ollama (charge le modèle en VRAM, ~30 s)
ollama run qwen2.5vl:7b "ok"
# → tape /bye après réponse

# 2. Lancer le tunnel dans un terminal DÉDIÉ (NE PAS FERMER)
ngrok http 11434
# → copie l'URL « Forwarding https://abcd-1234.ngrok-free.app -> http://localhost:11434 »
# → partage l'URL sur le channel équipe
```

### Test de validation (Lorenzo ou Ahmed avant la démo)

```bash
# Connectivité
curl -H "ngrok-skip-browser-warning: any" https://abcd-1234.ngrok-free.app
# → "Ollama is running"

# Liste modèles
curl -H "ngrok-skip-browser-warning: any" https://abcd-1234.ngrok-free.app/api/tags
# → {"models":[{"name":"qwen2.5vl:7b",…},{"name":"gemma3:4b",…}]}

# Appel chat complet
curl -H "ngrok-skip-browser-warning: any" -H "Content-Type: application/json" \
  https://abcd-1234.ngrok-free.app/api/chat \
  -d '{"model":"qwen2.5vl:7b","messages":[{"role":"user","content":"Retourne {\"ok\":true}"}],"stream":false,"format":"json"}'
```

### Lancement app Spring Boot (démonstrateur)

```powershell
$env:OLLAMA_URL = "https://abcd-1234.ngrok-free.app"
mvn spring-boot:run
```

L'`application.properties` doit contenir :
```properties
spring.ai.ollama.base-url=${OLLAMA_URL:http://localhost:11434}
```

### ⚠ Gotchas ngrok à connaître

- **Header obligatoire** : `ngrok-skip-browser-warning: any` sur toutes les requêtes, sinon ngrok renvoie une page HTML d'avertissement que Spring AI ne peut pas parser. Ahmed doit injecter ce header dans le `RestClient` / `WebClient` utilisé par `OllamaServiceImpl`.
- **URL change à chaque `ngrok http`** sur le plan gratuit. Ne jamais hardcoder l'URL, toujours via env var `OLLAMA_URL`.
- **Session doit rester ouverte** : fermer le terminal ngrok coupe le tunnel → démo morte.
- **Rate limit gratuit** : 40 req/min (largement OK pour démo).
- **Latence tunnel** : ~200 ms additionnels par appel (acceptable, on reste sur 2-10 s au total).
- **Cold start Ollama** : au-delà de 5 min d'inactivité, le modèle se décharge. Refaire warm-up avant la démo si on attend plus de 5 min entre la prépa et le pitch.
