$ErrorActionPreference = "Stop"

# Version Configuration
$modVersion = "1.3"
$mcVersion  = "1.21.1"

$jarName = "ling_q_shop-v$modVersion-neoforge-$mcVersion.jar"

$projectRoot = "C:/Users/vivo9/Desktop/Minecraft mod/FtbQshop-0.1"
$outputDir   = "$projectRoot/build/libs"
$distDir     = "C:/Users/vivo9/Desktop/Minecraft mod"

if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Building LING Quest Shop (NeoForge)   " -ForegroundColor Cyan
Write-Host " Version: v$modVersion | MC: $mcVersion" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Compile Java Source Files
Write-Host "`n[1/2] Compiling Java source files..." -ForegroundColor Yellow
& powershell -ExecutionPolicy Bypass -File "$projectRoot/scripts/compile_mod.ps1"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Java compilation failed!"
    exit 1
}

# 2. Package NeoForge JAR
Write-Host "`n[2/2] Packaging ($jarName)..." -ForegroundColor Yellow
$tempNf = "$projectRoot/build/temp_neoforge"
if (Test-Path $tempNf) { Remove-Item -Recurse -Force $tempNf }
New-Item -ItemType Directory -Path $tempNf | Out-Null

Copy-Item -Recurse "$projectRoot/assets" "$tempNf/"
Copy-Item -Recurse "$projectRoot/com" "$tempNf/"
Copy-Item -Recurse "$projectRoot/data" "$tempNf/"
Copy-Item "$projectRoot/logo.png" "$tempNf/"

New-Item -ItemType Directory -Path "$tempNf/META-INF" -Force | Out-Null
Copy-Item "$projectRoot/META-INF/MANIFEST.MF" "$tempNf/META-INF/"
Copy-Item "$projectRoot/META-INF/neoforge.mods.toml" "$tempNf/META-INF/"

$nfJar = "$outputDir/$jarName"
if (Test-Path $nfJar) { Remove-Item -Force $nfJar }
Set-Location $tempNf
jar cvfm $nfJar "META-INF/MANIFEST.MF" . | Out-Null

# Output to Distribution Directory
Copy-Item $nfJar "$distDir/$jarName" -Force

# Cleanup temp build folder
Remove-Item -Recurse -Force $tempNf -ErrorAction SilentlyContinue
Set-Location $projectRoot

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " BUILD SUCCESSFUL! " -ForegroundColor Green
Write-Host " Output: $distDir/$jarName" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
