param([switch]$WaitForOn)

# Serial Gradle runs: concurrent builds can deadlock the shared file-hash cache.
# Each gate keeps its own log and stops on failure; no result is inferred from file existence.
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot -ErrorAction Stop
if ($WaitForOn) {
    while (Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object { $_.CommandLine -match '-Dpiratesmca.visualFixReview=full-final-on(?: |$)' }) {
        Start-Sleep -Seconds 10
    }
}
$onLog = Get-Content -LiteralPath 'reports/visual-fix-full-final-on.log' -Raw -ErrorAction Stop
if (-not $onLog.Contains('BUILD SUCCESSFUL') -or -not $onLog.Contains('RECON_AVATAR_PASS')) {
    throw 'The final Fresh ON matrix is not complete. Subsequent gates were not started.'
}
$expectedHash = (Get-FileHash -LiteralPath 'build/libs/piratesmca-13.0.0-alpha.19.jar' -Algorithm SHA256).Hash.ToLowerInvariant()
if (-not $onLog.Contains('QA_ARTIFACT_SHA256 ' + $expectedHash)) {
    & python qa/verify_visual_reuse.py
    if ($LASTEXITCODE -ne 0) { throw 'ON matrix is from another candidate and preservation proof failed.' }
}
$reuse = Get-Content -LiteralPath 'reports/visual-fix/on-matrix-reuse.json' -Raw | ConvertFrom-Json

$compat = '-PcompatMods=qa/fixtures/mca-skin-3d/mods'
$wide = '-PclientUuid=00000000-0000-0000-0000-000000000009'
$slim = '-PclientUuid=00000000-0000-0000-0000-000000000001'
$gates = @(
    @{ Name='full-final-off'; Arguments=@('runProductionClientTest','-PvisualFixReview=full-final-off',$compat,$wide,'-PdisableFreshAnimations'); Marker='RECON_AVATAR_PASS' },
    @{ Name='weapons-on'; Arguments=@('runProductionClientTest','-PvisualFixReview=weapons-on',$compat,$wide); Marker='QA_HELD_WEAPONS_COMPLETE' },
    @{ Name='weapons-off'; Arguments=@('runProductionClientTest','-PvisualFixReview=weapons-off',$compat,$wide,'-PdisableFreshAnimations'); Marker='QA_HELD_WEAPONS_COMPLETE' },
    @{ Name='prototypes-on'; Arguments=@('runProductionClientTest','-PvisualFixReview=prototypes-on',$compat,$wide); Marker='QA_PROTOTYPES_COMPLETE' },
    @{ Name='prototypes-off'; Arguments=@('runProductionClientTest','-PvisualFixReview=prototypes-off',$compat,$wide,'-PdisableFreshAnimations'); Marker='QA_PROTOTYPES_COMPLETE' },
    @{ Name='variants-on'; Arguments=@('runProductionClientTest','-PvisualFixReview=variants-on',$compat,$wide); Marker='QA_VISUAL_VARIANTS_COMPLETE' },
    @{ Name='heads-on'; Arguments=@('runProductionClientTest','-PvisualFixReview=heads-on',$compat,$wide); Marker='QA_FACE_CAPTURE_COMPLETE' },
    @{ Name='heads-off'; Arguments=@('runProductionClientTest','-PvisualFixReview=heads-off',$compat,$wide,'-PdisableFreshAnimations'); Marker='QA_FACE_CAPTURE_COMPLETE' },
    @{ Name='avatar-slim-on'; Arguments=@('runProductionClientTest','-PvisualFixReview=avatar-slim-on',$compat,$slim); Marker='RECON_AVATAR_PASS' },
    @{ Name='avatar-slim-off'; Arguments=@('runProductionClientTest','-PvisualFixReview=avatar-slim-off',$compat,$slim,'-PdisableFreshAnimations'); Marker='RECON_AVATAR_PASS' },
    @{ Name='probe-comparison-on'; Arguments=@('runProductionClientTest','-PvisualFixReview=probe-comparison-on','-PvisualComparisonSave=run/visual-fix-before-on/saves/New World',$compat,$wide); Marker='QA_COMPARISON_SAVED_IDENTITIES' },
    @{ Name='persistence'; Arguments=@('runProductionClientTest','-PpersistenceReview',$compat,$wide); Marker='QA_CHUNK_UNLOAD_CONFIRMED count=48' },
    @{ Name='persistence-reopen'; Arguments=@('runProductionClientTest','-PpersistenceReview','-PclientReopen',$compat,$wide); Marker='QA_FULL_PROCESS_RESTART_CONFIRMED count=48' },
    @{ Name='server-gametests'; Arguments=@('runGameTest'); Marker='required tests passed' },
    @{ Name='build-regressions'; Arguments=@('build','ledgerRegressions','ledgerSafetyRegressions'); Marker='BUILD SUCCESSFUL' }
)
foreach ($gate in $gates) {
    $log = 'reports/visual-fix-' + $gate.Name + '.log'
    if (Test-Path -LiteralPath $log) {
        $existing = Get-Content -LiteralPath $log -Raw
        $reusedHash = $reuse.reusable_gates.($gate.Name)
        $preserved = $reuse.final_jar_sha256 -eq $expectedHash -and $reusedHash -and $existing.Contains('QA_ARTIFACT_SHA256 ' + $reusedHash)
        if ($existing.Contains('BUILD SUCCESSFUL') -and $existing.Contains($gate.Marker) -and ($preserved -or $gate.Name -in @('server-gametests','build-regressions') -or $existing.Contains('QA_ARTIFACT_SHA256 ' + $expectedHash))) {
            Write-Output ('KEPT completed gate: ' + $gate.Name)
            continue
        }
        # Failed attempts remain available; never rewrite their evidence.
        Copy-Item -LiteralPath $log -Destination ($log + '.' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.previous') -ErrorAction Stop
    }
    Write-Output ('RUN ' + $gate.Name)
    $gateArgs = $gate.Arguments
    & '.\gradlew.bat' @gateArgs *> $log
    $result = $LASTEXITCODE
    $content = Get-Content -LiteralPath $log -Raw
    if ($result -ne 0 -or -not $content.Contains('BUILD SUCCESSFUL') -or -not $content.Contains($gate.Marker)) {
        throw ('Gate failed: ' + $gate.Name + '. See ' + $log)
    }
    $actualHash = (Get-FileHash -LiteralPath 'build/libs/piratesmca-13.0.0-alpha.19.jar' -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualHash -ne $expectedHash) { throw ('JAR changed during validation: ' + $gate.Name) }
    Write-Output ('PASS execution: ' + $gate.Name)
}
Write-Output ('All execution gates finished for JAR ' + $expectedHash + '. Manual visual review remains a separate gate.')
