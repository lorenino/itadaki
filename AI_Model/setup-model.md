# 🚀 Setup Ollama + ngrok + Spring Boot

**Infrastructure IA locale — Commandes avec PATH correct**

---

## 📋 Prérequis

- ✅ Java 21+
- ✅ Maven 3.8+
- ✅ GNU Make (télécharger ou installer)
- ✅ Ollama
- ✅ ngrok

---

## 🚀 UTILISATION RAPIDE

```bash
make run
```

Cela lance un menu interactif avec 3 options :

1. **TOUT EN LOCAL** - Ollama + Spring Boot
2. **APP SEULE** - Seulement Spring Boot (si Ollama déjà lancé)
3. **OLLAMA + NGROK SEULS** - Pour partager avec une autre machine

---

## 📥 Installation Ollama

### ✨ Option 1 : Installation via PowerShell (RECOMMANDÉ) ⚡

**Lien de téléchargement :** https://ollama.com/download

**Copie et colle cette commande seule :**

```powershell
irm https://ollama.com/install.ps1 | iex
```

**Attends que l'installation se termine.**

---

**Puis vérifie l'installation :**

```powershell
ollama --version
```

---

### 📦 Option 2 : Téléchargement manuel

1. Va à : **https://ollama.com/download**
2. Clique sur le lien de téléchargement Windows
3. Double-clique sur `OllamaSetup.exe`
4. Suis l'installation (Next → Install → Finish)
5. Redémarre (optionnel)

**Puis vérifie :**

```powershell
ollama --version
```

---

### 🔧 Option 3 : Via winget

```powershell
winget install ollama.ollama
```

---

---

## 🎯 Terminal 1 : Lancer Ollama en serveur

**Copie et colle :**

```powershell
$env:PATH += ";C:\Users\letai\AppData\Local\Programs\Ollama"
```

**Puis :**

```powershell
$env:OLLAMA_HOST = "0.0.0.0:11434"
```

**Puis :**

```powershell
ollama serve
```

✅ **Ollama actif sur http://localhost:11434**

**OU utilise le script automatique :**

```powershell
. automation/start-ollama.ps1
```

---

---

## 📥 Terminal 2 : Télécharger le modèle IA

**Copie et colle :**

```powershell
$env:PATH += ";C:\Users\letai\AppData\Local\Programs\Ollama"
```

**Puis :**

```powershell
ollama pull qwen2.5vl:7b
```

⏳ **Attends 10-15 minutes (6 GB)**

✅ **Quand c'est fini :**

```powershell
ollama list
```

**OU utilise le script :**

```powershell
. automation/download-model.ps1
```

---

---

## 🌐 Installation ngrok

### 📦 Option 1 : Téléchargement ZIP

1. Va à : **https://ngrok.com/download**
2. Clique sur **Windows**
3. Télécharge le ZIP
4. Extrait le contenu dans : `C:\ngrok`

---

**Puis copie et colle cette commande pour vérifier :**

```powershell
C:\ngrok\ngrok.exe --version
```

---

### 🔧 Option 2 : Via winget

```powershell
winget install ngrok.ngrok
```

---

---

## 🔑 Configuration ngrok

**Avant de continuer :**

1. Crée un compte gratuit : **https://dashboard.ngrok.com/signup**
2. Récupère ton **Auth Token** : **https://dashboard.ngrok.com/get-started/your-authtoken**
3. Copie le token

---

**Dans Terminal 2 (après le téléchargement du modèle), copie et colle :**

```powershell
ngrok config add-authtoken <COLLE_TON_TOKEN_ICI>
```

**Remplace `<COLLE_TON_TOKEN_ICI>` par ton token réel et appuie sur ENTRÉE.**

---

---

## 🚇 Terminal 3 : Lancer le tunnel ngrok

**Copie et colle :**

```powershell
ngrok http 11434
```

Tu devrais voir :
```
Session Status      online
Forwarding          https://xxxx-yyyy-zzzz.ngrok-free.app -> http://localhost:11434
```

📝 **NOTE L'URL HTTPS** 

**OU utilise le script :**

```powershell
. automation/start-ngrok.ps1
```

---

---

## ✅ Test de connectivité

### Test 1 : Via localhost

**Ouvre Terminal 4 (ne ferme pas T1, T2, T3).**

**Copie et colle :**

```powershell
curl http://localhost:11434
```

✅ **Résultat attendu :**
```
Ollama is running
```

---

### Test 2 : Via ngrok

**Copie et colle (remplace l'URL par la tienne) :**

```powershell
curl -H "ngrok-skip-browser-warning: any" https://xxxx-yyyy-zzzz.ngrok-free.app
```

✅ **Résultat attendu :**
```
Ollama is running
```

---

---

## ⚙️ Mettre à jour application.properties

**Fichier à éditer :** `src/main/resources/application.properties`

---

### Pour développement local (RECOMMANDÉ pour tester d'abord)

**Copie et colle :**

```properties
spring.ai.ollama.base-url=http://localhost:11434
```

---

### Pour démo en réseau (avec ngrok)

**Remplace `https://xxxx-yyyy-zzzz.ngrok-free.app` par TON URL ngrok :**

```properties
spring.ai.ollama.base-url=https://xxxx-yyyy-zzzz.ngrok-free.app
```

---

### Ajoute aussi ces propriétés :

```properties
spring.ai.ollama.chat.options.model=qwen2.5vl:7b
```

```properties
spring.ai.ollama.chat.options.temperature=0.2
```

```properties
spring.ai.ollama.chat.options.num-ctx=4096
```

```properties
spring.ai.ollama.client.read-timeout=180s
```

```properties
spring.ai.ollama.client.connect-timeout=10s
```

---

---

## 🎬 Terminal 5 : Lancer Spring Boot

**Ouvre un NOUVEAU Terminal PowerShell (Terminal 5).**

**Copie et colle cette commande seule :**

```powershell
cd C:\Users\letai\OneDrive\Bureau\ESGI\Hackathon\itadaki
```

**Appuie sur ENTRÉE.**

---

**Puis copie et colle :**

```powershell
mvn spring-boot:run
```

**Appuie sur ENTRÉE et attends le démarrage.**

✅ **L'app sera accessible sur :** `http://localhost:8080`

**⚠️ NE FERME PAS ce terminal**

---

---

## 📋 Résumé : 5 terminaux ouverts en permanence

| # | Commande | État |
|:---:|:---|:---:|
| **T1** 🎯 | `ollama serve` | 🟢 Actif |
| **T2** 📥 | `ollama pull qwen2.5vl:7b` | ✅ Terminé |
| **T3** 🚇 | `ngrok http 11434` | 🟢 Actif |
| **T4** 🧪 | Tests (`curl`) | ✅ Terminé |
| **T5** 🎬 | `mvn spring-boot:run` | 🟢 Actif |

**ATTENTION :** Les terminaux T1, T3, T5 doivent rester ouverts tout le temps.

---


## 🚨 Notes importantes

⚠️ **NE FERME JAMAIS** les 3 terminaux (T1, T3, T5) une fois en production

⚠️ **Warm-up** : Premier appel Ollama = ~50 secondes, les suivants = 1-10 secondes

⚠️ **Cold start** : Après 5 minutes d'inactivité, le modèle se décharge de la VRAM

---

---

## 🔍 Commandes de diagnostic (à copier une par une)

**Test Ollama local :**

```powershell
curl http://localhost:11434
```

---

**Voir tous les modèles :**

```powershell
ollama list
```

---

**Test via ngrok (remplace l'URL) :**

```powershell
curl -H "ngrok-skip-browser-warning: any" https://xxxx-yyyy-zzzz.ngrok-free.app/api/tags
```

---

---

**✅ C'est bon ? Tu peux commencer ! 🚀**

