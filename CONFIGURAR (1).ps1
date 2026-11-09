# Pirates of MCA Blockbench + Codex setup (Windows PowerShell 5.1+)
# Execute from the extracted kit folder. Does not edit Java source code or game installs.
[CmdletBinding()]
param(
    [Alias('Projeto')]
    [string]$ProjectPath = (Get-Location).Path,
    [switch]$InstallBlockbench,
    [switch]$GlobalSkill,
    [switch]$Force
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$kit = Split-Path -Parent $MyInvocation.MyCommand.Path
$project = [System.IO.Path]::GetFullPath($ProjectPath)
$skillName = 'pirates-mca-blockbench'

function Ensure-Folder([string]$path) {
    if (-not (Test-Path -LiteralPath $path -PathType Container)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
    }
}

function Copy-Safely([string]$from, [string]$to) {
    Ensure-Folder (Split-Path -Parent $to)
    if ((Test-Path -LiteralPath $to) -and -not $Force) {
        Write-Host "[SKIP] Ja existe: $to (use -Force se desejar substituir)" -ForegroundColor Yellow
        return
    }
    Copy-Item -LiteralPath $from -Destination $to -Force
    Write-Host "[OK] $to" -ForegroundColor Green
}

function Copy-DirectorySafely([string]$from, [string]$to) {
    if (-not (Test-Path -LiteralPath $from -PathType Container)) {
        throw "Diretorio do kit nao encontrado: $from"
    }
    Ensure-Folder $to
    foreach ($file in (Get-ChildItem -LiteralPath $from -File -Recurse)) {
        $relative = $file.FullName.Substring($from.Length).TrimStart([char[]]@('\', '/'))
        Copy-Safely $file.FullName (Join-Path $to $relative)
    }
}

Write-Host "Pirates of MCA | Blockbench + Codex" -ForegroundColor Cyan
Write-Host "Kit: $kit"
Write-Host "Projeto: $project"
Ensure-Folder $project

# No destructive or automatic dependency installation.
foreach ($subdir in @(
    'art\concepts',
    'docs',
    'models\blockbench\head', 'models\blockbench\face',
    'models\blockbench\torso', 'models\blockbench\back',
    'models\blockbench\hand', 'models\blockbench\leg',
    'models\blockbench\armor',
    'textures\skins', 'textures\outfits', 'textures\wearables', 'textures\armor',
    'definitions\outfits', 'definitions\presets', 'definitions\armor_sets',
    'exports\emf', 'exports\fabric_renderer', 'exports\items',
    'reports', 'scripts\pmca'
)) {
    Ensure-Folder (Join-Path $project $subdir)
}

# Skill local to repository (Codex discovery path)
$sourceSkill = Join-Path $kit '.agents\skills\pirates-mca-blockbench'
$repoSkill = Join-Path $project '.agents\skills\pirates-mca-blockbench'
Copy-DirectorySafely $sourceSkill $repoSkill

# Optional global copy. Explicit opt-in only.
if ($GlobalSkill) {
    $globalPath = Join-Path $HOME '.agents\skills\pirates-mca-blockbench'
    Copy-DirectorySafely $sourceSkill $globalPath
}

Copy-Safely (Join-Path $kit 'GUIA_PIRATES_MCA_BLOCKBENCH_CODEX.md') (Join-Path $project 'docs\GUIA_PIRATES_MCA_BLOCKBENCH_CODEX.md')
Copy-Safely (Join-Path $kit 'PROMPT_INICIAL_CODEX.md') (Join-Path $project 'docs\PROMPT_INICIAL_CODEX.md')
Copy-Safely (Join-Path $kit 'definitions\asset_catalog.json') (Join-Path $project 'definitions\asset_catalog.json')
Copy-DirectorySafely (Join-Path $kit 'concepts') (Join-Path $project 'art\concepts')
Copy-DirectorySafely (Join-Path $kit 'scripts') (Join-Path $project 'scripts\pmca')

# Blockbench installation is explicit; no untrusted plugin installation.
if ($InstallBlockbench) {
    $winget = Get-Command winget -ErrorAction SilentlyContinue
    if ($null -eq $winget) {
        Write-Warning 'winget nao encontrado. Instale Blockbench manualmente em https://www.blockbench.net/downloads'
    } else {
        Write-Host '[INSTALL] Blockbench via winget: JannisX11.Blockbench'
        & winget install --id JannisX11.Blockbench --exact --accept-package-agreements --accept-source-agreements
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "winget retornou codigo $LASTEXITCODE. Verifique se Blockbench ja esta instalado."
        }
    }
} else {
    Write-Host '[INFO] Blockbench nao foi instalado (adicione -InstallBlockbench se desejar).'
}

# Inventory of available development commands.
foreach ($name in @('codex','python','py','java','gradle','git')) {
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($null -eq $cmd) {
        Write-Host "[INFO] $name nao localizado no PATH" -ForegroundColor Yellow
    } else {
        Write-Host "[OK] $name : $($cmd.Source)" -ForegroundColor Green
    }
}

# Validator is optional and reports planned assets as planned, not as failures.
$validator = Join-Path $project 'scripts\pmca\validar_assets.py'
if (Get-Command py -ErrorAction SilentlyContinue) {
    & py -3 $validator --root $project
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    & python $validator --root $project
} else {
    Write-Host '[INFO] Sem Python; execute o validador manualmente quando disponivel.'
}

Write-Host ''
Write-Host 'Configuracao concluida. Codex reconhece a skill dentro do projeto.' -ForegroundColor Cyan
Write-Host "Abra o Codex em: $project"
Write-Host 'Invoque: $pirates-mca-blockbench'
Write-Host 'Primeiro passo recomendado: auditoria do renderer MCA (Lote 0).'
