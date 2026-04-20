# Script d'installation des prérequis
# Lance ce script une seule fois pour tout installer

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  ITADAKI - Installation des Prérequis                          ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check Java
Write-Host "1. Vérification Java..." -ForegroundColor Yellow
$java = java -version 2>&1 | Select-String "version"
if ($java) {
    Write-Host "   ✅ Java installé: $java" -ForegroundColor Green
} else {
    Write-Host "   ❌ Java NON installé" -ForegroundColor Red
    Write-Host "   👉 Télécharge: https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Blue
}

# Check Maven
Write-Host ""
Write-Host "2. Vérification Maven..." -ForegroundColor Yellow
$mvn = mvn -version 2>&1 | Select-String "Apache Maven"
if ($mvn) {
    Write-Host "   ✅ Maven installé" -ForegroundColor Green
} else {
    Write-Host "   ❌ Maven NON installé" -ForegroundColor Red
    Write-Host "   👉 Télécharge: https://maven.apache.org/download.cgi" -ForegroundColor Blue
}

# Check Ollama
Write-Host ""
Write-Host "3. Vérification Ollama..." -ForegroundColor Yellow
$ollama = "C:\Users\letai\AppData\Local\Programs\Ollama\ollama.exe"
if (Test-Path $ollama) {
    $version = & $ollama --version
    Write-Host "   ✅ Ollama installé: $version" -ForegroundColor Green
} else {
    Write-Host "   ❌ Ollama NON installé" -ForegroundColor Red
    Write-Host "   👉 Télécharge: https://ollama.com/download" -ForegroundColor Blue
}

# Check ngrok
Write-Host ""
Write-Host "4. Vérification ngrok..." -ForegroundColor Yellow
$ngrok = "C:\ngrok\ngrok.exe"
if (Test-Path $ngrok) {
    Write-Host "   ✅ ngrok installé" -ForegroundColor Green
} else {
    Write-Host "   ❌ ngrok NON installé" -ForegroundColor Red
    Write-Host "   👉 Télécharge: https://ngrok.com/download" -ForegroundColor Blue
    Write-Host "      Extrait dans: C:\ngrok" -ForegroundColor Blue
}

# Install Make
Write-Host ""
Write-Host "5. Installation GNU Make..." -ForegroundColor Yellow

# Check if Make exists
if (Get-Command make -ErrorAction SilentlyContinue) {
    Write-Host "   ✅ GNU Make déjà installé" -ForegroundColor Green
} else {
    Write-Host "   ⏳ Téléchargement et installation de GNU Make..." -ForegroundColor Yellow

    # Créer répertoire
    $makeDir = "C:\Program Files\GNU Make"
    if (-not (Test-Path $makeDir)) {
        New-Item -ItemType Directory -Path $makeDir -Force | Out-Null
    }

    # Télécharger GNU Make pour Windows
    # Utiliser la version de GnuWin32
    $url = "https://sourceforge.net/projects/gnuwin32/files/make/3.81/make-3.81-bin.zip/download"
    $zipPath = "$env:TEMP\make-3.81-bin.zip"

    try {
        Write-Host "   📥 Téléchargement..." -ForegroundColor Blue
        Invoke-WebRequest -Uri $url -OutFile $zipPath -ErrorAction Stop

        Write-Host "   📦 Extraction..." -ForegroundColor Blue
        Expand-Archive -Path $zipPath -DestinationPath $makeDir -Force

        Write-Host "   ✅ GNU Make installé dans: $makeDir" -ForegroundColor Green

        # Ajouter au PATH
        $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
        if ($currentPath -notlike "*$makeDir*") {
            [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$makeDir\bin", "User")
            $env:PATH += ";$makeDir\bin"
            Write-Host "   ✅ Ajouté au PATH" -ForegroundColor Green
        }

        # Cleanup
        Remove-Item $zipPath -Force -ErrorAction SilentlyContinue

    } catch {
        Write-Host "   ⚠️  Téléchargement échoué" -ForegroundColor Yellow
        Write-Host "   👉 Télécharge manuellement: https://sourceforge.net/projects/gnuwin32/files/make/" -ForegroundColor Blue
    }
}

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  Installation terminée !                                       ║" -ForegroundColor Green
Write-Host "║  Redémarre PowerShell pour que les changements prennent effet  ║" -ForegroundColor Green
Write-Host "║                                                                ║" -ForegroundColor Green
Write-Host "║  Puis lance:  make run                                         ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

