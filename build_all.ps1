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
Write-Host "`n[1/4] Compiling Java source files..." -ForegroundColor Yellow
& powershell -ExecutionPolicy Bypass -File "C:/Users/vivo9/.gemini/antigravity-ide/brain/95dc99c2-9a1c-4c10-9321-9d12f1abe430/scratch/compile_mod.ps1"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Java compilation failed!"
    exit 1
}

# 2. Package NeoForge JAR
Write-Host "`n[2/4] Packaging NeoForge 1.21.1 JAR..." -ForegroundColor Yellow
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
Write-Host "`n[3/4] Packaging Forge 1.21.1 JAR..." -ForegroundColor Yellow
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

# 4. Copy to Distribution Directory and Test Instances
Write-Host "`n[4/4] Deploying artifacts..." -ForegroundColor Yellow
Copy-Item $nfJar "$distDir/ling_q_shop-neoforge-1.21.1.jar" -Force
Copy-Item $fgJar "$distDir/ling_q_shop-forge-1.21.1.jar" -Force

# Standard fallback name for existing NeoForge profiles
Copy-Item $nfJar "$distDir/ling_q_shop-1.2.jar" -Force
Copy-Item $nfJar "$projectRoot/ling_q_shop-1.2.jar" -Force

# Copy to CurseForge test instances
$test33Mods = "C:/Users/vivo9/curseforge/minecraft/Instances/test33/mods"
if (Test-Path $test33Mods) {
    Copy-Item $nfJar "$test33Mods/ling_q_shop-1.2.jar" -Force
    Copy-Item $nfJar "$test33Mods/ling_q_shop-neoforge-1.21.1.jar" -Force
}

$arsSkyMods = "C:/Users/vivo9/curseforge/minecraft/Instances/Ars Sky Island/mods"
if (Test-Path $arsSkyMods) {
    Copy-Item $nfJar "$arsSkyMods/ling_q_shop-1.2.jar" -Force
    Copy-Item $nfJar "$arsSkyMods/ling_q_shop-neoforge-1.21.1.jar" -Force
}

# Cleanup temp build folders
Remove-Item -Recurse -Force $tempNf -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force $tempFg -ErrorAction SilentlyContinue
Set-Location $projectRoot

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " MULTI-LOADER BUILD SUCCESSFUL! " -ForegroundColor Green
Write-Host " 1. $distDir/ling_q_shop-neoforge-1.21.1.jar" -ForegroundColor White
Write-Host " 2. $distDir/ling_q_shop-forge-1.21.1.jar" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
