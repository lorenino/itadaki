# Itadaki — Benchmark Ollama multimodal
#
# Usage :
#   .\benchmark.ps1 -ImagesDir "C:\itadaki-test" -Models @("qwen2.5vl:7b","gemma3:4b") -OutputCsv results.csv
#
# Pour chaque (image x modele), appelle Ollama et mesure :
#   - latence en secondes
#   - validite JSON
#   - champs extraits (nomPlat, calories, confiance)
#
# Sortie : CSV + affichage console recap.

param(
    [Parameter(Mandatory=$true)]
    [string]$ImagesDir,

    [string[]]$Models = @("qwen2.5vl:7b", "gemma3:4b"),

    [string]$OutputCsv = "results.csv",

    [string]$OllamaHost = "http://localhost:11434",

    [int]$TimeoutSec = 300
)

$ErrorActionPreference = "Continue"

$SystemPrompt = @"
You are a nutritionist analyzing a photo of a plate/dish.

Return ONLY valid JSON matching this exact schema (no prose, no markdown, no code fences):
{
  "nomPlat": string,
  "ingredients": string[],
  "portion": string,
  "caloriesMin": integer,
  "caloriesMax": integer,
  "confiance": string
}

Rules:
- JSON only. No explanation.
- nomPlat and ingredients MUST be in French.
- portion must be one of: petit, moyen, grand.
- confiance must be one of: haute, moyenne, basse.
- caloriesMin < caloriesMax, positive integers.
- If no dish visible: {"nomPlat":"inconnu","ingredients":[],"portion":"moyen","caloriesMin":0,"caloriesMax":0,"confiance":"basse"}.
"@

$UserPrompt = "Identifie ce plat, liste les ingredients visibles et estime les calories."

# --- Collect images ---
if (-not (Test-Path $ImagesDir)) {
    Write-Error "Dossier introuvable : $ImagesDir"
    exit 1
}

$images = Get-ChildItem -Path $ImagesDir -Include *.jpg,*.jpeg,*.png,*.webp -Recurse -File
if ($images.Count -eq 0) {
    Write-Error "Aucune image trouvee dans $ImagesDir"
    exit 1
}

Write-Host "Images trouvees : $($images.Count)" -ForegroundColor Cyan
Write-Host "Modeles a tester : $($Models -join ', ')" -ForegroundColor Cyan
Write-Host ""

# --- Warm-up des modeles ---
foreach ($model in $Models) {
    Write-Host "Warm-up $model ..." -ForegroundColor Yellow
    try {
        $warmupBody = @{
            model = $model
            messages = @(@{ role = "user"; content = "ok" })
            stream = $false
        } | ConvertTo-Json -Depth 5
        Invoke-RestMethod -Uri "$OllamaHost/api/chat" -Method Post -Body $warmupBody -ContentType "application/json" -TimeoutSec 120 | Out-Null
    } catch {
        Write-Warning "Warm-up $model a echoue : $_"
    }
}
Write-Host ""

# --- Bench loop ---
$results = @()

foreach ($img in $images) {
    Write-Host "Image : $($img.Name)" -ForegroundColor Green

    try {
        $bytes = [System.IO.File]::ReadAllBytes($img.FullName)
        $b64 = [Convert]::ToBase64String($bytes)
    } catch {
        Write-Warning "  Echec lecture fichier : $_"
        continue
    }

    foreach ($model in $Models) {
        Write-Host "  - $model ... " -NoNewline

        $body = @{
            model = $model
            messages = @(
                @{ role = "system"; content = $SystemPrompt },
                @{ role = "user"; content = $UserPrompt; images = @($b64) }
            )
            stream = $false
            format = "json"
            options = @{
                temperature = 0.2
                num_ctx = 4096
                num_predict = 512
            }
        } | ConvertTo-Json -Depth 10

        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        $raw = $null
        $parsed = $null
        $jsonValide = $false
        $err = ""

        try {
            $response = Invoke-RestMethod -Uri "$OllamaHost/api/chat" -Method Post -Body $body -ContentType "application/json" -TimeoutSec $TimeoutSec
            $sw.Stop()
            $raw = $response.message.content
            try {
                $parsed = $raw | ConvertFrom-Json
                $jsonValide = $true
            } catch {
                $jsonValide = $false
                $err = "JSON parse error"
            }
        } catch {
            $sw.Stop()
            $err = "HTTP error: $_"
        }

        $latenceS = [math]::Round($sw.Elapsed.TotalSeconds, 2)

        $row = [PSCustomObject]@{
            image         = $img.Name
            model         = $model
            latence_s     = $latenceS
            json_valide   = $jsonValide
            nomPlat       = if ($parsed) { $parsed.nomPlat } else { "" }
            portion       = if ($parsed) { $parsed.portion } else { "" }
            caloriesMin   = if ($parsed -and $parsed.caloriesMin) { $parsed.caloriesMin } else { 0 }
            caloriesMax   = if ($parsed -and $parsed.caloriesMax) { $parsed.caloriesMax } else { 0 }
            confiance     = if ($parsed) { $parsed.confiance } else { "" }
            ingredients   = if ($parsed -and $parsed.ingredients) { ($parsed.ingredients -join '; ') } else { "" }
            raw_json      = if ($raw) { $raw.Replace("`n"," ").Replace("`r","") } else { "" }
            error         = $err
        }

        $results += $row

        if ($jsonValide) {
            Write-Host "OK  ($latenceS s, $($parsed.nomPlat), $($parsed.caloriesMin)-$($parsed.caloriesMax) kcal)" -ForegroundColor Green
        } else {
            Write-Host "ECHEC ($latenceS s, $err)" -ForegroundColor Red
        }
    }
}

# --- Export CSV ---
$results | Export-Csv -Path $OutputCsv -NoTypeInformation -Encoding UTF8
Write-Host ""
Write-Host "Resultats ecrits dans $OutputCsv" -ForegroundColor Cyan

# --- Resume console ---
Write-Host ""
Write-Host "=== RESUME ===" -ForegroundColor Cyan
foreach ($model in $Models) {
    $modelResults = $results | Where-Object { $_.model -eq $model }
    $total = $modelResults.Count
    $validJson = ($modelResults | Where-Object { $_.json_valide }).Count
    $avgLatence = if ($total -gt 0) {
        [math]::Round(($modelResults | Measure-Object -Property latence_s -Average).Average, 2)
    } else { 0 }

    Write-Host ""
    Write-Host "Modele : $model" -ForegroundColor Yellow
    Write-Host "  Total : $total"
    Write-Host "  JSON valide : $validJson / $total ($([math]::Round(($validJson/$total)*100,1))%)"
    Write-Host "  Latence moyenne : $avgLatence s"
}
