$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
python "$projectRoot/compile_neoforge.py"
