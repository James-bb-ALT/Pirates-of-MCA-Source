# Fase 3 — próteses e pose efetiva

P1: a máscara agora considera os campos reais leftLegwear/rightLegwear e leftArmwear/rightArmwear do MCA, além dos overlays vanilla. O estado de visibilidade é restaurado depois do desenho. O corte da roupa superior e a remoção da anatomia inferior usam o mesmo lado anatômico; não derivam de yaw mundial.

Os componentes originais de perna de pau dos 708 conjuntos peg foram separados de seus volumes de roupa, mantendo os UVs e o material de cada variante. A extensão inferior foi normalizada de 13,65 para 12 unidades locais e o encaixe superior para 6,26, sobrepondo o corte da calça em 6,3. A normalização está nos BBModels editáveis, não somente em uma transformação oculta de runtime. Botas de lado incompatível usam a variante normal naquele pé. A prótese avulsa mantém sua malha própria: os 17 BBModels existentes não foram substituídos.

P2: a auditoria do JAR real de EMF 3.3.11 localizou falseAnimation no MixinLivingEntityRenderer, imediatamente depois de EntityModel.setupAnim, com animateNoPause no root efetivo. Os layers do addon usam root.translateAndRotate e o ModelPart efetivamente fornecido pelo renderer; não copiam somente ângulos Euler do modelo vanilla. Os arquivos EMF-*.javap.txt preservam a evidência da API e do ponto de execução.

AnimationPoseProbe é opcional e só atua com piratesmca.reconstructionReview. A telemetria é emitida no submit real de PirateLimbsLayer, depois da aplicação das matrizes de root e membro. O teste com FA+Player ativo registrou EMFModelPartRoot/EMFModelPartVanilla no jogador e no MCA; sem FA+Player, o jogador registrou ModelPart vanilla. Assim, a comparação confirma a troca efetiva de modelo, além da simples presença do ZIP.

PASS executado: 16 fixtures temporais MCA, três quadros por pose, e nove ações por entrada real do jogador, três quadros por ação, nos quatro cenários wide/slim com FA+Player ativo/desativado. As ações do jogador incluem ataque Better Combat, arco com munição, escudo e comida. Logs reconstruction-ab-*.log; o resultado consolidado e o SHA256 exato constam em validation.json após o gate de entrega.

FAIL de preparação, corrigidos e preservados nos logs: cavalo selvagem desmontava o NPC; a fixture agora usa cavalo domado sem IA. O teste de arco sobrescrevia a munição ao equipar a mão principal; agora fornece flechas após equipar o arco. Não são apresentados como falhas corrigidas do mod.

BLOCKED: emote sem mod/trigger verificado. Primeira pessoa e a matriz completa de poses de player, além das nove executadas, continuam sem certificação. As fixtures MCA controlam os estados: não comprovam mineração, natação ou combate autônomos da IA.
