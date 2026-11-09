# Pirates of MCA — 2.0.0 Alpha 5 (correção de tripulação)

Alvo: Minecraft 26.2, Fabric, MCA 8.1.11+26.2, Fish of Thieves 26.2.2.0.

## Diagnóstico

A Alpha 3 registrava automaticamente todo pirata do `/summon` como capitão de sua própria crew provisória. O perfil `pmca_captain` era confundido na prática com cargo por causa da criação da crew individual. `/pirates captain claim` exigia pelo menos 2 adultos do **mesmo navio** e aprovação de 50% com >=40 hearts, o que um grupo de NPCs recém-criados não tinha. Ao transferir, a tag antiga de crew no NPC também ficava desatualizada.

## Corrigido

- Pirata invocado manualmente e sem origem (`pmca_origin_*`) = marinheiro `CREW` livre, não capitão automático; qualquer perfil é permitido.
- Migração no próximo tick/consulta de piratas livres já criados nas Alphas 1-3: a captaincy individual indevida é removida, sem mexer no perfil MCA, nas roupas, no nome e no vínculo de crews reais.
- `/pirates captain found`: agrupa **os dois piratas adultos livres mais próximos**, dentro de 14 blocos. Não exige hearts; não recruta piratas de outras crews.
- `/pirates captain force`: para operadores/cheats; agrupa todos os piratas livres a até 14 blocos e torna o jogador capitão, mesmo com apenas um marinheiro. Se não houver livres, assume a tripulação do pirata mais próximo sem fundir embarcações. O comando não funciona sem permissão de moderador.
- `/pirates captain claim`: reservado para assumir tripulação **existente**, com 50% de apoio e >=40 hearts; dá orientação clara se tentar sobre NPC livre.
- Ao transferir/recrutar, sincroniza `pmca_crew_<UUID>` da entidade com o arquivo de dados `data/piratesmca/crews_v2.properties`, respeitando o ledger como fonte de verdade.
- `/pirates crew status` agora inclui a origem da tripulação (`UNASSIGNED`, `PLAYER_FOUNDED`, `pmca_origin_*`).

## Como testar

1. Backup do mundo. Remova todas as versões antigas da pasta `mods`.
2. Instale somente `Pirates_of_MCA_v2.0.0_alpha5_Fabric26.2_CREW_FIX.jar`.
3. Invoque dois NPCs dentro de 14 blocos:

```mcfunction
/summon mca:male_villager ~ ~ ~ {Age:0,Tags:["pmca_seapost","pmca_bruiser","pmca_gender_male","pmca_adult_required"]}
/summon mca:female_villager ~2 ~ ~ {Age:0,Tags:["pmca_seapost","pmca_friendly","pmca_gender_female","pmca_adult_required"]}
```

4. Crie a tripulação em sobrevivência, sem necessitar hearts:

```mcfunction
/pirates captain found
/pirates crew status
/pirates crew roster
/pirates order nearby follow
```

5. Apenas em mundo com cheats/operador, caso queira pular as condições de gameplay:

```mcfunction
/pirates captain force
```

Ao executar `force`, todos os piratas livres em 14 blocos passam à crew. Quando não há marinheiros livres, o comando assume a tripulação do NPC pirata existente mais próximo. **Não use perto de uma crew que você não deseja assumir**.

## Persistência

O sistema continua usando `data/piratesmca/crews_v2.properties` dentro da pasta do mundo. Não exclua o arquivo para corrigir este bug. A migração é aplicada a crews antigas com âncora `UNASSIGNED` e somente um NPC, mantendo intactas crews reais `PLAYER_FOUNDED`/`pmca_origin_*`.

## Verificação

- JAR compilado com Java 21 usando interfaces de referência (stubs) do Minecraft/Fabric 26.2; as interfaces não são incluídas no JAR.
- Testes locais do `CrewLedger`: migração de capitão solo, transferência entre crews, capitão jogador, salvamento/recarregamento, conspiração.
- Inspeção dos descritores de bytecode para callback de comandos, Brigadier e permissões Fabric/Minecraft 26.2. `Permissions.COMMANDS_MODERATOR` tem descritor `Lnet/minecraft/server/permissions/Permission;`.
- Integridade e recursos: 48 texturas, 20 trocas e 5 trade sets.
- **Não foi executado dentro de um cliente/servidor Minecraft real**. O JAR ainda é uma build experimental.
