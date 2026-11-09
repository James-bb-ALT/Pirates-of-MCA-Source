$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$classes = Join-Path ([IO.Path]::GetTempPath()) ('pmca-domain-' + [guid]::NewGuid())
New-Item -ItemType Directory -Path $classes | Out-Null
& javac --release 25 -encoding UTF-8 -d $classes (Join-Path $root 'source/src/main/java/dev/arandu/piratesmca/CrewLedger.java') (Join-Path $PSScriptRoot 'LedgerRegressions.java') (Join-Path $PSScriptRoot 'LedgerSafetyRegressions.java')
if ($LASTEXITCODE) { exit $LASTEXITCODE }
& java -cp $classes LedgerRegressions
if ($LASTEXITCODE) { exit $LASTEXITCODE }
& java -cp $classes LedgerSafetyRegressions
exit $LASTEXITCODE
