# Reconstrução v11 — Fase 1

PASS: 3333 entradas do CSV lidas; 167 seções do diagnóstico indexadas; ZIP da auditoria com CRC válido. Os diagnósticos são tratados como achados da auditoria, não como confirmação automática de causa.

PASS: reprodução nova no cliente Fabric real dos dois conjuntos captain_armor_pristine_normal / fisherman_teal_pristine_peg_left e skins boatswain / deckhand_f. 12 capturas em before/. Comando: gradlew runProductionClientTest -PreconstructionReview=before --no-daemon. Boot e execução em before/execution.log.

## Causas verificadas
- S1/S2: AtelierSkinMixin seleciona a textura de skin completa correta. AtelierFaceHairMixin não trata ClothingLayer. RECON_TRACE confirma skin correta mais roupa piratesmca:skins/clothing/male/21.png e female/15.png. As camadas são separadas no renderer MCA, e a roupa é sobreposta à skin. Os 20 PNGs não precisam ser repintados.
- A1/A2/A3: PirateAtelier.register usa Item.Properties.humanoidArmor e o renderer MCA cria HumanoidArmorLayer vanilla. Não há camada de geometria de armadura importada em PiratesMCAClient. Os arquivos equipment JSON definem camadas/texturas, não malhas. A biblioteca original tem 1062 diretórios CEM de vestuário; os 29 equipamentos legados não têm modelos correspondentes nessa biblioteca. Isso exige adaptação explícita por peça, sem alegar fonte inexistente.
- P1: PirateLimbMask oculta bone e nomes left_pants/right_pants, mas o MCA possui leftLegwear/rightLegwear/leftArmwear/rightArmwear separados. A ausência desses campos na máscara é comprovada pelo código e API real; sua contribuição visual será verificada após correção. PirateLimbsLayer também reaplica ClothingLayer no coto sem considerar a skin completa.
- P2: não confirmado por imagem estática. Necessário teste temporal pós-pose com EMF e FA+Player.

## Ambiente
Minecraft 26.2, Fabric Loader 0.20.1, Fabric API 0.155.2+26.2, MCA 8.1.11+26.2, Loom 1.17.21 e Java 25. Baseline sem EMF; ordem real do launcher salva em launcher-pack-order.json; configuração pessoal somente lida. A/B dos packs será registrado separadamente, sem atribuir culpa ao pack.

Fases 2–5: pendentes no momento desta baseline. Nenhuma correção gráfica foi declarada com base apenas nesta etapa.
