# Copyright (c) 2024 LingRube.
# Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0).
# See LICENSE file in the project root for full license information.

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
python "$projectRoot/compile_neoforge.py"
