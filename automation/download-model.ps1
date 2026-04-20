# Download Ollama model

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

Write-Host ""
Write-Host "Downloading model qwen2.5vl:7b..." -ForegroundColor Yellow
Write-Host "This will take 10-15 minutes (6 GB)..." -ForegroundColor Yellow
Write-Host ""

$ollamaDir = Split-Path -Parent $OLLAMA_PATH
$env:PATH += ";$ollamaDir"

& $OLLAMA_PATH pull qwen2.5vl:7b

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Model downloaded successfully!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "ERROR: Model download failed" -ForegroundColor Red
    exit 1
}

