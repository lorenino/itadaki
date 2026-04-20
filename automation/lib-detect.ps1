# ITADAKI - Detection and installation helpers
# Dot-sourced by menu.ps1

$ProjectRoot   = Split-Path -Parent $PSScriptRoot
$AutomationDir = $PSScriptRoot

# Dynamic base paths - no hardcoded usernames or drive letters
$UserHome     = $env:USERPROFILE
$LocalAppData = $env:LOCALAPPDATA
$ProgFiles    = $env:PROGRAMFILES
$ProgFilesX86 = ${env:PROGRAMFILES(X86)}
$ProgData     = $env:PROGRAMDATA

# --- Path finders ---

function Get-Java25Path {
    $searchRoots = @(
        "$UserHome\.jdks",
        "$LocalAppData\Programs\Eclipse Adoptium",
        "$LocalAppData\Programs\Java",
        "$ProgFiles\Java",
        "$ProgFiles\Eclipse Adoptium",
        "$ProgFiles\Microsoft",
        "$ProgFiles\Zulu",
        "$ProgFiles\BellSoft",
        "$ProgFiles\Amazon Corretto",
        "$ProgFilesX86\Java"
    )
    foreach ($root in $searchRoots) {
        if (-not (Test-Path $root)) { continue }
        foreach ($dir in (Get-ChildItem $root -ErrorAction SilentlyContinue)) {
            $javaExe = Join-Path $dir.FullName "bin\java.exe"
            if (Test-Path $javaExe) {
                $verStr = (& $javaExe -version 2>&1) | Select-Object -First 1
                if ($verStr -match '"(\d+)' -and [int]$Matches[1] -ge 25) {
                    return $dir.FullName
                }
            }
        }
    }
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) {
        $verStr = (& java -version 2>&1) | Select-Object -First 1
        if ($verStr -match '"(\d+)' -and [int]$Matches[1] -ge 25) {
            return (Split-Path -Parent (Split-Path -Parent $cmd.Source))
        }
    }
    return $null
}

function Get-OllamaPath {
    $paths = @(
        "$LocalAppData\Programs\Ollama\ollama.exe",
        "$ProgFiles\Ollama\ollama.exe",
        "$ProgFilesX86\Ollama\ollama.exe"
    )
    foreach ($p in $paths) { if (Test-Path $p) { return $p } }
    $cmd = Get-Command ollama -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    return $null
}

function Get-NgrokPath {
    $paths = @(
        "$LocalAppData\Programs\ngrok\ngrok.exe",
        "$ProgData\chocolatey\bin\ngrok.exe",
        "$ProgFiles\ngrok\ngrok.exe",
        "C:\ngrok\ngrok.exe"
    )
    foreach ($p in $paths) { if (Test-Path $p) { return $p } }
    $cmd = Get-Command ngrok -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    return $null
}

function Get-MvnCommand {
    if (Test-Path "$ProjectRoot\mvnw.cmd") { return "$ProjectRoot\mvnw.cmd" }
    $cmd = Get-Command mvn -ErrorAction SilentlyContinue
    if ($cmd) { return "mvn" }
    return $null
}

function Get-FreeRamGB {
    $os = Get-CimInstance Win32_OperatingSystem -ErrorAction SilentlyContinue
    if ($os) { return [math]::Round($os.FreePhysicalMemory / 1MB, 1) }
    return $null
}

function Test-ModelDownloaded {
    $ollamaPath = Get-OllamaPath
    if (-not $ollamaPath) { return $false }
    $ollamaDir = Split-Path -Parent $ollamaPath
    $env:PATH = "$ollamaDir;$env:PATH"
    $models = & $ollamaPath list 2>&1
    return ($models | Select-String "qwen2.5vl:7b") -ne $null
}

# --- Installer helpers ---

function Install-NgrokAuto {
    $ngrokDir = "$LocalAppData\Programs\ngrok"
    try {
        $ngrokZip = "$env:TEMP\ngrok.zip"
        Write-Host "  Downloading ngrok..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri "https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-windows-amd64.zip" -OutFile $ngrokZip -UseBasicParsing
        if (-not (Test-Path $ngrokDir)) { New-Item -ItemType Directory -Path $ngrokDir -Force | Out-Null }
        Expand-Archive -Path $ngrokZip -DestinationPath $ngrokDir -Force
        Remove-Item $ngrokZip -Force
        $env:PATH = "$ngrokDir;$env:PATH"
        Write-Host "  ngrok installed to $ngrokDir" -ForegroundColor Green
        Write-Host ""
        Write-Host "  Get your authtoken: https://dashboard.ngrok.com/get-started/your-authtoken" -ForegroundColor Cyan
        $token = Read-Host "  Paste your authtoken (ENTER to skip)"
        if ($token -and $token.Trim() -ne "") {
            & "$ngrokDir\ngrok.exe" config add-authtoken $token.Trim()
            Write-Host "  Token configured." -ForegroundColor Green
        }
        return $true
    } catch {
        Write-Host "  Auto-install failed: $_" -ForegroundColor Red
        return $false
    }
}

function Install-MavenAuto {
    $mavenVersion = "3.9.14"
    $mavenDir = "$LocalAppData\Programs\maven"
    try {
        $mavenZip = "$env:TEMP\maven.zip"
        $mavenUrl = "https://downloads.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
        Write-Host "  Downloading Maven $mavenVersion..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -UseBasicParsing
        if (-not (Test-Path $mavenDir)) { New-Item -ItemType Directory -Path $mavenDir -Force | Out-Null }
        Expand-Archive -Path $mavenZip -DestinationPath $mavenDir -Force
        Remove-Item $mavenZip -Force
        $env:PATH = "$mavenDir\apache-maven-$mavenVersion\bin;$env:PATH"
        $env:MAVEN_HOME = "$mavenDir\apache-maven-$mavenVersion"
        Write-Host "  Maven installed to $mavenDir" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "  Maven install failed: $_" -ForegroundColor Red
        return $false
    }
}
