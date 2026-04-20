# ITADAKI - Interactive launch menu
# Entry point: dot-sources lib-detect.ps1 and lib-services.ps1

. "$PSScriptRoot\lib-detect.ps1"
. "$PSScriptRoot\lib-services.ps1"

# --- Prerequisite check ---

Clear-Host
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "     ITADAKI - Checking prerequisites  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  User:    $env:USERNAME" -ForegroundColor DarkGray
Write-Host "  Project: $ProjectRoot" -ForegroundColor DarkGray
Write-Host ""

$javaOk   = $false
$mavenOk  = $false
$ollamaOk = $false
$ngrokOk  = $false

# Java 25 - REQUIRED
Write-Host "  Java 25+  " -NoNewline
$java25Path = Get-Java25Path
if ($java25Path) {
    Write-Host "OK ($java25Path)" -ForegroundColor Green
    $javaOk = $true
} else {
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $javaVerStr = (& java -version 2>&1) | Select-Object -First 1
        Write-Host "wrong version in PATH: $javaVerStr" -ForegroundColor Red
    } else {
        Write-Host "MISSING (required)" -ForegroundColor Red
    }
    Write-Host "           -> https://www.oracle.com/java/technologies/downloads/" -ForegroundColor DarkGray
}

# Maven / mvnw.cmd - REQUIRED
Write-Host "  Maven...   " -NoNewline
if (Test-Path "$ProjectRoot\mvnw.cmd") {
    Write-Host "OK (mvnw.cmd wrapper in repo)" -ForegroundColor Green
    $mavenOk = $true
} elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "OK (system Maven)" -ForegroundColor Green
    $mavenOk = $true
} else {
    Write-Host "not found" -ForegroundColor Yellow
    $install = Read-Host "  Install Maven 3.9.14 automatically? (Y/n)"
    if ($install -ne "n" -and $install -ne "N") { $mavenOk = Install-MavenAuto }
    else { Write-Host "  Manual: https://maven.apache.org/download.cgi" -ForegroundColor DarkGray }
}

# Ollama - needed for modes 1, 3, 4
Write-Host "  Ollama...  " -NoNewline
if (Get-OllamaPath) {
    Write-Host "OK" -ForegroundColor Green; $ollamaOk = $true
} else {
    Write-Host "not found  (needed for modes 1, 3, 4)" -ForegroundColor Yellow
    Write-Host "           -> https://ollama.com/download" -ForegroundColor DarkGray
}

# ngrok - needed for modes 3, 4
Write-Host "  ngrok...   " -NoNewline
if (Get-NgrokPath) {
    Write-Host "OK" -ForegroundColor Green; $ngrokOk = $true
} else {
    Write-Host "not found  (needed for modes 3, 4)" -ForegroundColor Yellow
    Write-Host ""
    $installNgrok = Read-Host "  Install ngrok automatically? (Y/n)"
    if ($installNgrok -ne "n" -and $installNgrok -ne "N") { $ngrokOk = Install-NgrokAuto }
    else {
        Write-Host "  Manual: https://ngrok.com/download/windows" -ForegroundColor DarkGray
        Write-Host "  Token:  https://dashboard.ngrok.com/get-started/your-authtoken" -ForegroundColor DarkGray
    }
}

Write-Host ""

if (-not $javaOk -or -not $mavenOk) {
    Write-Host "Java 25 and Maven are required. Please install them and run again." -ForegroundColor Red
    Read-Host "Press ENTER to exit"
    exit 1
}

if (-not $ollamaOk -or -not $ngrokOk) {
    Write-Host "Some optional tools are missing - those modes will not work." -ForegroundColor Yellow
    Write-Host ""
    $cont = Read-Host "Continue to menu anyway? (Y/n)"
    if ($cont -eq "n" -or $cont -eq "N") { exit 0 }
    Write-Host ""
}

# --- Menu ---

function Show-Menu {
    Clear-Host
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "         ITADAKI - Launch Menu         " -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  1  - LOCAL        (Ollama + Spring Boot)" -ForegroundColor Blue
    Write-Host "  2  - APP ONLY     (Spring Boot only)" -ForegroundColor Blue
    Write-Host "  3  - OLLAMA+NGROK (share GPU with team)" -ForegroundColor Blue
    Write-Host "  4  - FULL         (Ollama + ngrok + Spring Boot)" -ForegroundColor Blue
    Write-Host "  5  - DOWNLOAD MODEL" -ForegroundColor Blue
    Write-Host "  0  - EXIT" -ForegroundColor Red
    Write-Host ""

    $choice = Read-Host "Choose option (0-5)"
    switch ($choice) {
        "1" { Run-Local }
        "2" { Run-AppOnly }
        "3" { Run-OllamaAndNgrok }
        "4" { Run-Full }
        "5" { Download-Model }
        "0" { exit }
        default { Show-Menu }
    }
}

function Run-Local {
    Clear-Host
    Write-Host ""
    Write-Host "=== LOCAL MODE ===" -ForegroundColor Green
    Write-Host ""
    if (-not (Open-OllamaWindow)) { Read-Host "Press ENTER to return to menu"; Show-Menu; return }
    Write-Host ""
    Open-SpringWindow -OllamaUrl "http://localhost:11434"
    Show-Summary -SpringStarted $true -OllamaStarted $true
    Read-Host "Press ENTER to return to menu..."
    Show-Menu
}

function Run-AppOnly {
    Clear-Host
    Write-Host ""
    Write-Host "=== APP ONLY MODE ===" -ForegroundColor Green
    Write-Host ""
    Write-Host "Someone on the team must have Ollama running and exposed via ngrok." -ForegroundColor Yellow
    Write-Host "Ask them for their ngrok URL and paste it below." -ForegroundColor Yellow
    Write-Host ""
    $ngrokUrl = Read-Host "Paste ngrok URL here (ENTER to keep current value in application.properties)"
    $ngrokUrl = $ngrokUrl.Trim()

    if ($ngrokUrl -ne "") {
        Update-OllamaUrl -Url $ngrokUrl
        Open-SpringWindow -OllamaUrl $ngrokUrl
        Show-Summary -SpringStarted $true -NgrokUrl $ngrokUrl
    } else {
        Write-Host "No URL pasted - using existing value from application.properties." -ForegroundColor DarkGray
        Open-SpringWindow
        Show-Summary -SpringStarted $true
    }
    Read-Host "Press ENTER to return to menu..."
    Show-Menu
}

function Run-OllamaAndNgrok {
    Clear-Host
    Write-Host ""
    Write-Host "=== OLLAMA + NGROK MODE ===" -ForegroundColor Green
    Write-Host ""
    if (-not (Open-OllamaWindow)) { Read-Host "Press ENTER to return to menu"; Show-Menu; return }
    Write-Host ""
    if (-not (Open-NgrokWindow)) { Read-Host "Press ENTER to return to menu"; Show-Menu; return }

    $ngrokUrl = Ask-NgrokUrl
    $ngrokParam = if ($ngrokUrl) { $ngrokUrl } else { "" }
    Show-Summary -OllamaStarted $true -NgrokUrl $ngrokParam
    if ($ngrokUrl) { Write-Host "  Share the ngrok URL with your team!" -ForegroundColor Yellow }
    Read-Host "Press ENTER to return to menu..."
    Show-Menu
}

function Run-Full {
    Clear-Host
    Write-Host ""
    Write-Host "=== FULL MODE ===" -ForegroundColor Green
    Write-Host ""
    if (-not (Open-OllamaWindow)) { Read-Host "Press ENTER to return to menu"; Show-Menu; return }
    Write-Host ""
    if (-not (Open-NgrokWindow)) { Read-Host "Press ENTER to return to menu"; Show-Menu; return }

    $ngrokUrl  = Ask-NgrokUrl
    $ollamaUrl = if ($ngrokUrl) { $ngrokUrl } else { "http://localhost:11434" }

    if ($ngrokUrl) { Update-OllamaUrl -Url $ngrokUrl }
    Open-SpringWindow -OllamaUrl $ollamaUrl
    $ngrokParam = if ($ngrokUrl) { $ngrokUrl } else { "" }
    Show-Summary -SpringStarted $true -OllamaStarted $true -NgrokUrl $ngrokParam
    Read-Host "Press ENTER to return to menu..."
    Show-Menu
}

function Download-Model {
    Clear-Host
    Write-Host ""
    Write-Host "=== DOWNLOAD MODEL ===" -ForegroundColor Green
    Write-Host ""
    $ollamaPath = Get-OllamaPath
    if (-not $ollamaPath) {
        Write-Host "ERROR: Ollama not found." -ForegroundColor Red
        Read-Host "Press ENTER to return to menu"
        Show-Menu; return
    }
    $ollamaDir = Split-Path -Parent $ollamaPath
    $env:PATH = "$ollamaDir;$env:PATH"
    Write-Host "Downloading model qwen2.5vl:7b (~6 GB, 10-15 min)..." -ForegroundColor Yellow
    Write-Host ""
    & $ollamaPath pull qwen2.5vl:7b
    if ($LASTEXITCODE -eq 0) { Write-Host "Model downloaded successfully!" -ForegroundColor Green }
    else { Write-Host "ERROR: Model download failed." -ForegroundColor Red }
    Write-Host ""
    Read-Host "Press ENTER to return to menu"
    Show-Menu
}

Show-Menu
