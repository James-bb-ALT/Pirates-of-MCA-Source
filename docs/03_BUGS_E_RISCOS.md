# Inventário de bugs, evidências, riscos e pontos do código

Este documento distingue relato do usuário, defeito reproduzido por teste e hipótese. Não equiparar compilação com stubs a execução real.

| ID | Sintoma reportado / histórico | Estado na Alpha 5 | Área mais provável | Prova requerida |
|---|---|---|---|---|
| BUG-01 | Primeira versão derrubava o jogo (`SeapostPiratesMixin` sem `@Mixin` reconhecida) | Correções anteriores não substituem smoke test | `mixin/SeapostPiratesMixin.java`, anotação e build | Mixin aplicado no loader real |
| BUG-02 | `NoSuchMethodError Component.translatable` | Regressão a prevenir | `PirateProfession.java` | Startup e bytecode real |
| BUG-03 | `minecraft:chain` inválido em registry | Auditoria JSON atual PASS, runtime não testado | `data/piratesmca/villager_trade` | `/reload`, UI de trades, todos níveis |
| BUG-04 | `NoSuchFieldError CommandRegistrationCallback$Event` | Regressão a prevenir | `PirateCommands.register()` | Carregamento Fabric 26.2 real |
| BUG-05 | `NoSuchMethodError ResourceKey.location()` em `/pirates captain found` | Bytecode Alpha 5 não usa chamada antiga; runtime não testado | `CrewRuntime.register()` e `stableCrewId()` | Fundar/forçar crew sem crash |
| BUG-06 | NPC invisível ou sem corpo, depois skin genérica | **Não confirmado corrigido** | `PirateBehavior.ensureMcaIdentity`, `CrewRuntime.enforceGender`, MCA resource lists | Spawn + unload/reload + relog + restart |
| BUG-07 | Roupas desaparecem; NPC 'pelado' depois de ir embora e voltar | **Não confirmado corrigido** | `ensureOutfit`, `outfitIndex`, clothing JSON, camada de render | Outfit e pele distintos e persistentes |
| BUG-08 | Piratas sem nome | **Não confirmado corrigido** | `Names.pickCitizenName`, init NBT MCA | Nome preservado após reload |
| BUG-09 | Spawn só feminino mesmo tipo masculino | **Não confirmado corrigido** | `enforceGender`, genética e tags | 20 male + 20 female consecutivos |
| BUG-10 | Spawn como bebê/criança mesmo Age:0 | **Não confirmado corrigido** | `AgeState`, `setAge(0)`, spawn NBT | `AgeState.ADULT` pós-reload |
| BUG-11 | Personalidades Flirty/Peaceful/Upbeat e conversas positivas | **Não confirmado corrigido** | `PirateBehavior.setup/keepProfile/playerRelationship` | Personality correto e hearts por perfil |
| BUG-12 | Pirata foge de monstros como villager normal | **Não confirmado corrigido** | `MCAPiratesMixin#isGuard`, `refreshBrain`, `defendAgainstMonsters` | IA ativa com zumbi/afogado/esqueleto |
| BUG-13 | Não conseguia fundar crew nem ser capitão com `/summon` | **Não confirmado corrigido** | `CrewRuntime.register`, `CrewLedger.isFreeSolo`, `PirateCommands.found/force/claim` | Comandos com dois NPCs livres, save/reload |
| BUG-14 | Mesmo NPC pode ser imediato e contramestre | **FALHA reproduzida na Alpha 5** | `CrewLedger.officer()` | `LEDGER-04` + teste in-game |
| BUG-15 | Contagem de apoio ao motim inclui capitão NPC | **FALHA reproduzida na Alpha 5** | `CrewLedger.requiredRebels()` | `LEDGER-03`, 7 elegíveis de 8 totais |
| BUG-16 | Scripts antigos podem gerar JAR Alpha 4 de fonte Alpha 5 | **FALHA constatada estaticamente** | `build_offline.sh`, `verify_jar.py`, `verify_commands_runtime_contract.py` | Novo build real reprodutível |

## Dívidas técnicas importantes (não afirmar como bug confirmado sem reproduzir)

- `CrewRuntime`: `LOADED` e `BRAWLS` são `static` sem limpeza explícita em unload/stop. Auditar retenção de entidades e contaminação entre mundos.
- `CrewRuntime`: `lastSaveTick` global entre servidores; reavaliar heartbeat e salvar por mundo sem atrasos silenciosos.
- `CrewLedger`: usa `.properties` manual em vez de `SavedData` e armazenamento de eventos como strings; avaliar atomicidade, versionamento e recuperação após falha.
- `CrewLedger.startConspiracy` exige o apoio mínimo já para começar a conspiração; a especificação v2.0 prevê conspiração antes do recrutamento completo. Corrigir o fluxo de estados, não apenas o percentual.
- `CrewRuntime.afterDamage`: auditar a detecção de morte e atribuição da fonte, especialmente quando o callback chega depois da aplicação do dano.
- `MCAPiratesMixin`: `@ModifyVariable` em `hurtServer` requer verificação de assinatura real, aplicação de mixin e impacto de outros mods.
- `PirateBehavior`: regras visuais e sociais no tick podem sobrescrever personalizações do jogador e causar leituras prematuras das listas de skins; validar idempotência.
- `SeapostPiratesMixin`: somente NPCs de **seaposts** do Fish of Thieves; grandes navios de outro mod exigem adaptador separado.
- `CrewRuntime`: `level.dimension().toString()` como identificador persiste agora, mas é melhor usar identificador estável com API comprovada, não inventar outra assinatura de `ResourceKey`.
- `CrewLedger.requiredRebels` também deve excluir crianças, tripulantes mortos/ausentes e considerar apenas elegíveis em cada fase (conforme a política definida), sem alterar o apoio mínimo de 40%.

## Divergência conhecida do projeto original

O código-fonte Alpha 5 fornecido preserva `build_offline.sh`, `verify_jar.py` e testes de comando antigos que ainda mencionam **Alpha 4**. **Não editar silenciosamente o snapshot**: o agente deve consertar esses scripts em sua branch e demonstrar build de release real. `reference-jars/` contém as duas dependências JAR fornecidas pelo usuário para a comparação de APIs.
