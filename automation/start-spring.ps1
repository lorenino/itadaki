# Start Spring Boot

# Get project root dynamically
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

if (-not (Test-Path "$ProjectRoot\pom.xml")) {
    Write-Host "ERROR: pom.xml not found at $ProjectRoot" -ForegroundColor Red
    exit 1
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: Maven not found" -ForegroundColor Red
    Write-Host "Download from: https://maven.apache.org/download.cgi" -ForegroundColor Blue
    exit 1
}

Write-Host "Starting Spring Boot..." -ForegroundColor Green
Write-Host "Project root: $ProjectRoot" -ForegroundColor Blue
Write-Host ""

Push-Location $ProjectRoot
mvn spring-boot:run
Pop-Location

