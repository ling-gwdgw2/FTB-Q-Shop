Add-Type -AssemblyName System.IO.Compression.FileSystem

$libDir = "C:/Users/vivo9/curseforge/minecraft/Install/libraries"

# Explicit client & neoforge jars with official Mojang mappings
$clientJars = @(
    "$libDir/net/neoforged/neoforge/21.1.244/neoforge-21.1.244-client.jar",
    "$libDir/net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-extra.jar",
    "$libDir/net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar",
    "$libDir/net/neoforged/neoforge/21.1.244/neoforge-21.1.244-universal.jar"
)

$otherJars = Get-ChildItem -Recurse $libDir -Filter "*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.FullName -notlike "*minecraftforge*" } | Select-Object -ExpandProperty FullName

$allCandidates = $clientJars + $otherJars
$validJars = [System.Collections.Generic.List[string]]::new()
$seen = [System.Collections.Generic.HashSet[string]]::new()

foreach ($j in $allCandidates) {
    if ($j -and (Test-Path $j) -and (-not $seen.Contains($j))) {
        $seen.Add($j) | Out-Null
        try {
            $zip = [System.IO.Compression.ZipFile]::OpenRead($j)
            $zip.Dispose()
            $validJars.Add($j.Replace("\", "/"))
        } catch {}
    }
}

$cp = $validJars -join ";"

$srcDir = "C:/Users/vivo9/Desktop/Minecraft mod/FtbQshop-0.1"
$javaFiles = Get-ChildItem -Recurse "$srcDir/com" -Filter "*.java" | Select-Object -ExpandProperty FullName | ForEach-Object { $_.Replace("\", "/") }

$argsList = [System.Collections.Generic.List[string]]::new()
$argsList.Add("-cp")
$argsList.Add("""$cp;$srcDir""")
$argsList.Add("-d")
$argsList.Add("""$srcDir""")
foreach ($f in $javaFiles) {
    $argsList.Add("""$f""")
}

$argFile = "$srcDir/javac_args.txt"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllLines($argFile, $argsList, $utf8NoBom)

Write-Host "Compiling $($javaFiles.Count) Java files with $($validJars.Count) classpath JARs..."
javac "@$argFile"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation SUCCESSFUL!"
    Remove-Item $argFile -Force -ErrorAction SilentlyContinue
} else {
    Write-Host "Compilation FAILED with code $LASTEXITCODE"
}
