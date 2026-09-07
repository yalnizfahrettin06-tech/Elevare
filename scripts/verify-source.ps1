param()
$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $PSScriptRoot
function Assert-Check([bool]$Condition, [string]$Label) {
 if (-not $Condition) { throw "FAIL: $Label" }
 Write-Output "PASS: $Label"
}
$catalogPath = Join-Path $projectPath 'app/src/main/assets/science-cards.json'
$catalog = Get-Content -LiteralPath $catalogPath -Raw -Encoding UTF8 | ConvertFrom-Json
Assert-Check (@($catalog.facts).Count -eq 60) '60 facts'
Assert-Check (@($catalog.sources).Count -eq 12) '12 sources'
Assert-Check (@($catalog.facts.id | Select-Object -Unique).Count -eq 60) 'Unique fact IDs'
Assert-Check (@($catalog.sources.id | Select-Object -Unique).Count -eq 12) 'Unique source IDs'
foreach ($fact in $catalog.facts) {
 Assert-Check ($fact.sourceId -in $catalog.sources.id) "$($fact.id): source exists"
 Assert-Check ($fact.minAge -ge 13 -and $fact.maxAge -le 21 -and $fact.minAge -le $fact.maxAge) "$($fact.id): age range"
 Assert-Check (-not [string]::IsNullOrWhiteSpace($fact.text)) "$($fact.id): text"
}
foreach ($source in $catalog.sources) {
 Assert-Check ($source.url -match '^https://(pubmed.ncbi.nlm.nih.gov|www.endocrine.org)/') "$($source.id): primary source URL"
 Assert-Check (-not [string]::IsNullOrWhiteSpace($source.population) -and -not [string]::IsNullOrWhiteSpace($source.summary) -and -not [string]::IsNullOrWhiteSpace($source.limit)) "$($source.id): population, finding and limitation"
 if ($source.pmid) { Assert-Check ($source.url -eq "https://pubmed.ncbi.nlm.nih.gov/$($source.pmid)/") "$($source.id): PMID matches URL" }
}
$srcPath = Join-Path $projectPath 'app/src/main'
$kotlin = @(Get-ChildItem -LiteralPath (Join-Path $srcPath 'java') -Recurse -Filter '*.kt')
$allSource = ($kotlin | ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8 }) -join [Environment]::NewLine
Assert-Check ($allSource -notmatch 'sports_cover|coach_poses|imageResource|drawImage') 'No old bitmap coach or photo rendering'
Assert-Check ($allSource -match 'val ages=13\.\.21') 'Age choices 13 through 21'
Assert-Check ($allSource -match 'val minutes=listOf\(5,10,15,30\)') 'Time choices 5, 10, 15, 30'
Assert-Check ($allSource -match 'if \(!answers.complete\(\)\) return') 'Required final answer validation'
Assert-Check ($allSource -match 'PREPARATION_DURATION_MS=30_000L') '30 second preparation'
Assert-Check ($allSource -match 'const val TRIAL_DAYS=3') 'Three day trial constant'
Assert-Check ($allSource -notmatch '7 gün ücretsiz|7 günlük demo') 'No seven day trial UI'
Assert-Check ($allSource -match 'isNetworkConnectionRequired') 'Offline Turkish TTS selection'
Assert-Check ($allSource -match 'ACTION_AUDIO_BECOMING_NOISY') 'Headphone disconnect handling'
Assert-Check ($allSource -match 'val pose=motionFrame') 'Canvas uses phase-based line geometry'
$onboarding = Get-Content -LiteralPath (Join-Path $srcPath 'java/com/elevare/active/OnboardingV5.kt') -Raw -Encoding UTF8
Assert-Check ($onboarding -notmatch 'heightCm|targetCm|KeyboardType.Number') 'No current or target height onboarding fields'
$xmlFiles = @(Get-ChildItem -LiteralPath $srcPath -Recurse -Filter '*.xml')
foreach ($file in $xmlFiles) {
 $null = [xml](Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8)
}
Assert-Check ($xmlFiles.Count -gt 0) 'All Android XML files parsed'
$manifest = Get-Content -LiteralPath (Join-Path $srcPath 'AndroidManifest.xml') -Raw
Assert-Check ($manifest -notmatch 'android.permission.(RECORD_AUDIO|INTERNET)') 'No microphone or network permission'
$tests = (Get-ChildItem -LiteralPath (Join-Path $projectPath 'app/src/test') -Recurse -Filter '*.kt' | ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw }) -join [Environment]::NewLine
Assert-Check ([regex]::Matches($tests, '@Test').Count -ge 28) 'JUnit tests present (not executed by this script)'
Write-Output 'SOURCE CHECK ONLY: no Kotlin compilation, JUnit execution, emulator, APK or AAB build.'
