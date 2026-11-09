# Pirates of MCA — Plano de testes e critérios de aceite

**Baseline sob investigação:** `2.0.0-alpha.5` (não considerada estável).  
**Alvo:** Minecraft `26.2`, Fabric, MCA `8.1.11+26.2`, Fish of Thieves `26.2.2.0`.  
**Objetivo:** confirmar bugs históricos, determinar causa-raiz, implementar correções e impedir regressões sem comprometer o mundo existente.

## 1. Regras de qualidade (gates)

**G0 — Baseline reproduzível.** Guardar SHA256 do JAR, código e dependências. Não declarar que `build_offline.sh` produz a Alpha 5: ele ainda produz Alpha 4 e compila com stubs Java 21. Registrar o estado atual antes de mudar código.

**G1 — Toolchain real.** Configurar Gradle/Loom **não ofuscado** para Minecraft 26.2, Java **25**, Fabric API e Loader compatíveis; compilar contra os dois JARs reais anexados (`reference-jars/`), não usar `generate_stubs.py` ou `extend_stubs.py` para verificação de release. O build precisa fazer `clean build` em checkout limpo, com versões fixadas. O `fabric.mod.json` deve corresponder ao JAR gerado. Não incorporar bibliotecas internas do Minecraft/MCA dentro do addon.

**G2 — Carregamento.** Cliente integrado e servidor dedicado devem iniciar, criar mundo novo, carregar mundo antigo, entrar e sair sem erro de mixin, registry, linkage ou `NoSuchMethodError/FieldError`. Validar também o Fabric Loader 0.20.1 observado no modpack, não exigir downgrade sem evidência.

**G3 — Regressões P0/P1.** Todos os casos críticos deste plano devem passar em jogo (não apenas testes estáticos), com logs, vídeos/capturas e save/reload.

**G4 — Segurança do mundo.** Migrar dados sem perder UUID, nome, gênero, skin MCA, roupa, hearts, profissão ou crew. Falha de migração deve deixar backup e erro claro; não sobrescrever silenciosamente os dados. Suportar crash/restart no meio de save e ofícios/transferências.

**G5 — Confiabilidade e release.** Gerar JAR + sources + `SHA256SUMS` + changelog + relatório com cenários PASS/FAIL/BLOCKED, causas, commits e riscos residuais. Nenhum `BLOCKED` P0 pode ser anunciado como pronto.

## 2. Matrizes e cenários

**Ambientes mínimos**: (A) cliente Fabric com MCA + Fish of Thieves + addon; (B) servidor dedicado com os mesmos mods; (C) Fabulously Optimized 14.1.1 e modpack habitual; (D) mundo novo; (E) cópia do mundo usado nas Alphas anteriores. Usar criativo para gerar NPCs e sobrevivência para validar dano real. Repetir testes de aparência com resource pack/shader desligados e depois ligados para isolar conflitos de renderização.

**Dados de teste**: `male_villager`, `female_villager`, perfis `captain`, `bruiser`, `greedy`, `suspicious`, `friendly`; 2 seaposts em coordenadas diferentes; 2 tripulações de 8 NPCs; capitão NPC e jogador; sobrevivência/criativo; chunk unload/reload, relog, reinício e troca de dimensão.

### 2.1 Bloqueadores: boot, registry e linkage

| ID | Prioridade | Teste / execução | Resultado exigido |
|---|---|---|---|
| TC-BOOT-01 | P0 | `clean build` com Gradle/Loom e dependências reais, Java 25 | Sem stubs, classes ligadas à API correta; build reproduzível |
| TC-BOOT-02 | P0 | Iniciar cliente limpo com 3 mods | Sem crash de mixin; menu e mundo abrem |
| TC-BOOT-03 | P0 | Iniciar servidor dedicado e conectar cliente | Entrypoints server/client e sincronização válidos |
| TC-BOOT-04 | P0 | Criar mundo novo e entrar no save antigo | Sem exception em registries, profissão ou JSON de trade |
| TC-BOOT-05 | P0 | Revalidar `SeapostPiratesMixin` e `MCAPiratesMixin`, alvos e anotações reais | Nenhum `InvalidMixinException` / injeção obrigatória ausente |
| TC-BOOT-06 | P0 | Executar `/pirates captain found`, `/pirates captain force`, `/pirates crew status` | Sem `ResourceKey.location()` inválido ou outro linkage |
| TC-BOOT-07 | P0 | Abrir comércio pirata, recarregar datapacks | Todos os trades válidos; sem `minecraft:chain` obsoleto |
| TC-BOOT-08 | P0 | Reiniciar várias vezes com Fabric Loader 0.20.1 | Sem erro causado por `CommandRegistrationCallback.EVENT` ou Brigadier |
| TC-BOOT-09 | P1 | Recarregar recursos com MCA, Fish of Thieves e outros packs | Sem `NoSuchMethodError Component.translatable` nem valores `unbound` |

### 2.2 Spawn, idade, gênero, identidade visual

| ID | Prioridade | Teste / execução | Resultado exigido |
|---|---|---|---|
| TC-SPAWN-01 | P0 | Spawn 20 `mca:male_villager` com `pmca_gender_male` | 20/20 masculinos, inclusive após relog |
| TC-SPAWN-02 | P0 | Spawn 20 `mca:female_villager` com `pmca_gender_female` | 20/20 femininas; sem troca espontânea de gênero |
| TC-SPAWN-03 | P0 | Spawn 40 NPCs com `Age:0` e `pmca_adult_required` | 40/40 adultos visual e logicamente (`AgeState.ADULT`) |
| TC-SPAWN-04 | P1 | Inspecionar `Age:30/40` e campo `AgeState` | Não interpretar 30/40 como anos; normalização adulta coerente |
| TC-SPAWN-05 | P0 | Spawn 24 NPCs, gravar nome, skin, cabelo, roupa e UUID; viajar 128 blocos, voltar | Mesmos valores; corpo, rosto e cabelo visíveis |
| TC-SPAWN-06 | P0 | Fechar cliente, reiniciar servidor, relogar e inspecionar mesmos NPCs | Roupas persistentes, sem NPC pelado ou skin genérica |
| TC-SPAWN-07 | P0 | Spawn e aguardar 5/30/120 s, incluindo `F3+T` | Sem entidade invisível, sem race de `BodySkinList`/`ClothingList` |
| TC-SPAWN-08 | P1 | Criar 48 NPCs com RNG distinto e contar skins base/roupas | Diversidade verificável, sem forçar a mesma skin; usar roupas MCA sobre pele própria |
| TC-SPAWN-09 | P1 | Pirata com mudança legítima de roupas via MCA e `isClothingLocked` | Não sobrescrever alterações intencionais fora da política configurada |
| TC-SPAWN-10 | P1 | Spawn em seapost natural e com `/summon` por tipo/gênero | As duas rotas funcionam; piratas têm nomes e profissões corretas |
| TC-SPAWN-11 | P1 | Verificar tags `pmca_captain` e cargo CAPTAIN | Perfil não cria liderança involuntária nem crew individual automática |
| TC-SPAWN-12 | P1 | Criar NPC com dados visuais parciais válidos e repetir load | Só campos inválidos são corrigidos; genética e nome existentes preservados |

**Comandos-base para testes de spawn** (cheats ativados, em local seguro):

```mcfunction
/summon mca:male_villager ~ ~ ~ {Age:0,Tags:["pmca_seapost","pmca_bruiser","pmca_gender_male","pmca_adult_required"]}
/summon mca:female_villager ~2 ~ ~ {Age:0,Tags:["pmca_seapost","pmca_friendly","pmca_gender_female","pmca_adult_required"]}
/pirates debug identity
/pirates crew status
```

**Importante:** confirmar no código e no runtime que os comandos acima continuam existindo; `Age:0` sozinho não prova `AgeState.ADULT`.

### 2.3 Combate, fuga e violência não letal

| ID | Prioridade | Teste / execução | Resultado exigido |
|---|---|---|---|
| TC-AI-01 | P0 | Pirata armado diante de zumbi, afogado e esqueleto; sem ordens | Defesa ativa/ataque em vez de fugir como villager comum (salvo retirada por pouca vida) |
| TC-AI-02 | P1 | Vida do pirata cai para 20–25% contra monstros | Recuo contextual, não pânico de villager de vida cheia |
| TC-AI-03 | P1 | Comparar pirata e villager MCA comum contra o mesmo monstro | Só pirata recebe IA combatente; villagers comuns preservados |
| TC-AI-04 | P0 | Forçar briga entre tripulantes com espada de dano alto | Nenhum golpe da briga reduz a vida abaixo de 25%, mesmo golpe fatal |
| TC-AI-05 | P0 | Durante briga, aplicar dano de jogador, queda, fogo e mob | Proteção não letal não cria imunidade global nem bloqueia dano externo |
| TC-AI-06 | P1 | Capitão acerta subordinado 1x, 5x e 10x | Sem ataque reflexo imediato; medo/ressentimento acumulam; motim possível |
| TC-AI-07 | P1 | Piratas iniciam briga não letal; contramestre tenta separar | Capitão não entra em briga trivial; terceiros não causam massacre |
| TC-AI-08 | P1 | Testar agressão em modo criativo/sobrevivência | Reputação muda nos dois; dano contra jogador respeita regras de modo |
| TC-AI-09 | P1 | Pirata luta com monstro perto de aliado ou capitão | Sem fogo amigo por IA de guarda, salvo motim declarado |
| TC-AI-10 | P1 | Afastar-se durante briga e retornar | Combates expiram/encerram de modo consistente; não prosseguem indefinidamente |

### 2.4 Tripulação, capitão jogador e ordens

| ID | Prioridade | Teste / execução | Resultado exigido |
|---|---|---|---|
| TC-CREW-01 | P0 | Criar dois NPCs livres, `/pirates captain found` | Uma crew com dois adultos; jogador único capitão |
| TC-CREW-02 | P0 | `/pirates captain force` como OP, com 1 NPC e crew existente | Assume liderança sem crash nem dupla capitania |
| TC-CREW-03 | P1 | Mesmo comando sem permissão de operador | Recusado de forma segura |
| TC-CREW-04 | P1 | `/pirates captain claim` com <40 hearts e depois >=40 hearts de 50% | Falha e sucesso de acordo com requisitos, com mensagem exata |
| TC-CREW-05 | P0 | Dois piratas invocados livres, 2 seaposts diferentes e proximidade | Sem fusão ou capitanias individuais acidentais |
| TC-CREW-06 | P1 | Recrutar NPC e salvar/recarregar, conferir entity tag vs ledger | `pmca_crew_...` consistente com UUID de tripulação persistida |
| TC-CREW-07 | P1 | Nomear imediato e contramestre, trocar cargo do mesmo NPC | Nunca acumular dois cargos ativos; somente um ocupante por posição |
| TC-CREW-08 | P1 | Ordens FOLLOW/HOLD/RALLY/GUARD/ATTACK/RETREAT/CEASE_FIGHT/DISMISS | Executa ou recusa com motivo; ordens expiram, não se repetem infinitamente |
| TC-CREW-09 | P1 | Jogador desloga/reloga com tripulação | Imediato atua temporariamente; não há capitão NPC permanente paralelo |
| TC-CREW-10 | P1 | Renúncia e rendição durante motim | Perda de autoridade persistente e sucessão válida |
| TC-CREW-11 | P1 | Testar capitão NPC e capitão jogador simultâneos | Invariante de um capitão efetivo por crew |

### 2.5 Motim, consequências e eventos sociais

| ID | Prioridade | Teste / execução | Resultado exigido |
|---|---|---|---|
| TC-SOC-01 | P0 | 8 tripulantes elegíveis, capitão jogador, 3 vs 4 rebeldes | 3 não bastam; 4 podem bastar se demais condições atendidas |
| TC-SOC-02 | P0 | 8 membros totais incluindo capitão NPC (7 elegíveis), 3 rebeldes | Limiar correto `max(2, ceil(0.4*7))=3` (baseline falha) |
| TC-SOC-03 | P1 | Ressentimento >=65, ambição >=55; recrutar conspiradores | Conspiração pode surgir sem motim imediato; exige apoio suficiente |
| TC-SOC-04 | P1 | Disparar ultimato contra capitão jogador online | Aviso durante 60s carregados e opção de render-se |
| TC-SOC-05 | P1 | Capitão jogador offline | Não executar combate ou ultimato offline sem aviso |
| TC-SOC-06 | P1 | Motim com rendição/negação e morte do capitão | Resultado consistente, capitão eleito sem duplicidade |
| TC-SOC-07 | P1 | Tripulante mata outro; testemunhas presentes/ausentes | Rumores/reputação apenas quando testemunho ou evento real; sem telepatia |
| TC-SOC-08 | P1 | Amigo/inimigo da vítima testemunha assassinato | Consequências diferenciadas; sem penalidade duplicada no reload |
| TC-SOC-09 | P1 | Guardar 130 eventos e salvar/recarregar | Mantém somente os 128 mais recentes, sem perdas inesperadas |
| TC-SOC-10 | P2 | Aliados/rivais de outro navio encontram-se | Diplomacia só muda por contato/eventos; **se ainda não implementado, marcar BLOCKED** |

### 2.6 Profissão, comércio, mundo e desempenho

| ID | Prioridade | Teste / execução | Resultado exigido |
|---|---|---|---|
| TC-DATA-01 | P0 | Validar 20 ofertas, 5 trade sets, todos IDs e tags | Sem referências inválidas; comércio abre em todos os 5 níveis |
| TC-DATA-02 | P1 | Comparar pirata e fisherman comum | Profissão própria e trades exclusivos só nos piratas |
| TC-DATA-03 | P1 | Recarregar datapack (`/reload`) e reabrir trades | Sem registry errors e sem perda de profissão |
| TC-SAVE-01 | P0 | Fazer backup, migrar mundo Alpha 1–5, relogar | Sem alteração arbitrária de nome, gender, skin, hearts, outfit, crew, UUID |
| TC-SAVE-02 | P0 | Reiniciar servidor entre transferência e salvamento | Estado íntegro ou recuperação explícita; sem crew fantasma |
| TC-SAVE-03 | P1 | Viajar entre dimensões e aproximar 2 navios | IDs estáveis; sem unir tripulações distintas |
| TC-SAVE-04 | P1 | Trocar de mundo no mesmo processo/servidor; reload | Sem vazamento de dados dos mapas estáticos `LOADED`/`BRAWLS` |
| TC-PERF-01 | P1 | 4, 12, 24 e 48 NPCs por 10 min com spark/profiler | Capturar ms/tick, p95, memória, IA/pathfinding; comparar baseline sem addon |
| TC-PERF-02 | P1 | Chunk unload/reload repetido com 48 NPCs | Sem aumento sustentado de objetos retidos, thrashing de IA ou saves excessivos |
| TC-COMPAT-01 | P1 | Fabulously Optimized + Friends & Foes + MCA/Fish | Sem colisões de mixin, registries ou renderer |

## 3. Evidências mínimas para marcar PASS

- **Bug de skin/roupa/nome/gênero/idade:** vídeo ou screenshots antes e após 1) afastar-se 128 blocos, 2) relog, 3) reinício, além da saída `/pirates debug identity` e UUID de cada NPC.
- **Bug de IA:** vídeo curto com pirata e villager MCA comum enfrentando a mesma ameaça, vida documentada, barra de dano e logs.
- **Bug de capitão:** stdout e mensagens dos comandos, roster e confirmação após reinício; não basta a mensagem de comando ter sido enviada.
- **Bug de crash:** `latest.log` + crash-report/stack original, build SHA256, primeiro `Caused by` atribuível; servidor e cliente devem iniciar sem a falha.
- **Bug de morte/briga:** instrumentar `DamageSource`, vida antes e depois e contexto de combate. Testar golpes cujo dano exceda a vida restante.
- **Desempenho:** perfil A/B mesmo mapa, mesmos mods/seeds/distâncias, contagens de entidades e versão da JVM.

## 4. Sequência de trabalho e stop conditions

1. Criar branch `fix/alpha5-regression`, sem alterar saves originais. Executar `qa/static_audit.py` e `qa/run_ledger_regressions.sh`; registrar as falhas esperadas.
2. Corrigir o build de release, gerar ambiente real e rodar `clean build`; impedir o uso dos stubs como prova de runtime.
3. Sanar bloqueadores TC-BOOT; não avançar para IA/hierarquia com startup instável.
4. Reproduzir e corrigir TC-SPAWN críticos. Não resolver problema de roupa tornando NPC artificialmente sempre vestido se ele perdeu a skin base.
5. Corrigir TC-AI/TC-CREW/TC-SOC, acrescentando testes unitários + testes integrados para cada bug.
6. Validar trade/registry, migração e performance; testar no modpack completo.
7. Entregar um JAR verificável, fontes, suite, tabela real PASS/FAIL/BLOCKED e changelog, explicando limitações existentes.

**Falha crítica:** se o agente não puder iniciar Minecraft 26.2, deve entregar melhorias de fonte e testes estáticos, mas marcar todos os testes in-game como `NOT RUN` ou `BLOCKED`. Nunca afirmar que os bugs foram corrigidos sem evidência in-game.

## 5. Referências externas de toolchain

- Fabric 26.2: https://www.fabricmc.net/2026/06/15/262.html
- Loom para Minecraft 26.2: https://docs.fabricmc.net/develop/loom/
- API `CommandRegistrationCallback` 26.2: https://maven.fabricmc.net/docs/fabric-api-0.155.2%2B26.2/net/fabricmc/fabric/api/command/v2/CommandRegistrationCallback.html
- Documentação do MCA: consultar o JAR exato incluído em `reference-jars/`; não pressupor APIs de outras versões.
