# Script pour lancer Spring Boot
# Usage: . automation/start-spring.ps1

$PROJECT_PATH = "C:\Users\letai\OneDrive\Bureau\ESGI\Hackathon\itadaki"

Write-Host "Lancement Spring Boot..." -ForegroundColor Green
Push-Location $PROJECT_PATH
mvn spring-boot:run
Pop-Location

