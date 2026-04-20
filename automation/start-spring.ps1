# Start Spring Boot
$PROJECT_ROOT = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

Write-Host "Starting Spring Boot..." -ForegroundColor Green
Push-Location $PROJECT_ROOT
mvn spring-boot:run
Pop-Location

