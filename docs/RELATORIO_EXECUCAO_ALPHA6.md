# Relatório de execução — 2.1.0-alpha.6

Data: 2026-11-03. **Auditoria e correções parciais; contrato v2.0 não concluído.**
O JAR é experimental. PASS de uma regressão não certifica funcionalidades ausentes
nem substitui cenários mais amplos do plano. A matriz de 62 casos está em
[`matriz congelada 2.1`](../qa/results/checkpoint-2.1.0-alpha.6/TEST_CASES.csv), com evidência e limite por ID.

**Resultado da matriz:** 37 PASS, 0 FAIL atual, 7 BLOCKED e 18 NOT RUN.
Os bloqueados e não executados impedem certificar a missão como concluída.
Automação de servidor: **15 métodos JUnit e 22 GameTests obrigatórios PASS**
(21 do addon e um teste vazio interno do Fabric). As suítes antigas estão
incluídas nos métodos JUnit, sem inflar artificialmente essa contagem.

## Ambiente e isolamento

- Windows, JDK Amazon Corretto 25.0.2, Gradle Wrapper 9.5.1 com SHA256 fixado,
  Fabric Loom 1.17.21; Minecraft 26.2 real, sem mappings artificiais ou stubs.
- Fabric Loader 0.20.1, Fabric API 0.155.2+26.2 no cenário isolado.
- MCA 8.1.11+26.2 e Fish of Thieves 26.2.2.0 exatamente dos JARs fornecidos.
  YACL 3.9.7+26.2-fabric é dependência obrigatória do Fish, detectada no boot real.
- Cliente real com NVIDIA RTX 3080; testes automatizados Fabric Client GameTest.
  Servidores/mundos em `run/` e `build/run/`; nenhum save pessoal foi modificado.
- Aceitação da EULA autorizada pelo usuário somente nesses servidores isolados.
- FO 14.1.1 obtido do mrpack oficial, SHA512 do pacote e dos downloads verificados.
  O pacote declara Loader 0.19.5; o teste usa **0.20.1**, como solicitado, e API
  **0.161.0+26.2** do pacote. Adicionais: Friends & Foes 5.0.4+mc26.2 e
  ResourcefulLib 5.0.4, copiados sem alterar o perfil pessoal. Inventário verificado
  em `qa/fixtures/fo-14.1.1/verified-mods.json`.
- Sem repositório `.git` ou remoto GitHub no material recebido: não houve commit,
  push, tag ou release remota. Não é uma falha de autorização.

## Baseline antes das correções

Todos os documentos, classes Java, recursos e scripts foram lidos antes das edições.
Os documentos originais, JAR Alpha 5 e hashes históricos foram preservados.

| Execução | Resultado original | Evidência |
|---|---|---|
| Auditoria estática fornecida | 8 PASS, 3 FAIL, 2 WARN | `qa/results/baseline/` |
| LedgerRegressions fornecido | 5 PASS, 2 FAIL | `qa/results/baseline/` |
| Suíte ledger original da fonte | PASS | `qa/results/baseline/` |
| Regressões adicionais de segurança, antes do patch | 1 PASS, 11 FAIL | `qa/results/baseline/` |

As duas falhas confirmadas foram reproduzidas: `LEDGER-03` contava capitão NPC no
denominador do motim; `LEDGER-04` mantinha o mesmo NPC em dois cargos.
Nenhuma compilação com stubs foi usada como evidência de release.

O JAR Alpha 5 fornecido também foi iniciado em servidor dedicado de produção,
com dependências reais: chegou a `Done (1.841s)` sem crash de inicialização
(`qa/results/baseline/alpha5-production-boot.log`). Seu banner anunciou Alpha 4,
confirmando a divergência de versão histórica. Após essa observação o processo
isolado foi encerrado pelo auditor; o código de saída desse encerramento não é
uma falha de boot. Portanto não se atribuem à Alpha 5 os crashes de versões mais
antigas que essa execução não reproduziu.

## Execuções atuais

| Grupo | Evidência principal | Escopo |
|---|---|---|
| `clean build` | `qa/results/final-clean-build.log` | JUnit + GameTests reais de servidor, JAR e sources |
| Estrutura/bytecode | `qa/audit_release.py`, `source/verify_commands_runtime_contract.py` | Java class version 69, recursos, referências, ausência de stubs/bibliotecas embutidas |
| Cliente isolado | `qa/results/isolated-48-final.log` | 48 NPCs, comandos, metadados, reload, chunks, dimensões e servidor integrado |
| Reinício completo do cliente | `qa/results/fo-full-process-restart.log` | Mesmos 48 UUIDs, identidades e crew em JVM distinta; encerramento normal |
| FO em produção | `qa/results/fo-48-final.log` | Asserções funcionais passaram; encerramento travado, compatibilidade BLOCKED |
| Reprodutibilidade | `qa/results/reproducibility.json` | Comparação SHA256 de dois builds limpos |

Os logs de cliente registram `QA_ARTIFACT_SHA256`, vinculando o teste ao binário.
Logs intermediários com versões 2.0.1 ou protótipos de teste foram preservados
como investigação; as linhas finais da tabela acima identificam a validação atual.
O JAR de testes não faz parte da distribuição normal. JUnit inclui wrappers das
suítes antigas: não somar suas asserções como se fossem métodos JUnit independentes.

O cliente usa 48 NPCs, 24 por gênero, `Age:0`, marcador adulto e **NoAI para a
inspeção visual**. Isso testa identidade, não desempenho de pathfinding. A IA
combatente é exercitada separadamente com NPCs ativos nos GameTests de servidor.
O roteiro verifica UUID/nome/pele/roupa/cabelo/gênero servidor↔cliente e ADULT,
diversidade, texturas compostas, checkpoints de tempo e recarga real de recursos.
Depois descarrega os chunks fora do spawn, confirma ausência de cada UUID no
servidor, retorna e compara; fecha o servidor integrado e reabre o mesmo mundo.
Uma execução adicional reiniciou o cliente inteiro: PID 30728 → 11556, mesmo UUID
do jogador, identidade de todos os 48 NPCs e crew comparadas após carregar o save
do processo anterior. A primeira JVM foi terminada pelo auditor após o mundo já
ter sido salvo/fechado e ocorrer o hang descrito adiante; a segunda encerrou normalmente.

## Causas e provas por bug histórico

| Bug | Diagnóstico e mudança | Validação e limite |
|---|---|---|
| BUG-01 | Build anterior não verificava assinaturas/anotações contra APIs reais. Novo Loom usa os JARs reais e mixins obrigatórios. | Classes MCA/Fish carregadas pelo loader e GameTests. Seapost natural ainda não executado. |
| BUG-02 | Risco de descriptor binário incorreto de `Component.translatable` no compilador por stubs. | Boot, profissão, comandos e bytecode compilados contra Minecraft real; sem erro nesse roteiro. Não alegar reprodução do crash histórico em Alpha 5 sem log específico. |
| BUG-03 | ID `minecraft:chain` já estava corrigido no snapshot Alpha 5. | 20 ofertas auditadas, cinco níveis consultados no servidor; UI + `/reload` pendentes. |
| BUG-04 | Descriptor de `CommandRegistrationCallback.EVENT` precisa vir da Fabric API real. | Registro e comandos executados em Loader 0.20.1. |
| BUG-05 | Stubs permitiam contratos inexistentes como `ResourceKey.location()`. Fonte atual usa API compilada real. | `found`, `force`, `status` executados; verificador de bytecode complementar PASS. |
| BUG-06/07 | Causa histórica **não reproduzida de forma válida**. Reparo parcial rerandomizava genética, roupa válida bloqueada podia ser sobrescrita; esses defeitos foram corrigidos e testados separadamente. | Metadados/texturas/unload/reabertura passam no cenário atual. Não declarar a causa histórica de invisibilidade/roupa perdida como confirmada. |
| BUG-08 | Normalização de nome MCA preservada; ausência tratada pelo fluxo existente. | Nome não vazio e mesmo nome por UUID após unload/reabertura. Não foi reproduzida regressão nova de nomes na Alpha 5. |
| BUG-09 | Normalização por tipo/tag de gênero já existia; sem rerrolar genética válida durante reparo. | 24 masculinos + 24 femininos no cliente, persistência comparada. |
| BUG-10 | Vanilla Age não representa anos humanos MCA; adulto precisa de AgeState coerente. | GameTests Age 0/30/40 e 48 ADULT no cliente. |
| BUG-11 | Tick reaplicava personalidade/humor e primeira abordagem podia apagar hearts existentes. Migração de perfil aplica uma vez; customizações e hearts não neutros permanecem. | `mcaCustomizationSurvivesRepairTick`, `existingMcaHeartsSurviveFirstMeet`. Diálogos completos por perfil não certificados. |
| BUG-12 | Guarda MCA e fallback de combate precisam funcionar com APIs reais; cooldown por coincidência de módulo era frágil. | NPC ativo combate zumbi, afogado e esqueleto; cada alvo recebe dano. Recuo completo e todos os contextos ainda pendentes. |
| BUG-13 | Capitão automático de summon impedia rota livre; fluxo atual distingue UNASSIGNED e crews naturais. | `found` com dois livres; force recruta somente livres ou assume crew existente sem mesclar outras; permissões verificadas. Fundação pela bandeira ainda ausente. |
| BUG-14 | `officer()` atualizava campo/cargo sem limpar o outro slot. | Baseline FAIL → regressões PASS; cliente troca FIRST_MATE→QUARTERMASTER e confirma exclusividade. |
| BUG-15 | `requiredRebels()` usava tamanho do roster incluindo capitão NPC. | Baseline FAIL → PASS; limiar de 7 elegíveis é 3; crianças/mortos/capitão não votam. |
| BUG-16 | Scripts e nomes de artefato estavam presos à Alpha 4 e build offline por stubs. | Wrapper/Loom reais, scripts encaminhados, metadados/runtime/changelog sincronizados; hashes de builds. |

Uma falsa reprodução visual foi descartada: a tela inicial MCA DestinyScreen
pausava o servidor integrado da fixture. Reenvio manual de metadados aparentou
resolver, mas o controle com onboarding concluído passou sem esse reenvio.
O patch de rede experimental foi **removido**, e a fixture foi corrigida. As
capturas antigas permanecem identificadas como investigação, não prova de correção.

## Correções adicionais e regressões

| Problema | Causa-raiz | Teste |
|---|---|---|
| Golpe de briga fatal/efeitos retardados | Proteção precisa atribuir fonte antes do dano e efeitos secundários da própria arma | `lethalBrawlHitAndExternalDamage`, Fire Aspect e efeito nocivo contextual; política pura JUnit |
| Guarda ataca aliado/capitão por retaliação trivial | Brain MCA ignora semântica social do duelo | Friendly fire GameTest; captain assault mantém consequência social |
| Contramestre encerra só um lado | Duelo é armazenado em duas entradas | `quartermaster` GameTest verifica ambos e evento de apartação |
| Morte ambiental conserva roster/cargo | Tratamento dependia de atacante no callback de dano | AFTER_DEATH real + queda fatal, regressões de morte idempotente |
| Referências cruzam unload/servidor | Mapas globais de NPCs/duelos sem lifecycle | `unloadCleansIndex`, fechamento/reabertura do servidor integrado |
| NPC capitão descarregado deposto | Ausência interpretada como morte | `absentCaptainIsNotDead` |
| Ultimato expira offline | Deadline absoluto de mundo | JUnit pausa offline/reload; aviso ao jogador ainda sem teste ponta a ponta |
| Conspiração exige maioria desde início | Condição do motim aplicada ao recrutamento | Regressão startConspiracy antes do limiar; beginUltimatum exige apoio |
| Assassinato penaliza testemunha novamente | Sem deduplicação persistente por evento | JUnit testemunha amigo/inimigo e reload; rumores completos ausentes |
| Snapshot parcialmente salvo ou sobrescrito após erro | Gravação direta/falha silenciosa e campos omitidos | Falha de escrita injetada, backup exato, retry, timestamps e ordens roundtrip |
| Corrupção vira ledger vazio regravável | Parse tolerava perda de referências/schema | Rejeição de schema/roster/capitão ausente/membro órfão mantendo bytes originais |
| Roupa pirata contamina pescador comum | Clothing JSON declarava profissão `null` | GameTest do pool original falhou; profissão explícita passou |
| `claim` depõe capitão ativo | Só validava hearts, sem vaga; denominador só dos NPCs próximos | GameTest exige vaga, recusa sem apoio e aceita 40 hearts/50%; denominator usa adultos vivos do roster |

## Limitações que impedem concluir a missão

1. **Contrato funcional incompleto:** bandeira colocável e menu de fundação,
   tesouraria/baús/recompensas reais, PARDON contextual, diálogos MCA, livro/HUD,
   administração completa, gestão interina do imediato, ATTACK com alvo e HOLD
   com âncora, diplomacia, rumores e eventos estruturados completos. Provocação
   ainda usa probabilidade periódica por perfil/ressentimento, sem toda a
   causalidade exigida. Não removemos recursos para passar testes.
2. **Persistência parcial:** Properties v2 reforçado, não SavedData/codec nativo.
   Fixtures sintéticas de dados legados não substituem mundos Alpha 1–5 reais.
   Não houve kill do processo no intervalo transferência/save nem migração
   histórica ponta a ponta. Schema desconhecido bloqueia carga preservando arquivo;
   não há UI de recuperação automática.
3. **Validação pendente:** conexão cliente→dedicado,
   seapost natural, UI de comércio com reload de datapacks, matriz inteira de dano
   externo, ordens e motim letal/rendição online. Veja cada NOT RUN/BLOCKED no CSV.
4. **Desempenho não certificado:** faltam perfis comparativos de dez minutos para
   4/12/24/48 NPCs com CPU, heap, p95, pathfinding e baseline sem addon; faltam
   ciclos repetidos de unload para medir retenção. Nenhuma cifra de performance
   foi inventada a partir do teste visual NoAI.
5. **Compatibilidade delimitada:** smoke automatizado do FO não cobre shaders,
   controle, multiplayer ou cada interação entre mods. Modo de desenvolvimento
   falha no mixin `modernfix-common.mixins.json:devenv.MinecraftMixin`
   (`createUserApiService` STATIC incompatível); modo de produção foi utilizado
   sem remover ModernFix. Logs conservam avisos externos e erros de autenticação
   Realms do usuário de teste offline; não são apresentados como execução sem erros.
   A rodada final de 48 NPCs concluiu asserções, salvou e reabriu o mundo, mas a JVM
   permaneceu presa após `Stopping`/`FastQuit Exiting` por mais de 120 segundos.
   `jcmd Thread.print` não respondeu; somente o processo isolado foi encerrado
   pelo auditor. Causa-raiz ainda não estabelecida, sem atribuição ao addon ou a
   um mod específico. `fo-shutdown-observation.json` registra a ocorrência.
   O cenário TC-COMPAT-01 permanece **BLOCKED**, embora rodadas anteriores mais
   curtas tenham encerrado normalmente.

## Fontes técnicas verificadas

- Fabric 26.2: https://www.fabricmc.net/2026/06/15/262.html
- Loom: https://docs.fabricmc.net/develop/loom/
- Testes Fabric: https://docs.fabricmc.net/develop/automatic-testing
- Pacote oficial: https://modrinth.com/modpack/fabulously-optimized/version/14.1.1

Os JARs locais e seus `fabric.mod.json`/`javap` foram a fonte principal dos
contratos MCA/Fish; não se presumiu compatibilidade por nomes semelhantes.
