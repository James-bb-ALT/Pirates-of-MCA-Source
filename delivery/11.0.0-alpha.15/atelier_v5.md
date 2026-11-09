# Pirate Atelier integrado — 11.0.0-alpha.15

Entrega: armaduras normais Minecraft, armas piratas, skins extras e próteses 3D.
PASS nos cenários executados; a biblioteca inteira não recebeu polimento visual
manual em todas as combinações de corpo/animação/mods.

## Inventário real e origem

Entrada do usuário: `Pirate_Atelier_CleanRoom_Player_3D_EMF_26.2_v5.zip`.
SHA256: `6b76d19769c80860ff1dadcc47208c5fa5e9eb05b61479ffd2862cd4f0588939`.
Extração verifica que cada caminho permanece dentro de `imports/pirate_atelier_v5`.
Os documentos foram tratados como descrição dos assets, sem executar seus scripts
ou seguir instruções como comandos. O pacote declara arte independente; a licença
e a origem foram preservadas. Não foi importado código do mod NeoForge de referência.

10.026 PNGs não são 10.026 itens: há atlas, texturas de equipamento, ícones e cópias.
Registro real: **4.444 itens novos**; com os oito anteriores, a aba criativa tem
4.452 itens. Quantidades verificadas por manifesto e registro do Minecraft:

| Tipo | Quantidade | Runtime |
|---|---:|---|
| Peças de armadura | 4.349 | 1.086 conjuntos completos + cinco capacetes; 1.091 equipment assets |
| Armas | 10 | Espadas/cutelos/sabres/rapier, modelos voxel de mão do pacote |
| Ganchos | 12 | Próteses 3D à esquerda/direita; modelos originais em GUI/mão preservados |
| Pernas avulsas | 5 | Próteses 3D em FEET; calças/botas peg_left/peg_right também as ativam |
| Outros acessórios | 68 | Itens de inventário; efeitos/slots especiais não implementados neste passe |
| Skins extras | 20 | Aparências, não itens; 19 masculinas e uma feminina explicitamente identificada |

Namespaces originais `pirateatelier` e `lfpirates` mantêm as referências dos assets.
A expansão não requer ativar o ZIP fonte como resourcepack. Não ativar o DEMO de
armadura global como parte desta integração. Nenhum player.jem/villager.jem genérico
foi instalado e os packs Fresh não foram sobrescritos.

## Comportamento

Armadura completa: defesa16 (ferro15), tenacidade2 (0,5 por peça; ferro0),
durabilidade20% acima do ferro e encantabilidade9. Todos os estilos/cores/desgastes
usam esse patamar. São itens de armadura reais com Equippable/EquipmentAsset,
atributos e durabilidade nativos. Tags vanilla mantêm encantamento/reparo; não
foram criadas receitas nem trades de armaduras/armas. Peças tattered têm rasgos
visuais intencionais, sem penalidade de resistência.

Armas novas substituem armas genéricas geradas no spawn, preservando equipamento
explícito e NPCs já salvos. Dano/velocidade: padrão6/1,6; rapier5,5/2,0;
falchion7/1,2; durabilidade320. Dados BetterCombat opcionais herdam cutlass/rapier.
Nomes poison/venom são temas visuais; não acrescentam veneno mágico nesta entrega.

Cada slot de armadura de um novo pirata tem 50% de ficar vazio; quando equipado,
escolhe entre as peças elegíveis do registro/tag. O peso fixo de vazio evita que
essa opção se torne irrelevante com milhares de peças. Ganchos podem aparecer na
mão secundária (15%). Marcadores existentes continuam impedindo reroll após load.

Ao assumir profissão Pirata, um adulto ainda pode manter aparência MCA (50%) ou
receber skin extra compatível com o gênero (50%, entre as disponíveis). Skins
extras têm peso zero no sorteio geral MCA; a seleção do addon é própria. Genética,
nome, idade, dados de cabelo, relações e comércio não são regravados. Skins completas
trazem rosto/cabelo pintados: enquanto ativas, FaceLayer/HairLayer não sobrepõem
outro rosto/cabelo; dados originais permanecem disponíveis para voltar à aparência
MCA. Essas faces pintadas não ganham automaticamente todas as expressões/blinks MCA.
Piratas READY antigos não têm identidade/equipamento sorteados retroativamente.

Loot de equipamento ao morrer por dano de jogador: 50% da mão principal e 35% dos
demais slots ocupados por itens desta biblioteca. Preserva os componentes/dano do
stack e impede duplicação; respeita GameRules.MOB_DROPS. MCA limpa equipamentos na
rotina die antes do drop vanilla, por isso o hook fica antes dessa limpeza.
Mortes ambientais sem jogador e multiplicador de Looting não foram ampliados.

## Próteses e correções de gaps/faces

Rota: Fabric render layer `PirateLimbsLayer`, usando snapshots de render e pivôs
reais. Gancho segue a mão ocupada; não é simplesmente um sprite carregado pelo NPC.
A perna avulsa ocupa FEET; peg_left/peg_right em calça/bota define o lado. Calça
prevalece se as peças discordam. O membro inferior original é ocultado, enquanto
a parte superior é redesenhada com pele, roupa e armadura, com UV interpolado no
corte. Não se remove o braço/perna inteiro. Hitbox e colisão não mudam.

O modelo de animação MCA possui membros sem cubos visíveis. Copiar geometria desse
esqueleto produzia parte superior vazia quando não havia armadura. Falha reproduzida
por `ATELIER-12 upper limb absent`; corrigida consultando a malha visível real
`VillagerEntityModelMCA.bodyData`. O esqueleto continua fornecendo a transformação.
Player slim usa seu mesh adequado quando necessário. Modelos nativos não são editados.

Cada cubo tem seis faces texturizadas. O encaixe tem sobreposição de0,04 pixel no
pulso/joelho, evitando fissura/coplamaridade. O gancho esquerdo usa malha refletida
com limites normais e faces laterais trocadas, sem escala X negativa em runtime.
Os cubos de cada prótese formam uma peça conectada; a abertura do U é intencional.
Os dez modelos de arma têm dez cubos cada, com as seis faces presentes.
Transparências de roupas rasgadas são intencionais e não foram preenchidas.

Outra falha visual real: material da prótese em pasta entity/cem não era costurado
no atlas de itens e aparecia magenta, embora os ícones estivessem corretos. Corrigida
copiando o material para item/attachments e ligando particle explicitamente. Testes
agora resolvem GUI, FIXED e os modelos dedicados de ambas as mãos.

A parte superior da armadura passa pelo EquipmentLayerRenderer nativo, preservando
trim/glint também nos encaixes e suportando equipamento vanilla usado com a prótese.
A ocultação é restaurada por modelo antes do próximo ator; o controle sem prótese
é verificado no mesmo frame. Nenhuma consulta a entidade/mundo ocorre em submit.

## Execução / contratos de validação

Java25 / Gradle9.5.1 / Loom1.17.21 / MC26.2 / FabricLoader0.20.1 /
API0.155.2+26.2 / MCA8.1.11+26.2 / Fish26.2.2.0 / YACL3.9.7.
Dependências reais e hashes preservados. Não há stubs na validação nem no JAR.

| ID / gate | Resultado | Evidência e escopo |
|---|---|---|
| ATELIER-01/02 | PASS | Todos os 4.444 registros; 4.349 armaduras têm slot, asset, atributos, durabilidade e pool válidos |
| ATELIER-03 | PASS | Pirata novo gera arma própria da biblioteca |
| ATELIER-04/05 | PASS | 20 skins aceitas no registro MCA e aparência não sorteada novamente em100 chamadas |
| ATELIER-06/11 | PASS | 8.917 resoluções reais de GUI/FIXED/modelos dedicados, sem missingno |
| ATELIER-07 | PASS | 20 screenshots de skins, no gênero compatível |
| ATELIER-08 | PASS | 17 próteses individuais; idle/walk/attack/swim em representantes, estado ativo exigido |
| ATELIER-09 | PASS | Conjunto real no NPC soma defesa16/tenacidade2 |
| ATELIER-10 | PASS | Morte real MCA produz loot, não duplica e respeita drops desativados |
| ATELIER-12 | PASS | Parte superior não vazia, vértices respeitam o corte, controle sem prótese conserva os quatro membros |
| ATELIER-13 | PASS | Jogador visível em3ª pessoa, cutelo próprio, BetterCombat com swing estritamente entre0 e1; trim/glint no fixture |
| ATELIER-14 | PASS | Um NPC foi ao Nether e voltou mantendo skin e os seis slots; ensaio OFF final |
| TC-BOOT-01 parcial | PASS | Build real: 35 JUnit, 78 GameTests e9 testes Python; checagens de assets. clean não executado nesta etapa |
| Reprodutibilidade | PASS | JAR e sources byte a byte iguais após recompilação Java/arquivos; resources não recopiados nesta checagem, integridade validada separadamente |
| TC-SPAWN-01/02/03 parcial | PASS | Adultos dos dois gêneros; não substitui os ensaios20/20/40 do contrato |
| TC-SPAWN-05/06 parcial | PASS | 12 UUIDs/identidades/equipamentos após descarga efetiva de chunks e reabertura do servidor integrado |
| TC-SPAWN-06 completo | BLOCKED | Mesmo save não foi reaberto após reiniciar o processo inteiro Minecraft |
| TC-SPAWN-09/12 parcial | PASS | Roupas legítimas e fixtures de restauração mantidas; dados anteriores não rerrolados |
| Editor | PASS | Hook e peg em Blockbench5.2.2 headless/WebGL; fontes de17 próteses editáveis |
| Cliente OFF / EMF completo | PASS | Mesmo SHA256 do JAR final; 57 capturas OFF e60 com extensões, sem reaproveitar capturas antigas |
| Todas as variações em todos os corpos/poses/shaders | BLOCKED | Não certificação universal por amostragem |
| FO14.1.1 integral / profiling4/12/24/48 | BLOCKED | Fixture de compatibilidade e distribuição48NPCs não são teste do modpack integral nem profiling |

Stack confirmado por assert: EMF3.3.11, ETF7.2.5, CompatCore2.3.2,
CompatBetterCombat2.1.0, BetterCombat3.2.2, PlayerAnimationLib1.2.8,
Cloth26.2.155. Packs: FreshAnimations1.11.2 → FA+AllExtensions1.10.1 →
FA+Player1.1. Nenhum pack/JAR opcional redistribuído ou perfil pessoal alterado.

Poses são fixtures de modelo/movimento; não certificam IA autônoma. O cenário tem
12 atores, ambos os gêneros, e controles. As17 próteses recebem vistas individuais,
mas não todas em cada gênero/extremo genético/cabelo. As4 poses usam4 representantes.
As4.349 peças são verificadas integralmente em registro/modelo/asset; só amostras
são inspecionadas vestidas no mundo. 68 acessórios extras não recebem funcionalidade
especial, papagaios vivos, slots adicionais ou magia nesta entrega.

## Evidências preservadas

- `qa/results/atelier-build.log`: primeira suíte, duas falhas de fixtures antigos
  (aba esperava só namespace piratesmca; teste de tapa-olho assumia cabeça vazia).
  Expectations foram ampliadas para a biblioteca; fixture agora começa explicitamente
  sem capacete. As verificações de duplicação, identidade e não sobrescrita continuam.
- `reports/atelier/debug-extensions`: automação inicial passou, inspeção visual FAIL
  por material magenta. Não foi promovida como release aprovada.
- `qa/results/atelier-client-extensions-seams.log`: FAIL real de membro superior vazio,
  causa/fix acima. `qa/results/atelier-gap-compile*.log` registra ajuste de API real.
- `reports/atelier/release/off` e `extensions`: logs/capturas/manifestos finais com hash
  por arquivo. O arquivador exige nomes e timestamps da execução atual, excluindo sobras.
- `reports/atelier/blockbench-hook-final` e `blockbench-peg`: provas do editor.
- `qa/results/atelier-final-build.log`, `atelier-static.log`, `atelier-python.log`,
  `atelier-assets.log`, `atelier-reproducible.log`; diff real do JAR em
  `reports/atelier/production-diff.json`.

Falha inicial intermitente de aplicação da etapa deckhand permanece documentada no
relatório anterior; esta entrega não a declara resolvida sem reprodução específica.

## Fontes e instalação

Copie só o JAR Fabric de `delivery/11.0.0-alpha.15`, substituindo a versão antiga.
Não use sources/gametest como mod, nem duas versões juntas. Dependências anteriores
mantidas. Recursos novos são embutidos. Exemplos de IDs no README e manifesto.

Código: `PirateAtelier`, `PirateSpawnEquipment`, `client/PirateLimbsLayer`,
`client/UpperLimbGeometry`, `client/UpperArmorModel` e mixins de snapshot/máscara.
Fontes `.bbmodel`: `models/blockbench/atelier`; atlas/fontes originais em imports.
Para geometria, edite o .bbmodel e execute `export_atelier_limbs.py`; PNG pintado deve
ser salvo no atlas fonte correspondente. O export preserva geometria editada, gera
as duas mãos e o verificador rejeita faces ausentes/UV inválido/peças desconectadas.
Armaduras comuns usam a malha vanilla; seus PNGs/equipment JSON são fontes editáveis,
sem cópia de código Java MCA. CEM/geo originais permanecem como fontes, não overrides.

```powershell
python scripts/pmca/import_atelier.py
python scripts/pmca/validate_atelier.py
.\gradlew.bat build
.\gradlew.bat runProductionClientTest -PatelierReview -PatelierBase
.\gradlew.bat runProductionClientTest -PatelierReview -PextensionsReview '-PcompatMods=qa/fixtures/tricorne-emf-extensions/mods'
```

Gradle sequencial. Fixture opcional preparada com arquivos licenciados do usuário;
não é parte do build mínimo. Fontes completas/PNG/modelos/scripts acompanham os ZIPs.
JAR sources contém Java/resources; workspace contém o projeto completo. SHA256 no
pacote. Sem commit/push/tag/release GitHub: pasta sem repositório Git/remoto.

JAR SHA256: `28591bfa3f609f986828e4ca3aa165b6a4406b8830548d33d7ad5f148c5e9858`.
