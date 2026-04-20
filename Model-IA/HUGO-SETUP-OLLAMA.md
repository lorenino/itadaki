# Hugo — Setup Ollama + tests + benchmark

**Machine cible** : PC Hugo (RTX 5060, 32 Go RAM, Windows).
**Objectif** : avoir Ollama installé, 2 modèles vision pullés, testés OK, accessibles en LAN pour Lorenzo et Ahmed.
**Durée estimée** : 45-90 min (dont ~20 min de download).

Coche chaque `[ ]` au fur et à mesure.

---

## Phase 1 — Install Ollama (10 min)

- [ ] 1.1 Télécharger Ollama Windows : https://ollama.com/download/windows → lance l'installeur `.exe`
- [ ] 1.2 Vérifier que le service tourne : ouvrir un **PowerShell**, taper
      ```powershell
      ollama --version
      ```
      → doit afficher une version (ex. `ollama version is 0.12.x`)
- [ ] 1.3 Vérifier que le serveur écoute :
      ```powershell
      curl http://localhost:11434
      ```
      → doit afficher `Ollama is running`
- [ ] 1.4 Vérifier que le GPU est utilisé (critique pour la latence) :
      ```powershell
      ollama ps
      ```
      (après le warm-up à la phase 3, cette commande doit montrer la VRAM allouée)

**Si bloqué** : redémarrer la machine après install, vérifier que le process `ollama.exe` tourne dans le Task Manager.

---

## Phase 2 — Pull des modèles (15-30 min selon connexion)

- [ ] 2.1 Pull `qwen2.5vl:7b` (~6 Go — **principal**)
      ```powershell
      ollama pull qwen2.5vl:7b
      ```
- [ ] 2.2 Pull `gemma3:4b` (~3.3 Go — **fallback**)
      ```powershell
      ollama pull gemma3:4b
      ```
- [ ] 2.3 (Optionnel) Pull `moondream` (~1.7 Go — secours CPU-only)
      ```powershell
      ollama pull moondream
      ```
- [ ] 2.4 Lister les modèles installés :
      ```powershell
      ollama list
      ```
      → doit afficher les 2 (ou 3) modèles avec leur taille.

**Tips** :
- Si le pull plante au milieu, relancer la commande : Ollama reprend où il en est.
- Les modèles sont stockés dans `C:\Users\<toi>\.ollama\models\`.

---

## Phase 3 — Warm-up + test texte (5 min)

Le premier appel charge le modèle en VRAM (~15-30 s). Les appels suivants sont "chauds" pendant 10 min (`keep_alive`).

- [ ] 3.1 Warm-up `qwen2.5vl:7b` :
      ```powershell
      ollama run qwen2.5vl:7b "Hello"
      ```
      → doit répondre "Hello! How can I help you today?" ou équivalent.
      Tape `/bye` pour sortir.

- [ ] 3.2 Test FR :
      ```powershell
      ollama run qwen2.5vl:7b "Décris une pomme en français, en 2 lignes."
      ```
      → doit répondre en **français**.

- [ ] 3.3 Mesurer la latence sur un prompt simple :
      ```powershell
      Measure-Command { ollama run qwen2.5vl:7b --format json "Dis 'ok' en JSON : {\"reponse\":\"ok\"}" }
      ```
      → `TotalSeconds` doit être < 10 s sur RTX 5060.

- [ ] 3.4 Vérifier l'utilisation GPU pendant l'appel :
      dans un autre PowerShell : `nvidia-smi` pendant que qwen2.5vl répond. La VRAM allouée doit être autour de 6-7 Go.

**Si lent (> 30 s sur un prompt texte simple)** : le modèle tourne peut-être en CPU. Vérifier les drivers NVIDIA, tester aussi `gemma3:4b` qui est plus léger.

---

## Phase 4 — Test avec image (10 min)

C'est LE test qui valide toute la chaîne pour Itadaki.

### Préparer 2-3 photos d'assiette

- [ ] 4.1 Prendre 2-3 photos avec ton smartphone (plats variés : plat principal, salade, dessert).
- [ ] 4.2 Les transférer dans un dossier, ex. `C:\itadaki-test\`.
- [ ] 4.3 Redimensionner éventuellement à ~1024 px max (tail courte) pour accélérer : plus c'est petit, plus c'est rapide, sans perte notable jusqu'à 672 px.

### Test via API REST (recommandé, reproductible)

Copier-coller dans un PowerShell, remplacer le chemin :

```powershell
$imgPath = "C:\itadaki-test\assiette1.jpg"

$imgBytes = [System.IO.File]::ReadAllBytes($imgPath)
$imgB64 = [Convert]::ToBase64String($imgBytes)

$body = @{
  model = "qwen2.5vl:7b"
  messages = @(
    @{
      role = "system"
      content = @"
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
"@
    },
    @{
      role = "user"
      content = "Identifie ce plat, liste les ingrédients visibles et estime les calories."
      images = @($imgB64)
    }
  )
  stream = $false
  format = "json"
  options = @{
    temperature = 0.2
    num_ctx = 4096
  }
} | ConvertTo-Json -Depth 10

$response = Invoke-RestMethod -Uri "http://localhost:11434/api/chat" -Method Post -Body $body -ContentType "application/json"
$response.message.content
Write-Host "`nLatence : $($response.total_duration / 1e9) s"
```

- [ ] 4.4 Lancer le script → doit renvoyer **un JSON strict** ressemblant à :
      ```json
      {"nomPlat":"Spaghetti bolognaise","ingredients":["spaghetti","sauce tomate","bœuf haché","parmesan"],"portion":"moyen","caloriesMin":520,"caloriesMax":680,"confiance":"moyenne"}
      ```
- [ ] 4.5 Vérifier :
      - JSON parse OK
      - Champs tous présents
      - `nomPlat` et `ingredients` en **français**
      - `portion` ∈ `{petit, moyen, grand}`
      - `confiance` ∈ `{haute, moyenne, basse}`
      - `caloriesMin < caloriesMax`

- [ ] 4.6 Refaire avec 2 autres photos pour voir la variance.

**Si le JSON est invalide** (texte + JSON, ou markdown fences) :
- Augmenter la sévérité du prompt système : `"You MUST respond with ONLY JSON. Any prose will be rejected."`
- Baisser `temperature` à `0.1`.
- Sinon, s'appuyer sur le retry STRICT MODE côté Java (déjà prévu dans `AnalyseService.java`).

---

## Phase 5 — Benchmark comparatif qwen2.5vl vs gemma3 (20 min)

**But** : trancher le modèle principal pour la démo.

### Protocole

- [ ] 5.1 Choisir 8-12 photos test variées (plats FR simples, composés, portions, plats asiatiques, cas limite).
- [ ] 5.2 Lancer le script `scripts/benchmark.ps1` (voir ci-dessous) sur les 2 modèles.
- [ ] 5.3 Analyser le CSV de sortie :
      - **Taux JSON valide** (sur 10 : combien parsables ?)
      - **Latence moyenne** (ms)
      - **Plausibilité nomPlat** (jugement humain : oui / non)
      - **Plausibilité calories** (±40% acceptable vs valeur connue ou estimée)

### Critère de choix

| Critère | Poids |
|---|---|
| JSON valide | critique (≥ 90%) |
| Latence | importante (< 10 s/photo en GPU) |
| Plausibilité nom plat | importante |
| Plausibilité calories | secondaire (on annonce ±30% en UI) |

- [ ] 5.4 Décider : **qwen2.5vl:7b** ou **gemma3:4b** en principal → mettre le choix dans `../DECISIONS.md` section A2.

### Script `scripts/benchmark.ps1`

Voir fichier séparé : `scripts/benchmark.ps1`. Usage :

```powershell
cd C:\...\itadaki\Model-IA\scripts
.\benchmark.ps1 -ImagesDir "C:\itadaki-test" -Models @("qwen2.5vl:7b","gemma3:4b") -OutputCsv results.csv
```

Sortie : `results.csv` avec colonnes `image, model, latence_s, json_valide, nomPlat, calories_min, calories_max, confiance, raw_json`.

---

## Phase 6 — Exposer Ollama sur le LAN (pour Lorenzo et Ahmed)

Par défaut, Ollama n'écoute que sur `localhost:11434`. Pour que les autres machines puissent l'atteindre depuis le réseau local du hackaton :

### 6.1 Trouver ton IP locale

- [ ] Dans PowerShell :
      ```powershell
      ipconfig | Select-String "IPv4"
      ```
      → note l'IP commençant par `192.168.x.x` ou `10.x.x.x` (ta carte WiFi / Ethernet).
      Ex. : `192.168.1.42`.

### 6.2 Faire écouter Ollama sur toutes les interfaces

Deux options — la **persistante** est préférable pour la démo.

**Option A — Variable d'env persistante (Windows)**

- [ ] Panneau de config → "Modifier les variables d'environnement système" → Variables utilisateur → Nouveau :
      - Nom : `OLLAMA_HOST`
      - Valeur : `0.0.0.0:11434`
- [ ] Redémarrer Ollama (fermer l'icône dans la tray + relancer depuis le menu Démarrer)
- [ ] Vérifier : `netstat -an | findstr :11434` → doit montrer `0.0.0.0:11434` (plus `127.0.0.1:11434`).

**Option B — Session en cours**

```powershell
$env:OLLAMA_HOST = "0.0.0.0:11434"
ollama serve
```

(À relancer à chaque reboot.)

### 6.3 Autoriser le port 11434 dans le firewall Windows

- [ ] PowerShell en **Administrateur** :
      ```powershell
      New-NetFirewallRule -DisplayName "Ollama" -Direction Inbound -Protocol TCP -LocalPort 11434 -Action Allow
      ```

### 6.4 Test de connexion depuis une autre machine

- [ ] Lorenzo ou Ahmed sur le même réseau :
      ```powershell
      curl http://<ip-hugo>:11434
      ```
      → doit afficher `Ollama is running`.
- [ ] Test d'inférence distant :
      ```powershell
      curl http://<ip-hugo>:11434/api/chat -d '{\"model\":\"qwen2.5vl:7b\",\"messages\":[{\"role\":\"user\",\"content\":\"Hello\"}],\"stream\":false}'
      ```

### 6.5 Communiquer l'IP à l'équipe

- [ ] Envoyer à Lorenzo + Ahmed : `OLLAMA_HOST=http://<ton-ip>:11434`

Ils mettront cette valeur dans leur `application.properties` (ou variable d'env shell) :

```properties
spring.ai.ollama.base-url=${OLLAMA_HOST:http://localhost:11434}
```

---

## Phase 7 — Checklist finale avant de dire « OK » à l'équipe

- [ ] 7.1 Les 2 modèles `qwen2.5vl:7b` et `gemma3:4b` sont pullés et testés.
- [ ] 7.2 Au moins 3 photos testées → JSON strict valide sur le modèle choisi.
- [ ] 7.3 Latence moyenne < 15 s par photo en GPU.
- [ ] 7.4 Ollama écoute sur `0.0.0.0:11434`, firewall ouvert.
- [ ] 7.5 Lorenzo a pu atteindre Ollama depuis sa machine.
- [ ] 7.6 CSV du benchmark sauvegardé dans `itadaki/Model-IA/scripts/results.csv`.
- [ ] 7.7 Décision A2 mise à jour dans `../DECISIONS.md` : modèle principal = ?
- [ ] 7.8 Prompts validés dans `../PROMPTS.md` (signaler si tu as ajusté).

Ping Lorenzo → il complète `application.properties` côté Spring Boot avec ton IP + le nom du modèle retenu.

---

## Troubleshooting

| Symptôme | Probable cause | Fix |
|---|---|---|
| `ollama pull` très lent | Connexion ESGI | Utiliser ta 4G personnelle ou à la maison avant d'aller en cours |
| `ollama run` utilise le CPU au lieu du GPU | Drivers NVIDIA pas à jour | `nvidia-smi` pour vérifier + `GeForce Experience` / site NVIDIA |
| Réponse vide ou très courte | `num_predict` bas par défaut | Ajouter `num_predict: 512` dans `options` |
| JSON parfois invalide | Modèle bavard | Augmenter strictness prompt système + baisser temp à 0.1 |
| Timeout > 3 min sur une photo | Image trop grosse | Redimensionner à 1024 px max avant envoi |
| `curl http://<ip>:11434` timeout depuis autre PC | Firewall bloque | `New-NetFirewallRule` (cf. 6.3) + vérifier qu'on est sur le même WiFi |
| Erreur `model not found` côté Spring | Typo ou modèle pas pullé | `ollama list` doit afficher exactement `qwen2.5vl:7b` |
| Ollama crash / OOM | Modèle trop gros en VRAM | Passer à `gemma3:4b` (2x plus léger) |
| `OLLAMA_HOST` ne prend pas | Variable pas relue | Fermer Ollama dans le tray + relancer |

---

## Pour info : ce que Hugo livre à Ahmed/Lorenzo

À la fin de ce setup, tu dois avoir :

1. **IP + port Ollama** → `http://192.168.x.x:11434`
2. **Modèle principal retenu** → `qwen2.5vl:7b` ou `gemma3:4b`
3. **CSV benchmark** → prouve que ça marche, utile pour la slide IA de la démo
4. **3 photos test de référence** commitées dans `itadaki/Model-IA/test-samples/` (pour que Ahmed teste le service Spring AI avec les mêmes images)
5. **Prompt validé** → si tu as ajusté `PROMPTS.md`, commit + push
