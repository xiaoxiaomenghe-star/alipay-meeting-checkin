param([switch]$SkipVerification)

$ErrorActionPreference = 'Stop'
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$envFile = Join-Path $projectRoot '.env'
$exampleFile = Join-Path $projectRoot '.env.example'

function New-RandomSecret {
    param([int]$ByteCount = 24)
    $bytes = New-Object byte[] $ByteCount
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    return [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

if (-not (Test-Path -LiteralPath $envFile)) {
    $content = Get-Content -LiteralPath $exampleFile -Encoding UTF8 -Raw
    $content = $content.Replace('CHANGE_ME_MYSQL_PASSWORD', (New-RandomSecret 18))
    $content = $content.Replace('CHANGE_ME_MYSQL_ROOT_PASSWORD', (New-RandomSecret 18))
    $content = $content.Replace('CHANGE_ME_RABBITMQ_PASSWORD', (New-RandomSecret 18))
    $content = $content.Replace('CHANGE_ME_AUTH_SECRET_AT_LEAST_32_CHARS', (New-RandomSecret 32))
    $content = $content.Replace('CHANGE_ME_ENTRY_SECRET_AT_LEAST_32_CHARS', (New-RandomSecret 32))
    [System.IO.File]::WriteAllText($envFile, $content, (New-Object System.Text.UTF8Encoding($false)))
    Write-Host 'Created local .env with generated demo secrets.'
}

Push-Location $projectRoot
try {
    $maven = Get-Command mvn -ErrorAction SilentlyContinue
    if (-not $maven) { throw 'Maven is required to build the backend JAR' }
    & $maven.Source -B -q -f (Join-Path $projectRoot 'server\pom.xml') clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw 'Maven package failed' }

    docker compose up --build -d
    if ($LASTEXITCODE -ne 0) { throw 'docker compose up failed' }

    $portLine = Get-Content -LiteralPath $envFile -Encoding UTF8 | Where-Object { $_ -match '^HTTP_PORT=' } | Select-Object -First 1
    $httpPort = if ($portLine) { $portLine.Split('=', 2)[1] } else { '8088' }
    $baseUrl = "http://localhost:$httpPort"
    $deadline = (Get-Date).AddMinutes(3)
    do {
        try {
            $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health/readiness" -TimeoutSec 3
        } catch {
            $health = $null
        }
        if ($health.status -eq 'UP') { break }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    if ($health.status -ne 'UP') {
        docker compose ps
        throw 'Application readiness check timed out'
    }

    if (-not $SkipVerification) {
        & (Join-Path $PSScriptRoot 'verify-compose.ps1') -BaseUrl $baseUrl
    }
    Write-Host "Local demo is ready at $baseUrl"
} finally {
    Pop-Location
}
