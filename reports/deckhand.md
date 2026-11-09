# Marinheiro — 10.0.0-alpha.13

Entrega incremental do Lote 1: uma roupa base, não um conjunto de armadura.
PASS nos cenários finais executados; não certificação integral do modpack/contrato.

## Arquivos e rota

- Fonte editável: `models/blockbench/outfits/deckhand.bbmodel` (6 partes, UV neutro).
- PNG original 64×64: `textures/outfits/deckhand.png`.
- Export descritivo: `exports/mca_overlay/deckhand.json`.
- Runtime: `assets/piratesmca/outfits/deckhand.png` + registro MCA
  `data/piratesmca/skins/clothing/outfits.json` (neutro, excluído do sorteio).
- Auditoria real: [OUTFIT_RENDER_TARGET.md](renderer-audit/OUTFIT_RENDER_TARGET.md),
  `clothing-model-full.txt`, `clothing-validation.txt` no mesmo diretório.
- Código: `PirateOutfits.java`; registro do molde antes da aba criativa existente.

A camada ClothingLayer do MCA aplica o overlay ao próprio modelo animado/genético.
Não altera a skin base, renderer MCA, classes do Fresh, hitbox, relações, profissão,
trades ou dados sociais. O .bbmodel é referência editável de UV; o jogo usa o PNG
com a geometria nativa, inclusive peito feminino. Mãos e cabeça transparentes.
48 roupas históricas permanecem byte a byte; diff do JAR é auditado no empacotamento.

## Uso

```text
/give @s piratesmca:deckhand_outfit
/pirates outfit nearest deckhand
/pirates outfit nearest restore
```

Molde reutilizável disponível na aba criativa. Clique em pirata adulto da própria
tripulação ou use como OP, até 6 blocos e com linha de visão. Não é equipado como
armadura; mantém tricórnio/tapa-olho/faixa/peitoral. NPC-only, sem defesa adicional.
`restore` recupera a roupa anterior e seu bloqueio, registrados em tags nativas.
Uma roupa válida escolhida depois via MCA prevalece; o reparo só atua em dados
inválidos/ausentes com preset ativo. Não há novo schema de SavedData.

## Execução

Ambiente: Windows, Java25, Gradle9.5.1, Loom1.17.21, MC26.2, Loader0.20.1,
FabricAPI0.155.2+26.2, MCA8.1.11+26.2, Fish26.2.2.0, YACL3.9.7.
Build e produção usam dependências reais, sem stubs.

| ID / gate | Resultado | Evidência e escopo |
|---|---|---|
| TC-BOOT-01 | PASS parcial | Gradle build real: 35 JUnit + 68 GameTests. JAR e sources idênticos após jar sourcesJar --rerun-tasks; não foi usado clean nesta etapa |
| OUTFIT-01/02 | PASS | MCA aceita overlay neutro nos dois gêneros; identidade preservada e aplicação repetida não substitui bookmark |
| OUTFIT-03 | PASS | Restaura roupa e bloqueio anteriores, sem apagar a seleção válida posterior do MCA |
| OUTFIT-04 | PASS | Serialização/load nativo da entidade mantém uniforme e bookmark de restauração |
| OUTFIT-05 / TC-SPAWN-09/12 parcial | PASS | Repara roupa ausente; preserva escolha válida externa e demais campos |
| OUTFIT-06 | PASS | Cenário corrigido de criança/villager comum aprovado no build final |
| OUTFIT-07 | PASS | Snapshot VillagerVisuals contém o ID real e recurso PNG existe; 6 vistas por configuração |
| TC-SPAWN-01/02/03 parcial | PASS | Dois adultos, um por gênero; não substitui o ensaio completo de 20/20/40 do contrato |
| TC-SPAWN-05 parcial | PASS | Dois UUIDs/identidades mantidos e descarga efetiva de chunks verificada |
| TC-SPAWN-06 parcial | PASS | Save reaberto com reinício do servidor integrado no mesmo processo cliente |
| TC-SPAWN-06 completo | BLOCKED | Não foi reiniciado o processo inteiro Minecraft para esta roupa |
| QA-INIT-01 | FAIL inicial | Aplicação no primeiro cliente retornou false; causa não confirmada, logs preservados e repetições passaram sem alteração do JAR |
| TC-SPAWN-07 | BLOCKED | Primeira aplicação no cliente falhou; causa não esclarecida, ver histórico abaixo. Ensaio completo 5/30/120s + F3+T não executado nesta etapa |
| Editor Blockbench5.2.2 | PASS | Chromium/Playwright headless, WebGL, 6 cubos, PNG64×64, zero page_errors |
| Regressões Python | PASS | 9 testes: UV/alfa, mãos/cabeça, cobertura do peito, integridade dos exports |
| Cliente OFF | PASS | Sem EMF/ETF/packs opcionais; 9 poses, dimensão, chunks, reabertura, 29 capturas |
| Cliente EMF completo | PASS | Mods/packs exigidos ativos, 9 poses, dimensão, chunks, reabertura, BetterCombat jogador, 32 capturas |
| FO14.1.1 integral / shaders / profiling4/12/24/48 | BLOCKED | Não executado nesta etapa; fixture de compatibilidade não equivale ao modpack completo |
| Clique real do molde / permissão de capitão por cliente | BLOCKED | Código compila, backend é testado; interação e comando novos não foram exercitados pelo cliente automatizado |

A troca de dimensão leva um dos dois NPCs ao Nether e de volta; ambos têm identidade
comparada. Poses são fixtures controladas: idle, giro da cabeça, andar, correr,
atacar, ferido, nadar, sentado, dormir. Não são testes de IA autônoma.
Genética padrão e cabelos sorteados nesses dois corpos não cobrem todas as combinações.

Stack completo confirmado pelo teste: EMF3.3.11, ETF7.2.5, CompatCore2.3.2,
CompatBetterCombat2.1.0, BetterCombat3.2.2, PlayerAnimationLib1.2.8, Cloth26.2.155;
FreshAnimations1.11.2 → FA+AllExtensions1.10.1 → FA+Player1.1, nesta prioridade.
Fixture isolada; perfil pessoal não modificado e packs opcionais não redistribuídos.
Melhor compatibilidade observada nos casos executados não é cobertura universal.

## Histórico de falhas preservado

1. Uso inicial de `displayClientMessage`, ausente na API26.2 real. Substituído
   por `sendSystemMessage`; compilação corrigida passou em `qa/results/outfits-compile.log`.
   O log inicial da compilação foi sobrescrito; não é apresentado como evidência arquivada.
2. `qa/results/outfits-build.log`: 67/68 GameTests passaram. O caso de exclusão
   criava criança com marcador de pirata processado pelo spawn adulto existente.
   Cenário corrigido: criança comum continua bebê, recebe marcador imediatamente
   antes da tentativa de aplicar roupa; nenhuma política anterior de spawn alterada.
3. `qa/results/outfits-extensions.log`: primeira tentativa de aplicar roupa retornou
   false. O estado anterior não havia sido registrado, portanto a causa permanece
   NÃO CONFIRMADA. Adicionados logs de idade/gênero/roupa/validade do ID. Reexecução
   com mesmo JAR de produção passou; execução OFF também passou. Não afirmar que
   houve correção de race nem ocultar esta falha inicial. TC-SPAWN-07 segue pendente.

## Logs, capturas e reprodução

- [Blockbench](outfits/deckhand/blockbench/execution.json),
  [captura do editor](outfits/deckhand/blockbench/deckhand-opened.png).
- [Cliente OFF](outfits/deckhand/off/execution.log),
  [manifesto e hashes](outfits/deckhand/off/manifest.json).
- [Cliente EMF completo](outfits/deckhand/extensions/execution.log),
  [manifesto e hashes](outfits/deckhand/extensions/manifest.json).
- [Frente EMF](outfits/deckhand/extensions/0009_OUTFIT-deckhand-0-front.png),
  [costas EMF](outfits/deckhand/extensions/0014_OUTFIT-deckhand-1-back.png).
- `qa/results/outfits-final-build.log`, `outfits-python.log`, `outfits-assets.log`.

```powershell
.\gradlew.bat build
python scripts/pmca/validar_assets.py --root .
python -m unittest discover -s scripts/pmca/tests -v
.\gradlew.bat runProductionClientTest -PoutfitReview -PoutfitBase
# Após preparar a fixture local com os arquivos licenciados do usuário:
.\gradlew.bat runProductionClientTest -PoutfitReview -PextensionsReview '-PcompatMods=qa/fixtures/tricorne-emf-extensions/mods'
```

Não rodar Gradle em paralelo. `build_outfits.py deckhand` preserva fontes existentes
para não sobrescrever edições manuais. Fonte atual completa acompanha o ZIP workspace;
JAR sources é apenas a fonte Java/resources. GitHub: este diretório não possui .git
ou remoto, portanto não houve commit/push/tag/release remota.

Próximo asset: contrabandista, após investigar a aplicação inicial intermitente.
Capitão civil, armaduras, ganchos e pernas de pau continuam pendentes.

JAR SHA256: `56b1499db28b7605a034e2f3f0ad584bed0c532637e21895a4bbf51588baaf1b`.
