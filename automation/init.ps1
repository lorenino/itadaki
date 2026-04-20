# Initialize project on first run
# Checks and installs prerequisites automatically

param(
    [switch]$Force
)

$ProjectRoot = Split-Path -Parent $PSScriptRoot

function Check-And-Install-Prerequisites {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "     ITADAKI - First Time Setup       " -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""

    $allOk = $true

    # Check Java
    Write-Host "Checking Java..." -ForegroundColor Yellow
    if (Get-Command java -ErrorAction SilentlyContinue) {
        $version = java -version 2>&1 | Select-String "version" | Select-Object -First 1
        Write-Host "  OK - $version" -ForegroundColor Green
    } else {
        Write-Host "  ERROR - Java not found" -ForegroundColor Red
        Write-Host "  Download: https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Blue
        $allOk = $false
    }

    # Check Maven
    Write-Host "Checking Maven..." -ForegroundColor Yellow
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        Write-Host "  OK - Maven installed" -ForegroundColor Green
    } else {
        Write-Host "  ERROR - Maven not found" -ForegroundColor Red
        Write-Host "  Download: https://maven.apache.org/download.cgi" -ForegroundColor Blue
        $allOk = $false
    }

    # Check Ollama
    Write-Host "Checking Ollama..." -ForegroundColor Yellow
    $ollamaPaths = @(
        "C:\Users\$env:USERNAME\AppData\Local\Programs\Ollama\ollama.exe",
        "C:\Program Files\Ollama\ollama.exe",
        "C:\Program Files (x86)\Ollama\ollama.exe"
    )

    $ollamaFound = $false
    foreach ($path in $ollamaPaths) {
        if (Test-Path $path) {
            Write-Host "  OK - Found at $path" -ForegroundColor Green
            $ollamaFound = $true
            break
        }
    }

    if (-not $ollamaFound) {
        Write-Host "  ERROR - Ollama not found" -ForegroundColor Red
        Write-Host "  Download: https://ollama.com/download" -ForegroundColor Blue
        $allOk = $false
    }

    # Check ngrok
    Write-Host "Checking ngrok..." -ForegroundColor Yellow
    $ngrokPaths = @(
        "C:\ngrok\ngrok.exe",
        "C:\Program Files\ngrok\ngrok.exe"
    )

    $ngrokFound = $false
    foreach ($path in $ngrokPaths) {
        if (Test-Path $path) {
            Write-Host "  OK - Found at $path" -ForegroundColor Green
            $ngrokFound = $true
            break
        }
    }

    if (-not $ngrokFound) {
        Write-Host "  ERROR - ngrok not found" -ForegroundColor Red
        Write-Host "  Download: https://ngrok.com/download" -ForegroundColor Blue
        Write-Host "  Extract to: C:\ngrok" -ForegroundColor Blue
        $allOk = $false
    }

    # Check GNU Make
    Write-Host "Checking GNU Make..." -ForegroundColor Yellow
    if (Get-Command make -ErrorAction SilentlyContinue) {
        Write-Host "  OK - GNU Make installed" -ForegroundColor Green
    } else {
        Write-Host "  WARNING - GNU Make not found" -ForegroundColor Yellow
        Write-Host "  You can use: powershell -ExecutionPolicy Bypass -File automation/menu.ps1" -ForegroundColor Blue
        Write-Host "  Or download GNU Make: https://sourceforge.net/projects/gnuwin32/files/make/" -ForegroundColor Blue
    }

    Write-Host ""

    if ($allOk) {
        Write-Host "All prerequisites OK!" -ForegroundColor Green
        Write-Host ""
        return $true
    } else {
        Write-Host "Please install missing prerequisites and run again." -ForegroundColor Red
        Write-Host ""
        return $false
    }
}

# Run check
if (Check-And-Install-Prerequisites) {
    Write-Host "Setup complete! You can now run: make run" -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host "Setup incomplete. Please install missing prerequisites." -ForegroundColor Red
    exit 1
}

