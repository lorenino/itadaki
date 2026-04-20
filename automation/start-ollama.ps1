# Start Ollama in background
$PROJECT_ROOT = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$OLLAMA_PATH = "C:\Users\letai\AppData\Local\Programs\Ollama\ollama.exe"

$env:PATH += ";C:\Users\letai\AppData\Local\Programs\Ollama"
$env:OLLAMA_HOST = "0.0.0.0:11434"

Write-Host "Starting Ollama on 0.0.0.0:11434..." -ForegroundColor Green
Start-Process -FilePath $OLLAMA_PATH -ArgumentList "serve" -NoNewWindow

Start-Sleep -Seconds 3
Write-Host "Ollama started (http://localhost:11434)" -ForegroundColor Green

