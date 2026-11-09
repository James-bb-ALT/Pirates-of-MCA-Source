# Spawn pirata — 10.1.0-alpha.14

Mudança de comportamento: roupa pirata por padrão e sorteio independente de peças.
Major x.Y.0 conforme classificação do usuário. Nenhuma armadura/modelo novo criado.

## Causa e correção

O mixin existente torna `isGuard()` verdadeiro para manter a IA combatente. No
MCA8.1.11 real, `EquipmentTask.checkExtraStartConditions` também usa essa condição
para requisitar `EquipmentSet` de guarda, e os métodos equipBestArmor/equipBestWeapon
escrevem nos slots. Assim a tarefa pode cobrir a roupa com armadura de guarda e
substituir as armas. Bytecode preservado em `qa/results/mca-equipment-task.txt`.

`PirateEquipmentTaskMixin` cancela apenas essa tarefa para entidades com marcador
pirata; não altera isGuard, sensores ou combate. Villagers/guardas comuns seguem
pela tarefa original. Não há limpeza repetida de armadura no tick.

`PirateSpawnEquipment` realiza o sorteio uma vez em entidades novas. Para cada
HEAD/CHEST/LEGS/FEET, consulta itens registrados e seu componente Equippable. São
aceitos os itens do namespace `piratesmca` ou explicitamente incluídos na tag
`piratesmca:spawn_equipment`. Cada item e a opção vazio têm peso 1: com N peças,
a probabilidade de vazio é 1/(N+1). Slots são sorteados separadamente, sem exigir
conjunto combinando. Slot sem opções fica vazio. Não inclui armaduras vanilla ou
de outros mods automaticamente; não confundir addon com o modpack inteiro.

| Slot | Opções atuais padrão |
|---|---|
| HEAD | vazio, tricórnio, tapa-olho, tricórnio+tapa-olho |
| CHEST | vazio ou faixa vermelha |
| LEGS | vazio, enquanto não houver peça registrada |
| FEET | vazio, enquanto não houver peça registrada |

As peças atuais são cosméticas, sem defesa adicional. Futuras armaduras precisam
registrar os componentes/atributos/render reais; o sorteador não cria armadura nem
faz modelos incompatíveis funcionarem automaticamente. A nova peça passa a integrar
o sorteio pelo componente do item, sem editar a lista do sorteador.

Roupa base: seleção no acervo MCA do namespace piratesmca, filtrada por gênero;
inclui as roupas históricas do addon e o uniforme novo. Pele/cabelo/nome/genética
permanecem nativos MCA. Escolha legítima bloqueada e presets explícitos são
preservados; configuração `outfits=false` continua respeitada. Listas de roupas
que chegam tarde mantêm uma marca pendente para nova tentativa, não uma roupa vazia.

`pmca_spawn_equipment_v1` persiste no NBT. READY impede sorteio em piratas já salvos;
slots explicitamente equipados antes da inicialização não são substituídos. Presets
explícitos têm prioridade. Retirar/trocar item depois não causa reposição nem novo
sorteio. NPCs antigos mantêm seus equipamentos, inclusive ferro já salvo. Armas por
perfil continuam como antes (ferro/pedra/madeira ou mão vazia); não foram criadas
armas piratas nesta etapa. A tarefa de autoequipar de guarda do MCA fica desativada
para piratas, inclusive os antigos: não usá-la para impor equipamento de guarda.

A consulta de itens ocorre no spawn, nunca em cada tick de NPC já inicializado.
Não há alteração de SavedData, relações, cargos, profissão ou comércio.

## Extensões por datapack

Uma armadura de outro namespace só entra mediante opt-in. Em um datapack compatível
com MC26.2, crie `data/piratesmca/tags/item/spawn_equipment.json`:

```json
{
  "replace": false,
  "values": ["outro_mod:chapeu", "outro_mod:botas"]
}
```

Os IDs devem existir e ter Equippable em um dos quatro slots suportados. IDs reais
substituem os exemplos. Tags/dados podem ser recarregados; só novos spawns sorteiam.
Sem copiar JARs ou alterar o código do MCA. Slots lógicos de acessórios futuros
fora desses quatro slots necessitam sua própria integração.

## Validação e limites

| Gate / ID | Resultado | Escopo/evidência |
|---|---|---|
| Compilação real / TC-BOOT-01 parcial | PASS | Java25, Loom1.17.21, MC26.2, Loader0.20.1; sem stubs |
| Build completo | PASS | 35 JUnit + 72 GameTests + 9 testes Python. JAR/sources idênticos após recompilação --rerun-tasks; integridade de assets e auditoria do JAR aprovadas |
| SPAWN-EQ-01/02 | PASS | 48 piratas: 12 cabeças vazias/36 equipadas; 21 peitos vazios/27 equipados na execução final. Sem ferro gerado; quatro slots independentes |
| SPAWN-EQ-03 | PASS | NBT mantém marcador/equipamento; remoção de chapéu e peitoral manual não são sorteados novamente |
| SPAWN-EQ-04 | PASS | READY antigo preservado; villager comum não recebe sorteio |
| SPAWN-EQ-05 | PASS | Invocação da tarefa real MCA interceptada só para pirata; predicado original continua executando para villager comum |
| SPAWN-EQ-06 | PASS | Cliente real, 12 adultos dos dois gêneros, roupas piratesmca, itens elegíveis ou vazio; sincronização de todos os slots cliente/servidor |
| SPAWN-EQ-07 / TC-SPAWN-05 parcial | PASS | Descarga efetiva de chunks verificada e mesmos UUIDs/identidades/itens após retorno |
| SPAWN-EQ-08 / TC-SPAWN-06 parcial | PASS | Reabre save e reinicia servidor integrado, mantendo identidades e todos os slots |
| TC-SPAWN-06 completo | BLOCKED | Processo inteiro Minecraft não reiniciado nesta etapa |
| TC-SPAWN-08 parcial | PASS | Preservada variedade das roupas piratas; não substitui ensaio completo de diversidade do contrato |
| TC-SPAWN-09/12 parcial | PASS | Guarda-roupa bloqueado/testes anteriores mantidos; default só em pirata novo |
| Cliente EMF/Fresh completo | PASS | Presença de todos os mods/packs exigida por assert; 4 capturas reais |
| Cliente sem EMF desta versão | BLOCKED | Não executado nesta etapa; servidor dedicado sem EMF coberto pela suíte |
| Armaduras futuras / mods arbitrários / FO14.1.1 integral | BLOCKED | Não instalados nem certificados pelo sorteador |
| Profiling CPU/memória/pathfinding | BLOCKED | Distribuição com 48 NPCs não é profiling |

Ambiente preservado: Java25, Gradle9.5.1, MC26.2, FabricLoader0.20.1,
API0.155.2+26.2, MCA8.1.11+26.2, Fish26.2.2.0, YACL3.9.7. Cliente: EMF3.3.11,
ETF7.2.5, CompatCore2.3.2, CompatBetterCombat2.1.0, BetterCombat3.2.2,
PlayerAnimationLib1.2.8 e Cloth26.2.155. Packs ativos e conferidos:
FreshAnimations1.11.2 → FA+AllExtensions1.10.1 → FA+Player1.1.
Perfil pessoal não alterado, packs/JARs opcionais não redistribuídos.

Falha inicial preservada em `qa/results/spawn-equipment-build.log`: teste EYE-01
assumia cabeça vazia por padrão, hipótese invalidada pela mudança solicitada.
O fixture agora limpa explicitamente o cosmético antes de testar tapa-olho sozinho;
as verificações de composição, identidade e persistência continuam intactas.
Os testes de troca de roupa usam um guarda-roupa inicial explícito bloqueado para
isolar restauração da nova política de spawn. Não se removeu funcionalidade para
passar os testes. O teste específico de spawn verifica a nova política diretamente.
A falha inicial intermitente de aplicação da etapa anterior continua registrada no
relatório deckhand; não é declarada corrigida por esta alteração de equipamento.

## Evidências e reprodução

- [Frente, cliente real](spawn-equipment/client/0000_SPAWN-EQ-front.png).
- [Costas](spawn-equipment/client/0002_SPAWN-EQ-back.png).
- [Após reabrir](spawn-equipment/client/0003_SPAWN-EQ-reopen.png).
- [Log cliente](spawn-equipment/client/execution.log) e
  [manifesto com SHA256](spawn-equipment/client/manifest.json).
- `qa/results/spawn-equipment-final-build.log`, `spawn-equipment-reproducible.log`.

```powershell
.\gradlew.bat build
python scripts/pmca/validar_assets.py --root .
.\gradlew.bat runProductionClientTest -PspawnEquipmentReview -PextensionsReview '-PcompatMods=qa/fixtures/tricorne-emf-extensions/mods'
```

Não executar tarefas Gradle em paralelo. Para preparar a fixture, use os próprios
packs/JARs licenciados no script `qa/prepare_emf_extensions_fixture.py`; não há
necessidade de clicar no desktop. Entrega local em `delivery/10.1.0-alpha.14` com
fontes completas, assets, fontes Java, relatório, changelog e SHA256. Sem commit,
push, tag ou release GitHub: a pasta não é um repositório Git e não possui remoto.

JAR SHA256: `5e0f4a24f3a525444d08e245f7386814ce1d16f78967c56783fca56d4cd8f5d8`.
