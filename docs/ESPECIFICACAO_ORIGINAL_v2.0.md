# Pirates of MCA — Especificação técnica v2.0

**Estado:** arquitetura e comportamento definidos para implementação; **não é um JAR implementado**.  
**Alvo:** Minecraft 26.2, Fabric, MCA Reborn 8.1.11+26.2, Fish of Thieves 26.2.2.0.  
**Base examinada:** código-fonte do Pirates of MCA v1.0.7 e JARs de referência fornecidos.  
**Objetivo:** tripulações piratas persistentes com capitão NPC ou jogador, cadeia de comando, ordens, brigas não letais, abuso de autoridade, reputação, testemunhas, motins, sucessão e diplomacia entre tripulações.

## 1. Invariantes (regras não negociáveis)

1. Uma tripulação possui **exatamente um capitão ativo**, que pode ser NPC ou jogador; em vacância há um `actingCaptain` temporário, sem dois capitães definitivos.
2. **Profissão** (`piratesmca:pirate`), **cargo** (`CAPTAIN`, `FIRST_MATE`, `QUARTERMASTER`, `CREW`), **perfil** (briguento, ganancioso, desconfiado, amigável etc.) e **personalidade MCA** são conceitos distintos.
3. Toda decisão social relevante tem **causa, participantes, testemunhas, resultado, prazo e consequência persistente**; eventos aleatórios só iniciam oportunidades de conflito, não geram agressão sem motivo.
4. Brigas ordinárias entre NPCs **não matam**. A proteção atua **antes de aplicar o dano**, inclusive no golpe que atravessaria 25% da vida; não se limita a interromper a perseguição após o dano.
5. Um capitão NPC não entra nas brigas triviais. Agressões praticadas pelo capitão geram medo, ressentimento e possível conspiração, mas não retaliação automática antes do motim.
6. Assassinatos, motins e ataques externos usam regras específicas; mortes deliberadas têm consequências sociais. O jogador em motim pode render-se; se recusar, pode morrer pelas regras normais do Minecraft.
7. Modificação de reputação e memória é **server-side**, com sincronização seletiva para a interface; dados permanecem após salvar/recarregar.
8. NPCs comuns do MCA continuam intocados. O addon só alcança NPCs marcados como piratas ou membros de tripulação.
9. Nenhuma atualização da IA deve randomizar novamente nomes, genética, aparência MCA, personalidade nativa ou relações já estabelecidas.
10. Em servidor multiplayer, ordens e atribuição de cargos exigem checagem de autoridade; clientes não alteram estado social diretamente.

## 2. Identidade da tripulação e navios

- `crewId`: UUID estável, definido no primeiro surgimento; **nunca** usar somente distância ou coordenadas para reunir membros de tripulações diferentes.
- `anchor`: `dimension`, `BlockPos`, `sourceType` (`SEAPOST`, `PIRATE_FLAG`, `EXTERNAL_SHIP_ADAPTER`) e raio operacional; não é uma referência para movimento automático de embarcações.
- Fish of Thieves v26.2.2.0: integração confirmada no código antigo para NPCs de **seaposts**. Não presumir que grandes navios vistos no modpack sejam gerados por esse mod; barcos de outros mods requerem adaptador de spawn separado.
- `PIRATE_FLAG`: bandeira/âncora colocável pelo jogador para criar uma tripulação própria em base/navio construído manualmente. Um baú pode ser vinculado como tesouraria da tripulação.
- Relacionamentos entre navios: matriz direcional de diplomacia por `crewId` com escala -100 a 100, estados `ALLY`, `NEUTRAL`, `RIVAL`, `HOSTILE`; o estado é derivado da escala com histerese para evitar alternância a cada pequeno evento.
- Diplomacia e notícias entre tripulações não viajam instantaneamente: exigem proximidade/contato carregado ou evento de mensageiro explicitamente simulado. Sem batalhas físicas offline.

## 3. Hierarquia

| Cargo | Quantidade | Responsabilidades e permissões |
|---|---:|---|
| Capitão (`CAPTAIN`) | 1 (NPC ou jogador) | Ordens gerais, nomear/destituir oficiais, autorizar missões, negociar, disciplinar, distribuir recursos, aceitar rendição |
| Imediato (`FIRST_MATE`) | 0–1 | Executar ordens na ausência do capitão, organizar combate, assumir comando interino |
| Contramestre (`QUARTERMASTER`) | 0–1 | Administrar tesouraria, manter disciplina, tentar apartar brigas, reportar insatisfação |
| Tripulante (`CREW`) | Restantes | Cumprir ordens conforme lealdade, cuidar da própria agenda social, testemunhar eventos, conspirar |

- Capitão não é uma profissão separada. Todos os NPCs usam a profissão `piratesmca:pirate` e mantêm suas trocas próprias; o **cargo** fica no estado de tripulação.
- Na ausência/desconexão de um capitão jogador, o imediato fica responsável por ordens rotineiras, **sem usurpar automaticamente** a liderança. Não pode nomear novo capitão sem processo de sucessão.
- Se um capitão jogador assume uma tripulação comandada por NPC, o NPC anterior é deposto, promovido a imediato **se elegível e aceito**, ou passa a ser rival/ex-capitão. Nunca coexistem dois capitães.
- O jogador pode comandar **uma tripulação principal por vez** na v2.0. Outros vínculos políticos são alianças, não comando direto.
- Apenas NPCs **adultos** podem votar em motins, ocupar cargos ou ser designados para brigas. Crianças do MCA não entram nas simulações violentas.

## 4. Como o jogador vira capitão

### Rotas de sobrevivência

1. **Fundar uma tripulação:** posicionar uma `PIRATE_FLAG` e recrutar pelo menos **2 NPCs piratas adultos** voluntários. Confirmar criação no menu da bandeira. Baús vinculados são opcionais.
2. **Assumir vaga:** quando não há capitão definitivo, o jogador pode candidatar-se se tiver reputação mínima de **+40** com a tripulação e apoio de **50%** dos membros adultos elegíveis.
3. **Depor o capitão:** apoiar uma rebelião e ser escolhido pelo novo conselho após a vitória; o resultado não concede o cargo automaticamente.
4. **Administração/testes:** operador do servidor pode designar um jogador capitão por comando de administração, sem contornar as permissões para jogadores comuns.

- Recrutamento é uma interação contextual do MCA integrada a uma tela simples de proposta; o NPC pode aceitar ou rejeitar com explicação curta (baixa confiança, facção rival, falta de vagas etc.).
- Não basta encontrar um navio ou usar `/summon` para reivindicar sua tripulação.
- O jogador perde o comando por renúncia, deposição, morte no motim sem sucessão favorável ou exclusão administrativa. Morte PvE normal **não** apaga automaticamente a liderança; admite-se retorno após respawn.

## 5. Estado social e dados persistidos

### Registro de membro

```text
PirateMember {
  memberUuid: UUID
  crewId: UUID
  role: CAPTAIN | FIRST_MATE | QUARTERMASTER | CREW
  profile: CRUEL | BRUISER | GREEDY | SUSPICIOUS | FRIENDLY
  ambition, courage, greed, sociability: int [0..100] (traços estáveis)
  loyaltyToCaptain, fearOfCaptain, respectForCaptain: int [0..100]
  resentment, morale: int [0..100]
  lastProvocationTick, lastFightEndTick, lastOrderTick: long
  allegiance: LOYAL | NEUTRAL | CONSPIRATOR | REBEL
  activeOrder: Order? (expira/é cancelada)
}
```

### Relações dirigidas entre dois indivíduos

```text
SocialEdge { fromUuid, toUuid, friendship[-100..100], trust[0..100],
             respect[0..100], resentment[0..100], lastInteractionTick }
```

- Relações são **direcionais**: A pode gostar de B mais do que B gosta de A. Não armazenar pares completos sem interações; usar índice esparso para reduzir memória.
- Manter os **hearts do MCA** para relações do jogador com NPC; reputação pirata é uma dimensão adicional. Não redefinir hearts continuamente nem forçar humor negativo a cada 20 ticks.
- Persistir `CrewRecord`, `PirateMember`, `SocialEdge`, `DiplomacyEdge`, `EventRecord` e `Version` em **Saved Data** do servidor, com codec de serialização e migração de schema.
- No NBT/tag da entidade, usar apenas vínculo e versão (`pmca_crew_id`, `pmca_member_v2`), nunca milhares de relações em entity tags.
- Registro de eventos: últimos **128 por tripulação**; mesmo evento tem `eventId` único e não pode aplicar penalidades duas vezes em recarregamentos.
- Dados do jogador são vinculados a `playerUUID`, não a nome ou sessão. Facções seguem existindo quando o jogador desloga.

## 6. Eventos e memória

Eventos fundamentais:

`CREW_CREATED`, `MEMBER_RECRUITED`, `ROLE_APPOINTED`, `ORDER_GIVEN`, `ORDER_REFUSED`, `INSULT`, `PROVOCATION`, `BRAWL_STARTED`, `SURRENDER`, `BRAWL_ENDED`, `CAPTAIN_ASSAULT`, `MURDER`, `WITNESSED`, `RUMOR_SHARED`, `CONSPIRACY_STARTED`, `MUTINY_ULTIMATUM`, `MUTINY_STARTED`, `CAPTAIN_YIELDED`, `CAPTAIN_KILLED`, `CAPTAIN_DEPOSED`, `SUCCESSION`, `ALLIANCE_CHANGED`.

- Todos carregam `eventId`, `crewId`, `timestampGameTick`, `actorUuid`, `targetUuid`, `reason`, `witnessIds`, `severity` e `appliedConsequences`.
- Testemunhas precisam estar vivas e próximas (padrão **16 blocos**) e possuir linha de visão ou escutar um evento audível, conforme tipo.
- Rumores se propagam por encontros carregados, com credibilidade menor que testemunho direto; a mesma notícia não pode circular indefinidamente.
- Piratas com relações negativas com a vítima podem apoiar uma agressão; amigos reagem muito mais negativamente.
- Crimes de jogadores contra membros da própria tripulação alimentam ressentimento e podem desencadear motim mesmo quando o jogador está em modo criativo (não há dano se o jogo não aplica dano real).

## 7. Brigas e combate não letal

- **15%** de chance de provocação elegível tornar-se briga; é **por evento de provocação**, nunca por tick.
- Uma provocação precisa de gatilho: rivalidade, insulto, disputa de recursos, humilhação, favoritismo ou boato.
- Duração máxima padrão **30 s** em combate carregado; descanso mínimo **8 minutos** por envolvido após luta.
- A briga termina por rendição, apartação de terceiro, fuga, timeout ou 25% da vida máxima.
- Um `CombatContext` atribui `BRAWL_NONLETHAL`, `DISCIPLINE`, `MUTINY_LETHAL`, `EXTERNAL_ATTACK`, `PLAYER_ATTACK` antes da aplicação de dano.
- Para `BRAWL_NONLETHAL`, a camada de dano limita o dano de um golpe a `max(0, vidaAtual - 0.25*vidaMax)`. O golpe final não atravessa o limite. Efeitos secundários causados pelo próprio confronto (fogo, veneno etc.) precisam ser neutralizados ou tratados como dano do contexto.
- Morte por outros agentes (jogador, mobs, queda/ambiente) não deve ser indevidamente bloqueada; usar atribuição de fonte/cenário, não imunidade global do NPC.
- Capitão não entra em `BRAWL_NONLETHAL`. Contramestre pode separar. Amigos podem defender verbalmente, mas não transformar automaticamente um duelo em guerra geral.

## 8. Abuso de poder pelo capitão

- Um único golpe do capitão não provoca retaliação física imediata dos subordinados. A vítima pode recuar, reclamar, obedecer ou guardar rancor.
- Repetição de agressão aumenta `resentment`, `fear` e reduz `respect`/`loyalty` em função da severidade.
- Ferir alguém popular eleva o ressentimento dos amigos **que testemunharam ou souberam do fato**.
- Assassinato deliberado por capitão é um evento grave, mesmo que o capitão seja jogador. Não transforma automaticamente todos em hostis; acelera conspiradores e altera facções.
- Agressões normais de capitão não são `BRAWL_NONLETHAL`: podem ferir de verdade e até matar; os NPCs podem se render/afastar. Dano letal gera `MURDER` e consequências.
- Quando o capitão é atacado por inimigos externos, leais podem defendê-lo. Quando um subordinado o confronta **durante motim ativo**, a autoridade deixa de bloquear retaliação.

## 9. Conspiração, motim e sucessão

### Critérios para abrir conspiração

1. Pelo menos um membro adulto com `resentment >= 65` e `ambition >= 55` (ou motivação forte após assassinato/abuso).
2. Sem outro motim/conspiração ativa na tripulação e cooldown de 1 dia Minecraft após fracasso recente.
3. O conspirador tenta recrutar aliados conforme suas relações e a avaliação de risco. Medo alto pode impedir adesão mesmo com ressentimento alto.

### Critérios para ultimato/motim

- Apoio de **`max(2, ceil(0.40 * eligibleCrewCount))`** membros adultos, **excluindo o capitão**. Exemplo: 8 NPCs elegíveis com capitão jogador precisam de **4 rebeldes**, não 3.
- Uma oportunidade real (abuso recente, capitão vulnerável, revolta popular ou superioridade rebelde) e cooldown respeitado.
- Antes do combate ocorre `MUTINY_ULTIMATUM` com aviso visível para jogador capitão; há **60 segundos de tempo carregado** para negociar, pagar, prometer mudança, renunciar ou reagir.
- Motim possui as fases `DISCONTENT -> CONSPIRACY -> ULTIMATUM -> REBELLION -> RESOLUTION -> AFTERMATH`, mas só uma fase ativa por tripulação.

### Resultado

- O capitão (NPC ou jogador) pode **render-se**, ser deposto, fugir ou morrer no combate letal do motim.
- Se jogador se rende, **perde autoridade imediatamente**, conserva inventário e integridade física, mas pode ser expulso/hostilizado socialmente. Se recusar e lutar, pode morrer segundo regras normais de combate do jogo.
- Nova liderança é decidida por eleição entre candidatos com apoio, respeito e ambição; imediato tem vantagem institucional, não garantia.
- Se a rebelião fracassa, sobreviventes ganham penalidades/reputação conforme atos individuais, e novas conspirações exigem recuperação de cooldown.
- Evitar motim instantâneo ao carregar mundo: atualizar estado com aviso e período de segurança, sem combate offline.

## 10. Ordens do jogador capitão

### Interface

- Ação `Comandar` no diálogo com piratas do MCA e `Livro de Bordo`/tela da bandeira para seleção coletiva.
- HUD discreto e opcional: nome da tripulação, oficiais, moral, insatisfação, ordens ativas, ameaça de motim.
- Ordens têm prioridade, alvo válido, duração máxima, alcance e possibilidade de recusa. Uma ordem recusada gera motivo textual e evento `ORDER_REFUSED`.

### Conjunto v2.0 (MVP)

| Ordem | Condição/efeito | Motivo de possível recusa |
|---|---|---|
| `FOLLOW` | Seguir capitão, com distância de segurança | Lealdade baixa, medo de perigo |
| `HOLD` | Ficar/aguardar em área | Disputa por ordens, posição insegura |
| `RALLY` | Reunir no ponto da bandeira ou capitão | Muito distante, em pânico |
| `GUARD` | Defender ponto/baú/aliados, sem caça ilimitada | Falta de confiança, ameaça muito forte |
| `ATTACK` | Atacar alvo hostil definido pelo capitão | Alvo é amigo/aliado, lealdade baixa, ordem ilegítima |
| `RETREAT` | Interromper ataque e recuar | Enraivecido, em motim ativo |
| `CEASE_FIGHT` | Tentar cessar brigas não letais | Autoridade baixa, rivalidade extrema |
| `DISMISS` | Encerrar ordem corrente e voltar à rotina | Não há recusa |

- `ATTACK` nunca aceita como alvo o próprio capitão, salvo em motim/deposição, e não provoca todos os NPCs indistintamente.
- Jogador escolhe até um pelotão `NEARBY`, `ALL` ou NPC específico; seleção de NPC deve ser validada no servidor por raio e vínculo com `crewId`.
- Capitão pode nomear/destituir `FIRST_MATE` e `QUARTERMASTER`, distribuir recompensas reais do estoque e instituir uma ordem de `PARDON` (redução limitada do ressentimento de uma punição específica).
- Distribuição de recursos requer itens verdadeiros no inventário do jogador ou baú vinculado. **Não gerar ouro/esmeralda do nada**, nem duplicar inventários por falha em transação.
- Ordens não são controle mental: obediência depende de lealdade, respeito, medo, risco percebido, perfil e prioridade moral do NPC.

### Probabilidade de recusa (proposta de implementação)

```text
obedience = clamp(0, 100,
  0.35*loyalty + 0.25*respect + 0.15*fear + 0.25*morale
  - riskPenalty - friendTargetPenalty - abuseMemoryPenalty)
```

- Para ordens seguras, usar limiar mínimo mais baixo; para atacar alvos neutros/amigos, aplicar grande penalidade.
- Teste aleatório ocorre **uma vez ao receber a ordem**, não a cada tick; registrar resultado e seed/evento para reproduzir bugs.
- Capitão tem **autoridade**, não garantia de obediência. Jogador pode punir recusa, mas punições aumentam ressentimento e risco de revolta.

## 11. Comandos propostos (AINDA NÃO EXISTEM na v1.0.7)

```mcfunction
/pirates crew status
/pirates crew roster
/pirates captain resign
/pirates captain claim
/pirates crew recruit nearest
/pirates officer appoint nearest first_mate
/pirates officer appoint nearest quartermaster
/pirates order nearby follow
/pirates order all rally
/pirates order nearby hold
/pirates order nearby guard
/pirates order all retreat
/pirates order nearby cease_fight
/pirates diplomacy status
/pirates debug crew
```

- `nearest` consulta o NPC pirata adulto em que o jogador está mirando, a até 6 blocos; `nearby` limita seleção ao raio configurado. O parser deve apresentar erro quando houver ambiguidade, nunca selecionar NPC aleatório silenciosamente.
- Ordens ordinárias são liberadas somente ao capitão e, quando apropriado, ao imediato atuante; `debug` e comandos administrativos exigem permissão de operador.
- Administração planejada: `/pirates admin captain set <crew_uuid> <player>` e `/pirates admin crew inspect <crew_uuid>`.
- Todos os comandos são um **contrato de implementação**, não instruções executáveis na versão atual.

## 12. Arquitetura de implementação

```text
PiratesOfMCA (entrypoint)
 ├─ integration/
 │   ├─ FishOfThievesSeapostAdapter   [marcador no spawn]
 │   ├─ MCAEntityAdapter              [skin/nome/diálogo/relacionamentos]
 │   └─ ExternalShipAdapter (futuro)
 ├─ domain/
 │   ├─ CrewRegistry, CrewRecord, MemberRecord, SocialGraph
 │   ├─ CrewLeadershipService, DiplomacyService
 │   ├─ SocialEventBus, MemoryService, RumorService
 │   ├─ OrderService, CaptainService
 │   └─ MutinyStateMachine, SuccessionService
 ├─ combat/
 │   ├─ CombatContext, NonLethalDamageGuard, FightCoordinator
 │   └─ WitnessResolver, CrimeClassifier
 ├─ persistence/
 │   ├─ CrewSavedData, SocialSavedData, MigrationV1toV2
 │   └─ DataVersion, CodecValidators
 ├─ commands/ PirateCommands [Brigadier]
 ├─ client/ CaptainMenu, CrewHud, SocialFeedback
 └─ config/ PirateConfig [server-authoritative]
```

- Preferir **eventos Fabric API** para ciclo de servidor, interação e dano quando houver ponto de extensão adequado. Usar mixins apenas onde APIs do MCA/Fish of Thieves não forneçam hooks.
- Não executar varredura de todos os villagers do mundo a cada tick. Manter índices por `crewId` e NPC carregado; tarefas sociais em intervalos escalonados de 20–100 ticks, pathfinding em baixa frequência conforme ordem, e verificações de dano apenas sob evento.
- Toda lógica autoritativa em servidor, inclusive singleplayer (servidor integrado). No cliente apenas UI/animação/renderização.
- Uso de `SavedData` e codecs do Minecraft 26.2. Persistência por mundo, com IDs dimensionais nos anchors e referências de entidade por UUID.
- A v1.0.7 mistura seleção de brigas, perseguição, profissão, roupa, relacionamento e agressão em `PirateBehavior.tick`, com `WeakHashMap` em memória. **Refatorar** em serviços antes de adicionar hierarquia para evitar dupla execução e perda de relações após restart.
- Ajustar `piratesmca.mixins.json` e nomes efetivos de métodos/classes contra os JARs exatos de MC 26.2 e MCA 8.1.11: não presumir API de outra versão.

## 13. Compatibilidade, migração e segurança de dados

- Migração 1.0.7 -> 2.0: localizar NPCs com `pmca_seapost`; atribuir `crewId` por origem conhecida; onde origem é ambígua, **não mesclar automaticamente** tripulações próximas — registrar como `UNASSIGNED` e oferecer associação administrativa.
- Tags `pmca_captain`, `pmca_bruiser`, etc. da v1.0.7 representam **perfil antigo**, não novo cargo. Para evitar vários capitães, escolher um líder inicial por critério estável e deixar os demais como candidatos com perfil correspondente.
- Preservar profissão `piratesmca:pirate`, trade sets 26.2, nome MCA, skin/genética, roupas e NPC UUID.
- Estado social novo precisa ter número de versão, validação de intervalos e migração idempotente; crashes entre save e reload não duplicam efeitos.
- NPC removido/morto: registrar evento, retirar do roster ativo, limpar ordens e abrir sucessão se ocupava comando; preservar histórico social relevante.
- Jogadores desconectados: imediato assume gestão rotineira; conspiração pode progredir enquanto tripulação está carregada, mas ultimato/confronto contra capitão jogador exige que ele esteja presente e receba aviso.
- Sem ataque direcionado a crianças, NPCs de outros mods por engano ou membros de tripulação errada.

## 14. Balanceamento e custos

| Parâmetro | Padrão |
|---|---:|
| `provocation_fight_chance` | 0.15 |
| `brawl_min_health_fraction` | 0.25 |
| `brawl_max_duration_ticks` | 600 (30 s) |
| `brawl_cooldown_ticks` | 9600 (8 min) |
| `conspiracy_resentment_threshold` | 65 |
| `conspiracy_ambition_threshold` | 55 |
| `mutiny_support_fraction` | 0.40 |
| `mutiny_min_conspirators` | 2 |
| `mutiny_ultimatum_ticks` | 1200 (60 s) |
| `witness_radius_blocks` | 16 |
| `captain_control_radius_blocks` | 24 |
| `rumor_repeat_block_ticks` | 12000 |
| `social_event_history_per_crew` | 128 |
| `max_player_captain_crews` | 1 |

- O servidor deve impor orçamento de trabalho por tick e teto de eventos simultâneos por tripulação; valores reais de latência serão medidos em teste, não presumidos.
- Menus e logs devem explicar o motivo de mudanças importantes: `Fulano ficou ressentido por você ter atacado Sicrano`.

## 15. Critérios de aceitação (teste em jogo)

1. Novo seapost gera `crewId` único; dois seaposts próximos não se fundem.
2. Todos os adultos piratas têm profissão própria; skins, nomes e roupas MCA preservados após relog/restart.
3. É possível fundar tripulação e assumir capitão pelo fluxo de sobrevivência; jogador comum não usa comando admin.
4. Só um capitão efetivo existe por tripulação; oficial assume apenas interinamente quando jogador sai.
5. Ordens `FOLLOW`, `HOLD`, `GUARD`, `RALLY`, `ATTACK`, `RETREAT`, `CEASE_FIGHT`, `DISMISS` funcionam e podem ser recusadas com motivo.
6. Sem jogador capitão, membros não obedecem ordens de estranhos; não há spoofing via pacotes cliente.
7. Provocação pode virar briga (15% por evento), nunca em loop por tick.
8. Briga entre NPCs com arma forte **não reduz vida abaixo de 25%**, mesmo se um golpe exceder o restante; morte por outras fontes não é bloqueada erroneamente.
9. Capitão não inicia briga trivial por sorteio; pode agredir e acumular ressentimento sem revides automáticos.
10. Jogador capitão bate repetidamente em subordinado: reações sociais, aviso de tensão, conspiração e motim são possíveis.
11. Num roster com 8 adultos elegíveis e capitão jogador, 3 rebeldes não bastam e 4 podem bastar (se outras condições atendidas).
12. Ultimato é exibido por 60s de tempo carregado; rendição do jogador remove comando e evita sua morte por esse confronto.
13. Recusa de rendição permite combate real e morte do jogador; novas lideranças são escolhidas de maneira coerente.
14. Assassinato gera testemunhas e rumores, não conhecimento telepático; ausência de testemunhas impede divulgação instantânea.
15. Diplomacia de navios separados não muda sem contato/evento válido.
16. Save/load, reinício, desconexão do jogador e descarga de chunk não apagam a hierarquia nem duplicam eventos.
17. Evento envolvendo NPC sem `crewId` não causa NPE nem o anexa automaticamente à tripulação mais próxima.
18. Em modpack com MCA, Fish of Thieves, Friends & Foes e Fabric 26.2, jogo inicia sem erros de registry/mixin, sem dependências duplicadas e com comportamento server-side correto.
19. Benchmarks com tripulações de 4, 12, 24 e 48 NPCs carregados medem tempo/tick, contagem de IA, eventos e pathfinding; sem queda substancial causada por varredura global.
20. Migração de mundo v1.0.7 mantém UUID, skin, nome, profissão, trade set e roupa; dados ambíguos são sinalizados, não inventados.

## 16. Plano de implementação em quatro entregas

**Entrega A — Fundação, migração e observabilidade:** `CrewRegistry`, IDs, cargos, persistência, comando status/debug, migração e testes de save/load. Sem violência nova.

**Entrega B — Capitão jogável e ordens:** bandeira, recrutamento, nomeação, permissões, menus, ordens individuais/coletivas, recusa contextual; teste multiplayer e desconexão.

**Entrega C — Social, combate e motins:** memórias, testemunhas, controle de dano não letal, abuso, facções, conspiração, ultimato, rendição, sucessão e registro de eventos.

**Entrega D — Diplomacia, polimento e performance:** relações entre navios, rumores intertripulação, negociação de recursos, ajustes de UI/feedback, profiling e testes de compatibilidade no modpack real.

**Critério de saída de cada entrega:** JAR compilado contra dependências reais + smoke test de inicialização + testes de mundo e persistência. Não declarar pronto apenas porque compilou com stubs.

## 17. Escopo fora da v2.0

- Pilotar um navio físico de outro mod, combate naval automático, canhões e navegação marítima real.
- IA LLM ou diálogos livres gerados por modelo; a interação usa menus e frases contextualizadas do MCA/addon.
- Prisões físicas, julgamento complexo de assassinato, roubos de inventários arbitrários e falsas acusações procedurais sofisticadas.
- Capitão jogador controlar múltiplas tripulações simultaneamente.
- Batalhas offline simuladas entre navios em chunks não carregados.

Esses recursos poderão ser adicionados depois sem alterar o núcleo do modelo de tripulação.

---

**Decisão final de design:** o sistema deve priorizar **causalidade social, autoridade contestável e estabilidade técnica** sobre a frequência de combate. A tripulação deve ter vida própria; o capitão jogador deve poder influenciar, mas nunca determinar unilateralmente, a lealdade dos piratas.
