# Main menu for ITADAKI
# Interactive menu to launch different modes

$ScriptDir = Split-Path -Parent $PSScriptRoot
$AutomationDir = Split-Path -Parent $PSScriptRoot

function Show-Menu {
    Clear-Host
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "     ITADAKI - Launch Menu             " -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  1  - LOCAL (Ollama + Spring Boot)" -ForegroundColor Blue
    Write-Host "  2  - APP ONLY (Spring Boot)" -ForegroundColor Blue
    Write-Host "  3  - OLLAMA + NGROK (Shared)" -ForegroundColor Blue
    Write-Host "  4  - DOWNLOAD MODEL" -ForegroundColor Blue
    Write-Host "  0  - EXIT" -ForegroundColor Red
    Write-Host ""

    $choice = Read-Host "Choose option (0-4)"

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
    Write-Host "Starting LOCAL mode..." -ForegroundColor Green
    Write-Host ""

    Write-Host "1. Starting Ollama..." -ForegroundColor Yellow
    & "$AutomationDir\automation\start-ollama.ps1"

    Write-Host ""
    Write-Host "2. Starting Spring Boot..." -ForegroundColor Yellow
    & "$AutomationDir\automation\start-spring.ps1"
}

function Run-App-Only {
    Clear-Host
    Write-Host ""
    Write-Host "Starting APP ONLY mode..." -ForegroundColor Green
    Write-Host ""

    Write-Host "Starting Spring Boot..." -ForegroundColor Yellow
    & "$AutomationDir\automation\start-spring.ps1"
}

function Run-Ollama-Ngrok-Only {
    Clear-Host
    Write-Host ""
    Write-Host "Starting OLLAMA + NGROK mode..." -ForegroundColor Green
    Write-Host ""

    Write-Host "1. Starting Ollama..." -ForegroundColor Yellow
    & "$AutomationDir\automation\start-ollama.ps1"

    Write-Host ""
    Write-Host "2. Starting ngrok..." -ForegroundColor Yellow
    & "$AutomationDir\automation\start-ngrok.ps1"

    Write-Host ""
    Write-Host "Ollama + ngrok started" -ForegroundColor Green
    Write-Host "Note the ngrok URL from the ngrok window" -ForegroundColor Blue

    Write-Host ""
    Read-Host "Press ENTER to keep services running..."
}

function Download-Model {
    Clear-Host
    Write-Host ""
    Write-Host "Downloading model..." -ForegroundColor Green
    Write-Host ""

    & "$AutomationDir\automation\download-model.ps1"

    Write-Host ""
    Read-Host "Press ENTER to return to menu..."
    Show-Menu
}

Show-Menu

