# Lote 0 — decisão do renderer

Alvo inspecionado: MCA 8.1.11+26.2 real, SHA256 conferido pelo Gradle;
Minecraft 26.2 oficial, Java 25. Os dumps javap desta pasta são evidência das APIs
e do bytecode, não de compatibilidade gráfica por si só.

## Pipeline observado

- VillagerEntityMCARenderer cria o modelo animado com ModelLayers.PLAYER via
  EntityRendererProvider.Context.bakeLayer. O corpo é humanoide, não o nariz e os
  braços cruzados do villager vanilla.
- Camadas SkinLayer, FaceLayer, ClothingLayer e HairLayer usam modelos visíveis
  próprios. createVisibleModel passa pelo LayerDefinition.create(...,64,64).bakeRoot.
- bodyData parte de PlayerModel.createMesh(deformation,false), acrescentando a
  região de peito. Cabelo usa a cabeça humana 8×8×8 com expansão adicional 0,3;
  as deformações de Face/Clothing/Hair são 0,01 / 0,0625 / 0,125.
- VillagerLikeEntityMCARenderer herda HumanoidMobRenderer, que já adiciona
  CustomHeadLayer e ItemInHandLayer. MCA acrescenta HumanoidArmorLayer própria.
- CommonVillagerModel.renderCommon aplica getDimensions().getHead() às partes da
  cabeça. O CustomHeadLayer genérico não aplica essa dimensão especial do MCA.
- O encaixe nativo usa a raiz e HeadedModel.translateToHead: o pivô animado real,
  não uma posição fixa no mundo. O offset de item é -0,25 e a escala é
  (0,625,-0,625,-0,625), com rotação Y de 180°.

## Candidata A — CEM via EMF

Não demonstrada como target correto para a entidade MCA. Um villager.jem genérico
não corresponde à malha humana nem às camadas visuais independentes do MCA.
O modelo animado passa pelo bake PLAYER; as camadas de pele/cabelo são montadas
separadamente. Isso impede tratar um único JEM como prova de substituição completa.
As rodadas isoladas com EMF 3.3.11, ETF 7.2.5 e Fresh 1.11.2 registram as classes
reais da raiz/cabeça em QA_MCA_HEAD_RENDER; consultar os logs, sem extrapolar para
outros packs CEM. Não foram copiados modelos do Fresh e nenhum .jem foi entregue.
Status: BLOCKED para export CEM específico, por ausência de target comprovado.

## Candidata B — camada Fabric / equipamento nativo MCA (escolhida)

Usar o CustomHeadLayer já instalado pelo renderer MCA. Um item equippable HEAD,
sem EquipmentAsset de armadura, é roteado para essa camada. O export Java Item
tem finalidade demonstrada de chapéu; isso não serve como solução de prótese.

O adaptador Fabric de escopo restrito captura a escala da cabeça no render state
somente quando o NPC equipa nosso tricórnio. O submit da camada nativa aplica essa
escala em push/finally/pop. Sem acessar entidades do servidor durante submit,
sem substituir renderer, corpo, cabeça, cabelo ou animador. Outros itens e entidades
continuam com escala 1 no adaptador. A camada nativa mantém rotação e pose.

Fonte do modelo: models/blockbench/head/tricorne_classic.bbmodel, formato 5.0 com
groups e outliner separados. Fonte PNG: textures/wearables/head/tricorne_classic.png.
Export: exports/fabric_renderer/tricorne_classic.json. Runtime usa models/item e
textures/item: o atlas de itens 26.2 não incluía a antiga pasta wearables.
UV do Blockbench em pixels é normalizado para 0–16 no export vanilla.

Display head scale=1,6 cancela a escala nativa 0,625. No modelo de item, y=12
coincide com o topo humano Java y=-8 após o offset nativo. Há espaço adicional
acima da cabeça; clipping com todos os cabelos/poses ainda precisa ser certificado.

## Referência editável

reference_adult_mca.bbmodel documenta a malha neutra adulta herdada de PlayerModel;
conversão de eixos: Blockbench Y=24-JavaY. É referência de editor, não skin MCA nem
export de corpo runtime. Escalas genéticas e características de gênero continuam
responsabilidade do MCA. As rodadas finais registram pivôs/classes em log e,
quando disponível, mca-neutral-reference-runtime.json contém os cubos reais.

Formato consultado: [documentação oficial bbmodel](https://www.blockbench.net/wiki/docs/bbmodel/).
O texto obtido está em blockbench-format-reference.md. JSON válido não substitui
abertura no editor: essa verificação deve ter status próprio no relatório.
