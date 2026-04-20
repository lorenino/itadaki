# Download Ollama model
$OLLAMA_PATH = "C:\Users\letai\AppData\Local\Programs\Ollama\ollama.exe"

Write-Host ""
Write-Host "Downloading model qwen2.5vl:7b..." -ForegroundColor Yellow
Write-Host "This will take 10-15 minutes (6 GB)..." -ForegroundColor Yellow
Write-Host ""

$env:PATH += ";C:\Users\letai\AppData\Local\Programs\Ollama"
& $OLLAMA_PATH pull qwen2.5vl:7b

Write-Host ""
Write-Host "Model downloaded!" -ForegroundColor Green
Write-Host "Verify with: ollama list" -ForegroundColor Blue

