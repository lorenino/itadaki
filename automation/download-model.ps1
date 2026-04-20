# Script pour télécharger le modèle qwen2.5vl:7b
# Usage: . automation/download-model.ps1

Write-Host "Téléchargement du modèle qwen2.5vl:7b..." -ForegroundColor Yellow
Write-Host "Cela prendra 10-15 minutes (6 GB)..." -ForegroundColor Yellow

$env:PATH += ";C:\Users\letai\AppData\Local\Programs\Ollama"
& "C:\Users\letai\AppData\Local\Programs\Ollama\ollama.exe" pull qwen2.5vl:7b

Write-Host ""
Write-Host "✅ Modèle téléchargé !" -ForegroundColor Green
Write-Host "Vérifie avec: ollama list" -ForegroundColor Blue

