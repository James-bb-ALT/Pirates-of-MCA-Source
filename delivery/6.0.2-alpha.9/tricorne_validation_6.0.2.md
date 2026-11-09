# Tricórnio — cabelos e stack EMF solicitado — 6.0.2-alpha.9

Data: 2026-11-04. Checkpoint experimental do Lote 0; nenhum asset novo neste incremento.
O contrato de compatibilidade inclui os cinco arquivos fornecidos pelo usuário.
Fresh base sozinho não satisfaz esse contrato. Não houve alteração do perfil pessoal.

## Matriz obrigatória

| Configuração | NPCs: poses/cabelos | Alcance |
|---|---|---|
| OFF: sem EMF/ETF, somente vanilla | PASS | Nove poses controladas, encaixe seco, oito texturas de cabelo, 12 vistas |
| EMF/ETF + Fresh base | PASS | Mesma bateria, shaders desligados |
| EMF/ETF + três packs + dois EMF Compat e dependências | PASS limitado aos NPCs | Mesma bateria; packs ativos e snapshot de cabelo real verificados |

Packs, em prioridade crescente, igual ao subconjunto do perfil:
FreshAnimations_v1.11.2_26.1+.zip, FA+All_Extensions-v1.10.1.zip, FA+Player-v1.1.zip.
JARs: EMF 3.3.11, ETF 7.2.5, EMF Compat Core 2.3.2, EMF Compat Better Combat 2.1.0,
Better Combat 3.2.2, Player Animation Library 1.2.8 e Cloth Config 26.2.155.
Hashes e dependências em hair-review/emf-extensions-fixture.json. Nenhum desses
JARs/packs opcionais é redistribuído nos ZIPs de entrega.

## Resultados e evidências

| ID | Estado | Evidência |
|---|---|---|
| HAT-09 | PASS histórico reutilizado | Mesmos assets, abertos em Blockbench Web 5.2.2 via Playwright; blockbench-review |
| HAT-10 / oito cabelos | PASS | Texturas 0/10/20/39 de cada gênero; estilo, camadas efetivas, recursos e identidade sincronizados; manifests off/fresh/extensions |
| HAT-10 / poses | PASS limitado | Nove estados controlados; captura extra seca remove oclusão da água, sem testar natação autônoma |
| HAT-13 / stack solicitado | PASS limitado aos NPCs | Gate exige seis IDs de mods e três packs ativos; 22 capturas por configuração |
| HAT-14 / jogador e Better Combat | PASS limitado | Jogador visível sem/com chapéu; golpe ativo progress=0.07999999; três capturas em hair-review/player-extensions |
| HAT-11 / shaders e perfil completo | BLOCKED | Não executados; fixture contém só dependências necessárias, não todos os mods/packs pessoais |
| HAT-12 / profiling | BLOCKED | 4/12/24/48 portadores não medidos |

Build real Java 25/MC 26.2/Loader 0.20.1: 35 JUnit, 53 GameTests obrigatórios,
três testes Python e validador PASS em qa/results/tricorne-hair-build.log.
Os IDs HAT são extensão visual do plano; não substituem seus 62 casos. A persistência
TC-SPAWN-05/06 e TC-SAVE-03 tem evidência histórica 6.0.0, não foi repetida aqui.

## Falhas do cenário de QA encontradas

1. **Falso positivo de cabelo.** setHair(String) atualiza o ID de estilo, mas
   HairLayer desenha cinco camadas independentes capturadas por VillagerVisuals.
   Fotos antes da correção mostravam cabelos iguais. Fixture agora usa
   HairStyle.singleLayer/setHairStyle, verifica hairBase e quatro camadas vazias
   no snapshot real do MCA, compara identidade e restaura as cinco camadas originais.
   diagnostic-style-id-only-* é FAIL de cobertura; não é evidência de compatibilidade.
2. **Screenshots antigos misturados.** A pasta do runner conserva arquivos entre
   execuções. archive_hair_review.py seleciona exatamente 22 nomes/índices e exige
   24 registros QA_HAT_RENDER_HAIR. Arquivos antigos não entram no manifesto final.
3. **Jogador invisível no teste.** Primeiras capturas mostravam só espada/chapéu,
   inclusive sem chapéu. Diagnóstico registrou invisible=true, spectator=false.
   Não foi atribuído aos packs sem investigação: fixture agora remove efeitos e
   flag de invisibilidade no servidor e espera confirmação visível no cliente.
   Os diagnósticos são preservados; resultado gráfico do reteste fica separado.
   Reteste PASS: invisible=false e corpo renderizado sem/com chapéu, sem modificar
   MCA nem packs. Não foi necessário desativar FA+Player ou EMF Compat.
4. **Falso positivo de ataque.** Better Combat retorna swingProgress=1 em repouso.
   A primeira espera >0 passou sem golpe. Teste agora gera um clique interno do
   Minecraft e exige 0<progress<1. Reteste registrou 0.07999999 e captura de golpe.
   player-idle-false-attack-diagnostic não conta como evidência de ataque ativo.

Artefato testado e reempacotado com bytes idênticos:
`4161cf4d88cce2bf887a35be4ef0c768f6c4c8bc14106cc5cd3aec72ef21ad12`.
Comparação com 6.0.1: apenas fabric.mod.json difere; código e assets preservados.

## Reprodução

```powershell
python qa/prepare_emf_extensions_fixture.py --profile 'C:/Users/victo/AppData/Roaming/ModrinthApp/profiles/Fabulously Optimized'
.\gradlew.bat runProductionClientTest -PhairReview -PextensionsReview -PcompatMods=qa/fixtures/tricorne-emf-extensions/mods --console=plain
```

Sem automação do desktop. Minecraft é controlado pela API de GameTests; Blockbench
usa Playwright isolado. Teste rápido só do jogador: acrescente -PplayerReview.
Para OFF, use apenas vanilla no options.txt do diretório de teste e omita compatMods.
Execute Gradle sequencialmente. Instalação: substituir só o JAR antigo do addon;
não instalar o JAR GameTest. `/pirates preset nearest tricorne_classic` continua
restrito ao capitão/OP, adulto próximo/visível e cabeça vazia.

Todos os 80 cabelos legados, combinações em camadas, IA autônoma, armas diferentes,
multiplayer, shaders e profiling seguem sem certificação. animation_safe permanece
false. Não houve remoção de funcionalidades para passar os testes. A comparação
dos JARs deve mostrar apenas fabric.mod.json alterado. Sem Git/remoto neste diretório.
