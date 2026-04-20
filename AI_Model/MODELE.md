# Modèles Ollama

## Primary : `qwen2.5vl:7b`

- Taille disque : **~6 Go**
- VRAM conseillée : 12 Go (ou 16 Go RAM en CPU-only)
- Latence : ~25-40 tok/s sur GPU 8-12 Go, 4-8 tok/s en CPU
- **Atouts** :
  - JSON structuré natif (critique pour notre parsing)
  - OCR fort (utile si texte visible sur l'assiette)
  - Français correct en sortie si imposé dans le prompt
  - Reconnaissance plats équilibrée (occidental + asiatique)
- **Commande** :
  ```bash
  ollama pull qwen2.5vl:7b
  ollama run qwen2.5vl:7b "Hello"   # warm-up (charge en RAM)
  ```

## Fallback : `gemma3:4b`

- **~3.3 Go**, 6 Go VRAM
- Multilingue natif (140 langues, FR impeccable)
- Vision correcte pour plats courants
- JSON `format:` supporté
- ```bash
  ollama pull gemma3:4b
  ```

## Secours CPU-only : `moondream`

- 1.7 Go, 15-25 tok/s même en CPU
- Qualité descriptive limitée mais suffisante pour prouver le pipeline
- ```bash
  ollama pull moondream
  ```

## Pre-pull ce soir (Hugo)

```bash
ollama pull qwen2.5vl:7b
ollama pull gemma3:4b
ollama pull moondream   # optionnel
```

Puis warm-up :
```bash
ollama run qwen2.5vl:7b "ok"
```

(Le premier appel charge le modèle en RAM/VRAM, ~15-30 s. Après, les appels suivants sont chauds pendant `keep_alive`.)

## Serveur Ollama en démo

Par défaut Ollama écoute sur `http://localhost:11434`.

**En démo, Ollama tourne sur la machine Hugo** (RTX 5060, 32 Go RAM). Les autres machines s'y connectent via variable d'env :

```bash
# Linux/Mac
OLLAMA_HOST=http://<ip-hugo>:11434

# Windows (PowerShell)
$env:OLLAMA_HOST="http://<ip-hugo>:11434"
```

Côté Spring Boot, c'est piloté par `spring.ai.ollama.base-url` dans `application.properties` (voir `APPLICATION-PROPERTIES.md`).

**Faire écouter Ollama sur toutes les interfaces** (pas juste localhost) côté Hugo :

```bash
# Linux/Mac
OLLAMA_HOST=0.0.0.0 ollama serve

# Windows
$env:OLLAMA_HOST="0.0.0.0"; ollama serve
```

## Tester le modèle sans Java

```bash
# Test texte
ollama run qwen2.5vl:7b "Décris une pomme en français en 2 lignes"

# Test API REST texte
curl http://localhost:11434/api/chat -d '{
  "model": "qwen2.5vl:7b",
  "messages": [{"role":"user","content":"Hello"}],
  "stream": false
}'

# Test avec image (base64)
IMG_B64=$(base64 -w 0 assiette.jpg)
curl http://localhost:11434/api/chat -d "{
  \"model\": \"qwen2.5vl:7b\",
  \"messages\": [{
    \"role\":\"user\",
    \"content\":\"Identifie ce plat en français, réponds en JSON\",
    \"images\": [\"$IMG_B64\"]
  }],
  \"stream\": false,
  \"format\": \"json\"
}"
```

Si cette commande renvoie du JSON cohérent, la suite côté Spring AI marchera.
