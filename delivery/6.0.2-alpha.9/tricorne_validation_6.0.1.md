# Validação incremental — 6.0.1-alpha.9

Data: 2026-11-04. Skill: `.agents/skills/pirates-mca-blockbench/SKILL.md`.
O checkpoint 6.0.0 permanece imutável em delivery/6.0.0-alpha.9.
Esta atualização acrescenta QA e versão; classes e assets de produção são
comparados byte a byte com o JAR anterior pelo empacotador.

| ID | Resultado | Evidência e alcance |
|---|---|---|
| HAT-09 | PASS | Blockbench Web 5.2.2 abriu tricorne_classic (15 cubos) e referência MCA (6 partes), textura carregada; execution.json e duas capturas em blockbench-review |
| HAT-10 / execução | PASS | Nove poses, dois NPCs, OFF e EMF/ETF/Fresh ON; equipamento e snapshots de identidade preservados |
| HAT-10 / certificação completa | BLOCKED | Todos os cabelos/formatos e clipping durante IA autônoma não cobertos; natação sob água e câmera de sono limitam avaliação visual |
| HAT-11 | BLOCKED | Shaders, FO completo e conjunto pessoal de 344 mods não executados nesta rodada |
| HAT-12 | BLOCKED | Profiling 4/12/24/48 portadores não executado |
| Regressão automatizada | PASS | 35 JUnit, 53 GameTests obrigatórios, três testes Python e validador; qa/results/tricorne-validation-build.log |

## Navegador sem desktop

`scripts/pmca/review_blockbench.py` abre Chromium headless com contexto temporário,
sem perfil pessoal. Arquivos locais entram pelo seletor virtual do Playwright;
POSTs são bloqueados, inclusive tentativas de telemetria. Não houve uso de cookies
pessoais nem upload dos assets. O console registra requisições abortadas e avisos
WebGL; não houve exceções de página. São capturas reais do editor, não montagens.

Reprodução neste ambiente:

```powershell
& 'C:\Users\victo\AppData\Local\Programs\Python\Python313\python.exe' scripts/pmca/review_blockbench.py
```

Em outro ambiente, use Python com Playwright e Chromium instalado. O editor web
exige rede. A abertura confirma interpretação da fonte/textura; o script não
executa roundtrip de edição/salvamento.

## Cliente real e limites

Minecraft 26.2 / Java 25 / Loader 0.20.1 / MCA 8.1.11+26.2 / Fish 26.2.2.0.
Compatibilidade testada: EMF 3.3.11, ETF 7.2.5, Fresh Animations 1.11.2.
O cenário dedicado usa dois adultos MCA, câmera programática e estados controlados:
idle, rotação de cabeça, caminhada, corrida, ataque, dano, natação, sentado e sono.
Asserções esperam os estados reais do cliente; não simulam pathfinding autônomo.
Artefato testado: `f41a706efb6e60762cb8d31ddc1faa5369f96ad477053424808749c4d35ebb71`.

Capturas/logs finais em `pose-review/off/` e `pose-review/fresh/`; cada pasta tem
manifesto com hashes. Rodadas preliminares ficam em diagnostic/ e off-pre-cleanup/;
não são a evidência final. A limpeza da água entre fases corrigiu o cenário de QA,
sem alteração de comportamento do addon. Água ainda reduz visibilidade na natação.
O chapéu aparece preso à cabeça nas capturas inspecionadas, mas a cobertura visual
não basta para promover animation_safe. Catálogo continua exported/false.

```powershell
.\gradlew.bat clean build --console=plain
.\gradlew.bat runProductionClientTest -PposeReview --console=plain
.\gradlew.bat runProductionClientTest -PposeReview -PcompatMods=qa/fixtures/tricorne-emf/mods --console=plain
```

Execute sequencialmente. Para rodada OFF após ON, selecione apenas vanilla em
run/tricorne-pose-test/options.txt. Fixture ON contém cópias locais de EMF/ETF e
overrides/resourcepacks com Fresh habilitado; esses arquivos não são redistribuídos.
O JAR GameTest é usado apenas pelo runner, não deve ser instalado no perfil normal.

Testes de 48 NPCs, unload/reabertura/dimensão pertencem à evidência histórica
6.0.0, não foram repetidos neste incremento. O empacotador exige que apenas
fabric.mod.json difira no JAR novo. Nenhum lote 1–4 foi iniciado nesta atualização.
Não há repositório Git/remoto neste diretório; não houve commit/push/release remota.
Instalação e comandos estão no LEIA-ME da entrega. Hashes em SHA256SUMS.txt.
