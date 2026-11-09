# Prompt para agente — Pirates of MCA v2.0 (estabilização, correções e testes de regressão)

Atue como engenheiro sênior de mods Minecraft/Fabric, com experiência em Java 25, Fabric Loom 26.2, mixins Sponge, MCA Reborn e testes de integração de gameplay. Sua missão é **assumir o desenvolvimento do projeto Pirates of MCA a partir do snapshot Alpha 5**, investigar falhas históricas, corrigir causas-raiz, finalizar o escopo previsto no documento de especificação e produzir artefatos verificáveis.

## Materiais obrigatórios (leia antes de alterar)

1. `source/` — **código-fonte completo da Alpha 5**, sem alterações prévias.
2. `docs/ESPECIFICACAO_ORIGINAL_v2.0.md` — contrato de produto e comportamento aprovado. Preserve as regras sociais e hierárquicas.
3. `docs/01_PLANO_DE_TESTES.md` — suíte de regressão, cenários, prioridades e evidências.
4. `docs/03_BUGS_E_RISCOS.md` — histórico dos bugs relatados, riscos identificados e resultados reproduzidos.
5. `docs/AUDITORIA_ESTATICA_EXECUCAO.txt` e `docs/TESTES_LEDGER_EXECUCAO.txt` — **baseline real**. Não confunda falhas já conhecidas com regressões introduzidas agora.
6. `baseline/PiratesMCA_Alpha5_CrewRuntime_FIX.jar` — binário referência para diff e rastreamento, **não fonte confiável de correção**.
7. `reference-jars/mca-fabric-8.1.11+26.2.jar` e `reference-jars/fish_of_thieves-mc26.2-v26.2.2.0-fabric(3).jar` — dependências EXATAS enviadas pelo usuário. Não modificar os JARs dos mods de terceiros.
8. `qa/` — testes e template de evidência.

## Resultados obrigatórios

- Um repositório Java com build Gradle/Fabric Loom **real**, versionado e reprodutível, compatível com MC **26.2** e **Java 25**; integrar MCA/Fish of Thieves e Fabric API reais. O perfil do usuário usa FO 14.1.1/Modrinth e Fabric Loader 0.20.1; validar compatibilidade real antes de sugerir trocas de versão.
- Testes automatizados para domínio, serialização, hierarquia, comércio e contratos de mixins. Usar GameTest ou ambiente de teste in-game sempre que possível para spawn, ataque, capitania e save/reload.
- Um JAR de teste/entrega, ZIP do código-fonte, scripts necessários, relatórios de testes, SHA256 e changelog por correção.
- Uma tabela `TC-ID | status PASS/FAIL/BLOCKED/NOT RUN | evidência | causa-raiz | commit`, cobrindo todos os testes P0/P1 e os P2 pertinentes. Nada de afirmações de funcionamento sem execução observável.

## Política de diagnóstico e execução

1. **Antes de corrigir**: faça inventário de código, arquivos, branches, manifest, JARs, versões de Loader/API/Java, métodos realmente exportados e estrutura de dados. Reproduza os testes da baseline (`python3 qa/static_audit.py`, `bash qa/run_ledger_regressions.sh`); capture logs e `git diff` limpo.
2. **Build real**: a baseline usa `generate_stubs.py`, `extend_stubs.py` e `build_offline.sh` para compilar contra interfaces falsas Java 21. Isso provocou erros de `NoSuchMethodError/NoSuchFieldError`. **Elimine qualquer dependência desses stubs como gate de release**. Configure `net.fabricmc.fabric-loom` apropriado para 26.2, Gradle e Java 25. Use os JARs verdadeiros; atualize scripts antigos que ainda apontam à Alpha 4. Não simplesmente reempacote classes do JAR Alpha 5.
3. **Fixar contratos**: valide assinaturas de `CommandRegistrationCallback.EVENT`, Brigadier, `ResourceKey`, `Component.translatable`, `ItemStack`, `VillagerEntityMCA`, `@Mixin`, `@Inject`, `@ModifyVariable`, `hurtServer` e marcadores de seapost com bytecode/API reais, verificando que cada injeção é aplicada no boot. Não use `remap=false` sem entender o target e namespace.
4. **Iteração segura**: reproduza um bug por vez, escreva um teste que falha, corrija a causa, execute `clean build` e a suíte, então valide no jogo. Pare de aplicar patches de signature por tentativa e erro sem comparar o JAR real; não esconda warnings.
5. **Integridade**: mantenha backups; não mexa em mundo original nem corrompa dados. Qualquer migração deve ser idempotente e com versão. Identificar mundos sem `crewId` sem aglutinar NPCs por proximidade.
6. **Relatório honesto**: se não tiver acesso ao Minecraft/GUI/servidor, execute todos os testes possíveis no código, deixe os in-game explicitamente `BLOCKED/NOT RUN` e entregue os comandos, fixtures e procedimentos para o usuário ou agente com GUI validar. Não crie screenshots ou logs fictícios.

## Correções prioritárias que precisam de prova

**A. Inicialização e regressões de crash**
- `InvalidMixinException`: annotation/retention/método alvo de `SeapostPiratesMixin`.
- `Component.translatable` e `ResourceKey.location` (erro confirmado ao executar `captain found`), `CommandRegistrationCallback$Event` e Brigadier.
- Registry `villager_trade` inválida (especialmente antigo `minecraft:chain`); testar todos os 20 trades e 5 níveis após `/reload`.
- Client integrado e servidor dedicado, com o ambiente de FO, MCA e Fish of Thieves.

**B. NPCs e aparência**
- Corrigir NPC invisível, sem nome, sem corpo/rosto, skin genérica, roupa de pirata ausente após carregar chunks, relog ou reiniciar. Manter a pele/rosto/cabelo **do MCA por baixo das roupas**, e 24 variantes masculinas + 24 femininas sem padronizar a skin corporal.
- Corrigir `/summon mca:male_villager` gerando mulher; o tipo de entidade/tags deve determinar o gênero, preservado na genética e no render.
- Corrigir `Age:0` ainda produzindo bebê; sincronizar `AgeState.ADULT` sem reinterpretar `30/40` como anos e preservar idade em saves.
- Personalidade MCA compatível com perfil (`CRABBY`, `GREEDY`, `ANXIOUS`, `FRIENDLY`), mas cargo hierárquico **não** é profissão/perfil. Os diálogos não podem dar hearts positivos sem resistência prevista para perfis hostis.

**C. IA e hierarquia**
- Piratas adultos defendem-se de monstros e são mais ousados que villagers comuns; comportamento de fuga só quando feridos/ameaça excessiva. Testar guard brain do MCA contra zumbi, afogado e esqueleto.
- `/pirates captain found`, `/pirates captain force`, `/pirates captain claim`, `/pirates crew status`, `roster`, `recruit`, nomeação de oficiais e ordens precisam funcionar sem crash, com feedback e persistência. Piratas de `/summon` são **livres**, não capitães automáticos. `pmca_captain` inicialmente identifica *perfil*, não liderança.
- **Dois bugs JÁ REPRODUZIDOS POR TESTE:** `CrewLedger.requiredRebels()` inclui capitão NPC no denominador (`LEDGER-03`); `CrewLedger.officer()` permite que um NPC acumule dois cargos (`LEDGER-04`). Corrija implementação, mantenha os testes e obtenha PASS real.
- Brigas comuns (15% após provocação) são não letais com limite de 25% da vida *antes do dano*. Capitão não intervém em conflitos triviais; agressão do capitão cria ressentimento e possível conspiração. Motim exige 65+ ressentimento, apoio mínimo de 40% dos adultos elegíveis, 60 segundos de ultimato, rendição ou combate e sucessão, inclusive contra jogador capitão.
- Corrigir o ciclo de `CONSPIRACY -> ULTIMATUM`: conspiração pode começar com líder ressentido e depois recrutar aliados; não iniciar motim sem base política. Apoio e eventos devem ser causalmente justificados.
- Separar cargos, personalidades, profissões e roupas; jogadores não podem usar comandos administrativos sem OP. Multiplayer server-side e ordens sem spoofing.

**D. Persistência e eficiência**
- Auditar `CrewRuntime.LOADED`, `BRAWLS`, `LEDGERS`, `lastSaveTick`, atualização por dimensão, cleanup de entidade e save/reload; garantir que não ocorra retenção de entidades ou cross-world bleed.
- Migrar `.properties` para mecanismo robusto `SavedData` se viável, mantendo importação compatível e testada. Adicionar testes de interrupção/recuperação, backup e idempotência.
- Instrumentar simulações com 4/12/24/48 NPCs e medir tempo/tick, p95, pathfinding, heap e efeitos de descarregar chunks.

## Comandos mínimos para rodar durante regressão

```mcfunction
/summon mca:male_villager ~ ~ ~ {Age:0,Tags:["pmca_seapost","pmca_bruiser","pmca_gender_male","pmca_adult_required"]}
/summon mca:female_villager ~2 ~ ~ {Age:0,Tags:["pmca_seapost","pmca_friendly","pmca_gender_female","pmca_adult_required"]}
/pirates debug identity
/pirates crew status
/pirates captain found
/pirates captain force
/pirates crew roster
/pirates order nearby follow
```

Execute `/pirates captain found` com ao menos 2 piratas livres e `force` com OP apenas. Para cada NPC, verifique UUID, sexo, idade, `AgeState`, skin, `clothes`, personalidade, hearts, cargo e `crewId` antes/depois de 128 blocos, relog e restart. Não assumir que uma mensagem de sucesso do comando prova alteração do mundo.

## Ordem de entrega e critérios de parada

**Fase 0** relatório baseline + prioridades + riscos.  
**Fase 1** toolchain e boot real + correção dos crashes.  
**Fase 2** identidade/skins/idade/gênero/roupas + regressões in-game.  
**Fase 3** IA hostil a monstros + combate não letal + segurança.  
**Fase 4** capitão jogável, tripulações, comandos, motins, relações e persistência.  
**Fase 5** testes completos, profiling, changelog, release e relatório final.

**Não começar nova feature sem estabilizar a anterior. Não atribuir `PASS` aos casos que não foram executados.** Quando um teste falhar, registre `onde falhou -> causa confirmada ou hipótese -> correção -> evidência do novo teste`.

**Formato do relatório final:** resumo executivo (versão e SHA256), tabela de requisitos/bugs/status, comandos e resultados realmente observados, logs relevantes, mudanças por arquivo, performance comparativa, riscos restantes, conteúdo final do ZIP + JAR. Use a especificação v2.0 como contrato, mas cite explicitamente funcionalidades ainda não implementadas em vez de alegar completude.
