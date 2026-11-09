# Correção visual — 13.0.0-alpha.19

Estado: **validação em andamento; sem aprovação visual integral**. Este relatório distingue execução, inspeção visual e limites de cobertura. A versão anterior permanece identificada como baseline, sem reclassificar suas falhas como sucesso.

## Implementação e causa-raiz

### Mangas, equipamentos e espaço de transformação

O defeito foi reproduzido com apenas um NPC visível, em mundo de teste isolado. Sobreposição de entidades foi descartada. As tentativas de substituir o esqueleto ou copiar novamente a pose não corrigiram o defeito; seus logs e capturas foram preservados como tentativas sem aprovação visual.

A inspeção do bytecode e o contrato executado contra o MCA real mostraram que `CommonVillagerModel.renderCommon()` desenha os membros sem aplicar a transformação de `root`. O addon aplicava essa transformação novamente aos equipamentos. Quando Fresh animava `root`, corpo e roupa passavam a usar espaços diferentes.

`EquipmentPose` agora seleciona o mesmo espaço do corpo MCA. `WardrobeLayer`, `PirateLimbsLayer`, `AtelierSkinDetailsLayer` e `SashLayer` compartilham essa regra. A escala da cabeça continua seguindo as dimensões MCA. Itens próprios na mão usam o braço MCA real através de `PirateHeldAttachmentMixin`; chapéus cosméticos próprios recebem a correção correspondente em `TricorneHeadLayerMixin`. O caso comum de jogador e itens de outros namespaces conserva sua transformação original.

O teste `EquipmentPoseContract` compara vértices reais emitidos por `renderCommon` com as transformações dos equipamentos em nove combinações. Inclui controle negativo com a transformação antiga. Há também contrato das duas mãos, controle de espada vanilla e restauração da anatomia ao desequipar.

As capturas reais ficam em `run/visual-fix-before-on`, `run/visual-fix-probe-root-on` e `run/visual-fix-full-final-on`. A triagem de cor das quatro famílias encontrou 44/52 quadros anteriores com manchas vermelhas grandes e 0/52 no ensaio direcionado corrigido. Essa medição auxilia a inspeção; não aprova geometria, UV ou animação por si só.

### Peitorais femininos

A matriz completa revelou anatomia atravessando casacos fechados. `WardrobeGeometry` verifica o envelope do torso e os pixels alfa das seis regiões UV da caixa envolvente. Somente uma casca completa e opaca pode ocultar a anatomia coberta. O resultado é armazenado no cache e reavaliado após reload de recursos.

`PirateMcaFinalMaskMixin` aplica a máscara depois que o MCA atualiza as dimensões; `PirateLimbMask` restaura a visibilidade anterior. Roupas abertas ou transparentes não recebem essa ocultação por suposição.

Antes: `run/visual-fix-full-on/screenshots/0993_AUDIT-ARMOR-captain_armor-female-FRONT.png`. Depois: mesmo nome em `run/visual-fix-full-final-on/screenshots`. O peitoral do imediato também foi inspecionado individualmente.

### Próteses

O defeito tinha duas causas: geometria curta/estreita e o espaço de transformação divergente descrito acima. A reconstrução em `definitions/prosthetic_leg_shape.json` define a fixação junto ao corte em 6,3 unidades e a extremidade no chão anatômico em 12 unidades. Há encaixe, aro, haste e ponta fechados, com pequenas interseções intencionais entre componentes. Não foi aplicado aumento global do item.

`rebuild_peg_legs.py` atualiza a origem compartilhada de 708 variantes de armadura e cinco próteses editáveis. `export_atelier_limbs.py` gera o anexo FIXED consumido pelo renderer. As UVs da madeira/aro/ponta vêm das regiões correspondentes dos materiais fornecidos. Uma primeira tentativa estendia indevidamente a região metálica pela haste; essa tentativa foi substituída antes da matriz completa.

As próteses seguem o osso da perna esquerda/direita; o membro humano e sua camada de roupa são ocultados seletivamente. As capturas isoladas incluem quatro vistas, caminhada, corrida, ataque e rotação, em múltiplos ticks. A fonte de madeira também abriu no Blockbench 5.2.2 em navegador headless isolado: quatro caixas, textura 128 × 128 e nenhum erro de página. Abrir o modelo não constitui aprovação in-game.

### Skins, rosto e cabelo

Os 20 IDs e PNGs originais foram preservados. O catálogo contém agora 38 skins, 19 masculinas e 19 femininas. As 18 adições femininas têm alterações reais de rosto/roupa, incluindo decote, painéis laterais, cintura e detalhes; não são somente etiquetas de gênero.

`build_layered_pirate_skins.py` gera corpo com cabeça neutra, rosto separado e olhos fechados. O ID salvo permanece o mesmo; `AtelierSkinMixin` resolve somente a textura corporal de apresentação. `AtelierFaceHairMixin` substitui exclusivamente a face das skins próprias, usa o estado de piscar do MCA e deixa o `HairLayer` nativo funcionar. O cabelo/genética salvo no NPC continua sendo usado. Skins nativas não recebem o perfil pirata.

Limite conhecido: a paleta de olhos do rosto autoral ainda é fixa. Não foi implementada heterocromia/tintura genética desses olhos nem o conjunto completo de expressões emocionais do MCA. Não declarar equivalência facial integral ao sistema nativo. O uso de cabelo nativo não prova que SkinLayers3D extruda a pele de NPCs.

## Proveniência dos candidatos

JAR final congelado: `6bf985d7804a6126a024033db7f2aaaf36d1ad4c30aaeafbcb2a82a9ad87898a`.

A matriz completa Fresh ON foi executada no candidato anterior `9a3df4c9365a34725129bfc77c16e36a85002ed1bef6227cf2214cf398fa1305`. Entre ele e o JAR final, somente o mixin de chapéu cosmético e sua configuração mudaram, e foi acrescentado o mixin dos itens próprios nas mãos. **21.515 entradas permaneceram byte a byte idênticas**. A prova detalhada está em `reports/visual-fix/on-matrix-reuse.json` e é regenerável com `python qa/verify_visual_reuse.py`.

O reaproveitamento limita-se à matriz de corpos, skins, armaduras e próteses com mãos vazias e sem os chapéus cosméticos de protótipo. Não é uma execução completa Fresh ON do JAR final. Os caminhos alterados exigem novas rodadas ON/OFF próprias. Cada captura conserva o hash de seu candidato de origem.

O candidato intermediário `5d7993ce89fe5b8a27d750c334891a31cb0cdef46d30aca7b3dc7fdb120f309b` executou a matriz OFF completa e as armas ON/OFF. Na revisão dos protótipos apareceu um erro adicional: o tapa-olho usava o encaixe baixo do rosto MCA nativo também nas faces autorais, cujos olhos ficam mais altos. As capturas de protótipo desse candidato têm execução concluída, mas **FAIL visual do tapa-olho**. Não foram reaproveitadas como evidência de correção.

`TricorneExtractMixin` agora resolve uma cópia transitória do equipamento com um seletor de modelo próprio para a face pirata. Usa a geometria do encaixe alto já existente; mantém o encaixe nativo MCA, o fallback de jogador, o chapéu combinado e os dados salvos. A prova de preservação também compara este candidato intermediário ao final: somente esse extrator e dois seletores novos mudaram. Por isso a matriz OFF sem cosméticos e as armas com cabeça vazia são reaproveitáveis. O teste rápido `patch-probe-on` produziu 28 capturas novas e mostrou o tapa-olho na altura do olho. A regressão completa dos protótipos foi reiniciada.

## Ambiente

Minecraft 26.2; Java Corretto 25.0.2; Fabric Loader 0.20.1; Fabric API 0.155.2+26.2; Loom 1.17.21; MCA 8.1.11+26.2; Fish of Thieves 26.2.2.0. Fixture adicional: EMF 3.3.11, ETF 7.2.5, Fresh Animations 1.11.2, FA+All Extensions 1.10.1, FA+Player 1.1, 3D Armor 0.9.4.2, SkinLayers3D 1.11.4, Better Combat 3.2.2 e os módulos EMF compat Core 2.3.2 / Better Combat 2.1.0.

As rodadas OFF removem os três packs Fresh da lista ativa e conservam os demais mods. As rodadas ON registram os três packs ativos. A fixture não equivale a testar integralmente o modpack Fabulously Optimized 14.1.1. Os logs contêm avisos de opcionais ausentes, atrasos durante capturas e falhas 401 de serviços online na sessão de teste; isso não será descrito como execução sem warnings/erros.

## Cobertura e resultados

Resultados executados são registrados por gate em `reports/visual-fix/gate-sequence.log`. Um gate concluído comprova somente as asserções e capturas explicitadas no log. A avaliação artística fica separada.

| Área | Estado atual | Escopo |
|---|---|---|
| Mangas das quatro famílias | PASS no ensaio dirigido ON; regressão em andamento | Capturas anteriores e posteriores, controle de um único NPC e contrato de vértices reais. |
| Peitorais femininos | PASS nas capturas dirigidas; regressão em andamento | Casacos opacos de capitão e imediato; máscara reversível. |
| Próteses | Correção implementada; revisão completa em andamento | Ambos os lados, quatro vistas e sequências temporais. |
| Catálogo feminino | PASS automatizado; revisão visual em andamento | 38 perfis, 19/19, fontes originais preservadas e variantes realmente distintas. |
| Geometria/UV | PASS estrutural; revisão artística em andamento | 1.091 exports, 4.349 slots, 54.098 caixas e 324.588 faces. |
| Blockbench | PASS de abertura/export | Fonte editável de madeira; não aprova render in-game. |
| Python | PASS, 14 testes na primeira rodada | Inclui contratos de skins, geometria e persistência de IDs; nova regressão agendada. |
| Matriz completa ON | PASS de execução no candidato anterior | 2.148 NPCs/poses capturados + 27 quadros de jogador = 2.175 PNGs. Reuso limitado conforme prova acima. |
| Matriz completa OFF | Em execução | JAR final; mesmas famílias e skins. |
| Armas/protótipos/variantes | Aguardando gates | JAR final, execuções separadas. |
| TC-SPAWN-05 | Aguardando gate | Descarregamento real de chunks e comparação de identidade/equipamentos. |
| TC-SPAWN-06 | Aguardando gates | Reinício do servidor integrado e reabertura em processo Java diferente. |
| TC-SPAWN-07 | Aguardando gate | 5/30/120 segundos e reload de recursos equivalente ao F3+T. |
| TC-SPAWN-08 | Aguardando gate | 48 NPCs, 24 de cada gênero, diversidade e estabilidade. |
| Mundo antigo | Aguardando gate | Cópia do mundo da baseline Alpha 18; origem preservada. |

A matriz de variantes representa 187 grupos de geometria/UV e 85 itens acessórios. Isso **não significa inspeção visual individual de todas as 1.091 texturas/combinações**. A matriz de skins nativas cobre 20 texturas corporais; as 322 roupas MCA não foram todas recapturadas individualmente. Movimento de NPCs é dirigido por fixture em ticks e não comprova pathfinding ou IA autônoma. Better Combat é exercitado por entradas reais de jogador; swing controlado de NPC MCA não comprova integração Better Combat para NPCs.

## Reproduzir

```powershell
python scripts/pmca/validate_wardrobe.py
python scripts/pmca/validate_atelier.py
python scripts/pmca/validar_assets.py --root .
python -m unittest discover -s scripts/pmca/tests -v
.\gradlew.bat build ledgerRegressions ledgerSafetyRegressions
.\gradlew.bat runGameTest
.\qa\run_visual_fix_gates.ps1
```

O script serial evita builds Gradle concorrentes, preserva tentativas falhas, valida o hash do JAR e só reaproveita gates concluídos com o mesmo candidato. Para gerar a galeria e ZIPs de PNGs originais, use `qa/package_visual_fix.py` com os nomes dos gates completos e `--raw`. A galeria contém recortes de navegação; os PNGs originais não recebem filtros nem retoques.

## Pendências que impedem aprovação integral

Finalizar os gates indicados, inspecionar seus quadros, registrar defeitos remanescentes por modelo/UV/animação e atualizar esta tabela. Fidelidade artística integral aos concepts não foi aprovada. Shaders, emotes, serviços online, todo o Fabulously Optimized e profiling de CPU/memória/pathfinding em 4/12/24/48 NPCs não foram certificados nesta rodada visual. Não há remoto Git configurado neste checkout; publicação GitHub não está disponível.

Errata da baseline: a matriz Alpha 18 correta contém 364 quadros (140 + 140 + 70 + 14). O ZIP histórico de 434 incluía 70 capturas nativas superadas. Foi mantido como histórico e gerado um pacote corrigido separado, sem tratar duplicatas como cobertura adicional.
