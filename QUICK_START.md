# ITADAKI - Demarrage rapide

## Lancer l'application

Dans un terminal PowerShell a la racine du projet :

```powershell
.\run.bat
```

---

## Configurer ngrok (une seule fois)

```powershell
# Telecharger et extraire ngrok
Invoke-WebRequest -Uri "https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-windows-amd64.zip" -OutFile "$env:TEMP\ngrok.zip" -UseBasicParsing
Expand-Archive "$env:TEMP\ngrok.zip" -DestinationPath "C:\ngrok" -Force

# Configurer le token (recuperer sur https://dashboard.ngrok.com/get-started/your-authtoken)
C:\ngrok\ngrok.exe config add-authtoken TON_TOKEN_ICI
```

---

Voir README.md pour la documentation complete.
