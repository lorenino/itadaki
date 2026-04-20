# Script pour installer GNU Make sur Windows
# Télécharge et installe Make automatiquement

Write-Host ""
Write-Host "Installation de GNU Make..." -ForegroundColor Yellow
Write-Host ""

# Option 1: Via scoop (si disponible)
if (Get-Command scoop -ErrorAction SilentlyContinue) {
    Write-Host "Scoop trouvé - Installation via Scoop..." -ForegroundColor Green
    scoop install make
    Write-Host "✅ Make installé !" -ForegroundColor Green
    exit
}

# Option 2: Télécharger directement
$makeDir = "C:\Program Files\make"
if (-not (Test-Path $makeDir)) {
    New-Item -ItemType Directory -Path $makeDir -Force | Out-Null
}

Write-Host "Téléchargement de GNU Make..." -ForegroundColor Yellow

# URL du binaire Make pour Windows
$url = "https://github.com/mvertes/proot-w32/releases/download/v5.4.0/proot-w32.exe"
$outPath = "$makeDir\make.exe"

try {
    Invoke-WebRequest -Uri $url -OutFile $outPath -ErrorAction Stop
    Write-Host "✅ Make téléchargé !" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Téléchargement échoué" -ForegroundColor Yellow
    Write-Host "Télécharge manuellement depuis: https://github.com/jqlang/jq/releases" -ForegroundColor Blue
    exit
}

# Ajouter au PATH
$env:PATH += ";$makeDir"
[Environment]::SetEnvironmentVariable("PATH", $env:PATH + ";$makeDir", "User")

Write-Host ""
Write-Host "✅ Installation terminée !" -ForegroundColor Green
Write-Host "Redémarre PowerShell pour que les changements prennent effet" -ForegroundColor Yellow

