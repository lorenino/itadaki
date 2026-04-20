# 🚀 ITADAKI Automation

Scripts d'automatisation pour lancer l'infrastructure IA.

## 📋 Fichiers

| Fichier | Description |
|---------|-------------|
| `menu.ps1` | Menu interactif (point d'entrée principal) |
| `start-ollama.ps1` | Démarre Ollama en arrière-plan |
| `start-ngrok.ps1` | Démarre ngrok en arrière-plan |
| `start-spring.ps1` | Démarre Spring Boot |
| `download-model.ps1` | Télécharge le modèle qwen2.5vl:7b |
| `install-prerequisites.ps1` | Installe tous les prérequis (Make, Ollama, ngrok) |

## 🚀 Utilisation

### Première fois : Installer les prérequis

```powershell
powershell -ExecutionPolicy Bypass -File automation/install-prerequisites.ps1
```

Puis redémarre PowerShell.

### Ensuite : Lancer le menu

```bash
make run
```

Ou directement :

```powershell
powershell -ExecutionPolicy Bypass -File automation/menu.ps1
```

## 📊 Menu Interactif

1. **TOUT EN LOCAL** - Ollama + Spring Boot
2. **APP SEULE** - Seulement Spring Boot
3. **OLLAMA + NGROK SEULS** - Pour infrastructure partagée
4. **TÉLÉCHARGER MODÈLE** - qwen2.5vl:7b

## 📝 Notes

- Scripts séparés pour chaque composant
- Utilise PowerShell (Windows natif)
- Lance en arrière-plan via Start-Process
- Pas de dépendance externe (sauf Make pour le Makefile)


