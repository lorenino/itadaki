#!/bin/bash
# Script pour lancer Ollama en arrière-plan
# Usage: . automation/start-ollama.sh

$env:PATH += ";C:\Users\letai\AppData\Local\Programs\Ollama"
$env:OLLAMA_HOST = "0.0.0.0:11434"

Write-Host "Lancement Ollama sur 0.0.0.0:11434..." -ForegroundColor Green
Start-Process -FilePath "C:\Users\letai\AppData\Local\Programs\Ollama\ollama.exe" -ArgumentList "serve" -NoNewWindow

Start-Sleep -Seconds 3
Write-Host "✅ Ollama lancé (localhost:11434)" -ForegroundColor Green

