# Start ngrok tunnel

# Find ngrok path dynamically
$ngrokPaths = @(
    "C:\ngrok\ngrok.exe",
    "C:\Program Files\ngrok\ngrok.exe"
)

$NGROK_PATH = $null
foreach ($path in $ngrokPaths) {
    if (Test-Path $path) {
        $NGROK_PATH = $path
        break
    }
}

if (-not $NGROK_PATH) {
    Write-Host "ERROR: ngrok not found" -ForegroundColor Red
    Write-Host "Download from: https://ngrok.com/download" -ForegroundColor Blue
    Write-Host "Extract to: C:\ngrok" -ForegroundColor Blue
    exit 1
}

Write-Host "Starting ngrok tunnel on port 11434..." -ForegroundColor Green
Start-Process -FilePath $NGROK_PATH -ArgumentList "http 11434"

Start-Sleep -Seconds 3
Write-Host "Ngrok started - Note the URL displayed in the ngrok window" -ForegroundColor Green

