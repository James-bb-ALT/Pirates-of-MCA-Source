# Pirates of MCA — 13.0.0-alpha.19

Correção do alinhamento entre equipamentos e corpo visível MCA; pernas de pau reconstruídas e 38 skins extras (19 masculinas/19 femininas), com rosto separado, piscadas e cabelo nativo MCA. Fontes editáveis para 1.091 configurações de equipamento. Resultados por cenário, evidências e limitações em reports/visual-fix-13.0.0-alpha.19.md. A compilação não equivale a aprovação visual universal.

Biblioteca Pirate Atelier integrada: **4.349 peÃ§as de armadura**, **10 armas prÃ³prias**,
**38 skins opcionais de piratas**, **12 ganchos e 5 pernas de pau 3D**.
A aba criativa lista os 4.444 itens importados e os itens anteriores do addon.

Armadura completa: 16 pontos de defesa, 2 de tenacidade e durabilidade 20% maior
que ferro. Sem receitas novas de crafting nem novas ofertas de comÃ©rcio; em
sobrevivÃªncia o equipamento Ã© obtido dos piratas. Drop respeita a regra de loot
nativa. Novos piratas sorteiam cada slot (50% vazio) e usam armas do pacote.
As skins continuam podendo ser MCA; extras sÃ£o escolhidas uma vez, por gÃªnero,
sem regravar genÃ©tica, cabelo salvo, nome, idade ou relaÃ§Ãµes.

```text
/give @s pirateatelier:naval_cutlass
/give @s pirateatelier:captain_armor_pristine_normal_chestplate
/give @s pirateatelier:pirate_crimson_pristine_peg_left_leggings
/give @s pirateatelier:atelier_hook_brass
/give @s pirateatelier:atelier_peg_leg_wood
```

Gancho em uma das mÃ£os ativa a prÃ³tese desse braÃ§o; a perna avulsa ocupa FEET.
CalÃ§a/bota com `peg_left` ou `peg_right` tambÃ©m ativa a prÃ³tese; calÃ§a tem prioridade
quando as peÃ§as discordam. O membro superior permanece visÃ­vel e animado.
Conjuntos e armas usam recursos embutidos no JAR: nÃ£o Ã© necessÃ¡rio ativar o ZIP
fonte ou o pack DEMO que substitui armaduras globalmente.

[RelatÃ³rio, screenshots reais, testes e limitaÃ§Ãµes](reports/atelier_v5.md).
68 outros acessÃ³rios foram registrados como itens; seus efeitos e slots especiais
nÃ£o fazem parte desta entrega. NPCs antigos nÃ£o recebem novo sorteio automÃ¡tico.


Spawn atualizado: **roupas piratas por padrÃ£o e peÃ§as aleatÃ³rias por slot**, incluindo
vazio. Sem armadura de ferro automÃ¡tica da IA de guarda MCA. Novos equipamentos
`piratesmca` com componente `equippable` entram automaticamente; extensÃµes podem
optar pela tag `piratesmca:spawn_equipment`. Sorteio Ãºnico no spawn; nÃ£o muda no
relog e nÃ£o altera NPCs jÃ¡ salvos ou equipamentos explÃ­citos. Armas atuais mantidas.
[PolÃ­tica, testes e extensÃ£o por datapack](reports/spawn_equipment.md).

Nova peÃ§a do Lote 1: **uniforme de marinheiro** (overlay nativo MCA, sem defesa).
Molde reutilizÃ¡vel na aba criativa Pirates of MCA; use no pirata adulto da sua
tripulaÃ§Ã£o, prÃ³ximo e visÃ­vel, ou como OP. Preserva rosto, cabelo e equipamentos.

```text
/give @s piratesmca:deckhand_outfit
/pirates outfit nearest deckhand
/pirates outfit nearest restore
```

`restore` recupera a roupa anterior e seu bloqueio. NÃ£o sobrescreve uma roupa
vÃ¡lida trocada posteriormente pelo editor MCA. Fonte `.bbmodel`, PNG e limites:
[relatÃ³rio de marinheiro](reports/deckhand.md). Contrabandista, capitÃ£o civil e
armaduras continuam pendentes; esta etapa entrega uma peÃ§a por vez.

Novo modelo: **faixa vermelha 3D**, com layer de torso independente da cabeÃ§a.
[RelatÃ³rio e limites de encaixe](reports/sash_red.md).
O encaixe da faixa na transiÃ§Ã£o de acordar foi corrigido em 9.0.1.
[Causa, teste reproduzÃ­vel e capturas](reports/sash_fit_9.0.1.md).
As validaÃ§Ãµes continuam limitadas aos cenÃ¡rios executados; nÃ£o certificam todos os mods ou shaders.

```text
/give @s piratesmca:sash_red
/pirates preset nearest sash_red
/pirates preset nearest sash_clear
```

Ocupa CHEST neste MVP e nÃ£o substitui um peitoral existente. Pode usar com tricÃ³rnio
e tapa-olho. Sem defesa adicional. `clear` continua limpando apenas cabeÃ§a/rosto;
para retirar a faixa, use `sash_clear`.

Novo modelo: **tapa-olho de couro**, sozinho ou combinado com o tricÃ³rnio.
Fonte editÃ¡vel, evidÃªncias e limites em [eyepatch_leather.md](reports/eyepatch_leather.md).

```text
/give @s piratesmca:eyepatch_leather
/give @s piratesmca:tricorne_eyepatch
/pirates preset nearest eyepatch_leather
/pirates preset nearest eyepatch_clear
```

Presets exigem capitÃ£o/OP e pirata adulto prÃ³ximo; o tapa-olho preserva o tricÃ³rnio.
Ambos sÃ£o cosmÃ©ticos e ocupam o equipamento HEAD nativo; nÃ£o acumulam com outro capacete.

Aba criativa **Pirates of MCA**: bandeira, tricÃ³rnio e ovos de pirata adulto
masculino/feminino. Todos os itens futuros do addon entram automaticamente.
Os ovos tambÃ©m aparecem na aba vanilla de ovos de spawn. Comandos alternativos:

```text
/give @s piratesmca:male_pirate_spawn_egg
/give @s piratesmca:female_pirate_spawn_egg
```

RelatÃ³rio desta versÃ£o: [aba criativa e ovos](reports/creative_tab_7.0.0.md).

ValidaÃ§Ã£o atual: [cabelos e stack EMF completo solicitado](reports/tricorne_validation_6.0.2.md).
Gate obrigatÃ³rio: Fresh base + FA All Extensions + FA Player, EMF/ETF,
EMF Compat Core e EMF Compat Better Combat. Rodadas antigas sÃ³ com Fresh base
nÃ£o substituem esse gate. Perfil pessoal nÃ£o Ã© modificado pelos testes isolados.

Checkpoint visual experimental: tricÃ³rnio original editÃ¡vel, equipamento HEAD
persistente e adaptaÃ§Ã£o Ã  escala da cabeÃ§a MCA. O catÃ¡logo ainda nÃ£o estÃ¡ pronto
para uso completo; veja [o relatÃ³rio do tricÃ³rnio](reports/tricorne_mvp.md).

Villagers gerados por estruturas em biomas costeiros viram piratas,
com uma tripulaÃ§Ã£o por origem. Praias/oceanos vanilla e tags comuns de mods sÃ£o
aceitos. O adaptador Fish e os comandos anteriores permanecem disponÃ­veis.
Consulte [o relatÃ³rio atual](docs/RELATORIO_COSTEIRO_5.0.md) e
[o inventÃ¡rio do modpack](docs/BIOMAS_DO_MODPACK.md).
Os checkpoints anteriores permanecem imutÃ¡veis em `delivery/`.

Para experimentar: `/give @s piratesmca:pirate_flag`, coloque a bandeira e clique
com o botÃ£o direito perto de dois piratas adultos livres e amigÃ¡veis. Confirme a
fundaÃ§Ã£o; depois abra a mesma bandeira para emitir ordens. A receita usa quatro
lÃ£s pretas, um osso e dois gravetos. Os comandos anteriores continuam disponÃ­veis.

Addon Fabric para Minecraft **26.2**, Java **25**, Fabric Loader **0.20.1**,
MCA Reborn **8.1.11+26.2** e Fish of Thieves **26.2.2.0**.

Esta Ã© uma entrega de auditoria em desenvolvimento. A especificaÃ§Ã£o v2.0 ainda
contÃ©m requisitos nÃ£o implementados. Consulte o relatÃ³rio de execuÃ§Ã£o; nÃ£o tratar
o JAR como release funcionalmente completa.

[RelatÃ³rio de execuÃ§Ã£o](docs/RELATORIO_EXECUCAO_ALPHA6.md) Â·
[62 casos de validaÃ§Ã£o](qa/TEST_CASES.csv) Â· [Changelog](CHANGELOG.md)

## Build e testes

No PowerShell, com JDK 25 no PATH:

```powershell
.\gradlew.bat clean build --console=plain
python qa/audit_release.py
.\gradlew.bat runClientGameTest --console=plain
```

Execute os comandos sequencialmente: o plugin limpa diretÃ³rios de GameTest.
O primeiro comando baixa Gradle 9.5.1/Loom 1.17.21 e dependÃªncias reais, compila,
executa JUnit e GameTests de servidor. NÃ£o usa stubs. Os dois JARs fornecidos
devem permanecer em `reference-jars/`; seus SHA256 sÃ£o verificados antes da compilaÃ§Ã£o.
Fish tambÃ©m exige YACL 3.9.7+26.2-fabric, resolvido para os testes pelo Gradle.
Minecraft e bibliotecas de outros mods nÃ£o sÃ£o empacotados no addon.

SaÃ­das: `build/libs/piratesmca-10.1.0-alpha.14.jar` e `*-sources.jar`.
O JAR `*-gametest.jar` contÃ©m somente testes e nÃ£o deve ser instalado para jogar.

## Compatibilidade isolada

```powershell
python qa/fetch_fo_fixture.py
.\gradlew.bat runProductionClientTest '-Pfabric_version=0.161.0+26.2' '-PcompatMods=qa/fixtures/fo-14.1.1/mods' --console=plain *> qa/results/fo-48-final.log
```

O downloader confere hashes do pacote oficial FO 14.1.1 e baixa somente para
`qa/fixtures`. Friends & Foes 5.0.4+mc26.2 e ResourcefulLib 5.0.4 usados na sessÃ£o
sÃ£o acrÃ©scimos explicitados no relatÃ³rio; devem ser colocados nessa pasta para
reproduzir esse cenÃ¡rio adicional. Nenhum perfil pessoal Ã© alterado.
O teste em modo de produÃ§Ã£o evita o mixin `devenv` incompatÃ­vel do ModernFix
presente no pacote. NÃ£o se deve remover um mod e declarar o pacote inteiro validado.
O relatÃ³rio registra um travamento intermitente no encerramento da rodada longa;
a compatibilidade completa permanece BLOCKED.

Para a verificaÃ§Ã£o adicional em outro processo, depois da rodada completa:

```powershell
$log = Get-Content qa/results/fo-48-final.log -Raw
$testUuid = [regex]::Match($log, 'capitao=([0-9a-f-]{36})').Groups[1].Value.Replace('-', '')
.\gradlew.bat runProductionClientTest '-Pfabric_version=0.161.0+26.2' '-PcompatMods=qa/fixtures/fo-14.1.1/mods' '-PclientReopen' "-PclientUuid=$testUuid" --console=plain
```

O mesmo UUID de jogador Ã© necessÃ¡rio para comparar a capitania entre processos.
O checkpoint `run/production-client-test/qa-client-reopen.properties` guarda o
save e as identidades esperadas; ele Ã© criado pela rodada completa.

## PersistÃªncia e limites

O runtime usa SavedData/codec v3, com gravaÃ§Ã£o atÃ´mica, backup e recuperaÃ§Ã£o.
Properties v2 Ã© importado sem alterar o original. NÃ£o Ã© prova de migraÃ§Ã£o de mundos Alpha 1â€“5.
NÃ£o abrir um mundo importante sem cÃ³pia de seguranÃ§a. Os documentos originais
em `docs/` permanecem como contrato e registro histÃ³rico.
