# ITADAKI Setup Automation
$OLLAMA_PATH = "C:\Users\letai\AppData\Local\Programs\Ollama\ollama.exe"
$NGROK_PATH = "C:\ngrok\ngrok.exe"
$PROJECT_PATH = "C:\Users\letai\OneDrive\Bureau\ESGI\Hackathon\itadaki"
$APP_PROPS = "$PROJECT_PATH\src\main\resources\application.properties"
function Show-Menu {
    Clear-Host
    Write-Host "========== ITADAKI Setup ==========" -ForegroundColor Cyan
    Write-Host "1 - Lancer app LOCAL"
    Write-Host "2 - Lancer app NGROK"
    Write-Host "3 - Telecharger modele"
    Write-Host "4 - Configurer ngrok"
    Write-Host "0 - Quitter"
    $choice = Read-Host "Option"
    switch ($choice) {
        "1" { Write-Host "Mode LOCAL selectionne" -ForegroundColor Green }
        "2" { Write-Host "Mode NGROK selectionne" -ForegroundColor Green }
        "3" { Write-Host "Telechargement modele..." -ForegroundColor Yellow }
        "0" { exit }
    }
}
Show-Menu
