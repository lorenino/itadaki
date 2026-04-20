# Script pour lancer ngrok
# Usage: . automation/start-ngrok.ps1

Write-Host "Lancement ngrok sur port 11434..." -ForegroundColor Green
Start-Process -FilePath "C:\ngrok\ngrok.exe" -ArgumentList "http 11434"

Start-Sleep -Seconds 3
Write-Host "✅ ngrok lancé - Note l'URL affichée dans la nouvelle fenêtre" -ForegroundColor Green

