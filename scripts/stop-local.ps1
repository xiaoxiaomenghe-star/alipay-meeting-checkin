$ErrorActionPreference = 'Stop'
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Push-Location $projectRoot
try {
    docker compose down
} finally {
    Pop-Location
}
