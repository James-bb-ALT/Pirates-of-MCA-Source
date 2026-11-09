# Rota da faixa — auditoria 26.2

Fontes primárias locais: JAR real MCA 8.1.11+26.2, minecraft-merged.jar 26.2 e
sources da API Fabric rendering-v1 25.3.3+515ac5339e (API 0.155.2+26.2).
Dumps: mca-torso-pipeline.txt e common-villager-model.txt.

MCA usa VillagerEntityModelMCA, herdando HumanoidModel. Torso neutro tem pivô
(0,0,0), x±4,z±2,y0..12. Renderer.scale aplica rawHorizontalScaleFactor em X/Z
e rawVerticalScaleFactor em Y antes das layers; a faixa não deve repetir essa escala.
Dimensões/cabeça/seios têm tratamento próprio no MCA. A peça compacta ocupa
Y Java 8,6..11,6, na cintura neutra e acima do pivô de pernas Y12. Isso não garante
ausência de sobreposição com todos os volumes morfológicos MCA: oclusão frontal
parcial foi observada numa NPC, conforme o relatório do asset.

Candidato CEM: não foi demonstrado mapeamento de parte/target EMF para anexar
geometria ao torso do MCA sem substituir modelos Fresh. Portanto não adotado.
Essa ausência de prova não significa impossibilidade universal do CEM.

Rota escolhida: LivingEntityRenderLayerRegistrationCallback, filtrado para
VillagerLikeEntityMCARenderer e AvatarRenderer. Em 9.0.1, SashLayer acompanha root e
o centro dos pivôs reais das pernas (quadril), mantendo o yaw do torso. Não herda
o offset/pitch transitório do peito, que enterrava a faixa nas pernas ao acordar.
Não usa o osso da cabeça. Modelos de item só fornecem geometria/material;
não seriam roupa no torso sem essa layer explícita. FIXED resolve a transformação
do modelo, e escala (1,-1,-1) converte item Y-up/Z para o espaço Java do corpo.
Item local x=X+8,y=8-Y,z=8-Z; malha fonte contém esse contrato.

SashStateMixin adiciona ItemStackRenderState a HumanoidRenderState. SashExtractMixin
resolve exclusivamente o item sash_red em CHEST durante extractRenderState, limpa
o estado quando ausente e exclui bebês. submit usa somente o snapshot do frame,
sem ler entidades, mundo, inventário ou aleatoriedade. PoseStack usa push/finally/pop.
Sem OpenGL direto. Item não define equipment asset nem atributos de armadura;
HumanoidArmorLayer não desenha um peitoral adicional.

A faixa ocupa CHEST nativo para persistência neste MVP. Não é slot de cintura
nativo do MCA nem sistema genérico de acessórios; não deve substituir outro peitoral.
Registro automático na aba criativa usa o namespace já implementado.

As provas de API/compilação não substituem as capturas, poses e transições em
reports/sash_red.md. Compatibilidade do torso com EMF/Fresh só pode ser declarada
no alcance dos clientes executados, especialmente para modelos de jogador CEM.
