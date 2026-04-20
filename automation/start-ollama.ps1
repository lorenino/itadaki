# Start Ollama in background

# Find Ollama path dynamically
$ollamaPaths = @(
    "C:\Users\$env:USERNAME\AppData\Local\Programs\Ollama\ollama.exe",
    "C:\Program Files\Ollama\ollama.exe",
    "C:\Program Files (x86)\Ollama\ollama.exe"
)

$OLLAMA_PATH = $null
foreach ($path in $ollamaPaths) {
    if (Test-Path $path) {
        $OLLAMA_PATH = $path
        break
    }
}

if (-not $OLLAMA_PATH) {
    Write-Host "ERROR: Ollama not found" -ForegroundColor Red
    Write-Host "Download from: https://ollama.com/download" -ForegroundColor Blue
    exit 1
}

$ollamaDir = Split-Path -Parent $OLLAMA_PATH
$env:PATH += ";$ollamaDir"
$env:OLLAMA_HOST = "0.0.0.0:11434"

Write-Host "Starting Ollama on 0.0.0.0:11434..." -ForegroundColor Green
Start-Process -FilePath $OLLAMA_PATH -ArgumentList "serve"

Start-Sleep -Seconds 3
Write-Host "Ollama started (http://localhost:11434)" -ForegroundColor Green

