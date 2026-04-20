# First time setup script
# This runs automatically the first time

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$InitFile = "$ProjectRoot\.initialized"

# Check if already initialized
if (Test-Path $InitFile) {
    # Already initialized, just run the menu
    & "$ProjectRoot\automation\menu.ps1"
} else {
    # First time - check prerequisites
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "     ITADAKI - First Time Setup       " -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""

    $allOk = $true

    # Check Java
    Write-Host "Checking prerequisites..." -ForegroundColor Yellow
    Write-Host ""

    Write-Host "1. Java..." -ForegroundColor Blue
    if (Get-Command java -ErrorAction SilentlyContinue) {
        Write-Host "   OK" -ForegroundColor Green
    } else {
        Write-Host "   MISSING - Download: https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Red
        $allOk = $false
    }

    # Check Maven
    Write-Host "2. Maven..." -ForegroundColor Blue
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        Write-Host "   OK" -ForegroundColor Green
    } else {
        Write-Host "   MISSING - Download: https://maven.apache.org/download.cgi" -ForegroundColor Red
        $allOk = $false
    }

    # Check Ollama
    Write-Host "3. Ollama..." -ForegroundColor Blue
    $ollamaPaths = @(
        "C:\Users\$env:USERNAME\AppData\Local\Programs\Ollama\ollama.exe",
        "C:\Program Files\Ollama\ollama.exe"
    )
    $ollamaFound = $false
    foreach ($path in $ollamaPaths) {
        if (Test-Path $path) {
            Write-Host "   OK (found at $path)" -ForegroundColor Green
            $ollamaFound = $true
            break
        }
    }
    if (-not $ollamaFound) {
        Write-Host "   MISSING - Download: https://ollama.com/download" -ForegroundColor Red
        $allOk = $false
    }

    # Check ngrok
    Write-Host "4. ngrok..." -ForegroundColor Blue
    $ngrokPaths = @("C:\ngrok\ngrok.exe", "C:\Program Files\ngrok\ngrok.exe")
    $ngrokFound = $false
    foreach ($path in $ngrokPaths) {
        if (Test-Path $path) {
            Write-Host "   OK (found at $path)" -ForegroundColor Green
            $ngrokFound = $true
            break
        }
    }
    if (-not $ngrokFound) {
        Write-Host "   MISSING - Download: https://ngrok.com/download (extract to C:\ngrok)" -ForegroundColor Red
        $allOk = $false
    }

    Write-Host ""

    if ($allOk) {
        Write-Host "All prerequisites installed!" -ForegroundColor Green
        Write-Host ""

        # Mark as initialized
        New-Item -ItemType File -Path $InitFile -Force | Out-Null

        # Run the menu
        & "$ProjectRoot\automation\menu.ps1"
    } else {
        Write-Host "Please install missing prerequisites and run again:" -ForegroundColor Red
        Write-Host "  make run" -ForegroundColor Yellow
        Write-Host ""
    }
}

