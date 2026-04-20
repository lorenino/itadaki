# Start ngrok tunnel
$NGROK_PATH = "C:\ngrok\ngrok.exe"

Write-Host "Starting ngrok tunnel on port 11434..." -ForegroundColor Green
Start-Process -FilePath $NGROK_PATH -ArgumentList "http 11434"

Start-Sleep -Seconds 3
Write-Host "Ngrok started - Note the URL displayed in the ngrok window" -ForegroundColor Green

