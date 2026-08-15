$ErrorActionPreference = "Stop"

$projectRoot = "C:/Users/vivo9/Desktop/Minecraft mod/FtbQshop-0.1"
$outputDir = "$projectRoot/build/libs"
$distDir = "C:/Users/vivo9/Desktop/Minecraft mod"

if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Building LING Quest Shop Multi-Loader " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Compile Java Source Files
Write-Host "`n[1/3] Compiling Java source files..." -ForegroundColor Yellow
& powershell -ExecutionPolicy Bypass -File "$projectRoot/scripts/compile_mod.ps1"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Java compilation failed!"
    exit 1
}

# 2. Package NeoForge JAR
Write-Host "`n[2/3] Packaging NeoForge 1.21.1 JAR..." -ForegroundColor Yellow
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

$nfJar = "$outputDir/ling_q_shop-neoforge-1.21.1.jar"
if (Test-Path $nfJar) { Remove-Item -Force $nfJar }
Set-Location $tempNf
jar cvfm $nfJar "META-INF/MANIFEST.MF" . | Out-Null

Write-Host "  -> Created: $nfJar" -ForegroundColor Green

# 3. Package Forge JAR
Write-Host "`n[3/3] Packaging Forge 1.21.1 JAR..." -ForegroundColor Yellow
$tempFg = "$projectRoot/build/temp_forge"
if (Test-Path $tempFg) { Remove-Item -Recurse -Force $tempFg }
New-Item -ItemType Directory -Path $tempFg | Out-Null

Copy-Item -Recurse "$projectRoot/assets" "$tempFg/"
Copy-Item -Recurse "$projectRoot/com" "$tempFg/"
Copy-Item -Recurse "$projectRoot/data" "$tempFg/"
Copy-Item "$projectRoot/logo.png" "$tempFg/"

New-Item -ItemType Directory -Path "$tempFg/META-INF" -Force | Out-Null
Copy-Item "$projectRoot/META-INF/MANIFEST.MF" "$tempFg/META-INF/"
Copy-Item "$projectRoot/META-INF/mods.toml" "$tempFg/META-INF/"

$fgJar = "$outputDir/ling_q_shop-forge-1.21.1.jar"
if (Test-Path $fgJar) { Remove-Item -Force $fgJar }
Set-Location $tempFg
jar cvfm $fgJar "META-INF/MANIFEST.MF" . | Out-Null

Write-Host "  -> Created: $fgJar" -ForegroundColor Green

# Output to Distribution Directory
Copy-Item $nfJar "$distDir/ling_q_shop-neoforge-1.21.1.jar" -Force
Copy-Item $fgJar "$distDir/ling_q_shop-forge-1.21.1.jar" -Force

# Cleanup temp build folders
Remove-Item -Recurse -Force $tempNf -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force $tempFg -ErrorAction SilentlyContinue
Set-Location $projectRoot

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " MULTI-LOADER BUILD SUCCESSFUL! " -ForegroundColor Green
Write-Host " 1. $distDir/ling_q_shop-neoforge-1.21.1.jar" -ForegroundColor White
Write-Host " 2. $distDir/ling_q_shop-forge-1.21.1.jar" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
