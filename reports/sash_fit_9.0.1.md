# SASH-FIT-01 — correção confirmada no caso reproduzido — 9.0.1-alpha.12

Data: 2026-11-04. Minor conforme classificação do usuário: correção incremental.
Skills: investigate-first e pirates-mca-blockbench. Nenhum novo asset nesta versão.

## Causa e prova

Na 9.0.0, a faixa herdava a transformação completa de body, cujo pivô fica na
parte superior do torso. Depois de dormir/acordar, a animação do MCA aplicava
transitoriamente deslocamento vertical e inclinação ao torso. A faixa era arrastada
para dentro da região das pernas e ficava encoberta. A hipótese inicial de defeito
específico da anatomia feminina não se confirmou: sexo, seis genes medidos, roupa,
skin, nome, UUID e câmera eram os mesmos antes/depois da manifestação.

A ordem das capturas confundiu a observação: a NPC feminina era fotografada primeiro,
durante a transição; quando o NPC masculino era fotografado, o movimento já havia
amortecido. Reabrir o mundo também eliminava esse estado transitório.

Foi copiado o mundo QA original da candidata 9.0.0, sem alterar a origem. Um primeiro
diagnóstico abriu os mesmos atores em quatro câmeras, antes/depois de reabrir.
Sem a sequência de poses, a faixa aparecia inteira. O replay das nove poses
reproduziu a oclusão com os mesmos UUIDs e genética.

Estado medido no ator original 9bc395de-31b7-406f-8b2f-0e206d7ec16b:
body y=1,606595; z=-0,5650008; xRot=0,07437499. Os valores são pixels/radianos
locais do modelo. Não representam mudanças no NBT/genética da entidade.

Na comparação dos vértices reais resolvidos do item:

| Mesmo frame | Máximo Y da faixa | Plano do quadril | Resultado |
|---|---:|---:|---|
| Âncora antiga do torso | 13,37656 | 12,11174 | Atravessava o limite das pernas |
| Âncora corrigida | 11,71174 | 12,11174 | Margem de 0,4 pixel acima do quadril |

Y positivo aponta para baixo no espaço Java. A regressão determinística reaplica
a pose medida, sem depender do FPS: âncora antiga 13,37655, nova 11,60000, quadril
12,00000. Ela exige que o caminho antigo atravesse o plano e o corrigido permaneça
acima dele. As capturas confirmam a faixa inteira depois da sequência que falhava.

## Alteração

SashLayer.applyWaistAnchor acompanha o centro dos pivôs reais leftLeg/rightLeg,
descontando o quadril neutro Y=12 da fonte. Preserva o yaw do torso, mas não herda
seu deslocamento ou pitch de respiração/despertar. O root e a escala externa do MCA
continuam aplicados. Sem ocultar corpo/roupa, sem alterar Fresh ou genética, sem
mudar malha/PNG/UV, slot de equipamento, hitbox ou dados sociais.

O código de produção não percorre vértices nem calcula bounds por frame: usa os
pivôs já disponíveis no modelo. A medição dos vértices é exclusiva do GameTest.
Não houve profiling para quantificar custo de CPU/memória.

## Execução

| ID | Estado | Evidência |
|---|---|---|
| FIT-01 | PASS | Mesmo mundo QA/UUIDs, quatro câmeras × duas aberturas, seis genes/identidades iguais |
| FIT-02 / SASH-FIT-01 | FAIL antes / PASS depois | Replay de poses original; legacy_front_clips=1 e corrected_clips=0 |
| FIT-03 | PASS | Pose medida reproduzida matematicamente com os vértices reais do item; margem exigida acima do quadril |
| FIT-04 | PASS OFF/ON | Nove poses e seis vistas; bounds verificados no cliente para os dois NPCs |
| FIT-05 / TC-SAVE-03 | PASS OFF/ON | Nether e retorno preservam identidade/HEAD/CHEST |
| FIT-06 / TC-SPAWN-05 | PASS OFF/ON | Unload confirmado, recarga e capturas com equipamentos preservados |
| FIT-07 / TC-SPAWN-06 | PASS limitado OFF/ON | Reabertura do servidor integrado; mundo QA 9.0.0 também aberto em JVM posterior durante diagnóstico/replay |
| FIT-08 | PASS ON limitado | Jogador visível com faixa/HEAD e golpe ativo Better Combat, com FA Player |
| FIT-09 | PASS | Fonte reaberta em Blockbench Web 5.2.2 via Playwright isolado, oito cubos e PNG carregado |
| FIT-10 | BLOCKED | Todos os corpos/roupas/avatares MCA, shaders, multiplayer, FO completo e profiling não certificados |

Build: **35 JUnit, 64 GameTests obrigatórios, sete testes Python e validador PASS**.
Cliente OFF e ON terminaram BUILD SUCCESSFUL. A regressão determinística medida
foi executada em OFF; o replay/live com stack completo confirmou o caso real ON.
Os IDs FIT complementam o plano original, não certificam seus 62 casos inteiros.
Asset permanece exported/animation_safe=false: o defeito observado foi corrigido,
mas isso não equivale à certificação de todas as combinações possíveis.

Ambiente: MC 26.2, Java 25, Loader 0.20.1, MCA 8.1.11+26.2, Fish 26.2.2.0.
Stack ON: EMF 3.3.11, ETF 7.2.5, EMF Compat Core 2.3.2, EMF Compat Better Combat
2.1.0, Better Combat 3.2.2, Player Animation Library 1.2.8, Cloth Config 26.2.155;
Fresh base 1.11.2, All Extensions 1.10.1 e Player 1.1 realmente ativos.
Perfil pessoal não modificado, packs/mods opcionais não redistribuídos.

## Evidências e reprodução

- qa/results/sash-fit-diagnostic.log — câmeras controladas e identidade/genética.
- qa/results/sash-fit-replay.log — replay com defeito no JAR 9.0.0.
- qa/results/sash-fit-fixed-replay.log — mesmo replay corrigido no JAR 9.0.1.
- reports/sash-fit/before/ e fixed-replay/ — antes/depois e logs.
- reports/sash-fit/final/off/ e extensions/ — 18/21 capturas finais com manifestos.
- qa/results/sash-fit-build.log, sash-fit-client-*.log, sash-fit-junit/.

O primeiro carregamento de diagnóstico apontou para um diretório fora de saves.
A API Fabric open() usa somente o nome final do caminho; isso abriu a tela de
recuperação no fixture incorreto. O runner foi corrigido para usar seu próprio
saves/Fit Original. A origem não foi sobrescrita. Esse erro de QA não é corrupção
de mundo causada pelo addon; log separado em sash-fit-before-save-path-fix.log.

```powershell
.\gradlew.bat build --console=plain
.\gradlew.bat runProductionClientTest -PsashReview --console=plain
.\gradlew.bat runProductionClientTest -PsashReview -PextensionsReview -PcompatMods=qa/fixtures/tricorne-emf-extensions/mods --console=plain
```

Executar sequencialmente. OFF depois de ON: somente vanilla em run/sash-test/options.txt.
Para replay opcional de um save QA existente:

```powershell
python qa/prepare_sash_fit_replay.py --save 'run/sash-test/saves/New World'
.\gradlew.bat runProductionClientTest -PsashReview -PsashFitReplay -PcompatMods=qa/fixtures/tricorne-emf-extensions/mods --console=plain
```

O preparador não sobrescreve destino existente e aceita apenas mundos de teste em run/.
As verificações de bounds e a pose determinística também fazem parte do teste normal;
o save original não é necessário para executá-las. Nenhum clique no desktop foi usado.

Instalação: substituir o JAR antigo, sem duas versões. Comandos de faixa e CHEST
permanecem iguais; sem defesa, receitas ou combinação com outro peitoral.
JAR testado: `b3ed99f80e5c9d2d43eeb8757c6d36a174785db3076819c30909aab1fb5d0d96`.
Reempacotamento reproduz hashes de JAR/sources; manifesto na entrega. Comparação
com 9.0.0 permite mudar apenas SashLayer.class e fabric.mod.json no JAR existente.
Sem Git/remoto neste diretório; não houve commit/push/release remota.
