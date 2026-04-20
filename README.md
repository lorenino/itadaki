# 🚀 ITADAKI - Hackathon ESGI

Application de tracking nutritionnel avec IA (Ollama + Spring Boot).

---

## ⚡ Démarrage Rapide

### Commande Unique (RECOMMANDÉ)

```bash
make run
```

Cela lance un menu interactif avec 3 options :

1. **TOUT EN LOCAL** - Ollama + Spring Boot (localhost:11434 + :8080)
2. **APP SEULE** - Seulement Spring Boot (si Ollama déjà lancé ailleurs)
3. **OLLAMA + NGROK SEULS** - Pour partager Ollama avec une autre machine

---

## 📚 Installation Première Fois

### Prérequis

1. **Java 21+** (https://www.oracle.com/java/technologies/downloads/)
2. **Maven 3.8+** (https://maven.apache.org/download.cgi)
3. **GNU Make** (https://github.com/jqlang/jq/releases ou via Scoop)
4. **Ollama** (https://ollama.com/download)
5. **ngrok** (https://ngrok.com/download)

### Installation Make (si absent)

```powershell
powershell -ExecutionPolicy Bypass -File automation/install-make.ps1
```

### Étapes de configuration

1. **Installer Ollama**
   ```bash
   irm https://ollama.com/install.ps1 | iex
   ```

2. **Installer ngrok**
   - Télécharger sur https://ngrok.com/download
   - Extraire dans `C:\ngrok`

3. **Télécharger le modèle IA**
   ```bash
   make run
   # Choix : 4 (Télécharger modèle)
   ```

4. **Lancer l'app**
   ```bash
   make run
   # Choix : 1 (Tout en local) ou 2 (App seule) ou 3 (Ollama+ngrok)
   ```

---

## 🗂️ Structure du Projet

```
itadaki/
├── src/                    # Code source
│   ├── main/java/         # Classes Java
│   └── main/resources/     # application.properties
├── automation/            # Scripts d'automatisation
│   ├── itadaki-setup.ps1  # Menu interactif PowerShell
│   └── README.md
├── AI_Model/              # Documentation IA
│   ├── setup-model.md     # Guide d'installation complet
│   ├── VALIDATION-OLLAMA.md
│   └── APPLICATION-PROPERTIES.md
├── Makefile               # Commandes Make
└── pom.xml                # Configuration Maven
```

---

## 🔧 Configuration

### application.properties

Les propriétés Ollama sont automatiquement mises à jour :

```properties
# Développement local
spring.ai.ollama.base-url=http://localhost:11434

# Ou avec ngrok
spring.ai.ollama.base-url=https://xxxx-yyyy-zzzz.ngrok-free.app
```

### Autres propriétés

- Port Spring Boot : `8080`
- Port Ollama : `11434`
- Base de données : H2 (fichier `./data/itadaki`)
- Upload de photos : `./uploads`

---

## 🚀 API Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/login` | Connexion |
| POST | `/api/photos` | Upload photo |
| POST | `/api/photos/{id}/analyses` | Analyser |
| GET | `/api/historique` | Historique |

Documentation Swagger : http://localhost:8080/swagger-ui.html

---

## 🛠️ Automatisation

Le dossier `automation/` contient un script PowerShell qui :

✅ Vérifie les dépendances
✅ Lance Ollama en arrière-plan
✅ Gère les terminaux
✅ Capture l'URL ngrok
✅ Met à jour automatiquement `application.properties`
✅ Lance Spring Boot

```bash
# Menu interactif
make run

# Ou directement
powershell -NoProfile -ExecutionPolicy Bypass -File automation/itadaki-setup.ps1
```

---

## 📝 Notes

- 🎯 **Première utilisation** : Lance `make run` et suis le menu
- ⏳ **Warm-up Ollama** : Premier appel prend ~50s, les suivants sont rapides
- 🔄 **Cold start** : Après 5 min d'inactivité, le modèle se décharge
- 🌐 **Démo ngrok** : L'URL change à chaque session (à noter!)

---

## 📚 Documentation

- `AI_Model/setup-model.md` - Guide installation Ollama + ngrok
- `AI_Model/VALIDATION-OLLAMA.md` - Détails IA et modèles
- `automation/README.md` - Guide automation
- `CONVENTIONS.md` - Conventions du projet

---

## 🚀 Démarrage Typique

```bash
# 1. Menu interactif
make run

# 2. Choix : "1" pour local ou "2" pour ngrok
# 3. Attendre les 3 terminaux0
# 4. Accéder à http://localhost:8080
```

---

**Bon développement ! 🎉**

