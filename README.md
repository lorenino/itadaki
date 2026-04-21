# ITADAKI

Application de tracking nutritionnel avec IA (Ollama + Spring Boot).

---

## Lancement (une seule commande)

Ouvre un terminal PowerShell dans le dossier du projet, puis :

```powershell
.\run.bat
```

Le menu interactif s'ouvre automatiquement.

> Si PowerShell bloque l'execution, utilise :
> ```powershell
> powershell -ExecutionPolicy Bypass -File .\automation\menu.ps1
> ```

---

## Prerequis

| Outil  | Version | Lien                                                  |
|--------|---------|-------------------------------------------------------|
| Java   | 25      | https://www.oracle.com/java/technologies/downloads/   |
| Ollama | latest  | https://ollama.com/download                           |
| ngrok  | latest  | https://ngrok.com/download/windows                    |

Maven n'est pas requis — le projet inclut `mvnw.cmd` qui le telecharge automatiquement.

---

## Installation de ngrok (a faire une seule fois)

### 1. Installer ngrok

Le script propose de l'installer automatiquement au premier lancement (telechargement + extraction).

**Ou manuellement via PowerShell :**
```powershell
Invoke-WebRequest -Uri "https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-windows-amd64.zip" -OutFile "$env:TEMP\ngrok.zip" -UseBasicParsing
Expand-Archive "$env:TEMP\ngrok.zip" -DestinationPath "C:\ngrok" -Force
```

### 2. Creer un compte et recuperer ton token

Va sur : https://dashboard.ngrok.com/get-started/your-authtoken

Connecte-toi (ou cree un compte gratuit), copie ton authtoken.

### 3. Configurer le token (une seule fois)

```powershell
C:\ngrok\ngrok.exe config add-authtoken TON_TOKEN_ICI
```

Remplace `TON_TOKEN_ICI` par le token copie depuis le dashboard.

### 4. Verifier l'installation

```powershell
ngrok version
```

---

## Options du menu

| Option            | Description                                          |
|-------------------|------------------------------------------------------|
| 1 - LOCAL         | Ollama en local + Spring Boot                        |
| 2 - APP ONLY      | Spring Boot seul (detecte ngrok auto si actif)       |
| 3 - OLLAMA + NGROK| Partage ton GPU via ngrok avec l'equipe              |
| 4 - FULL          | Ollama + ngrok + Spring Boot tout-en-un              |
| 5 - DOWNLOAD MODEL| Telecharge qwen2.5vl:7b (~6 Go)                     |

---

## Detection automatique de ngrok

Quand ngrok est deja actif, le script detecte l'URL publique automatiquement
et injecte `OLLAMA_URL` avant de demarrer Spring Boot — aucune configuration manuelle.

---

## Acces a l'application

- API : http://localhost:8080
- Swagger : http://localhost:8080/swagger-ui.html
- H2 Console : http://localhost:8080/h2-console
