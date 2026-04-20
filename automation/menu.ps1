# Menu interactif pour ITADAKI
# Lance les différents modes

function Show-Menu {
    Clear-Host
    Write-Host ""
    Write-Host "╔═════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║       ITADAKI - Mode de Lancement       ║" -ForegroundColor Cyan
    Write-Host "╚═════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  1️⃣  TOUT EN LOCAL" -ForegroundColor Blue
    Write-Host "      Ollama + Spring Boot (localhost:11434 + :8080)"
    Write-Host ""
    Write-Host "  2️⃣  APP SEULE" -ForegroundColor Blue
    Write-Host "      Seulement Spring Boot (supposant Ollama déjà lancé)"
    Write-Host ""
    Write-Host "  3️⃣  OLLAMA + NGROK SEULS" -ForegroundColor Blue
    Write-Host "      Pour partager Ollama avec une autre machine"
    Write-Host ""
    Write-Host "  4️⃣  TÉLÉCHARGER MODÈLE" -ForegroundColor Blue
    Write-Host "      qwen2.5vl:7b (10-15 min, 6 GB)"
    Write-Host ""
    Write-Host "  0️⃣  QUITTER" -ForegroundColor Red
    Write-Host ""

    $choice = Read-Host "Choisis une option (0-4)"

    switch ($choice) {
        "1" { Run-All-Local }
        "2" { Run-App-Only }
        "3" { Run-Ollama-Ngrok-Only }
        "4" { Download-Model }
        "0" { exit }
        default { Show-Menu }
    }
}

function Run-All-Local {
    Clear-Host
    Write-Host ""
    Write-Host "🚀 Mode LOCAL : Ollama + Spring Boot" -ForegroundColor Green
    Write-Host ""

    # Ollama
    Write-Host "1️⃣  Lancement d'Ollama..." -ForegroundColor Yellow
    & "C:\Users\letai\OneDrive\Bureau\ESGI\Hackathon\itadaki\automation\start-ollama.ps1"

    # Spring Boot
    Write-Host ""
    Write-Host "2️⃣  Lancement de Spring Boot..." -ForegroundColor Yellow
    & "C:\Users\letai\OneDrive\Bureau\ESGI\Hackathon\itadaki\automation\start-spring.ps1"
}

function Run-App-Only {
    Clear-Host
    Write-Host ""
    Write-Host "🚀 Mode APP SEULE : Spring Boot" -ForegroundColor Green
    Write-Host ""

    Write-Host "Lancement de Spring Boot..." -ForegroundColor Yellow
    & "C:\Users\letai\OneDrive\Bureau\ESGI\Hackathon\itadaki\automation\start-spring.ps1"
}

function Run-Ollama-Ngrok-Only {
    Clear-Host
    Write-Host ""
    Write-Host "🚀 Mode OLLAMA + NGROK : Infrastructure IA" -ForegroundColor Green
    Write-Host ""

    # Ollama
    Write-Host "1️⃣  Lancement d'Ollama..." -ForegroundColor Yellow
    & "C:\Users\letai\OneDrive\Bureau\ESGI\Hackathon\itadaki\automation\start-ollama.ps1"

    # ngrok
    Write-Host ""
    Write-Host "2️⃣  Lancement de ngrok..." -ForegroundColor Yellow
    & "C:\Users\letai\OneDrive\Bureau\ESGI\Hackathon\itadaki\automation\start-ngrok.ps1"

    Write-Host ""
    Write-Host "✅ Ollama + ngrok lancés" -ForegroundColor Green
    Write-Host "📝 Note l'URL ngrok affichée dans la fenêtre ngrok" -ForegroundColor Blue
    Write-Host "   Utilise-la dans ta variable OLLAMA_URL si tu lances l'app ailleurs" -ForegroundColor Blue

    Write-Host ""
    Read-Host "Appuie sur ENTRÉE pour garder les services actifs..."
}

function Download-Model {
    Clear-Host
    Write-Host ""
    Write-Host "📥 Téléchargement du modèle" -ForegroundColor Green
    Write-Host ""

    & "C:\Users\letai\OneDrive\Bureau\ESGI\Hackathon\itadaki\automation\download-model.ps1"

    Write-Host ""
    Read-Host "Appuie sur ENTRÉE pour revenir au menu..."
    Show-Menu
}

# Lancer le menu
Show-Menu

