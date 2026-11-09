# Pirate Atelier v3 — novos trajes derivados das concept arts fornecidas pelo usuário

## 1. Conteúdo novo

- **10 conjuntos de armadura**: capitão, imediato, contramestre, saqueador, duelista, artilheiro, brutamontes, oficial naval, fantasma amaldiçoado e guardião de tesouros.
- **6 roupas civis**: comerciante do porto, contrabandista, navegador renegado, corsário de taverna, marinheiro veterano e pescador pirata.
- **6 trajes de tripulação**: capitão civil, dois marujos, cozinheiro, artilheiro e mestre de embarcação.
- Cada conjunto tem **3 estados** (intacto, gasto e rasgado) e **3 opções de pernas** (normal, prótese esquerda e direita), num total de **198 novos perfis** além dos 864 da v2.
- Cada perfil novo tem 4 sprites de inventário, 2 atlas de armadura em UV vanilla, 1 atlas CEM original de 128x128, 8 modelos CEM (capacete, peito, calças, botas, gancho esquerdo/direito e tapa-olho esquerdo/direito).
- Acessórios modulares 3D com textura e sprite separados (gancho, perna de pau, tapa-olho, chapéu, bandana, papagaio, cinto, lanterna, bainha, ombreira, medalha, joalheria, cachecol, mochila, luneta e objetos amaldiçoados).

## 2. Onde estão os assets

- `assets/pirateatelier/textures/entity/equipment/humanoid/` e `humanoid_leggings/`: atlas para armaduras do Minecraft 26.2.
- `assets/pirateatelier/textures/item/`: ícones para itens (precisam ser registrados em um mod).
- `assets/pirateatelier/textures/entity/cem/`: material atlas para modelos de roupa 3D.
- `development_assets/cem_wardrobe/<id>/`: modelos CEM `.jem` por peça e suas alternativas.
- `development_assets/cem_accessories/`: módulos 3D CEM individuais.
- `manifests/v3_concept_outfits.json` e `manifests/v3_accessories.json`: índices para integração de itens/equipamentos em Fabric.

## 3. Limites de integração

**Este ZIP é uma biblioteca de assets, NÃO um mod funcional com itens novos equipáveis.** A v2 continua preservada. EMF e Fresh Player Animations NÃO selecionam automaticamente esses `.jem` por ID de um item `pirateatelier:`. Para colocar cada modelo no jogador em runtime, um mod Fabric 26.2 deve registrar os itens e associar renderer de camada à peça equipada. Não sobrescrevemos o `player.jem` do Fresh Player Animations.

Há geometria 3D completa, inclusive de ganchos e próteses; **ocultar a mão e a perna originais do modelo vanilla** continua dependendo do renderer. Os modelos foram gerados e testados sintaticamente (JSON, UV e PNG), mas **não foram testados no Minecraft/EMF**. Alguns módulos exigem ajustes finais de pivot ao serem conectados a um renderer real.

## 4. Autoria

Desenhos de textura e modelos foram implementados proceduralmente para este pacote, com base **nas concept arts enviadas pelo usuário**, sem inserir PNGs dessas concept arts dentro das texturas do Minecraft, e sem reutilizar pixels do mod de referência.
