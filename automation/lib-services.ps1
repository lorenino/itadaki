# ITADAKI - Service launchers, tab helper, URL injection, display helpers
# Dot-sourced by menu.ps1 (after lib-detect.ps1)

$WtExe = "$LocalAppData\Microsoft\WindowsApps\wt.exe"

# --- Tab/window launcher helper ---

function Open-Tab {
    param([string]$Title, [string]$Command)

    # Write command to a temp .ps1 file - avoids all argument escaping issues
    $tempFile = "$env:TEMP\itadaki-$($Title.ToLower() -replace '\s+','-').ps1"
    Set-Content -Path $tempFile -Value $Command -Encoding UTF8

    if (Test-Path $WtExe) {
        # -w 0 = open in the first WT window (creates one if none exists)
        & $WtExe -w 0 new-tab --title $Title powershell -NoProfile -ExecutionPolicy Bypass -NoExit -File $tempFile
    } else {
        Start-Process powershell -ArgumentList "-NoProfile", "-ExecutionPolicy", "Bypass", "-NoExit", "-File", $tempFile
    }
    Start-Sleep -Milliseconds 500
}

# --- application.properties updater ---

function Update-OllamaUrl {
    param([string]$Url)
    $propsFile = "$ProjectRoot\src\main\resources\application.properties"
    if (-not (Test-Path $propsFile)) {
        Write-Host "WARNING: application.properties not found at $propsFile" -ForegroundColor Yellow
        return
    }
    $content = Get-Content $propsFile -Raw
    $updated = $content -replace '(?m)^spring\.ai\.ollama\.base-url=.*$', "spring.ai.ollama.base-url=`${OLLAMA_URL:$Url}"
    Set-Content -Path $propsFile -Value $updated -Encoding UTF8 -NoNewline
    Write-Host "  application.properties -> spring.ai.ollama.base-url set to: $Url" -ForegroundColor Cyan
}

# --- Resource warning ---

function Show-ResourceWarning {
    $freeRam = Get-FreeRamGB
    if ($freeRam -ne $null -and $freeRam -lt 13) {
        Write-Host ""
        Write-Host "  MEMORY WARNING:" -ForegroundColor Red
        Write-Host "  qwen2.5vl:7b needs ~12.5 GB RAM to run on CPU." -ForegroundColor Red
        Write-Host "  Free RAM detected: ${freeRam} GB" -ForegroundColor Red
        Write-Host "  Consider using mode 2 (APP ONLY) with a teammate's ngrok URL instead." -ForegroundColor Yellow
        Write-Host ""
    }
}

# --- Service launchers ---

function Open-OllamaWindow {
    $ollamaPath = Get-OllamaPath
    if (-not $ollamaPath) {
        Write-Host "ERROR: Ollama not found. Download from https://ollama.com/download" -ForegroundColor Red
        return $false
    }

    Show-ResourceWarning

    if (-not (Test-ModelDownloaded)) {
        Write-Host ""
        Write-Host "  WARNING: model qwen2.5vl:7b is not downloaded!" -ForegroundColor Red
        Write-Host "  Ollama will start but every AI request will return 404." -ForegroundColor Red
        Write-Host "  Run option 5 (DOWNLOAD MODEL) first." -ForegroundColor Yellow
        Write-Host ""
        $cont = Read-Host "  Start anyway? (Y/n)"
        if ($cont -eq "n" -or $cont -eq "N") { return $false }
    }

    $ollamaDir = Split-Path -Parent $ollamaPath
    $cmd = @"
`$env:OLLAMA_HOST         = '0.0.0.0:11434'
`$env:OLLAMA_NUM_PARALLEL  = '1'
`$env:OLLAMA_MAX_LOADED_MODELS = '1'
`$env:OLLAMA_LOAD_TIMEOUT = '10m0s'
`$env:PATH = '$ollamaDir;' + `$env:PATH
Write-Host 'Ollama (CPU fallback enabled)' -ForegroundColor Cyan
Write-Host 'Note: qwen2.5vl:7b needs ~12.5 GB RAM - loading may take 1-2 min' -ForegroundColor Yellow
& '$ollamaPath' serve
"@
    Open-Tab -Title "Ollama" -Command $cmd
    Write-Host "Ollama tab opened." -ForegroundColor Green
    Start-Sleep -Seconds 3
    return $true
}

function Open-NgrokWindow {
    $ngrokPath = Get-NgrokPath
    if (-not $ngrokPath) {
        Write-Host "ERROR: ngrok not found." -ForegroundColor Red
        return $false
    }
    $cmd = @"
Write-Host 'ngrok' -ForegroundColor Cyan
& '$ngrokPath' http 11434
"@
    Open-Tab -Title "ngrok" -Command $cmd
    Write-Host "ngrok tab opened." -ForegroundColor Green
    return $true
}

function Ask-NgrokUrl {
    Write-Host ""
    Write-Host "Copy the https://... URL displayed in the ngrok window." -ForegroundColor Cyan
    $url = Read-Host "Paste ngrok URL here (ENTER to use localhost)"
    $url = $url.Trim()
    if ($url -eq "") { return $null }
    return $url
}

function Open-SpringWindow {
    param([string]$OllamaUrl = "")

    if (-not (Test-Path "$ProjectRoot\pom.xml")) {
        Write-Host "ERROR: pom.xml not found at $ProjectRoot" -ForegroundColor Red
        return $false
    }
    $mvnCmd = Get-MvnCommand
    if (-not $mvnCmd) {
        Write-Host "ERROR: Maven not found." -ForegroundColor Red
        return $false
    }

    $java25Home = Get-Java25Path
    $javaSetup  = if ($java25Home) { "`$env:JAVA_HOME='$java25Home'; `$env:PATH='$java25Home\bin;' + `$env:PATH; " } else { "" }
    $ollamaSetup = if ($OllamaUrl) { "`$env:OLLAMA_URL='$OllamaUrl'; " } else { "" }

    $cmd = @"
$javaSetup
$ollamaSetup
Write-Host 'Spring Boot' -ForegroundColor Cyan
Set-Location '$ProjectRoot'
& '$mvnCmd' spring-boot:run
"@
    Open-Tab -Title "Spring Boot" -Command $cmd
    Write-Host "Spring Boot tab opened." -ForegroundColor Green
    return $true
}

# --- Summary ---

function Show-Summary {
    param(
        [string]$NgrokUrl    = "",
        [bool]$SpringStarted = $false,
        [bool]$OllamaStarted = $false
    )
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Instances launched:" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    if ($SpringStarted) {
        Write-Host "  App URL        : http://localhost:8080" -ForegroundColor Cyan
        Write-Host "  Swagger API    : http://localhost:8080/swagger-ui/index.html" -ForegroundColor Cyan
        Write-Host "  H2 Console     : http://localhost:8080/h2-console" -ForegroundColor Cyan
    }
    if ($NgrokUrl -ne "") {
        Write-Host "  ngrok URL      : $NgrokUrl" -ForegroundColor Cyan
    }
    if ($OllamaStarted) {
        Write-Host "  Local model    : http://localhost:11434" -ForegroundColor Cyan
    }
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
}
