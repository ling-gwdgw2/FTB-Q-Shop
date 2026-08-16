$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
Set-Location $projectRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Building LING Quest Shop (NeoForge)   " -ForegroundColor Cyan
Write-Host " Version: 1.3 | MC: 1.21.1             " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

python "$projectRoot/compile_neoforge.py"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed!"
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " BUILD SUCCESSFUL!                     " -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
