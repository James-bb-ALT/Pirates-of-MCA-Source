# Estruturas costeiras — 5.0.0-alpha.8 — 2026-11-03

Extensão funcional autorizada pelo usuário: villagers gerados por qualquer
estrutura em praia/oceano devem virar piratas. Inclui a inspeção do modpack local.
Esta extensão prevalece sobre a restrição anterior de adaptador apenas Fish;
docs/ESPECIFICACAO_ORIGINAL_v2.0.md permanece como registro histórico imutável.

## Regra implementada

Durante StructureStart.placeInChunk, villagers vanilla ou MCA adicionados por
ServerLevel/WorldGenRegion em `piratesmca:pirate_waters` recebem as tags de pirata.
O contexto termina em finally, inclusive em geração aninhada ou com exceção.
Não há busca por estruturas em cada tick nem conversão só por estar perto do mar.
Modos normais de reprodução/comando não passam por esse contexto de geração.
O bioma é avaliado na posição de spawn do NPC; estruturas que cruzam biomas podem
ter conversão parcial. Há uma crew por dimensão, ID de estrutura e chunk de origem.
NPCs de seaposts Fish preservam o adaptador e as origens anteriores.

A tag aceita oceanos/praias Minecraft e c, variedades de oceano profundo/raso,
costa rochosa, KS deeper_cold_ocean, Rawlands mist_coast e ilha tropical Fish.
Entradas opcionais não exigem instalar esses mods. A ilha tropical é uma extensão
temática explícita. Um datapack pode ajustar a tag sem alterar código.

## Execução e resultados

| Caso | Resultado | Escopo/evidência |
|---|---|---|
| TC-BOOT-01: clean build real | PASS | coastal-final-build.log; 35 JUnit; 49 GameTests obrigatórios, sendo 48 addon e 1 Fabric |
| COAST-01: oceano | PASS | 4 NPCs por teste, 2 vanilla convertidos MCA e 2 MCA diretos, duas crews distintas |
| COAST-02: praia | PASS | mesmo pipeline StructureStart e addFreshEntity |
| COAST-03: bioma de mod via tag c | PASS | fixture piratesmca-tests:coast |
| COAST-04: planícies | PASS | 4 NPCs preservados, zero piratas |
| COAST-05: ilha tropical Fish | PASS | bioma real fishofthieves:tropical_island |
| COAST-06: isolamento e limpeza do contexto | PASS | crianças comuns fora da estrutura preservadas; exceção de geração não contamina spawn posterior |
| TC-SPAWN-10 parcial: template Fish real | PASS parcial | 2 adultos com nome, pele, roupa e capitão; teste de servidor |
| Spawn comum vanilla/MCA | PASS | 36 NPCs comuns, 12 crianças preservadas, fora do contexto da estrutura |
| Cliente com JAR de produção | PASS | 48 piratas via comandos, identidade/texturas, bandeira, reload, Nether, unload e reabertura integrada |
| Colocação costeira por todos os mods do perfil | NOT RUN | Os 344 JARs não foram executados juntos nesta rodada |
| Mundo oceânico percorrido, geração natural por seed | NOT RUN | Testes usam peças controladas e template Fish real |
| Reinício de todo o processo cliente nesta versão | NOT RUN | Não inferido da reabertura integrada |
| FO completo, multiplayer, profiling 4/12/24/48 por dez minutos | NOT RUN | Não certificado |
| Saves históricos Alpha 1–5 | BLOCKED | Não fornecidos |

As peças genéricas de teste são fixtures dentro de StructureStart registrado como
shipwreck; não são a prova de que todos os templates de todos os mods foram
executados. O caminho WorldGenRegion é instrumentado e carrega sem erro de mixin,
mas as novas asserções funcionais exercitam ServerLevel. Templates vanilla/mods
que adicionam villagers por essas APIs durante a geração entram no mecanismo.
Spawns agendados para depois da geração, sistemas próprios que bypassam essas APIs,
villagers não compatíveis com MCA e estruturas não registradas exigem avaliação.

## Biomas do perfil

Perfil Fabulously Optimized: 344 JARs ativos, 294 IDs com definições JSON, 18
definições cobertas pela regra. É inventário estático de recursos, não dump do
registro vivo; referências opcionais sem definição encontrada não contam como
biomas confirmados. Ver BIOMAS_DO_MODPACK.md e qa/results/pack-biomes/biomes.csv.
`joshie:village_ocean` do Tidal Towns é candidata identificada, não validada em jogo.
O perfil ainda contém Pirates 3.0.0-alpha.6; nenhum perfil pessoal foi alterado.

## Artefatos e reprodução

Java 25 / Minecraft 26.2 / Loader 0.20.1 / Loom 1.17.21 / Gradle 9.5.1;
MCA 8.1.11+26.2, Fish 26.2.2.0, API 0.155.2+26.2, YACL 3.9.7+26.2.
Sem stubs; dependências não foram alteradas. SavedData continua no schema v4.

```
.\gradlew.bat clean build --console=plain
.\gradlew.bat runProductionClientTest -PplayableSmoke --console=plain
python qa/audit_release.py
```

Executar sequencialmente. Logs: qa/results/coastal-final-build.log e coastal-client.log.
JUnit XML: qa/results/coastal-junit. Capturas: qa/results/coastal-client-evidence.
JAR entregue/testado: `9c29c92a6a9b7d95556da3874639652d2ed06bc5312e1d0434e96393d5bd95c8`. Reempacotamento produziu JAR/sources idênticos;
coastal-first-hashes.json e coastal-reproducible.log registram os dois passes.
Isso não comprova build offline em outra máquina.

## Limitações restantes

**A regra vale para novas gerações. Não reclassifica automaticamente NPCs de
estruturas que já estavam salvas.** Isso evita transformar moradores transportados
ou nascidos depois em estruturas antigas. Seaposts já marcados continuam funcionando.
Não há migração automática desse novo critério para barcos antigos nesta entrega.
Faltam tesouraria/PARDON, diplomacia, Livro/HUD e integração completa de diálogos MCA.
ATTACK continua restrito a monstros. Smoke visual de 48 usa NoAI; não mede IA.
Advertências/erros deliberados de recuperação e autenticação do perfil de teste
permanecem nos logs; não se declara execução sem erros apenas por BUILD SUCCESSFUL.

Entrega parcial; preservadas as funcionalidades e os checkpoints anteriores.
Nenhum mundo/perfil pessoal foi alterado. EULA só nos testes isolados autorizados.
Sem commit/push/tag/release remota: diretório recebido não possui Git nem remoto.
