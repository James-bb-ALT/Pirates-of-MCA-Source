<#
.PURPOSE
  Gera um resource pack CEM de demonstração para um uniforme de Pirate Atelier.
  REQUER o ZIP Pirate_Atelier_CleanRoom_Player_3D_EMF_26.2_v2.zip.
  ATENCAO: ativa a geometria escolhida para qualquer armadura equipada enquanto o
  pack de teste estiver ligado. Isto NAO faz selecao por ID de item.
.EXAMPLE
  .\SELECIONAR_ROUPA_EMF.ps1 -Profile 'corsair_teal_tattered_peg_left' -Hook right -Eyepatch left
#>
[CmdletBinding()]
param(
  [Parameter(Mandatory=$true)][string]$Profile,
  [ValidateSet('none','left','right')][string]$Hook='none',
  [ValidateSet('none','left','right')][string]$Eyepatch='none',
  [string]$PackZip=(Join-Path $PSScriptRoot 'Pirate_Atelier_CleanRoom_Player_3D_EMF_26.2_v2.zip'),
  [string]$OutZip=(Join-Path $PSScriptRoot 'Pirate_Atelier_EMF_TESTE_PERSONALIZADO.zip')
)
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
if (-not (Test-Path -LiteralPath $PackZip)) { throw "ZIP principal nao encontrado: $PackZip" }
$src=[System.IO.Compression.ZipFile]::OpenRead($PackZip)
try {
  $base="development_assets/cem_wardrobe/$Profile/"
  if (-not ($src.GetEntry($base+'helmet.jem'))) { throw "Perfil invalido: $Profile" }
  if (Test-Path -LiteralPath $OutZip) { Remove-Item -LiteralPath $OutZip -Force }
  $out=[System.IO.Compression.ZipFile]::Open($OutZip,[System.IO.Compression.ZipArchiveMode]::Create)
  try {
    $pack=$out.CreateEntry('pack.mcmeta')
    $sw=New-Object System.IO.StreamWriter($pack.Open(),(New-Object System.Text.UTF8Encoding($false)))
    try { $sw.Write('{"pack":{"min_format":88,"max_format":88,"description":"Pirate Atelier EMF DEMO - '+$Profile+'"}}') } finally {$sw.Dispose()}
    foreach ($slot in @('helmet','chestplate','leggings','boots')) {
      $file="$slot.jem"
      if ($slot -eq 'helmet' -and $Eyepatch -ne 'none') { $file="helmet_eyepatch_$Eyepatch.jem" }
      if ($slot -eq 'chestplate' -and $Hook -ne 'none') { $file="chestplate_hook_$Hook.jem" }
      $entry=$src.GetEntry($base+$file)
      if ($null -eq $entry) { throw "Modelo nao encontrado: $base$file" }
      $dst=$out.CreateEntry('assets/minecraft/emf/cem/'+$slot+'.jem')
      $read=$entry.Open();$write=$dst.Open()
      try {$read.CopyTo($write)} finally {$read.Dispose();$write.Dispose()}
    }
    $entry=$out.CreateEntry('AVISO.txt')
    $sw=New-Object System.IO.StreamWriter($entry.Open(),(New-Object System.Text.UTF8Encoding($false)))
    try {$sw.Write("EMF DEMO: $Profile. Este pack muda o modelo de TODAS as armaduras por slot, não apenas Pirate Atelier. Nao substitui player.jem. Desative apos testar.") } finally {$sw.Dispose()}
  } finally {$out.Dispose()}
} finally {$src.Dispose()}
Write-Host "Arquivo pronto: $OutZip"
Write-Host 'Coloque em resourcepacks, por cima de Fresh Animations Player Extension.'
