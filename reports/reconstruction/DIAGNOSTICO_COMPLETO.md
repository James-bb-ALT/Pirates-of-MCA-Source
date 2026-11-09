# Auditoria técnica e visual — Pirates of MCA (v11.0.0-alpha.15)

**Escopo:** 8 arquivos, 3.333 capturas (3.273 armaduras + 60 skins), 1.091 conjuntos de armadura, 118 arquétipos de Pirate Atelier em 9 variantes cada, 29 conjuntos `lfpirates` e 20 skins. Todas as capturas possuem vistas frontal, lateral e traseira.

## Método e limites
Esta auditoria combina inventário e leitura programática de **cada uma das 3.333 imagens**, visualização comparativa em pranchas de imagens por arquétipo, comparação com os atlases PNG reais, inspeção dos arquivos JSON/JEM/BBMODEL e varredura dos ZIPs de resource packs. As marcações por imagem delimitam **regiões comuns de falha**, não significam que cada detalhe geométrico foi isoladamente comprovado em todas as variações. Capturas estáticas não validam comportamento dinâmico. Não foram fornecidos o `.jar` completo, o código Java de renderer, o `latest.log`, o `options.txt` nem a lista de resource packs ativos com ordem; causas de runtime dependentes desses arquivos permanecem hipóteses a validar.

## Resultado consolidado
| Item | Resultado |
|---|---|
| Capturas auditadas | 3333 |
| Conjuntos de armadura | 1091 |
| Arquétipos Atelier | 118 |
| Skins distintas | 20 |
| Definições CEM `.jem` na referência | 8565 |
| Definições CEM `.jem` no runtime v11 | 0 |
| Arquivos comuns idênticos referência/v11 | 19978 |
| Arquivos comuns modificados | 17 |
| Referências de textura de equipamento quebradas | 0 |
| Resource packs ZIP no arquivo | 45 |
| Skins capturadas com uniforme vermelho | 19 |

**Causa estrutural principal (com evidência):** as 1.091 definições `assets/*/equipment/*.json` especificam apenas camadas `humanoid` e `humanoid_leggings`; **não são geometria 3D personalizada**. Os milhares de `.jem` do atelier original vivem em `development_assets`, ausentes no runtime v11. O pacote v11 inclui somente 17 fontes `.bbmodel` de ganchos/pernas e **não comprova** a implantação de geometria vestível. Os screenshots mostram modelos bípede vanilla e componentes majoritariamente planos.

**Texturas:** todos os mapeamentos de textura nos 1.091 arquivos `equipment/*.json` apontam para PNGs presentes. Isso não prova UV correto ou compatibilidade, mas não encontramos erro de caminho bruto.

**Comparação referência/v11:** dos arquivos com mesmo caminho nas duas distribuições, a imensa maioria é byte a byte igual. As 17 alterações comuns se restringem a descritores `.json` de itens e próteses. O `README` da v11 descreve fonte `source/src/main/resources/assets`, porém o pacote entregue não inclui o código Java que implementa a renderização.

## Taxonomia de erros — evidência e tratamento
- **A1 [VISUAL/ALTA] Face encoberta:** nas vistas de frente de armaduras, o volume do capacete/chapéu ocupa o cubo facial; não aparecem olhos/expressões. Diferenciar elmo fechado intencional de chapéu/capuz que precisa deixar o rosto livre. Corrigir geometria e camadas na cabeça; não resolver trocando somente a skin.
- **A2 [VISUAL/ALTA] Perda de relevo:** casacas, abas, ombreiras, bainhas, insígnias, cintos, acessórios e pregas são em grande parte pintados diretamente nas camadas humanoides vanilla. Usar malha 3D/partes articuladas com pivôs por osso, preservando base têxtil em PNG.
- **A3 [DADOS/ALTA] Biblioteca 3D não aplicada:** `.jem` do Atelier somente em diretórios de desenvolvimento e não exportados/ativados no runtime examinado. Conferir renderer Java e a efetiva seleção do modelo por item equipado.
- **P1 [VISUAL/ALTA] Próteses sobrepostas:** em fotos estáticas de `_peg_left/_peg_right`, vê-se pequena haste exposta ao final de uma perna com calça/armadura, em vez de substituição integral. Máscara deve retirar o membro anatômico e a armadura/segunda camada naquele lado, aplicando volume 3D de encaixe.
- **P2 [RELATO/NÃO VALIDÁVEL EM IMAGEM ESTÁTICA] Dessincronização da prótese:** usuário relata perna de pau desacoplada de Fresh Animations. Inspecionar o momento do render e aplicar matriz transformada de `left_leg`/`right_leg` **após** animação EMF; validar frames caminhando, correndo, nadando etc.
- **S1 [VISUAL+ATLAS/ALTA] Uniformização indevida de roupa:** 19 de 20 skins capturadas têm exatamente a mesma amostra de cor no peito `(65,26,29)` e emblema dourado. Suas texturas PNG de corpo são diferentes; a exceção `deckhand_f` tem roupa listrada que também não coincide integralmente com a roupa original. Identificar camada MCA/armadura/capture harness que as sobrepõe.
- **S2 [TESTE AUSENTE] Modelo real de skin:** três screenshots com roupa sobreposta não verificam os mapeamentos UV e o corpo original de cada PNG. Capturar skin nua com armadura e roupas do MCA explicitamente desligadas antes de declarar cada skin corrigida.

## Resource packs e possíveis conflitos
Foram encontrados **45** ZIPs de resource pack, incluindo `FA+Player-v1.1.zip`, `FreshAnimations_v1.11.2_26.1+.zip` e `FA+All_Extensions-v1.10.1.zip`. O arquivo **não contém a ordem efetivamente carregada** e inclui alguns pacotes desabilitados/arquivos `.rpo`. Não é correto atribuir prioridade entre eles apenas pelo nome/ordem no ZIP.

`FA+Player-v1.1.zip` inclui `assets/minecraft/emf/cem/player.jem`, `player_slim.jem`, `a_player_equipment.jpm` e recursos de armadura do jogador. Esse é o conflito/compatibilidade **mais relevante para investigar** na sincronização de ganchos e pernas; outros pacotes da lista não definem diretamente recursos nos namespaces `pirateatelier`, `lfpirates` ou `piratesmca`. A sobreposição do renderer e a precedência efetiva devem ser testadas em execução.

## Skins — 20 análises individuais
### `boatswain`
- **Referência:** `assets/pirateatelier/textures/skins/boatswain.png` (64×64). O atlas contém uniforme azul de contramestre, com colete e lenço; atributo identificável: boné azul escuro e cabeça escura.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `boatswain`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `boatswain` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/boatswain/{front,side,back}.jpg`.

### `captain_armor_skin`
- **Referência:** `assets/pirateatelier/textures/skins/captain_armor_skin.png` (64×64). O atlas contém uniforme de capitão vermelho, com detalhes dourados; atributo identificável: boné naval com faixa dourada.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `captain_armor_skin`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `captain_armor_skin` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/captain_armor_skin/{front,side,back}.jpg`.

### `captain_civilian`
- **Referência:** `assets/pirateatelier/textures/skins/captain_civilian.png` (64×64). O atlas contém casaca civil vinho/vermelha; atributo identificável: boné/chapéu de capitão.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `captain_civilian`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `captain_civilian` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/captain_civilian/{front,side,back}.jpg`.

### `deckhand`
- **Referência:** `assets/pirateatelier/textures/skins/deckhand.png` (64×64). O atlas contém jaqueta e calça marrons do marinheiro; atributo identificável: cabelo/cabeça castanhos.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `deckhand`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `deckhand` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/deckhand/{front,side,back}.jpg`.

### `deckhand_f`
- **Referência:** `assets/pirateatelier/textures/skins/deckhand_f.png` (64×64). O atlas contém roupa marrom/azul da tripulante; atributo identificável: cabelos castanhos.
- **Captura observada:** cabeça varia, mas o corpo usa uniforme azul/cinza listrado; amostra RGB do peito: `(58, 56, 72)`. A roupa observada não corresponde à individualidade de `deckhand_f`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `deckhand_f` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/deckhand_f/{front,side,back}.jpg`.

### `fisherman_turned_pirate`
- **Referência:** `assets/pirateatelier/textures/skins/fisherman_turned_pirate.png` (64×64). O atlas contém roupa azul-petróleo do pescador; atributo identificável: cabeça clara/cabelos acinzentados.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `fisherman_turned_pirate`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `fisherman_turned_pirate` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/fisherman_turned_pirate/{front,side,back}.jpg`.

### `gunner`
- **Referência:** `assets/pirateatelier/textures/skins/gunner.png` (64×64). O atlas contém uniforme marrom com alça azul; atributo identificável: rosto escuro e cabelo curto.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `gunner`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `gunner` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/gunner/{front,side,back}.jpg`.

### `harbor_merchant`
- **Referência:** `assets/pirateatelier/textures/skins/harbor_merchant.png` (64×64). O atlas contém roupa de mercador vinho; atributo identificável: rosto pálido e cabelos castanhos.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `harbor_merchant`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `harbor_merchant` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/harbor_merchant/{front,side,back}.jpg`.

### `naval_officer_skin`
- **Referência:** `assets/pirateatelier/textures/skins/naval_officer_skin.png` (64×64). O atlas contém farda naval azul e dourado; atributo identificável: chapéu azul de oficial.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `naval_officer_skin`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `naval_officer_skin` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/naval_officer_skin/{front,side,back}.jpg`.

### `rogue_navigator`
- **Referência:** `assets/pirateatelier/textures/skins/rogue_navigator.png` (64×64). O atlas contém uniforme azul de navegador; atributo identificável: capuz/chapéu azul.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `rogue_navigator`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `rogue_navigator` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/rogue_navigator/{front,side,back}.jpg`.

### `ships_cook`
- **Referência:** `assets/pirateatelier/textures/skins/ships_cook.png` (64×64). O atlas contém roupa branca de cozinheiro; atributo identificável: touca clara.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `ships_cook`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `ships_cook` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/ships_cook/{front,side,back}.jpg`.

### `smuggler`
- **Referência:** `assets/pirateatelier/textures/skins/smuggler.png` (64×64). O atlas contém jaqueta marrom escura; atributo identificável: capuz escuro.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `smuggler`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `smuggler` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/smuggler/{front,side,back}.jpg`.

### `tavern_raider`
- **Referência:** `assets/pirateatelier/textures/skins/tavern_raider.png` (64×64). O atlas contém uniforme com mangas claras e colete vinho; atributo identificável: cabelo castanho.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `tavern_raider`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `tavern_raider` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/tavern_raider/{front,side,back}.jpg`.

### `the_cabin_boy`
- **Referência:** `assets/pirateatelier/textures/skins/the_cabin_boy.png` (64×64). O atlas contém traje de aprendiz marrom; atributo identificável: rosto claro e cabelo castanho.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `the_cabin_boy`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `the_cabin_boy` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/the_cabin_boy/{front,side,back}.jpg`.

### `the_corsair`
- **Referência:** `assets/pirateatelier/textures/skins/the_corsair.png` (64×64). O atlas contém casaca vermelha de corsário; atributo identificável: chapéu azul de corsário.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `the_corsair`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `the_corsair` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/the_corsair/{front,side,back}.jpg`.

### `the_cursed_sailor`
- **Referência:** `assets/pirateatelier/textures/skins/the_cursed_sailor.png` (64×64). O atlas contém roupa amaldiçoada roxa com efeitos brilhantes; atributo identificável: rosto/cabeça ciano-escuros.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `the_cursed_sailor`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `the_cursed_sailor` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/the_cursed_sailor/{front,side,back}.jpg`.

### `the_dock_rat`
- **Referência:** `assets/pirateatelier/textures/skins/the_dock_rat.png` (64×64). O atlas contém roupa marrom do marinheiro de cais; atributo identificável: capuz/acessório cor de vinho.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `the_dock_rat`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `the_dock_rat` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/the_dock_rat/{front,side,back}.jpg`.

### `the_naval_deserter`
- **Referência:** `assets/pirateatelier/textures/skins/the_naval_deserter.png` (64×64). O atlas contém farda azul de desertor; atributo identificável: chapéu azul.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `the_naval_deserter`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `the_naval_deserter` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/the_naval_deserter/{front,side,back}.jpg`.

### `the_privateer`
- **Referência:** `assets/pirateatelier/textures/skins/the_privateer.png` (64×64). O atlas contém roupa vinho com colete claro; atributo identificável: chapéu vermelho-escuro.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `the_privateer`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `the_privateer` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/the_privateer/{front,side,back}.jpg`.

### `weathered_sailor`
- **Referência:** `assets/pirateatelier/textures/skins/weathered_sailor.png` (64×64). O atlas contém uniforme azul desgastado; atributo identificável: boné azul.
- **Captura observada:** cabeça varia, mas o corpo é tomado pelo mesmo uniforme vermelho com emblema do catálogo; amostra RGB do peito: `(65, 26, 29)`. A roupa observada não corresponde à individualidade de `weathered_sailor`.
- **Frente:** modelo de roupa incorreto/encoberto, impedindo verificar frente do PNG. **Lateral:** roupa sobreposta e acessórios 3D não demonstrados. **Costas:** identidade do traje de origem substituída (ou não verificável, no caso listrado).
- **Correção verificável:** renderizar `weathered_sailor` sem roupas/armaduras, comparar UV de cabeça e todas as faces do tronco com atlas e depois adicionar componentes MCA/Atelier um por vez; gerar captura de regressão `front/side/back`. **Evidência:** `piratesmca-skins.zip`, `skins/weathered_sailor/{front,side,back}.jpg`.

## Armaduras lfpirates — 29 conjuntos, análise individual
### `corsair_tier_1`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 1 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `corsair_tier_2`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 2 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `corsair_tier_3`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 3 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `craftsman`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `deckhand`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `fisherman`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `merchant_1`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `merchant_2`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `merchant_3`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `pirate_tier_1`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 1 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `pirate_tier_2`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 2 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `pirate_tier_3`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 3 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_blue_tier_1`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 1 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_blue_tier_2`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 2 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_blue_tier_3`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 3 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_gray_tier_1`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 1 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_gray_tier_2`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 2 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_gray_tier_3`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 3 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_nordic_tier_1`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 1 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_nordic_tier_2`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 2 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_nordic_tier_3`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 3 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_red_tier_1`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 1 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_red_tier_2`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 2 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `sailor_red_tier_3`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e nível 3 com faixa/chapéu e casaca sem relevo.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `skull_1`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `skull_2`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `skull_3`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `skull_4`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

### `skull_5`
- **Frente:** máscara/chapéu cobrindo cabeça e rosto, elementos frontais e traje antigo em camada bípede sem volume em lapelas/bolsos/abas.
- **Lado:** espessura de chapéu/ombros/casaco limitada ao corpo vanilla.
- **Costas:** costas pouco definidas e poucos elementos físicos do traje; comparar com atlas `assets/lfpirates/textures/...`.
- **Correção:** inspecionar `assets/lfpirates/equipment/`, planejar modelagem das peças que precisam de 3D e preservar as texturas base; validar o set real em cada slot.

## Pirate Atelier — 118 arquétipos x 9 variantes = 1.062 conjuntos
Cada arquétipo abaixo representa suas 9 combinações: `pristine / tattered / weathered × normal / peg_left / peg_right`. A falha base A1/A2/A3 aplica-se ao arquétipo. Para cada combinação foi feita a checagem individual de existência das 3 vistas, identificação de prótese/desgaste e métrica de diferença em relação ao `pristine_normal`. As diferenças de desgaste foram detectadas, portanto **não** são reportadas como texturas idênticas ou ausentes sem evidência.

### `boarding_raider_armor`
- **Defeito visual específico:** ombreiras, tiras de couro e peças metálicas deveriam ter espessura; na captura os recortes permanecem colados ao torso e os ombros são volumes padrão. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `boarding_raider_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/boarding_raider_armor_pristine_normal/{front,side,back}.jpg`.
  - `boarding_raider_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.49, L=1.14, T=1.99. Capturas: `armor/pirateatelier/boarding_raider_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `boarding_raider_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.40, L=0.82, T=2.14. Capturas: `armor/pirateatelier/boarding_raider_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `boarding_raider_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.63, L=1.11, T=2.08. Capturas: `armor/pirateatelier/boarding_raider_armor_tattered_normal/{front,side,back}.jpg`.
  - `boarding_raider_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.88, L=1.49, T=2.69. Capturas: `armor/pirateatelier/boarding_raider_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `boarding_raider_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.51, L=0.91, T=2.60. Capturas: `armor/pirateatelier/boarding_raider_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `boarding_raider_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.51, L=0.77, T=1.66. Capturas: `armor/pirateatelier/boarding_raider_armor_weathered_normal/{front,side,back}.jpg`.
  - `boarding_raider_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.14, L=1.25, T=2.20. Capturas: `armor/pirateatelier/boarding_raider_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `boarding_raider_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.00, L=0.63, T=2.16. Capturas: `armor/pirateatelier/boarding_raider_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/boarding_raider_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/boarding_raider_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/boarding_raider_armor_pristine_normal/`.

### `boatswain`
- **Defeito visual específico:** lapelas, cordões dourados e gola do contramestre aparecem pintados na jaqueta sem espessura lateral. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `boatswain_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/boatswain_pristine_normal/{front,side,back}.jpg`.
  - `boatswain_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.05, L=1.01, T=1.82. Capturas: `armor/pirateatelier/boatswain_pristine_peg_left/{front,side,back}.jpg`.
  - `boatswain_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.64, L=0.40, T=1.99. Capturas: `armor/pirateatelier/boatswain_pristine_peg_right/{front,side,back}.jpg`.
  - `boatswain_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.87, L=0.74, T=2.35. Capturas: `armor/pirateatelier/boatswain_tattered_normal/{front,side,back}.jpg`.
  - `boatswain_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.59, L=1.47, T=2.84. Capturas: `armor/pirateatelier/boatswain_tattered_peg_left/{front,side,back}.jpg`.
  - `boatswain_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.54, L=0.96, T=2.53. Capturas: `armor/pirateatelier/boatswain_tattered_peg_right/{front,side,back}.jpg`.
  - `boatswain_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.57, L=0.88, T=1.50. Capturas: `armor/pirateatelier/boatswain_weathered_normal/{front,side,back}.jpg`.
  - `boatswain_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.10, L=1.19, T=2.12. Capturas: `armor/pirateatelier/boatswain_weathered_peg_left/{front,side,back}.jpg`.
  - `boatswain_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.67, L=0.44, T=2.09. Capturas: `armor/pirateatelier/boatswain_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/boatswain_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/boatswain_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/boatswain_pristine_normal/`.

### `captain_armor`
- **Defeito visual específico:** chapéu/tiara cobre a região facial, o brasão dourado fica achatado e as ombreiras não têm peças 3D independentes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `captain_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/captain_armor_pristine_normal/{front,side,back}.jpg`.
  - `captain_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.07, L=1.09, T=1.82. Capturas: `armor/pirateatelier/captain_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `captain_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.92, L=0.62, T=2.17. Capturas: `armor/pirateatelier/captain_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `captain_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.33, L=1.22, T=2.69. Capturas: `armor/pirateatelier/captain_armor_tattered_normal/{front,side,back}.jpg`.
  - `captain_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.07, L=1.99, T=3.39. Capturas: `armor/pirateatelier/captain_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `captain_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.28, L=1.35, T=3.17. Capturas: `armor/pirateatelier/captain_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `captain_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.19, L=0.95, T=1.89. Capturas: `armor/pirateatelier/captain_armor_weathered_normal/{front,side,back}.jpg`.
  - `captain_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.17, L=1.29, T=1.94. Capturas: `armor/pirateatelier/captain_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `captain_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.04, L=0.53, T=1.97. Capturas: `armor/pirateatelier/captain_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/captain_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/captain_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/captain_armor_pristine_normal/`.

### `civilian_captain`
- **Defeito visual específico:** o chapéu ocupa a face; a casaca civil, lapelas e barra posterior não se separam do torso. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `civilian_captain_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/civilian_captain_pristine_normal/{front,side,back}.jpg`.
  - `civilian_captain_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.06, L=1.08, T=1.93. Capturas: `armor/pirateatelier/civilian_captain_pristine_peg_left/{front,side,back}.jpg`.
  - `civilian_captain_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.88, L=0.71, T=2.09. Capturas: `armor/pirateatelier/civilian_captain_pristine_peg_right/{front,side,back}.jpg`.
  - `civilian_captain_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.03, L=1.00, T=2.28. Capturas: `armor/pirateatelier/civilian_captain_tattered_normal/{front,side,back}.jpg`.
  - `civilian_captain_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.67, L=1.65, T=2.79. Capturas: `armor/pirateatelier/civilian_captain_tattered_peg_left/{front,side,back}.jpg`.
  - `civilian_captain_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.85, L=0.95, T=2.45. Capturas: `armor/pirateatelier/civilian_captain_tattered_peg_right/{front,side,back}.jpg`.
  - `civilian_captain_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.51, L=0.63, T=1.12. Capturas: `armor/pirateatelier/civilian_captain_weathered_normal/{front,side,back}.jpg`.
  - `civilian_captain_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.10, L=0.99, T=1.88. Capturas: `armor/pirateatelier/civilian_captain_weathered_peg_left/{front,side,back}.jpg`.
  - `civilian_captain_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.21, L=0.37, T=1.77. Capturas: `armor/pirateatelier/civilian_captain_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/civilian_captain_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/civilian_captain_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/civilian_captain_pristine_normal/`.

### `corsair_charcoal` — paleta carvão/preto
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_charcoal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_charcoal_pristine_normal/{front,side,back}.jpg`.
  - `corsair_charcoal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.15, L=1.17, T=2.11. Capturas: `armor/pirateatelier/corsair_charcoal_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_charcoal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.26, L=0.89, T=1.97. Capturas: `armor/pirateatelier/corsair_charcoal_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_charcoal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.87, L=1.15, T=1.90. Capturas: `armor/pirateatelier/corsair_charcoal_tattered_normal/{front,side,back}.jpg`.
  - `corsair_charcoal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.27, L=1.56, T=2.54. Capturas: `armor/pirateatelier/corsair_charcoal_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_charcoal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.25, L=0.87, T=2.66. Capturas: `armor/pirateatelier/corsair_charcoal_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_charcoal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.18, L=0.36, T=0.81. Capturas: `armor/pirateatelier/corsair_charcoal_weathered_normal/{front,side,back}.jpg`.
  - `corsair_charcoal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.92, L=0.96, T=1.68. Capturas: `armor/pirateatelier/corsair_charcoal_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_charcoal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.13, L=0.55, T=1.55. Capturas: `armor/pirateatelier/corsair_charcoal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_charcoal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_charcoal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_charcoal_pristine_normal/`.

### `corsair_copper` — paleta cobre
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_copper_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_copper_pristine_normal/{front,side,back}.jpg`.
  - `corsair_copper_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.66, L=1.18, T=2.07. Capturas: `armor/pirateatelier/corsair_copper_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_copper_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.97, L=0.99, T=2.13. Capturas: `armor/pirateatelier/corsair_copper_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_copper_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.76, L=0.95, T=1.98. Capturas: `armor/pirateatelier/corsair_copper_tattered_normal/{front,side,back}.jpg`.
  - `corsair_copper_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.88, L=1.18, T=2.37. Capturas: `armor/pirateatelier/corsair_copper_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_copper_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.20, L=0.52, T=2.03. Capturas: `armor/pirateatelier/corsair_copper_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_copper_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.31, L=0.54, T=1.30. Capturas: `armor/pirateatelier/corsair_copper_weathered_normal/{front,side,back}.jpg`.
  - `corsair_copper_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.06, L=1.14, T=2.22. Capturas: `armor/pirateatelier/corsair_copper_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_copper_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.39, L=0.71, T=2.02. Capturas: `armor/pirateatelier/corsair_copper_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_copper_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_copper_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_copper_pristine_normal/`.

### `corsair_crimson` — paleta carmesim
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_crimson_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_crimson_pristine_normal/{front,side,back}.jpg`.
  - `corsair_crimson_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.83, L=1.00, T=2.08. Capturas: `armor/pirateatelier/corsair_crimson_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_crimson_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.10, L=0.67, T=2.08. Capturas: `armor/pirateatelier/corsair_crimson_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_crimson_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.66, L=0.55, T=1.47. Capturas: `armor/pirateatelier/corsair_crimson_tattered_normal/{front,side,back}.jpg`.
  - `corsair_crimson_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.59, L=0.99, T=2.71. Capturas: `armor/pirateatelier/corsair_crimson_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_crimson_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.09, L=0.89, T=2.42. Capturas: `armor/pirateatelier/corsair_crimson_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_crimson_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.42, L=0.76, T=1.42. Capturas: `armor/pirateatelier/corsair_crimson_weathered_normal/{front,side,back}.jpg`.
  - `corsair_crimson_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.12, L=1.16, T=2.18. Capturas: `armor/pirateatelier/corsair_crimson_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_crimson_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.41, L=0.57, T=2.09. Capturas: `armor/pirateatelier/corsair_crimson_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_crimson_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_crimson_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_crimson_pristine_normal/`.

### `corsair_emerald` — paleta esmeralda
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_emerald_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_emerald_pristine_normal/{front,side,back}.jpg`.
  - `corsair_emerald_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.06, L=0.95, T=1.88. Capturas: `armor/pirateatelier/corsair_emerald_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_emerald_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.33, L=0.58, T=2.08. Capturas: `armor/pirateatelier/corsair_emerald_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_emerald_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.97, L=0.91, T=1.83. Capturas: `armor/pirateatelier/corsair_emerald_tattered_normal/{front,side,back}.jpg`.
  - `corsair_emerald_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.13, L=1.43, T=2.28. Capturas: `armor/pirateatelier/corsair_emerald_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_emerald_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.22, L=1.09, T=2.60. Capturas: `armor/pirateatelier/corsair_emerald_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_emerald_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.58, L=0.90, T=1.46. Capturas: `armor/pirateatelier/corsair_emerald_weathered_normal/{front,side,back}.jpg`.
  - `corsair_emerald_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.01, L=1.31, T=2.07. Capturas: `armor/pirateatelier/corsair_emerald_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_emerald_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.28, L=0.76, T=2.08. Capturas: `armor/pirateatelier/corsair_emerald_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_emerald_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_emerald_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_emerald_pristine_normal/`.

### `corsair_forest` — paleta verde floresta
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_forest_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_forest_pristine_normal/{front,side,back}.jpg`.
  - `corsair_forest_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.91, L=0.92, T=1.97. Capturas: `armor/pirateatelier/corsair_forest_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_forest_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.27, L=0.75, T=2.36. Capturas: `armor/pirateatelier/corsair_forest_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_forest_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.18, L=1.12, T=2.40. Capturas: `armor/pirateatelier/corsair_forest_tattered_normal/{front,side,back}.jpg`.
  - `corsair_forest_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.32, L=1.59, T=2.61. Capturas: `armor/pirateatelier/corsair_forest_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_forest_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.36, L=1.05, T=2.44. Capturas: `armor/pirateatelier/corsair_forest_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_forest_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.39, L=0.91, T=1.47. Capturas: `armor/pirateatelier/corsair_forest_weathered_normal/{front,side,back}.jpg`.
  - `corsair_forest_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.75, L=1.10, T=2.18. Capturas: `armor/pirateatelier/corsair_forest_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_forest_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.27, L=0.67, T=2.44. Capturas: `armor/pirateatelier/corsair_forest_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_forest_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_forest_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_forest_pristine_normal/`.

### `corsair_ivory` — paleta marfim/cinza-claro
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_ivory_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_ivory_pristine_normal/{front,side,back}.jpg`.
  - `corsair_ivory_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.03, L=1.27, T=2.72. Capturas: `armor/pirateatelier/corsair_ivory_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_ivory_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.08, L=1.03, T=2.86. Capturas: `armor/pirateatelier/corsair_ivory_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_ivory_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.20, L=1.44, T=3.17. Capturas: `armor/pirateatelier/corsair_ivory_tattered_normal/{front,side,back}.jpg`.
  - `corsair_ivory_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=4.16, L=1.75, T=3.41. Capturas: `armor/pirateatelier/corsair_ivory_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_ivory_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=4.01, L=1.20, T=3.17. Capturas: `armor/pirateatelier/corsair_ivory_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_ivory_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.71, L=0.86, T=1.88. Capturas: `armor/pirateatelier/corsair_ivory_weathered_normal/{front,side,back}.jpg`.
  - `corsair_ivory_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.11, L=1.25, T=2.61. Capturas: `armor/pirateatelier/corsair_ivory_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_ivory_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.07, L=0.96, T=3.00. Capturas: `armor/pirateatelier/corsair_ivory_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_ivory_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_ivory_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_ivory_pristine_normal/`.

### `corsair_navy` — paleta azul-marinho
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_navy_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_navy_pristine_normal/{front,side,back}.jpg`.
  - `corsair_navy_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.90, L=1.24, T=2.03. Capturas: `armor/pirateatelier/corsair_navy_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_navy_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.15, L=1.03, T=2.53. Capturas: `armor/pirateatelier/corsair_navy_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_navy_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.40, L=1.32, T=2.38. Capturas: `armor/pirateatelier/corsair_navy_tattered_normal/{front,side,back}.jpg`.
  - `corsair_navy_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=4.21, L=1.79, T=2.81. Capturas: `armor/pirateatelier/corsair_navy_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_navy_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.46, L=1.01, T=2.21. Capturas: `armor/pirateatelier/corsair_navy_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_navy_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.38, L=0.54, T=0.50. Capturas: `armor/pirateatelier/corsair_navy_weathered_normal/{front,side,back}.jpg`.
  - `corsair_navy_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.85, L=0.96, T=1.71. Capturas: `armor/pirateatelier/corsair_navy_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_navy_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.10, L=0.67, T=2.20. Capturas: `armor/pirateatelier/corsair_navy_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_navy_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_navy_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_navy_pristine_normal/`.

### `corsair_ochre` — paleta ocre
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_ochre_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_ochre_pristine_normal/{front,side,back}.jpg`.
  - `corsair_ochre_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.01, L=1.19, T=2.48. Capturas: `armor/pirateatelier/corsair_ochre_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_ochre_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.02, L=1.10, T=2.78. Capturas: `armor/pirateatelier/corsair_ochre_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_ochre_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.91, L=1.25, T=3.07. Capturas: `armor/pirateatelier/corsair_ochre_tattered_normal/{front,side,back}.jpg`.
  - `corsair_ochre_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.46, L=1.49, T=3.14. Capturas: `armor/pirateatelier/corsair_ochre_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_ochre_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.39, L=0.90, T=2.69. Capturas: `armor/pirateatelier/corsair_ochre_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_ochre_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.52, L=0.71, T=1.58. Capturas: `armor/pirateatelier/corsair_ochre_weathered_normal/{front,side,back}.jpg`.
  - `corsair_ochre_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.28, L=1.06, T=2.47. Capturas: `armor/pirateatelier/corsair_ochre_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_ochre_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.11, L=0.77, T=2.50. Capturas: `armor/pirateatelier/corsair_ochre_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_ochre_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_ochre_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_ochre_pristine_normal/`.

### `corsair_plum` — paleta ameixa/roxo
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_plum_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_plum_pristine_normal/{front,side,back}.jpg`.
  - `corsair_plum_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.74, L=1.23, T=1.82. Capturas: `armor/pirateatelier/corsair_plum_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_plum_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.81, L=0.78, T=2.04. Capturas: `armor/pirateatelier/corsair_plum_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_plum_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.55, L=0.89, T=2.11. Capturas: `armor/pirateatelier/corsair_plum_tattered_normal/{front,side,back}.jpg`.
  - `corsair_plum_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.15, L=1.26, T=3.07. Capturas: `armor/pirateatelier/corsair_plum_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_plum_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.28, L=0.82, T=2.57. Capturas: `armor/pirateatelier/corsair_plum_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_plum_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.28, L=0.75, T=1.29. Capturas: `armor/pirateatelier/corsair_plum_weathered_normal/{front,side,back}.jpg`.
  - `corsair_plum_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.20, L=1.18, T=2.00. Capturas: `armor/pirateatelier/corsair_plum_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_plum_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.08, L=0.68, T=2.00. Capturas: `armor/pirateatelier/corsair_plum_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_plum_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_plum_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_plum_pristine_normal/`.

### `corsair_sea_blue` — paleta azul-mar
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_sea_blue_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_sea_blue_pristine_normal/{front,side,back}.jpg`.
  - `corsair_sea_blue_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.43, L=1.22, T=1.91. Capturas: `armor/pirateatelier/corsair_sea_blue_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_sea_blue_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.24, L=0.56, T=1.71. Capturas: `armor/pirateatelier/corsair_sea_blue_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_sea_blue_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.71, L=0.60, T=1.89. Capturas: `armor/pirateatelier/corsair_sea_blue_tattered_normal/{front,side,back}.jpg`.
  - `corsair_sea_blue_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.06, L=1.41, T=2.90. Capturas: `armor/pirateatelier/corsair_sea_blue_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_sea_blue_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.40, L=1.07, T=3.11. Capturas: `armor/pirateatelier/corsair_sea_blue_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_sea_blue_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.26, L=0.85, T=1.75. Capturas: `armor/pirateatelier/corsair_sea_blue_weathered_normal/{front,side,back}.jpg`.
  - `corsair_sea_blue_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.20, L=1.38, T=2.40. Capturas: `armor/pirateatelier/corsair_sea_blue_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_sea_blue_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.32, L=0.77, T=2.22. Capturas: `armor/pirateatelier/corsair_sea_blue_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_sea_blue_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_sea_blue_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_sea_blue_pristine_normal/`.

### `corsair_teal` — paleta verde-azulado
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_teal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_teal_pristine_normal/{front,side,back}.jpg`.
  - `corsair_teal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.28, L=1.16, T=2.15. Capturas: `armor/pirateatelier/corsair_teal_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_teal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.67, L=0.70, T=2.20. Capturas: `armor/pirateatelier/corsair_teal_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_teal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.31, L=0.78, T=1.66. Capturas: `armor/pirateatelier/corsair_teal_tattered_normal/{front,side,back}.jpg`.
  - `corsair_teal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.20, L=1.31, T=2.61. Capturas: `armor/pirateatelier/corsair_teal_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_teal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.21, L=0.89, T=2.82. Capturas: `armor/pirateatelier/corsair_teal_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_teal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.46, L=0.86, T=1.62. Capturas: `armor/pirateatelier/corsair_teal_weathered_normal/{front,side,back}.jpg`.
  - `corsair_teal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.12, L=1.32, T=2.34. Capturas: `armor/pirateatelier/corsair_teal_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_teal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.29, L=0.83, T=2.25. Capturas: `armor/pirateatelier/corsair_teal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_teal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_teal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_teal_pristine_normal/`.

### `corsair_wine` — paleta vinho
- **Defeito visual específico:** chapéu/lenço de corsair forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `corsair_wine_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/corsair_wine_pristine_normal/{front,side,back}.jpg`.
  - `corsair_wine_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.92, L=1.02, T=1.91. Capturas: `armor/pirateatelier/corsair_wine_pristine_peg_left/{front,side,back}.jpg`.
  - `corsair_wine_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.25, L=0.76, T=2.22. Capturas: `armor/pirateatelier/corsair_wine_pristine_peg_right/{front,side,back}.jpg`.
  - `corsair_wine_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.26, L=1.05, T=2.22. Capturas: `armor/pirateatelier/corsair_wine_tattered_normal/{front,side,back}.jpg`.
  - `corsair_wine_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.15, L=1.70, T=2.56. Capturas: `armor/pirateatelier/corsair_wine_tattered_peg_left/{front,side,back}.jpg`.
  - `corsair_wine_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.23, L=1.07, T=2.62. Capturas: `armor/pirateatelier/corsair_wine_tattered_peg_right/{front,side,back}.jpg`.
  - `corsair_wine_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.28, L=0.94, T=1.58. Capturas: `armor/pirateatelier/corsair_wine_weathered_normal/{front,side,back}.jpg`.
  - `corsair_wine_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.68, L=1.25, T=1.98. Capturas: `armor/pirateatelier/corsair_wine_weathered_peg_left/{front,side,back}.jpg`.
  - `corsair_wine_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.24, L=0.73, T=2.28. Capturas: `armor/pirateatelier/corsair_wine_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/corsair_wine_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/corsair_wine_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/corsair_wine_pristine_normal/`.

### `craftsman_charcoal` — paleta carvão/preto
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_charcoal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_charcoal_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_charcoal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.53, L=0.94, T=1.88. Capturas: `armor/pirateatelier/craftsman_charcoal_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_charcoal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.07, L=0.71, T=2.31. Capturas: `armor/pirateatelier/craftsman_charcoal_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_charcoal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.16, L=1.19, T=2.33. Capturas: `armor/pirateatelier/craftsman_charcoal_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_charcoal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.68, L=1.74, T=2.60. Capturas: `armor/pirateatelier/craftsman_charcoal_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_charcoal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.32, L=1.21, T=2.64. Capturas: `armor/pirateatelier/craftsman_charcoal_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_charcoal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.52, L=0.85, T=1.47. Capturas: `armor/pirateatelier/craftsman_charcoal_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_charcoal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.86, L=1.09, T=1.62. Capturas: `armor/pirateatelier/craftsman_charcoal_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_charcoal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.91, L=0.52, T=2.19. Capturas: `armor/pirateatelier/craftsman_charcoal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_charcoal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_charcoal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_charcoal_pristine_normal/`.

### `craftsman_copper` — paleta cobre
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_copper_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_copper_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_copper_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.92, L=0.84, T=1.81. Capturas: `armor/pirateatelier/craftsman_copper_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_copper_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.77, L=0.79, T=2.22. Capturas: `armor/pirateatelier/craftsman_copper_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_copper_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.64, L=1.19, T=2.46. Capturas: `armor/pirateatelier/craftsman_copper_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_copper_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.20, L=1.53, T=2.92. Capturas: `armor/pirateatelier/craftsman_copper_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_copper_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.08, L=1.16, T=2.50. Capturas: `armor/pirateatelier/craftsman_copper_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_copper_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.37, L=0.84, T=1.52. Capturas: `armor/pirateatelier/craftsman_copper_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_copper_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.90, L=0.96, T=1.76. Capturas: `armor/pirateatelier/craftsman_copper_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_copper_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.71, L=0.62, T=1.94. Capturas: `armor/pirateatelier/craftsman_copper_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_copper_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_copper_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_copper_pristine_normal/`.

### `craftsman_crimson` — paleta carmesim
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_crimson_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_crimson_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_crimson_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.80, L=0.95, T=1.76. Capturas: `armor/pirateatelier/craftsman_crimson_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_crimson_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.87, L=0.79, T=2.11. Capturas: `armor/pirateatelier/craftsman_crimson_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_crimson_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.49, L=1.02, T=2.57. Capturas: `armor/pirateatelier/craftsman_crimson_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_crimson_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.08, L=1.57, T=2.78. Capturas: `armor/pirateatelier/craftsman_crimson_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_crimson_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.08, L=1.03, T=2.62. Capturas: `armor/pirateatelier/craftsman_crimson_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_crimson_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.24, L=0.60, T=1.00. Capturas: `armor/pirateatelier/craftsman_crimson_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_crimson_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.83, L=0.86, T=1.66. Capturas: `armor/pirateatelier/craftsman_crimson_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_crimson_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.90, L=0.41, T=1.67. Capturas: `armor/pirateatelier/craftsman_crimson_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_crimson_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_crimson_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_crimson_pristine_normal/`.

### `craftsman_emerald` — paleta esmeralda
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_emerald_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_emerald_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_emerald_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.91, L=1.16, T=1.89. Capturas: `armor/pirateatelier/craftsman_emerald_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_emerald_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.82, L=0.96, T=2.00. Capturas: `armor/pirateatelier/craftsman_emerald_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_emerald_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.48, L=1.17, T=2.05. Capturas: `armor/pirateatelier/craftsman_emerald_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_emerald_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.98, L=1.49, T=2.63. Capturas: `armor/pirateatelier/craftsman_emerald_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_emerald_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.01, L=0.96, T=2.18. Capturas: `armor/pirateatelier/craftsman_emerald_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_emerald_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.23, L=0.39, T=0.79. Capturas: `armor/pirateatelier/craftsman_emerald_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_emerald_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.81, L=0.97, T=2.14. Capturas: `armor/pirateatelier/craftsman_emerald_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_emerald_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.05, L=0.55, T=1.78. Capturas: `armor/pirateatelier/craftsman_emerald_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_emerald_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_emerald_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_emerald_pristine_normal/`.

### `craftsman_forest` — paleta verde floresta
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_forest_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_forest_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_forest_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.74, L=1.16, T=2.26. Capturas: `armor/pirateatelier/craftsman_forest_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_forest_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.95, L=0.89, T=2.08. Capturas: `armor/pirateatelier/craftsman_forest_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_forest_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.66, L=0.86, T=1.93. Capturas: `armor/pirateatelier/craftsman_forest_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_forest_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.86, L=1.14, T=2.56. Capturas: `armor/pirateatelier/craftsman_forest_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_forest_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.59, L=0.70, T=2.15. Capturas: `armor/pirateatelier/craftsman_forest_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_forest_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.01, L=0.60, T=1.50. Capturas: `armor/pirateatelier/craftsman_forest_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_forest_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.80, L=1.06, T=1.98. Capturas: `armor/pirateatelier/craftsman_forest_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_forest_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.33, L=0.73, T=2.11. Capturas: `armor/pirateatelier/craftsman_forest_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_forest_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_forest_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_forest_pristine_normal/`.

### `craftsman_ivory` — paleta marfim/cinza-claro
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_ivory_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_ivory_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_ivory_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.19, L=1.49, T=2.79. Capturas: `armor/pirateatelier/craftsman_ivory_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_ivory_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.32, L=1.19, T=2.88. Capturas: `armor/pirateatelier/craftsman_ivory_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_ivory_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.05, L=1.18, T=2.69. Capturas: `armor/pirateatelier/craftsman_ivory_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_ivory_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.24, L=1.42, T=2.88. Capturas: `armor/pirateatelier/craftsman_ivory_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_ivory_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.72, L=0.94, T=3.19. Capturas: `armor/pirateatelier/craftsman_ivory_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_ivory_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.94, L=0.80, T=2.03. Capturas: `armor/pirateatelier/craftsman_ivory_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_ivory_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.92, L=1.47, T=2.94. Capturas: `armor/pirateatelier/craftsman_ivory_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_ivory_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.13, L=1.05, T=2.83. Capturas: `armor/pirateatelier/craftsman_ivory_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_ivory_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_ivory_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_ivory_pristine_normal/`.

### `craftsman_navy` — paleta azul-marinho
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_navy_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_navy_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_navy_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.97, L=1.13, T=1.98. Capturas: `armor/pirateatelier/craftsman_navy_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_navy_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.44, L=0.65, T=2.27. Capturas: `armor/pirateatelier/craftsman_navy_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_navy_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.15, L=0.80, T=2.04. Capturas: `armor/pirateatelier/craftsman_navy_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_navy_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.26, L=1.47, T=2.32. Capturas: `armor/pirateatelier/craftsman_navy_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_navy_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.92, L=1.03, T=2.55. Capturas: `armor/pirateatelier/craftsman_navy_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_navy_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.17, L=0.87, T=1.35. Capturas: `armor/pirateatelier/craftsman_navy_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_navy_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.89, L=1.34, T=2.14. Capturas: `armor/pirateatelier/craftsman_navy_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_navy_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.31, L=0.71, T=2.27. Capturas: `armor/pirateatelier/craftsman_navy_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_navy_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_navy_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_navy_pristine_normal/`.

### `craftsman_ochre` — paleta ocre
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_ochre_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_ochre_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_ochre_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.06, L=0.95, T=2.42. Capturas: `armor/pirateatelier/craftsman_ochre_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_ochre_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.42, L=0.81, T=2.76. Capturas: `armor/pirateatelier/craftsman_ochre_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_ochre_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.40, L=1.23, T=2.89. Capturas: `armor/pirateatelier/craftsman_ochre_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_ochre_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.40, L=1.58, T=3.02. Capturas: `armor/pirateatelier/craftsman_ochre_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_ochre_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.14, L=1.22, T=2.80. Capturas: `armor/pirateatelier/craftsman_ochre_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_ochre_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.50, L=0.98, T=1.95. Capturas: `armor/pirateatelier/craftsman_ochre_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_ochre_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.75, L=1.14, T=2.41. Capturas: `armor/pirateatelier/craftsman_ochre_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_ochre_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.34, L=0.86, T=2.65. Capturas: `armor/pirateatelier/craftsman_ochre_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_ochre_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_ochre_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_ochre_pristine_normal/`.

### `craftsman_plum` — paleta ameixa/roxo
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_plum_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_plum_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_plum_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.49, L=0.96, T=1.98. Capturas: `armor/pirateatelier/craftsman_plum_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_plum_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.09, L=0.84, T=2.46. Capturas: `armor/pirateatelier/craftsman_plum_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_plum_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.67, L=1.40, T=2.49. Capturas: `armor/pirateatelier/craftsman_plum_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_plum_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.56, L=1.77, T=2.93. Capturas: `armor/pirateatelier/craftsman_plum_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_plum_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.13, L=1.03, T=2.41. Capturas: `armor/pirateatelier/craftsman_plum_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_plum_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.99, L=0.83, T=1.12. Capturas: `armor/pirateatelier/craftsman_plum_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_plum_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.75, L=1.03, T=1.94. Capturas: `armor/pirateatelier/craftsman_plum_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_plum_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.97, L=0.69, T=2.29. Capturas: `armor/pirateatelier/craftsman_plum_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_plum_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_plum_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_plum_pristine_normal/`.

### `craftsman_sea_blue` — paleta azul-mar
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_sea_blue_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_sea_blue_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_sea_blue_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.04, L=1.20, T=2.07. Capturas: `armor/pirateatelier/craftsman_sea_blue_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_sea_blue_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.08, L=0.96, T=2.68. Capturas: `armor/pirateatelier/craftsman_sea_blue_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_sea_blue_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.02, L=1.31, T=2.57. Capturas: `armor/pirateatelier/craftsman_sea_blue_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_sea_blue_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.73, L=1.78, T=3.04. Capturas: `armor/pirateatelier/craftsman_sea_blue_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_sea_blue_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.63, L=1.00, T=2.62. Capturas: `armor/pirateatelier/craftsman_sea_blue_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_sea_blue_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.22, L=0.59, T=0.86. Capturas: `armor/pirateatelier/craftsman_sea_blue_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_sea_blue_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.09, L=0.98, T=1.72. Capturas: `armor/pirateatelier/craftsman_sea_blue_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_sea_blue_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.88, L=0.57, T=2.22. Capturas: `armor/pirateatelier/craftsman_sea_blue_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_sea_blue_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_sea_blue_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_sea_blue_pristine_normal/`.

### `craftsman_teal` — paleta verde-azulado
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_teal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_teal_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_teal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.27, L=1.14, T=10.76. Capturas: `armor/pirateatelier/craftsman_teal_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_teal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=4.34, L=0.83, T=2.16. Capturas: `armor/pirateatelier/craftsman_teal_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_teal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.62, L=1.06, T=2.26. Capturas: `armor/pirateatelier/craftsman_teal_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_teal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.48, L=1.64, T=2.79. Capturas: `armor/pirateatelier/craftsman_teal_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_teal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.23, L=0.99, T=2.47. Capturas: `armor/pirateatelier/craftsman_teal_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_teal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.33, L=0.46, T=1.00. Capturas: `armor/pirateatelier/craftsman_teal_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_teal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.14, L=1.02, T=1.80. Capturas: `armor/pirateatelier/craftsman_teal_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_teal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.23, L=0.58, T=1.80. Capturas: `armor/pirateatelier/craftsman_teal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_teal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_teal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_teal_pristine_normal/`.

### `craftsman_wine` — paleta vinho
- **Defeito visual específico:** roupa de craftsman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `craftsman_wine_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/craftsman_wine_pristine_normal/{front,side,back}.jpg`.
  - `craftsman_wine_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.72, L=1.16, T=2.17. Capturas: `armor/pirateatelier/craftsman_wine_pristine_peg_left/{front,side,back}.jpg`.
  - `craftsman_wine_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.04, L=0.88, T=2.16. Capturas: `armor/pirateatelier/craftsman_wine_pristine_peg_right/{front,side,back}.jpg`.
  - `craftsman_wine_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.68, L=0.92, T=2.23. Capturas: `armor/pirateatelier/craftsman_wine_tattered_normal/{front,side,back}.jpg`.
  - `craftsman_wine_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.84, L=1.40, T=2.69. Capturas: `armor/pirateatelier/craftsman_wine_tattered_peg_left/{front,side,back}.jpg`.
  - `craftsman_wine_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.14, L=0.74, T=2.67. Capturas: `armor/pirateatelier/craftsman_wine_tattered_peg_right/{front,side,back}.jpg`.
  - `craftsman_wine_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.17, L=0.44, T=1.27. Capturas: `armor/pirateatelier/craftsman_wine_weathered_normal/{front,side,back}.jpg`.
  - `craftsman_wine_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.60, L=1.14, T=1.91. Capturas: `armor/pirateatelier/craftsman_wine_weathered_peg_left/{front,side,back}.jpg`.
  - `craftsman_wine_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.12, L=0.62, T=2.22. Capturas: `armor/pirateatelier/craftsman_wine_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/craftsman_wine_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/craftsman_wine_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/craftsman_wine_pristine_normal/`.

### `crew_gunner`
- **Defeito visual específico:** bandoleira e acessórios de municiamento não projetam volume; cabeça fica encoberta pelo capacete quadrado. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `crew_gunner_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/crew_gunner_pristine_normal/{front,side,back}.jpg`.
  - `crew_gunner_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.10, L=1.25, T=2.13. Capturas: `armor/pirateatelier/crew_gunner_pristine_peg_left/{front,side,back}.jpg`.
  - `crew_gunner_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.24, L=0.94, T=2.37. Capturas: `armor/pirateatelier/crew_gunner_pristine_peg_right/{front,side,back}.jpg`.
  - `crew_gunner_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.48, L=1.06, T=1.92. Capturas: `armor/pirateatelier/crew_gunner_tattered_normal/{front,side,back}.jpg`.
  - `crew_gunner_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.60, L=1.43, T=2.20. Capturas: `armor/pirateatelier/crew_gunner_tattered_peg_left/{front,side,back}.jpg`.
  - `crew_gunner_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.34, L=0.69, T=2.25. Capturas: `armor/pirateatelier/crew_gunner_tattered_peg_right/{front,side,back}.jpg`.
  - `crew_gunner_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.17, L=0.45, T=1.39. Capturas: `armor/pirateatelier/crew_gunner_weathered_normal/{front,side,back}.jpg`.
  - `crew_gunner_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.76, L=1.08, T=1.95. Capturas: `armor/pirateatelier/crew_gunner_weathered_peg_left/{front,side,back}.jpg`.
  - `crew_gunner_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.03, L=0.76, T=2.18. Capturas: `armor/pirateatelier/crew_gunner_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/crew_gunner_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/crew_gunner_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/crew_gunner_pristine_normal/`.

### `cursed_ghost_armor`
- **Defeito visual específico:** peças espectrais e detalhes cianos são somente pixels da superfície; contorno 3D e separação das partes não aparecem. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `cursed_ghost_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/cursed_ghost_armor_pristine_normal/{front,side,back}.jpg`.
  - `cursed_ghost_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.74, L=1.60, T=3.39. Capturas: `armor/pirateatelier/cursed_ghost_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `cursed_ghost_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.27, L=1.21, T=3.03. Capturas: `armor/pirateatelier/cursed_ghost_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `cursed_ghost_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.76, L=1.79, T=3.33. Capturas: `armor/pirateatelier/cursed_ghost_armor_tattered_normal/{front,side,back}.jpg`.
  - `cursed_ghost_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.75, L=1.95, T=3.72. Capturas: `armor/pirateatelier/cursed_ghost_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `cursed_ghost_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.59, L=1.54, T=3.50. Capturas: `armor/pirateatelier/cursed_ghost_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `cursed_ghost_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.39, L=0.89, T=2.02. Capturas: `armor/pirateatelier/cursed_ghost_armor_weathered_normal/{front,side,back}.jpg`.
  - `cursed_ghost_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.83, L=1.33, T=2.39. Capturas: `armor/pirateatelier/cursed_ghost_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `cursed_ghost_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.76, L=1.15, T=3.22. Capturas: `armor/pirateatelier/cursed_ghost_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/cursed_ghost_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/cursed_ghost_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/cursed_ghost_armor_pristine_normal/`.

### `cutlass_duelist_armor`
- **Defeito visual específico:** aba frontal do chapéu e peitoral se confundem com o rosto; bainha/bandoleira do duelista não apresentam relevo. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `cutlass_duelist_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/cutlass_duelist_armor_pristine_normal/{front,side,back}.jpg`.
  - `cutlass_duelist_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.09, L=1.30, T=2.28. Capturas: `armor/pirateatelier/cutlass_duelist_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `cutlass_duelist_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.80, L=0.87, T=2.38. Capturas: `armor/pirateatelier/cutlass_duelist_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `cutlass_duelist_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.62, L=1.00, T=2.09. Capturas: `armor/pirateatelier/cutlass_duelist_armor_tattered_normal/{front,side,back}.jpg`.
  - `cutlass_duelist_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.90, L=1.58, T=2.80. Capturas: `armor/pirateatelier/cutlass_duelist_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `cutlass_duelist_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.71, L=0.81, T=2.29. Capturas: `armor/pirateatelier/cutlass_duelist_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `cutlass_duelist_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.68, L=0.42, T=1.19. Capturas: `armor/pirateatelier/cutlass_duelist_armor_weathered_normal/{front,side,back}.jpg`.
  - `cutlass_duelist_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.99, L=1.09, T=1.90. Capturas: `armor/pirateatelier/cutlass_duelist_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `cutlass_duelist_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.42, L=0.62, T=2.00. Capturas: `armor/pirateatelier/cutlass_duelist_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/cutlass_duelist_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/cutlass_duelist_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/cutlass_duelist_armor_pristine_normal/`.

### `deckhand`
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.97, L=0.96, T=1.97. Capturas: `armor/pirateatelier/deckhand_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.99, L=0.59, T=1.84. Capturas: `armor/pirateatelier/deckhand_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.00, L=0.86, T=2.17. Capturas: `armor/pirateatelier/deckhand_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.02, L=1.20, T=2.48. Capturas: `armor/pirateatelier/deckhand_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.38, L=0.96, T=2.65. Capturas: `armor/pirateatelier/deckhand_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.86, L=1.00, T=2.01. Capturas: `armor/pirateatelier/deckhand_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.25, L=1.31, T=2.52. Capturas: `armor/pirateatelier/deckhand_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.13, L=0.77, T=2.23. Capturas: `armor/pirateatelier/deckhand_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_pristine_normal/`.

### `deckhand_charcoal` — paleta carvão/preto
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_charcoal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_charcoal_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_charcoal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.06, L=1.24, T=2.18. Capturas: `armor/pirateatelier/deckhand_charcoal_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_charcoal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.51, L=1.06, T=2.53. Capturas: `armor/pirateatelier/deckhand_charcoal_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_charcoal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.21, L=1.30, T=2.19. Capturas: `armor/pirateatelier/deckhand_charcoal_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_charcoal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.56, L=1.29, T=2.56. Capturas: `armor/pirateatelier/deckhand_charcoal_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_charcoal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.04, L=0.54, T=2.35. Capturas: `armor/pirateatelier/deckhand_charcoal_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_charcoal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=0.81, L=0.47, T=1.18. Capturas: `armor/pirateatelier/deckhand_charcoal_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_charcoal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.79, L=1.12, T=1.89. Capturas: `armor/pirateatelier/deckhand_charcoal_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_charcoal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.23, L=0.84, T=2.26. Capturas: `armor/pirateatelier/deckhand_charcoal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_charcoal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_charcoal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_charcoal_pristine_normal/`.

### `deckhand_copper` — paleta cobre
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_copper_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_copper_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_copper_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.45, L=1.13, T=2.27. Capturas: `armor/pirateatelier/deckhand_copper_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_copper_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.91, L=1.09, T=2.71. Capturas: `armor/pirateatelier/deckhand_copper_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_copper_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.18, L=1.40, T=2.54. Capturas: `armor/pirateatelier/deckhand_copper_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_copper_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.25, L=1.49, T=2.83. Capturas: `armor/pirateatelier/deckhand_copper_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_copper_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.89, L=0.79, T=2.44. Capturas: `armor/pirateatelier/deckhand_copper_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_copper_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.42, L=0.62, T=1.33. Capturas: `armor/pirateatelier/deckhand_copper_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_copper_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.92, L=1.09, T=2.02. Capturas: `armor/pirateatelier/deckhand_copper_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_copper_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.14, L=0.87, T=2.34. Capturas: `armor/pirateatelier/deckhand_copper_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_copper_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_copper_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_copper_pristine_normal/`.

### `deckhand_crimson` — paleta carmesim
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_crimson_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_crimson_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_crimson_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.79, L=0.98, T=1.79. Capturas: `armor/pirateatelier/deckhand_crimson_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_crimson_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.67, L=0.76, T=1.96. Capturas: `armor/pirateatelier/deckhand_crimson_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_crimson_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.67, L=0.97, T=2.23. Capturas: `armor/pirateatelier/deckhand_crimson_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_crimson_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.00, L=1.35, T=2.65. Capturas: `armor/pirateatelier/deckhand_crimson_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_crimson_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.52, L=0.76, T=2.84. Capturas: `armor/pirateatelier/deckhand_crimson_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_crimson_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.29, L=0.64, T=1.75. Capturas: `armor/pirateatelier/deckhand_crimson_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_crimson_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.81, L=1.09, T=2.38. Capturas: `armor/pirateatelier/deckhand_crimson_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_crimson_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.96, L=0.62, T=2.34. Capturas: `armor/pirateatelier/deckhand_crimson_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_crimson_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_crimson_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_crimson_pristine_normal/`.

### `deckhand_emerald` — paleta esmeralda
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_emerald_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_emerald_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_emerald_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.82, L=1.17, T=2.25. Capturas: `armor/pirateatelier/deckhand_emerald_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_emerald_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.12, L=0.94, T=2.40. Capturas: `armor/pirateatelier/deckhand_emerald_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_emerald_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.88, L=0.94, T=2.20. Capturas: `armor/pirateatelier/deckhand_emerald_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_emerald_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.08, L=1.17, T=2.31. Capturas: `armor/pirateatelier/deckhand_emerald_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_emerald_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.69, L=0.55, T=2.44. Capturas: `armor/pirateatelier/deckhand_emerald_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_emerald_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.07, L=0.63, T=1.18. Capturas: `armor/pirateatelier/deckhand_emerald_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_emerald_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.66, L=1.08, T=1.99. Capturas: `armor/pirateatelier/deckhand_emerald_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_emerald_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.27, L=0.77, T=2.12. Capturas: `armor/pirateatelier/deckhand_emerald_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_emerald_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_emerald_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_emerald_pristine_normal/`.

### `deckhand_f`
- **Defeito visual específico:** parte frontal rosa/acinzentada não tem saia/barra ou fechos em geometria independente. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_f_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_f_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_f_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.00, L=1.20, T=2.47. Capturas: `armor/pirateatelier/deckhand_f_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_f_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.38, L=1.10, T=2.81. Capturas: `armor/pirateatelier/deckhand_f_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_f_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.57, L=1.28, T=2.49. Capturas: `armor/pirateatelier/deckhand_f_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_f_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.90, L=1.41, T=2.62. Capturas: `armor/pirateatelier/deckhand_f_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_f_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.43, L=0.93, T=2.45. Capturas: `armor/pirateatelier/deckhand_f_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_f_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.68, L=0.83, T=1.87. Capturas: `armor/pirateatelier/deckhand_f_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_f_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.24, L=1.22, T=2.60. Capturas: `armor/pirateatelier/deckhand_f_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_f_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.32, L=1.06, T=2.63. Capturas: `armor/pirateatelier/deckhand_f_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_f_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_f_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_f_pristine_normal/`.

### `deckhand_forest` — paleta verde floresta
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_forest_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_forest_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_forest_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.76, L=1.06, T=1.92. Capturas: `armor/pirateatelier/deckhand_forest_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_forest_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.94, L=0.90, T=2.35. Capturas: `armor/pirateatelier/deckhand_forest_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_forest_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.01, L=1.08, T=2.33. Capturas: `armor/pirateatelier/deckhand_forest_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_forest_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.44, L=1.19, T=3.02. Capturas: `armor/pirateatelier/deckhand_forest_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_forest_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.42, L=0.71, T=2.74. Capturas: `armor/pirateatelier/deckhand_forest_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_forest_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.47, L=0.68, T=1.63. Capturas: `armor/pirateatelier/deckhand_forest_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_forest_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.03, L=1.05, T=2.31. Capturas: `armor/pirateatelier/deckhand_forest_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_forest_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.82, L=0.56, T=2.59. Capturas: `armor/pirateatelier/deckhand_forest_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_forest_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_forest_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_forest_pristine_normal/`.

### `deckhand_ivory` — paleta marfim/cinza-claro
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_ivory_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_ivory_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_ivory_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.12, L=1.33, T=2.70. Capturas: `armor/pirateatelier/deckhand_ivory_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_ivory_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.97, L=0.90, T=2.61. Capturas: `armor/pirateatelier/deckhand_ivory_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_ivory_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.44, L=1.02, T=2.77. Capturas: `armor/pirateatelier/deckhand_ivory_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_ivory_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.22, L=1.57, T=3.46. Capturas: `armor/pirateatelier/deckhand_ivory_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_ivory_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.56, L=1.29, T=3.68. Capturas: `armor/pirateatelier/deckhand_ivory_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_ivory_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.65, L=1.13, T=2.28. Capturas: `armor/pirateatelier/deckhand_ivory_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_ivory_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.33, L=1.52, T=3.07. Capturas: `armor/pirateatelier/deckhand_ivory_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_ivory_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.37, L=0.99, T=2.94. Capturas: `armor/pirateatelier/deckhand_ivory_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_ivory_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_ivory_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_ivory_pristine_normal/`.

### `deckhand_navy` — paleta azul-marinho
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_navy_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_navy_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_navy_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.04, L=1.20, T=2.05. Capturas: `armor/pirateatelier/deckhand_navy_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_navy_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.41, L=0.78, T=2.22. Capturas: `armor/pirateatelier/deckhand_navy_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_navy_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.99, L=0.82, T=1.48. Capturas: `armor/pirateatelier/deckhand_navy_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_navy_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.00, L=1.03, T=2.16. Capturas: `armor/pirateatelier/deckhand_navy_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_navy_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.85, L=0.81, T=2.58. Capturas: `armor/pirateatelier/deckhand_navy_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_navy_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.32, L=0.89, T=1.49. Capturas: `armor/pirateatelier/deckhand_navy_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_navy_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.09, L=1.48, T=2.16. Capturas: `armor/pirateatelier/deckhand_navy_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_navy_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.65, L=0.76, T=2.52. Capturas: `armor/pirateatelier/deckhand_navy_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_navy_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_navy_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_navy_pristine_normal/`.

### `deckhand_ochre` — paleta ocre
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_ochre_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_ochre_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_ochre_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.45, L=1.16, T=2.58. Capturas: `armor/pirateatelier/deckhand_ochre_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_ochre_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.59, L=0.99, T=2.71. Capturas: `armor/pirateatelier/deckhand_ochre_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_ochre_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.60, L=1.14, T=2.77. Capturas: `armor/pirateatelier/deckhand_ochre_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_ochre_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.95, L=1.36, T=3.09. Capturas: `armor/pirateatelier/deckhand_ochre_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_ochre_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.14, L=1.13, T=3.07. Capturas: `armor/pirateatelier/deckhand_ochre_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_ochre_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.51, L=0.95, T=2.37. Capturas: `armor/pirateatelier/deckhand_ochre_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_ochre_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.14, L=1.29, T=2.86. Capturas: `armor/pirateatelier/deckhand_ochre_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_ochre_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.07, L=1.09, T=2.89. Capturas: `armor/pirateatelier/deckhand_ochre_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_ochre_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_ochre_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_ochre_pristine_normal/`.

### `deckhand_plum` — paleta ameixa/roxo
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_plum_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_plum_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_plum_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.88, L=1.01, T=1.96. Capturas: `armor/pirateatelier/deckhand_plum_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_plum_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.97, L=0.66, T=2.36. Capturas: `armor/pirateatelier/deckhand_plum_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_plum_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.95, L=0.97, T=2.27. Capturas: `armor/pirateatelier/deckhand_plum_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_plum_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.20, L=1.61, T=2.80. Capturas: `armor/pirateatelier/deckhand_plum_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_plum_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.12, L=1.14, T=2.69. Capturas: `armor/pirateatelier/deckhand_plum_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_plum_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.18, L=0.93, T=1.61. Capturas: `armor/pirateatelier/deckhand_plum_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_plum_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.67, L=1.37, T=2.14. Capturas: `armor/pirateatelier/deckhand_plum_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_plum_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.61, L=0.67, T=2.27. Capturas: `armor/pirateatelier/deckhand_plum_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_plum_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_plum_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_plum_pristine_normal/`.

### `deckhand_sea_blue` — paleta azul-mar
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_sea_blue_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_sea_blue_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_sea_blue_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.08, L=1.39, T=2.22. Capturas: `armor/pirateatelier/deckhand_sea_blue_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_sea_blue_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.27, L=0.97, T=2.28. Capturas: `armor/pirateatelier/deckhand_sea_blue_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_sea_blue_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.83, L=0.98, T=1.97. Capturas: `armor/pirateatelier/deckhand_sea_blue_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_sea_blue_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.36, L=1.51, T=2.66. Capturas: `armor/pirateatelier/deckhand_sea_blue_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_sea_blue_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.36, L=0.79, T=2.62. Capturas: `armor/pirateatelier/deckhand_sea_blue_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_sea_blue_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.35, L=0.57, T=1.29. Capturas: `armor/pirateatelier/deckhand_sea_blue_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_sea_blue_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.19, L=1.24, T=2.18. Capturas: `armor/pirateatelier/deckhand_sea_blue_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_sea_blue_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.62, L=0.79, T=2.10. Capturas: `armor/pirateatelier/deckhand_sea_blue_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_sea_blue_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_sea_blue_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_sea_blue_pristine_normal/`.

### `deckhand_teal` — paleta verde-azulado
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_teal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_teal_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_teal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.21, L=1.14, T=2.06. Capturas: `armor/pirateatelier/deckhand_teal_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_teal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.63, L=0.99, T=2.59. Capturas: `armor/pirateatelier/deckhand_teal_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_teal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.10, L=1.21, T=2.66. Capturas: `armor/pirateatelier/deckhand_teal_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_teal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.69, L=1.62, T=2.93. Capturas: `armor/pirateatelier/deckhand_teal_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_teal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.55, L=1.01, T=2.84. Capturas: `armor/pirateatelier/deckhand_teal_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_teal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.36, L=0.74, T=1.11. Capturas: `armor/pirateatelier/deckhand_teal_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_teal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.95, L=1.09, T=1.73. Capturas: `armor/pirateatelier/deckhand_teal_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_teal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.37, L=0.62, T=2.25. Capturas: `armor/pirateatelier/deckhand_teal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_teal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_teal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_teal_pristine_normal/`.

### `deckhand_wine` — paleta vinho
- **Defeito visual específico:** roupa de deckhand reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `deckhand_wine_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/deckhand_wine_pristine_normal/{front,side,back}.jpg`.
  - `deckhand_wine_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.83, L=0.92, T=1.69. Capturas: `armor/pirateatelier/deckhand_wine_pristine_peg_left/{front,side,back}.jpg`.
  - `deckhand_wine_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.58, L=0.40, T=2.05. Capturas: `armor/pirateatelier/deckhand_wine_pristine_peg_right/{front,side,back}.jpg`.
  - `deckhand_wine_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.36, L=0.81, T=2.47. Capturas: `armor/pirateatelier/deckhand_wine_tattered_normal/{front,side,back}.jpg`.
  - `deckhand_wine_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.17, L=1.55, T=3.16. Capturas: `armor/pirateatelier/deckhand_wine_tattered_peg_left/{front,side,back}.jpg`.
  - `deckhand_wine_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.44, L=1.19, T=3.12. Capturas: `armor/pirateatelier/deckhand_wine_tattered_peg_right/{front,side,back}.jpg`.
  - `deckhand_wine_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.34, L=0.96, T=1.76. Capturas: `armor/pirateatelier/deckhand_wine_weathered_normal/{front,side,back}.jpg`.
  - `deckhand_wine_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.80, L=1.28, T=2.36. Capturas: `armor/pirateatelier/deckhand_wine_weathered_peg_left/{front,side,back}.jpg`.
  - `deckhand_wine_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.88, L=0.61, T=1.67. Capturas: `armor/pirateatelier/deckhand_wine_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/deckhand_wine_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/deckhand_wine_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/deckhand_wine_pristine_normal/`.

### `first_mate_armor`
- **Defeito visual específico:** cordões e detalhes da jaqueta de imediato são superficiais; ombreira e chapéu sem geometria independente. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `first_mate_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/first_mate_armor_pristine_normal/{front,side,back}.jpg`.
  - `first_mate_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.25, L=1.25, T=2.21. Capturas: `armor/pirateatelier/first_mate_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `first_mate_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.20, L=0.77, T=1.99. Capturas: `armor/pirateatelier/first_mate_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `first_mate_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.23, L=0.79, T=2.20. Capturas: `armor/pirateatelier/first_mate_armor_tattered_normal/{front,side,back}.jpg`.
  - `first_mate_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.69, L=1.54, T=3.17. Capturas: `armor/pirateatelier/first_mate_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `first_mate_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.91, L=1.20, T=3.19. Capturas: `armor/pirateatelier/first_mate_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `first_mate_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.22, L=1.08, T=2.13. Capturas: `armor/pirateatelier/first_mate_armor_weathered_normal/{front,side,back}.jpg`.
  - `first_mate_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.59, L=1.50, T=2.52. Capturas: `armor/pirateatelier/first_mate_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `first_mate_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.37, L=0.83, T=2.26. Capturas: `armor/pirateatelier/first_mate_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/first_mate_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/first_mate_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/first_mate_armor_pristine_normal/`.

### `fisherman_charcoal` — paleta carvão/preto
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_charcoal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_charcoal_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_charcoal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.87, L=1.06, T=2.00. Capturas: `armor/pirateatelier/fisherman_charcoal_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_charcoal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.24, L=0.70, T=2.39. Capturas: `armor/pirateatelier/fisherman_charcoal_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_charcoal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.44, L=1.02, T=2.51. Capturas: `armor/pirateatelier/fisherman_charcoal_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_charcoal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.62, L=1.64, T=2.85. Capturas: `armor/pirateatelier/fisherman_charcoal_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_charcoal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.35, L=1.10, T=3.07. Capturas: `armor/pirateatelier/fisherman_charcoal_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_charcoal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.44, L=1.04, T=1.61. Capturas: `armor/pirateatelier/fisherman_charcoal_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_charcoal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.94, L=1.39, T=2.27. Capturas: `armor/pirateatelier/fisherman_charcoal_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_charcoal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.96, L=0.77, T=2.13. Capturas: `armor/pirateatelier/fisherman_charcoal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_charcoal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_charcoal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_charcoal_pristine_normal/`.

### `fisherman_copper` — paleta cobre
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_copper_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_copper_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_copper_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.04, L=0.90, T=1.53. Capturas: `armor/pirateatelier/fisherman_copper_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_copper_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.66, L=0.50, T=1.78. Capturas: `armor/pirateatelier/fisherman_copper_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_copper_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.77, L=0.82, T=2.20. Capturas: `armor/pirateatelier/fisherman_copper_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_copper_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.03, L=1.40, T=3.09. Capturas: `armor/pirateatelier/fisherman_copper_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_copper_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.12, L=1.23, T=3.09. Capturas: `armor/pirateatelier/fisherman_copper_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_copper_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.54, L=0.97, T=2.00. Capturas: `armor/pirateatelier/fisherman_copper_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_copper_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.90, L=1.21, T=2.39. Capturas: `armor/pirateatelier/fisherman_copper_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_copper_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.79, L=0.73, T=1.96. Capturas: `armor/pirateatelier/fisherman_copper_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_copper_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_copper_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_copper_pristine_normal/`.

### `fisherman_crimson` — paleta carmesim
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_crimson_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_crimson_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_crimson_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.96, L=1.04, T=2.02. Capturas: `armor/pirateatelier/fisherman_crimson_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_crimson_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.28, L=0.74, T=2.08. Capturas: `armor/pirateatelier/fisherman_crimson_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_crimson_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.99, L=0.67, T=2.12. Capturas: `armor/pirateatelier/fisherman_crimson_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_crimson_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.19, L=1.09, T=2.37. Capturas: `armor/pirateatelier/fisherman_crimson_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_crimson_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.96, L=0.92, T=2.97. Capturas: `armor/pirateatelier/fisherman_crimson_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_crimson_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.38, L=0.90, T=1.85. Capturas: `armor/pirateatelier/fisherman_crimson_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_crimson_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.88, L=1.38, T=2.56. Capturas: `armor/pirateatelier/fisherman_crimson_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_crimson_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.88, L=0.84, T=2.37. Capturas: `armor/pirateatelier/fisherman_crimson_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_crimson_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_crimson_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_crimson_pristine_normal/`.

### `fisherman_emerald` — paleta esmeralda
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_emerald_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_emerald_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_emerald_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.80, L=1.11, T=1.88. Capturas: `armor/pirateatelier/fisherman_emerald_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_emerald_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.21, L=0.81, T=2.27. Capturas: `armor/pirateatelier/fisherman_emerald_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_emerald_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.27, L=0.91, T=2.17. Capturas: `armor/pirateatelier/fisherman_emerald_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_emerald_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.84, L=1.45, T=2.59. Capturas: `armor/pirateatelier/fisherman_emerald_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_emerald_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.06, L=1.01, T=2.61. Capturas: `armor/pirateatelier/fisherman_emerald_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_emerald_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.18, L=0.99, T=1.56. Capturas: `armor/pirateatelier/fisherman_emerald_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_emerald_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.73, L=1.35, T=2.27. Capturas: `armor/pirateatelier/fisherman_emerald_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_emerald_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.74, L=0.83, T=2.15. Capturas: `armor/pirateatelier/fisherman_emerald_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_emerald_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_emerald_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_emerald_pristine_normal/`.

### `fisherman_forest` — paleta verde floresta
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_forest_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_forest_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_forest_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.86, L=0.98, T=1.71. Capturas: `armor/pirateatelier/fisherman_forest_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_forest_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.49, L=0.59, T=1.76. Capturas: `armor/pirateatelier/fisherman_forest_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_forest_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.40, L=0.62, T=2.28. Capturas: `armor/pirateatelier/fisherman_forest_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_forest_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.10, L=1.43, T=2.95. Capturas: `armor/pirateatelier/fisherman_forest_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_forest_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.03, L=1.02, T=2.91. Capturas: `armor/pirateatelier/fisherman_forest_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_forest_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.27, L=0.96, T=1.96. Capturas: `armor/pirateatelier/fisherman_forest_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_forest_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.85, L=1.27, T=2.60. Capturas: `armor/pirateatelier/fisherman_forest_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_forest_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.88, L=0.78, T=1.84. Capturas: `armor/pirateatelier/fisherman_forest_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_forest_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_forest_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_forest_pristine_normal/`.

### `fisherman_ivory` — paleta marfim/cinza-claro
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_ivory_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_ivory_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_ivory_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.27, L=1.44, T=2.66. Capturas: `armor/pirateatelier/fisherman_ivory_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_ivory_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.35, L=1.03, T=2.61. Capturas: `armor/pirateatelier/fisherman_ivory_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_ivory_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.60, L=0.96, T=2.38. Capturas: `armor/pirateatelier/fisherman_ivory_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_ivory_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.23, L=1.44, T=3.34. Capturas: `armor/pirateatelier/fisherman_ivory_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_ivory_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.37, L=1.09, T=3.43. Capturas: `armor/pirateatelier/fisherman_ivory_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_ivory_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.60, L=0.94, T=2.07. Capturas: `armor/pirateatelier/fisherman_ivory_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_ivory_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.37, L=1.54, T=2.87. Capturas: `armor/pirateatelier/fisherman_ivory_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_ivory_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.47, L=0.97, T=2.86. Capturas: `armor/pirateatelier/fisherman_ivory_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_ivory_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_ivory_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_ivory_pristine_normal/`.

### `fisherman_navy` — paleta azul-marinho
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_navy_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_navy_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_navy_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.20, L=1.12, T=2.11. Capturas: `armor/pirateatelier/fisherman_navy_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_navy_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.59, L=0.65, T=2.38. Capturas: `armor/pirateatelier/fisherman_navy_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_navy_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.20, L=1.00, T=2.16. Capturas: `armor/pirateatelier/fisherman_navy_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_navy_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.50, L=1.56, T=3.05. Capturas: `armor/pirateatelier/fisherman_navy_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_navy_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.13, L=1.09, T=3.02. Capturas: `armor/pirateatelier/fisherman_navy_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_navy_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.33, L=0.99, T=1.58. Capturas: `armor/pirateatelier/fisherman_navy_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_navy_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.01, L=1.35, T=2.10. Capturas: `armor/pirateatelier/fisherman_navy_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_navy_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.55, L=0.73, T=2.42. Capturas: `armor/pirateatelier/fisherman_navy_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_navy_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_navy_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_navy_pristine_normal/`.

### `fisherman_ochre` — paleta ocre
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_ochre_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_ochre_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_ochre_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.24, L=1.04, T=2.52. Capturas: `armor/pirateatelier/fisherman_ochre_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_ochre_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.71, L=1.03, T=2.81. Capturas: `armor/pirateatelier/fisherman_ochre_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_ochre_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.19, L=1.31, T=3.06. Capturas: `armor/pirateatelier/fisherman_ochre_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_ochre_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.71, L=1.58, T=3.48. Capturas: `armor/pirateatelier/fisherman_ochre_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_ochre_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.61, L=1.30, T=3.25. Capturas: `armor/pirateatelier/fisherman_ochre_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_ochre_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.88, L=1.02, T=2.21. Capturas: `armor/pirateatelier/fisherman_ochre_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_ochre_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.17, L=1.24, T=2.36. Capturas: `armor/pirateatelier/fisherman_ochre_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_ochre_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.61, L=0.92, T=2.67. Capturas: `armor/pirateatelier/fisherman_ochre_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_ochre_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_ochre_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_ochre_pristine_normal/`.

### `fisherman_pirate`
- **Defeito visual específico:** roupa e acessórios específicos devem ser modelados em 3D mantendo cabeça e proporções corretas. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_pirate_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_pirate_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_pirate_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.49, L=1.18, T=2.52. Capturas: `armor/pirateatelier/fisherman_pirate_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_pirate_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.41, L=1.00, T=2.89. Capturas: `armor/pirateatelier/fisherman_pirate_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_pirate_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.07, L=1.58, T=3.13. Capturas: `armor/pirateatelier/fisherman_pirate_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_pirate_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.97, L=2.06, T=3.59. Capturas: `armor/pirateatelier/fisherman_pirate_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_pirate_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.79, L=1.25, T=3.13. Capturas: `armor/pirateatelier/fisherman_pirate_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_pirate_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.33, L=0.81, T=1.75. Capturas: `armor/pirateatelier/fisherman_pirate_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_pirate_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.50, L=1.33, T=2.55. Capturas: `armor/pirateatelier/fisherman_pirate_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_pirate_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.60, L=0.86, T=2.72. Capturas: `armor/pirateatelier/fisherman_pirate_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_pirate_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_pirate_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_pirate_pristine_normal/`.

### `fisherman_plum` — paleta ameixa/roxo
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_plum_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_plum_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_plum_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.91, L=1.00, T=1.99. Capturas: `armor/pirateatelier/fisherman_plum_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_plum_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.66, L=0.81, T=2.66. Capturas: `armor/pirateatelier/fisherman_plum_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_plum_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.77, L=1.25, T=2.86. Capturas: `armor/pirateatelier/fisherman_plum_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_plum_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.46, L=1.67, T=3.11. Capturas: `armor/pirateatelier/fisherman_plum_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_plum_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.13, L=1.20, T=2.73. Capturas: `armor/pirateatelier/fisherman_plum_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_plum_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.04, L=0.63, T=1.07. Capturas: `armor/pirateatelier/fisherman_plum_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_plum_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.98, L=0.99, T=1.77. Capturas: `armor/pirateatelier/fisherman_plum_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_plum_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.87, L=0.50, T=2.22. Capturas: `armor/pirateatelier/fisherman_plum_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_plum_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_plum_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_plum_pristine_normal/`.

### `fisherman_sea_blue` — paleta azul-mar
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_sea_blue_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_sea_blue_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_sea_blue_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.42, L=1.30, T=2.24. Capturas: `armor/pirateatelier/fisherman_sea_blue_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_sea_blue_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.08, L=0.90, T=2.59. Capturas: `armor/pirateatelier/fisherman_sea_blue_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_sea_blue_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.84, L=1.16, T=2.52. Capturas: `armor/pirateatelier/fisherman_sea_blue_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_sea_blue_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.69, L=1.66, T=2.94. Capturas: `armor/pirateatelier/fisherman_sea_blue_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_sea_blue_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.46, L=0.90, T=2.38. Capturas: `armor/pirateatelier/fisherman_sea_blue_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_sea_blue_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.02, L=0.45, T=0.90. Capturas: `armor/pirateatelier/fisherman_sea_blue_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_sea_blue_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.33, L=1.17, T=1.91. Capturas: `armor/pirateatelier/fisherman_sea_blue_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_sea_blue_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.25, L=0.47, T=2.01. Capturas: `armor/pirateatelier/fisherman_sea_blue_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_sea_blue_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_sea_blue_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_sea_blue_pristine_normal/`.

### `fisherman_teal` — paleta verde-azulado
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_teal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_teal_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_teal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.08, L=1.15, T=1.82. Capturas: `armor/pirateatelier/fisherman_teal_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_teal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.14, L=0.73, T=1.88. Capturas: `armor/pirateatelier/fisherman_teal_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_teal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.53, L=0.94, T=2.17. Capturas: `armor/pirateatelier/fisherman_teal_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_teal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.25, L=1.35, T=2.88. Capturas: `armor/pirateatelier/fisherman_teal_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_teal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.63, L=1.03, T=2.62. Capturas: `armor/pirateatelier/fisherman_teal_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_teal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.41, L=0.71, T=1.51. Capturas: `armor/pirateatelier/fisherman_teal_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_teal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.35, L=1.33, T=2.36. Capturas: `armor/pirateatelier/fisherman_teal_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_teal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.43, L=0.58, T=2.02. Capturas: `armor/pirateatelier/fisherman_teal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_teal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_teal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_teal_pristine_normal/`.

### `fisherman_wine` — paleta vinho
- **Defeito visual específico:** roupa de fisherman reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `fisherman_wine_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/fisherman_wine_pristine_normal/{front,side,back}.jpg`.
  - `fisherman_wine_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.99, L=1.20, T=1.94. Capturas: `armor/pirateatelier/fisherman_wine_pristine_peg_left/{front,side,back}.jpg`.
  - `fisherman_wine_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.22, L=0.78, T=1.75. Capturas: `armor/pirateatelier/fisherman_wine_pristine_peg_right/{front,side,back}.jpg`.
  - `fisherman_wine_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.55, L=0.56, T=1.63. Capturas: `armor/pirateatelier/fisherman_wine_tattered_normal/{front,side,back}.jpg`.
  - `fisherman_wine_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.75, L=1.15, T=2.93. Capturas: `armor/pirateatelier/fisherman_wine_tattered_peg_left/{front,side,back}.jpg`.
  - `fisherman_wine_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.15, L=1.04, T=2.87. Capturas: `armor/pirateatelier/fisherman_wine_tattered_peg_right/{front,side,back}.jpg`.
  - `fisherman_wine_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.28, L=0.86, T=1.89. Capturas: `armor/pirateatelier/fisherman_wine_weathered_normal/{front,side,back}.jpg`.
  - `fisherman_wine_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.02, L=1.24, T=2.27. Capturas: `armor/pirateatelier/fisherman_wine_weathered_peg_left/{front,side,back}.jpg`.
  - `fisherman_wine_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.06, L=0.69, T=1.99. Capturas: `armor/pirateatelier/fisherman_wine_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/fisherman_wine_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/fisherman_wine_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/fisherman_wine_pristine_normal/`.

### `gunner_armor`
- **Defeito visual específico:** faixas de couro, ombreiras e bolsa de munição não criam volumes distintos do torso. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `gunner_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/gunner_armor_pristine_normal/{front,side,back}.jpg`.
  - `gunner_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.24, L=1.10, T=2.35. Capturas: `armor/pirateatelier/gunner_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `gunner_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.34, L=0.77, T=2.39. Capturas: `armor/pirateatelier/gunner_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `gunner_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.25, L=0.92, T=2.29. Capturas: `armor/pirateatelier/gunner_armor_tattered_normal/{front,side,back}.jpg`.
  - `gunner_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.80, L=1.40, T=2.96. Capturas: `armor/pirateatelier/gunner_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `gunner_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.75, L=1.18, T=2.94. Capturas: `armor/pirateatelier/gunner_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `gunner_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.06, L=0.96, T=2.05. Capturas: `armor/pirateatelier/gunner_armor_weathered_normal/{front,side,back}.jpg`.
  - `gunner_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.47, L=1.33, T=2.37. Capturas: `armor/pirateatelier/gunner_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `gunner_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.61, L=0.81, T=2.46. Capturas: `armor/pirateatelier/gunner_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/gunner_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/gunner_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/gunner_armor_pristine_normal/`.

### `harbor_merchant`
- **Defeito visual específico:** tira diagonal de couro e casaco vinho não têm separação tridimensional; cabelo/rosto ficam recobertos. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `harbor_merchant_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/harbor_merchant_pristine_normal/{front,side,back}.jpg`.
  - `harbor_merchant_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.83, L=0.99, T=1.90. Capturas: `armor/pirateatelier/harbor_merchant_pristine_peg_left/{front,side,back}.jpg`.
  - `harbor_merchant_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.04, L=0.73, T=2.04. Capturas: `armor/pirateatelier/harbor_merchant_pristine_peg_right/{front,side,back}.jpg`.
  - `harbor_merchant_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.12, L=0.97, T=1.88. Capturas: `armor/pirateatelier/harbor_merchant_tattered_normal/{front,side,back}.jpg`.
  - `harbor_merchant_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.38, L=1.35, T=2.40. Capturas: `armor/pirateatelier/harbor_merchant_tattered_peg_left/{front,side,back}.jpg`.
  - `harbor_merchant_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.56, L=0.96, T=2.34. Capturas: `armor/pirateatelier/harbor_merchant_tattered_peg_right/{front,side,back}.jpg`.
  - `harbor_merchant_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.69, L=0.68, T=1.24. Capturas: `armor/pirateatelier/harbor_merchant_weathered_normal/{front,side,back}.jpg`.
  - `harbor_merchant_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.92, L=1.04, T=1.92. Capturas: `armor/pirateatelier/harbor_merchant_weathered_peg_left/{front,side,back}.jpg`.
  - `harbor_merchant_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.13, L=0.65, T=2.09. Capturas: `armor/pirateatelier/harbor_merchant_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/harbor_merchant_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/harbor_merchant_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/harbor_merchant_pristine_normal/`.

### `heavy_bruiser_armor`
- **Defeito visual específico:** a armadura pesada parece uma jaqueta fina; ombreiras e placas metálicas não possuem profundidade proporcional. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `heavy_bruiser_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/heavy_bruiser_armor_pristine_normal/{front,side,back}.jpg`.
  - `heavy_bruiser_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.19, L=1.41, T=2.30. Capturas: `armor/pirateatelier/heavy_bruiser_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `heavy_bruiser_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.89, L=1.27, T=2.96. Capturas: `armor/pirateatelier/heavy_bruiser_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `heavy_bruiser_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.06, L=1.45, T=2.58. Capturas: `armor/pirateatelier/heavy_bruiser_armor_tattered_normal/{front,side,back}.jpg`.
  - `heavy_bruiser_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.06, L=1.75, T=2.92. Capturas: `armor/pirateatelier/heavy_bruiser_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `heavy_bruiser_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.01, L=1.12, T=2.69. Capturas: `armor/pirateatelier/heavy_bruiser_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `heavy_bruiser_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.95, L=0.75, T=1.17. Capturas: `armor/pirateatelier/heavy_bruiser_armor_weathered_normal/{front,side,back}.jpg`.
  - `heavy_bruiser_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.10, L=1.18, T=2.02. Capturas: `armor/pirateatelier/heavy_bruiser_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `heavy_bruiser_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.59, L=1.00, T=2.43. Capturas: `armor/pirateatelier/heavy_bruiser_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/heavy_bruiser_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/heavy_bruiser_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/heavy_bruiser_armor_pristine_normal/`.

### `merchant_charcoal` — paleta carvão/preto
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_charcoal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_charcoal_pristine_normal/{front,side,back}.jpg`.
  - `merchant_charcoal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.97, L=1.24, T=2.40. Capturas: `armor/pirateatelier/merchant_charcoal_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_charcoal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.15, L=1.01, T=2.64. Capturas: `armor/pirateatelier/merchant_charcoal_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_charcoal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.31, L=1.25, T=2.29. Capturas: `armor/pirateatelier/merchant_charcoal_tattered_normal/{front,side,back}.jpg`.
  - `merchant_charcoal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.35, L=1.53, T=2.63. Capturas: `armor/pirateatelier/merchant_charcoal_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_charcoal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.07, L=0.84, T=1.92. Capturas: `armor/pirateatelier/merchant_charcoal_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_charcoal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.01, L=0.39, T=1.25. Capturas: `armor/pirateatelier/merchant_charcoal_weathered_normal/{front,side,back}.jpg`.
  - `merchant_charcoal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.60, L=1.00, T=2.02. Capturas: `armor/pirateatelier/merchant_charcoal_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_charcoal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.19, L=0.68, T=2.33. Capturas: `armor/pirateatelier/merchant_charcoal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_charcoal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_charcoal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_charcoal_pristine_normal/`.

### `merchant_copper` — paleta cobre
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_copper_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_copper_pristine_normal/{front,side,back}.jpg`.
  - `merchant_copper_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.94, L=1.14, T=2.04. Capturas: `armor/pirateatelier/merchant_copper_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_copper_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.48, L=1.03, T=2.46. Capturas: `armor/pirateatelier/merchant_copper_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_copper_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.71, L=1.22, T=2.44. Capturas: `armor/pirateatelier/merchant_copper_tattered_normal/{front,side,back}.jpg`.
  - `merchant_copper_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.10, L=1.51, T=2.85. Capturas: `armor/pirateatelier/merchant_copper_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_copper_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.81, L=0.99, T=2.23. Capturas: `armor/pirateatelier/merchant_copper_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_copper_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.50, L=0.47, T=1.29. Capturas: `armor/pirateatelier/merchant_copper_weathered_normal/{front,side,back}.jpg`.
  - `merchant_copper_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.01, L=1.03, T=1.89. Capturas: `armor/pirateatelier/merchant_copper_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_copper_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.88, L=0.71, T=2.24. Capturas: `armor/pirateatelier/merchant_copper_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_copper_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_copper_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_copper_pristine_normal/`.

### `merchant_crimson` — paleta carmesim
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_crimson_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_crimson_pristine_normal/{front,side,back}.jpg`.
  - `merchant_crimson_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.55, L=1.11, T=1.81. Capturas: `armor/pirateatelier/merchant_crimson_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_crimson_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.44, L=0.84, T=1.92. Capturas: `armor/pirateatelier/merchant_crimson_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_crimson_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.17, L=1.11, T=2.47. Capturas: `armor/pirateatelier/merchant_crimson_tattered_normal/{front,side,back}.jpg`.
  - `merchant_crimson_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.73, L=1.21, T=2.77. Capturas: `armor/pirateatelier/merchant_crimson_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_crimson_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.22, L=0.74, T=2.34. Capturas: `armor/pirateatelier/merchant_crimson_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_crimson_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.10, L=0.64, T=1.66. Capturas: `armor/pirateatelier/merchant_crimson_weathered_normal/{front,side,back}.jpg`.
  - `merchant_crimson_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.12, L=1.03, T=2.20. Capturas: `armor/pirateatelier/merchant_crimson_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_crimson_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.78, L=0.46, T=2.15. Capturas: `armor/pirateatelier/merchant_crimson_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_crimson_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_crimson_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_crimson_pristine_normal/`.

### `merchant_emerald` — paleta esmeralda
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_emerald_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_emerald_pristine_normal/{front,side,back}.jpg`.
  - `merchant_emerald_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.02, L=1.09, T=1.84. Capturas: `armor/pirateatelier/merchant_emerald_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_emerald_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.89, L=0.56, T=1.76. Capturas: `armor/pirateatelier/merchant_emerald_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_emerald_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.45, L=0.72, T=2.23. Capturas: `armor/pirateatelier/merchant_emerald_tattered_normal/{front,side,back}.jpg`.
  - `merchant_emerald_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.20, L=1.27, T=2.79. Capturas: `armor/pirateatelier/merchant_emerald_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_emerald_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.83, L=0.97, T=2.73. Capturas: `armor/pirateatelier/merchant_emerald_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_emerald_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.02, L=0.90, T=1.53. Capturas: `armor/pirateatelier/merchant_emerald_weathered_normal/{front,side,back}.jpg`.
  - `merchant_emerald_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.51, L=1.33, T=2.14. Capturas: `armor/pirateatelier/merchant_emerald_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_emerald_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.53, L=0.57, T=1.85. Capturas: `armor/pirateatelier/merchant_emerald_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_emerald_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_emerald_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_emerald_pristine_normal/`.

### `merchant_forest` — paleta verde floresta
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_forest_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_forest_pristine_normal/{front,side,back}.jpg`.
  - `merchant_forest_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.09, L=0.97, T=1.91. Capturas: `armor/pirateatelier/merchant_forest_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_forest_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.14, L=0.51, T=1.80. Capturas: `armor/pirateatelier/merchant_forest_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_forest_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.68, L=0.75, T=1.78. Capturas: `armor/pirateatelier/merchant_forest_tattered_normal/{front,side,back}.jpg`.
  - `merchant_forest_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.54, L=1.27, T=2.94. Capturas: `armor/pirateatelier/merchant_forest_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_forest_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.81, L=1.11, T=2.89. Capturas: `armor/pirateatelier/merchant_forest_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_forest_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.07, L=1.07, T=1.72. Capturas: `armor/pirateatelier/merchant_forest_weathered_normal/{front,side,back}.jpg`.
  - `merchant_forest_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.57, L=1.30, T=2.35. Capturas: `armor/pirateatelier/merchant_forest_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_forest_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.81, L=0.74, T=2.02. Capturas: `armor/pirateatelier/merchant_forest_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_forest_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_forest_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_forest_pristine_normal/`.

### `merchant_ivory` — paleta marfim/cinza-claro
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_ivory_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_ivory_pristine_normal/{front,side,back}.jpg`.
  - `merchant_ivory_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.82, L=1.37, T=2.66. Capturas: `armor/pirateatelier/merchant_ivory_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_ivory_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.09, L=1.06, T=2.93. Capturas: `armor/pirateatelier/merchant_ivory_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_ivory_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.75, L=1.13, T=2.87. Capturas: `armor/pirateatelier/merchant_ivory_tattered_normal/{front,side,back}.jpg`.
  - `merchant_ivory_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.60, L=1.66, T=3.40. Capturas: `armor/pirateatelier/merchant_ivory_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_ivory_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.65, L=1.10, T=3.20. Capturas: `armor/pirateatelier/merchant_ivory_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_ivory_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.72, L=1.08, T=2.17. Capturas: `armor/pirateatelier/merchant_ivory_weathered_normal/{front,side,back}.jpg`.
  - `merchant_ivory_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.54, L=1.50, T=2.82. Capturas: `armor/pirateatelier/merchant_ivory_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_ivory_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.26, L=1.02, T=3.00. Capturas: `armor/pirateatelier/merchant_ivory_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_ivory_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_ivory_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_ivory_pristine_normal/`.

### `merchant_navy` — paleta azul-marinho
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_navy_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_navy_pristine_normal/{front,side,back}.jpg`.
  - `merchant_navy_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.09, L=1.06, T=2.03. Capturas: `armor/pirateatelier/merchant_navy_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_navy_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.41, L=0.75, T=2.42. Capturas: `armor/pirateatelier/merchant_navy_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_navy_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.45, L=1.21, T=2.66. Capturas: `armor/pirateatelier/merchant_navy_tattered_normal/{front,side,back}.jpg`.
  - `merchant_navy_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.47, L=1.81, T=2.76. Capturas: `armor/pirateatelier/merchant_navy_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_navy_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.32, L=1.29, T=2.53. Capturas: `armor/pirateatelier/merchant_navy_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_navy_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.32, L=1.01, T=1.44. Capturas: `armor/pirateatelier/merchant_navy_weathered_normal/{front,side,back}.jpg`.
  - `merchant_navy_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.85, L=1.42, T=1.96. Capturas: `armor/pirateatelier/merchant_navy_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_navy_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.14, L=0.81, T=2.18. Capturas: `armor/pirateatelier/merchant_navy_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_navy_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_navy_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_navy_pristine_normal/`.

### `merchant_ochre` — paleta ocre
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_ochre_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_ochre_pristine_normal/{front,side,back}.jpg`.
  - `merchant_ochre_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.10, L=0.90, T=1.94. Capturas: `armor/pirateatelier/merchant_ochre_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_ochre_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.16, L=0.75, T=2.31. Capturas: `armor/pirateatelier/merchant_ochre_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_ochre_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.97, L=1.15, T=2.56. Capturas: `armor/pirateatelier/merchant_ochre_tattered_normal/{front,side,back}.jpg`.
  - `merchant_ochre_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.03, L=1.61, T=3.18. Capturas: `armor/pirateatelier/merchant_ochre_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_ochre_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.23, L=1.33, T=3.03. Capturas: `armor/pirateatelier/merchant_ochre_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_ochre_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.34, L=1.05, T=2.00. Capturas: `armor/pirateatelier/merchant_ochre_weathered_normal/{front,side,back}.jpg`.
  - `merchant_ochre_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.81, L=1.25, T=2.37. Capturas: `armor/pirateatelier/merchant_ochre_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_ochre_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.42, L=0.71, T=2.28. Capturas: `armor/pirateatelier/merchant_ochre_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_ochre_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_ochre_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_ochre_pristine_normal/`.

### `merchant_plum` — paleta ameixa/roxo
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_plum_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_plum_pristine_normal/{front,side,back}.jpg`.
  - `merchant_plum_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.30, L=1.07, T=2.08. Capturas: `armor/pirateatelier/merchant_plum_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_plum_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.39, L=0.74, T=2.03. Capturas: `armor/pirateatelier/merchant_plum_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_plum_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.40, L=1.00, T=2.16. Capturas: `armor/pirateatelier/merchant_plum_tattered_normal/{front,side,back}.jpg`.
  - `merchant_plum_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.94, L=1.77, T=2.84. Capturas: `armor/pirateatelier/merchant_plum_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_plum_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.28, L=1.25, T=2.94. Capturas: `armor/pirateatelier/merchant_plum_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_plum_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.30, L=0.93, T=1.42. Capturas: `armor/pirateatelier/merchant_plum_weathered_normal/{front,side,back}.jpg`.
  - `merchant_plum_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.75, L=1.26, T=2.06. Capturas: `armor/pirateatelier/merchant_plum_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_plum_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.32, L=0.78, T=2.01. Capturas: `armor/pirateatelier/merchant_plum_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_plum_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_plum_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_plum_pristine_normal/`.

### `merchant_sea_blue` — paleta azul-mar
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_sea_blue_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_sea_blue_pristine_normal/{front,side,back}.jpg`.
  - `merchant_sea_blue_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.53, L=1.27, T=2.13. Capturas: `armor/pirateatelier/merchant_sea_blue_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_sea_blue_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.79, L=0.85, T=2.60. Capturas: `armor/pirateatelier/merchant_sea_blue_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_sea_blue_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.08, L=1.17, T=2.16. Capturas: `armor/pirateatelier/merchant_sea_blue_tattered_normal/{front,side,back}.jpg`.
  - `merchant_sea_blue_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.96, L=1.69, T=2.86. Capturas: `armor/pirateatelier/merchant_sea_blue_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_sea_blue_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.63, L=1.19, T=2.63. Capturas: `armor/pirateatelier/merchant_sea_blue_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_sea_blue_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.41, L=0.94, T=1.65. Capturas: `armor/pirateatelier/merchant_sea_blue_weathered_normal/{front,side,back}.jpg`.
  - `merchant_sea_blue_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.50, L=1.30, T=2.00. Capturas: `armor/pirateatelier/merchant_sea_blue_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_sea_blue_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.61, L=0.76, T=2.46. Capturas: `armor/pirateatelier/merchant_sea_blue_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_sea_blue_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_sea_blue_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_sea_blue_pristine_normal/`.

### `merchant_teal` — paleta verde-azulado
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_teal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_teal_pristine_normal/{front,side,back}.jpg`.
  - `merchant_teal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.02, L=1.09, T=2.06. Capturas: `armor/pirateatelier/merchant_teal_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_teal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.41, L=0.81, T=2.59. Capturas: `armor/pirateatelier/merchant_teal_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_teal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.94, L=1.23, T=2.57. Capturas: `armor/pirateatelier/merchant_teal_tattered_normal/{front,side,back}.jpg`.
  - `merchant_teal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.66, L=1.74, T=2.95. Capturas: `armor/pirateatelier/merchant_teal_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_teal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.38, L=1.11, T=2.75. Capturas: `armor/pirateatelier/merchant_teal_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_teal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.36, L=0.73, T=1.41. Capturas: `armor/pirateatelier/merchant_teal_weathered_normal/{front,side,back}.jpg`.
  - `merchant_teal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.12, L=1.10, T=1.86. Capturas: `armor/pirateatelier/merchant_teal_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_teal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.13, L=0.56, T=2.15. Capturas: `armor/pirateatelier/merchant_teal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_teal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_teal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_teal_pristine_normal/`.

### `merchant_wine` — paleta vinho
- **Defeito visual específico:** roupa de merchant reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `merchant_wine_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/merchant_wine_pristine_normal/{front,side,back}.jpg`.
  - `merchant_wine_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.82, L=1.00, T=1.90. Capturas: `armor/pirateatelier/merchant_wine_pristine_peg_left/{front,side,back}.jpg`.
  - `merchant_wine_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.87, L=0.80, T=2.44. Capturas: `armor/pirateatelier/merchant_wine_pristine_peg_right/{front,side,back}.jpg`.
  - `merchant_wine_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.65, L=1.38, T=2.54. Capturas: `armor/pirateatelier/merchant_wine_tattered_normal/{front,side,back}.jpg`.
  - `merchant_wine_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.17, L=1.77, T=2.92. Capturas: `armor/pirateatelier/merchant_wine_tattered_peg_left/{front,side,back}.jpg`.
  - `merchant_wine_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.34, L=1.12, T=2.70. Capturas: `armor/pirateatelier/merchant_wine_tattered_peg_right/{front,side,back}.jpg`.
  - `merchant_wine_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.01, L=0.75, T=1.24. Capturas: `armor/pirateatelier/merchant_wine_weathered_normal/{front,side,back}.jpg`.
  - `merchant_wine_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.02, L=0.99, T=1.65. Capturas: `armor/pirateatelier/merchant_wine_weathered_peg_left/{front,side,back}.jpg`.
  - `merchant_wine_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.03, L=0.42, T=2.03. Capturas: `armor/pirateatelier/merchant_wine_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/merchant_wine_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/merchant_wine_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/merchant_wine_pristine_normal/`.

### `naval_officer_armor`
- **Defeito visual específico:** epaulettes douradas e botões frontais sem relevo; chapéu frontal ocupa a face. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `naval_officer_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/naval_officer_armor_pristine_normal/{front,side,back}.jpg`.
  - `naval_officer_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.38, L=1.39, T=2.30. Capturas: `armor/pirateatelier/naval_officer_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `naval_officer_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.35, L=0.88, T=3.06. Capturas: `armor/pirateatelier/naval_officer_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `naval_officer_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.18, L=1.20, T=2.98. Capturas: `armor/pirateatelier/naval_officer_armor_tattered_normal/{front,side,back}.jpg`.
  - `naval_officer_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.96, L=1.94, T=3.63. Capturas: `armor/pirateatelier/naval_officer_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `naval_officer_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.85, L=1.33, T=3.42. Capturas: `armor/pirateatelier/naval_officer_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `naval_officer_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.49, L=0.73, T=1.51. Capturas: `armor/pirateatelier/naval_officer_armor_weathered_normal/{front,side,back}.jpg`.
  - `naval_officer_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.36, L=1.12, T=2.19. Capturas: `armor/pirateatelier/naval_officer_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `naval_officer_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.71, L=0.75, T=2.29. Capturas: `armor/pirateatelier/naval_officer_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/naval_officer_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/naval_officer_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/naval_officer_armor_pristine_normal/`.

### `nordic_charcoal` — paleta carvão/preto
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_charcoal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_charcoal_pristine_normal/{front,side,back}.jpg`.
  - `nordic_charcoal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.90, L=1.14, T=2.01. Capturas: `armor/pirateatelier/nordic_charcoal_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_charcoal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.25, L=1.01, T=2.31. Capturas: `armor/pirateatelier/nordic_charcoal_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_charcoal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.16, L=1.26, T=1.97. Capturas: `armor/pirateatelier/nordic_charcoal_tattered_normal/{front,side,back}.jpg`.
  - `nordic_charcoal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.53, L=1.57, T=2.23. Capturas: `armor/pirateatelier/nordic_charcoal_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_charcoal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.45, L=0.93, T=2.32. Capturas: `armor/pirateatelier/nordic_charcoal_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_charcoal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.22, L=0.61, T=0.77. Capturas: `armor/pirateatelier/nordic_charcoal_weathered_normal/{front,side,back}.jpg`.
  - `nordic_charcoal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.75, L=0.94, T=1.67. Capturas: `armor/pirateatelier/nordic_charcoal_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_charcoal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.29, L=0.64, T=2.15. Capturas: `armor/pirateatelier/nordic_charcoal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_charcoal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_charcoal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_charcoal_pristine_normal/`.

### `nordic_copper` — paleta cobre
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_copper_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_copper_pristine_normal/{front,side,back}.jpg`.
  - `nordic_copper_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.10, L=1.15, T=2.18. Capturas: `armor/pirateatelier/nordic_copper_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_copper_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.05, L=1.03, T=2.53. Capturas: `armor/pirateatelier/nordic_copper_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_copper_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.26, L=1.18, T=2.50. Capturas: `armor/pirateatelier/nordic_copper_tattered_normal/{front,side,back}.jpg`.
  - `nordic_copper_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.34, L=1.38, T=2.38. Capturas: `armor/pirateatelier/nordic_copper_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_copper_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.01, L=0.95, T=1.88. Capturas: `armor/pirateatelier/nordic_copper_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_copper_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.38, L=0.44, T=1.00. Capturas: `armor/pirateatelier/nordic_copper_weathered_normal/{front,side,back}.jpg`.
  - `nordic_copper_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.64, L=0.85, T=1.89. Capturas: `armor/pirateatelier/nordic_copper_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_copper_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.96, L=0.64, T=2.07. Capturas: `armor/pirateatelier/nordic_copper_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_copper_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_copper_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_copper_pristine_normal/`.

### `nordic_crimson` — paleta carmesim
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_crimson_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_crimson_pristine_normal/{front,side,back}.jpg`.
  - `nordic_crimson_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.95, L=0.97, T=2.00. Capturas: `armor/pirateatelier/nordic_crimson_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_crimson_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.79, L=0.81, T=2.28. Capturas: `armor/pirateatelier/nordic_crimson_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_crimson_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.94, L=1.12, T=2.48. Capturas: `armor/pirateatelier/nordic_crimson_tattered_normal/{front,side,back}.jpg`.
  - `nordic_crimson_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.36, L=1.53, T=2.67. Capturas: `armor/pirateatelier/nordic_crimson_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_crimson_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.13, L=0.80, T=2.25. Capturas: `armor/pirateatelier/nordic_crimson_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_crimson_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.42, L=0.39, T=1.11. Capturas: `armor/pirateatelier/nordic_crimson_weathered_normal/{front,side,back}.jpg`.
  - `nordic_crimson_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.84, L=0.82, T=1.65. Capturas: `armor/pirateatelier/nordic_crimson_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_crimson_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.02, L=0.51, T=1.88. Capturas: `armor/pirateatelier/nordic_crimson_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_crimson_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_crimson_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_crimson_pristine_normal/`.

### `nordic_emerald` — paleta esmeralda
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_emerald_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_emerald_pristine_normal/{front,side,back}.jpg`.
  - `nordic_emerald_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.08, L=1.23, T=1.89. Capturas: `armor/pirateatelier/nordic_emerald_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_emerald_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.99, L=0.90, T=1.86. Capturas: `armor/pirateatelier/nordic_emerald_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_emerald_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.83, L=0.99, T=1.82. Capturas: `armor/pirateatelier/nordic_emerald_tattered_normal/{front,side,back}.jpg`.
  - `nordic_emerald_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.13, L=1.50, T=2.67. Capturas: `armor/pirateatelier/nordic_emerald_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_emerald_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.23, L=0.76, T=2.21. Capturas: `armor/pirateatelier/nordic_emerald_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_emerald_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.29, L=0.45, T=1.22. Capturas: `armor/pirateatelier/nordic_emerald_weathered_normal/{front,side,back}.jpg`.
  - `nordic_emerald_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.89, L=1.02, T=1.73. Capturas: `armor/pirateatelier/nordic_emerald_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_emerald_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.30, L=0.65, T=1.98. Capturas: `armor/pirateatelier/nordic_emerald_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_emerald_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_emerald_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_emerald_pristine_normal/`.

### `nordic_forest` — paleta verde floresta
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_forest_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_forest_pristine_normal/{front,side,back}.jpg`.
  - `nordic_forest_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.94, L=1.13, T=2.14. Capturas: `armor/pirateatelier/nordic_forest_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_forest_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.15, L=1.00, T=2.52. Capturas: `armor/pirateatelier/nordic_forest_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_forest_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.99, L=1.20, T=2.46. Capturas: `armor/pirateatelier/nordic_forest_tattered_normal/{front,side,back}.jpg`.
  - `nordic_forest_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.27, L=1.57, T=2.19. Capturas: `armor/pirateatelier/nordic_forest_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_forest_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.96, L=0.88, T=2.23. Capturas: `armor/pirateatelier/nordic_forest_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_forest_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.22, L=0.32, T=1.06. Capturas: `armor/pirateatelier/nordic_forest_weathered_normal/{front,side,back}.jpg`.
  - `nordic_forest_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.61, L=0.92, T=1.80. Capturas: `armor/pirateatelier/nordic_forest_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_forest_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.07, L=0.72, T=2.20. Capturas: `armor/pirateatelier/nordic_forest_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_forest_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_forest_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_forest_pristine_normal/`.

### `nordic_ivory` — paleta marfim/cinza-claro
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_ivory_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_ivory_pristine_normal/{front,side,back}.jpg`.
  - `nordic_ivory_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.89, L=1.27, T=2.93. Capturas: `armor/pirateatelier/nordic_ivory_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_ivory_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.59, L=1.03, T=3.18. Capturas: `armor/pirateatelier/nordic_ivory_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_ivory_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.18, L=1.42, T=3.23. Capturas: `armor/pirateatelier/nordic_ivory_tattered_normal/{front,side,back}.jpg`.
  - `nordic_ivory_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=4.03, L=1.74, T=3.31. Capturas: `armor/pirateatelier/nordic_ivory_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_ivory_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.83, L=1.07, T=2.81. Capturas: `armor/pirateatelier/nordic_ivory_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_ivory_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.07, L=0.56, T=1.78. Capturas: `armor/pirateatelier/nordic_ivory_weathered_normal/{front,side,back}.jpg`.
  - `nordic_ivory_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.89, L=1.20, T=2.83. Capturas: `armor/pirateatelier/nordic_ivory_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_ivory_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.95, L=0.86, T=2.94. Capturas: `armor/pirateatelier/nordic_ivory_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_ivory_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_ivory_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_ivory_pristine_normal/`.

### `nordic_navy` — paleta azul-marinho
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_navy_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_navy_pristine_normal/{front,side,back}.jpg`.
  - `nordic_navy_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.92, L=1.33, T=1.92. Capturas: `armor/pirateatelier/nordic_navy_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_navy_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.93, L=0.83, T=1.97. Capturas: `armor/pirateatelier/nordic_navy_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_navy_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.00, L=0.98, T=2.01. Capturas: `armor/pirateatelier/nordic_navy_tattered_normal/{front,side,back}.jpg`.
  - `nordic_navy_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.57, L=1.53, T=2.77. Capturas: `armor/pirateatelier/nordic_navy_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_navy_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.55, L=0.80, T=2.57. Capturas: `armor/pirateatelier/nordic_navy_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_navy_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.23, L=0.50, T=1.23. Capturas: `armor/pirateatelier/nordic_navy_weathered_normal/{front,side,back}.jpg`.
  - `nordic_navy_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.87, L=1.14, T=1.82. Capturas: `armor/pirateatelier/nordic_navy_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_navy_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.16, L=0.69, T=1.77. Capturas: `armor/pirateatelier/nordic_navy_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_navy_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_navy_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_navy_pristine_normal/`.

### `nordic_ochre` — paleta ocre
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_ochre_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_ochre_pristine_normal/{front,side,back}.jpg`.
  - `nordic_ochre_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.35, L=1.26, T=2.33. Capturas: `armor/pirateatelier/nordic_ochre_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_ochre_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.17, L=1.12, T=2.74. Capturas: `armor/pirateatelier/nordic_ochre_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_ochre_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.12, L=1.36, T=2.58. Capturas: `armor/pirateatelier/nordic_ochre_tattered_normal/{front,side,back}.jpg`.
  - `nordic_ochre_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.29, L=1.41, T=2.71. Capturas: `armor/pirateatelier/nordic_ochre_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_ochre_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.04, L=0.90, T=2.51. Capturas: `armor/pirateatelier/nordic_ochre_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_ochre_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.90, L=0.78, T=1.72. Capturas: `armor/pirateatelier/nordic_ochre_weathered_normal/{front,side,back}.jpg`.
  - `nordic_ochre_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.17, L=1.07, T=2.42. Capturas: `armor/pirateatelier/nordic_ochre_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_ochre_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.38, L=0.83, T=2.55. Capturas: `armor/pirateatelier/nordic_ochre_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_ochre_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_ochre_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_ochre_pristine_normal/`.

### `nordic_plum` — paleta ameixa/roxo
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_plum_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_plum_pristine_normal/{front,side,back}.jpg`.
  - `nordic_plum_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.80, L=1.09, T=1.84. Capturas: `armor/pirateatelier/nordic_plum_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_plum_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.80, L=0.74, T=2.17. Capturas: `armor/pirateatelier/nordic_plum_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_plum_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.79, L=1.10, T=2.60. Capturas: `armor/pirateatelier/nordic_plum_tattered_normal/{front,side,back}.jpg`.
  - `nordic_plum_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.30, L=1.72, T=2.90. Capturas: `armor/pirateatelier/nordic_plum_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_plum_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.35, L=1.15, T=2.90. Capturas: `armor/pirateatelier/nordic_plum_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_plum_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.39, L=0.86, T=1.68. Capturas: `armor/pirateatelier/nordic_plum_weathered_normal/{front,side,back}.jpg`.
  - `nordic_plum_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.92, L=1.09, T=1.80. Capturas: `armor/pirateatelier/nordic_plum_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_plum_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.91, L=0.56, T=1.73. Capturas: `armor/pirateatelier/nordic_plum_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_plum_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_plum_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_plum_pristine_normal/`.

### `nordic_sea_blue` — paleta azul-mar
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_sea_blue_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_sea_blue_pristine_normal/{front,side,back}.jpg`.
  - `nordic_sea_blue_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.38, L=1.21, T=2.04. Capturas: `armor/pirateatelier/nordic_sea_blue_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_sea_blue_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.68, L=0.88, T=2.41. Capturas: `armor/pirateatelier/nordic_sea_blue_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_sea_blue_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.10, L=1.12, T=2.16. Capturas: `armor/pirateatelier/nordic_sea_blue_tattered_normal/{front,side,back}.jpg`.
  - `nordic_sea_blue_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.79, L=1.71, T=2.80. Capturas: `armor/pirateatelier/nordic_sea_blue_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_sea_blue_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.66, L=1.08, T=2.76. Capturas: `armor/pirateatelier/nordic_sea_blue_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_sea_blue_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.78, L=0.99, T=1.56. Capturas: `armor/pirateatelier/nordic_sea_blue_weathered_normal/{front,side,back}.jpg`.
  - `nordic_sea_blue_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.28, L=1.32, T=1.94. Capturas: `armor/pirateatelier/nordic_sea_blue_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_sea_blue_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.27, L=0.69, T=1.99. Capturas: `armor/pirateatelier/nordic_sea_blue_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_sea_blue_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_sea_blue_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_sea_blue_pristine_normal/`.

### `nordic_teal` — paleta verde-azulado
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_teal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_teal_pristine_normal/{front,side,back}.jpg`.
  - `nordic_teal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.20, L=1.03, T=1.80. Capturas: `armor/pirateatelier/nordic_teal_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_teal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.04, L=0.55, T=1.92. Capturas: `armor/pirateatelier/nordic_teal_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_teal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.97, L=0.92, T=2.28. Capturas: `armor/pirateatelier/nordic_teal_tattered_normal/{front,side,back}.jpg`.
  - `nordic_teal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.65, L=1.62, T=3.05. Capturas: `armor/pirateatelier/nordic_teal_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_teal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.67, L=1.29, T=2.91. Capturas: `armor/pirateatelier/nordic_teal_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_teal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.68, L=0.97, T=1.87. Capturas: `armor/pirateatelier/nordic_teal_weathered_normal/{front,side,back}.jpg`.
  - `nordic_teal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.15, L=1.34, T=2.10. Capturas: `armor/pirateatelier/nordic_teal_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_teal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.29, L=0.78, T=1.83. Capturas: `armor/pirateatelier/nordic_teal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_teal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_teal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_teal_pristine_normal/`.

### `nordic_wine` — paleta vinho
- **Defeito visual específico:** chapéu/lenço de nordic forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `nordic_wine_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/nordic_wine_pristine_normal/{front,side,back}.jpg`.
  - `nordic_wine_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.96, L=0.97, T=1.94. Capturas: `armor/pirateatelier/nordic_wine_pristine_peg_left/{front,side,back}.jpg`.
  - `nordic_wine_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.21, L=0.54, T=1.97. Capturas: `armor/pirateatelier/nordic_wine_pristine_peg_right/{front,side,back}.jpg`.
  - `nordic_wine_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.30, L=0.72, T=1.88. Capturas: `armor/pirateatelier/nordic_wine_tattered_normal/{front,side,back}.jpg`.
  - `nordic_wine_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.43, L=1.34, T=2.29. Capturas: `armor/pirateatelier/nordic_wine_tattered_peg_left/{front,side,back}.jpg`.
  - `nordic_wine_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.22, L=0.95, T=2.46. Capturas: `armor/pirateatelier/nordic_wine_tattered_peg_right/{front,side,back}.jpg`.
  - `nordic_wine_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.35, L=0.92, T=1.52. Capturas: `armor/pirateatelier/nordic_wine_weathered_normal/{front,side,back}.jpg`.
  - `nordic_wine_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.95, L=1.38, T=2.36. Capturas: `armor/pirateatelier/nordic_wine_weathered_peg_left/{front,side,back}.jpg`.
  - `nordic_wine_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.89, L=0.79, T=2.02. Capturas: `armor/pirateatelier/nordic_wine_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/nordic_wine_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/nordic_wine_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/nordic_wine_pristine_normal/`.

### `pirate_charcoal` — paleta carvão/preto
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_charcoal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_charcoal_pristine_normal/{front,side,back}.jpg`.
  - `pirate_charcoal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.81, L=1.01, T=1.45. Capturas: `armor/pirateatelier/pirate_charcoal_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_charcoal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.63, L=0.54, T=1.92. Capturas: `armor/pirateatelier/pirate_charcoal_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_charcoal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.53, L=0.79, T=2.29. Capturas: `armor/pirateatelier/pirate_charcoal_tattered_normal/{front,side,back}.jpg`.
  - `pirate_charcoal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.44, L=1.26, T=3.09. Capturas: `armor/pirateatelier/pirate_charcoal_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_charcoal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.35, L=1.05, T=2.75. Capturas: `armor/pirateatelier/pirate_charcoal_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_charcoal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.37, L=0.91, T=1.64. Capturas: `armor/pirateatelier/pirate_charcoal_weathered_normal/{front,side,back}.jpg`.
  - `pirate_charcoal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.95, L=1.33, T=2.29. Capturas: `armor/pirateatelier/pirate_charcoal_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_charcoal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.95, L=0.83, T=2.22. Capturas: `armor/pirateatelier/pirate_charcoal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_charcoal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_charcoal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_charcoal_pristine_normal/`.

### `pirate_copper` — paleta cobre
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_copper_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_copper_pristine_normal/{front,side,back}.jpg`.
  - `pirate_copper_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.04, L=1.05, T=2.08. Capturas: `armor/pirateatelier/pirate_copper_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_copper_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.27, L=0.80, T=2.12. Capturas: `armor/pirateatelier/pirate_copper_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_copper_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.04, L=0.82, T=1.51. Capturas: `armor/pirateatelier/pirate_copper_tattered_normal/{front,side,back}.jpg`.
  - `pirate_copper_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.95, L=0.86, T=2.24. Capturas: `armor/pirateatelier/pirate_copper_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_copper_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.60, L=0.79, T=2.60. Capturas: `armor/pirateatelier/pirate_copper_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_copper_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.34, L=0.87, T=1.70. Capturas: `armor/pirateatelier/pirate_copper_weathered_normal/{front,side,back}.jpg`.
  - `pirate_copper_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.73, L=1.21, T=2.17. Capturas: `armor/pirateatelier/pirate_copper_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_copper_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.73, L=0.91, T=2.31. Capturas: `armor/pirateatelier/pirate_copper_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_copper_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_copper_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_copper_pristine_normal/`.

### `pirate_crimson` — paleta carmesim
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_crimson_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_crimson_pristine_normal/{front,side,back}.jpg`.
  - `pirate_crimson_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.37, L=0.98, T=1.80. Capturas: `armor/pirateatelier/pirate_crimson_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_crimson_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.70, L=0.80, T=2.09. Capturas: `armor/pirateatelier/pirate_crimson_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_crimson_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.67, L=0.94, T=2.15. Capturas: `armor/pirateatelier/pirate_crimson_tattered_normal/{front,side,back}.jpg`.
  - `pirate_crimson_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.17, L=1.27, T=2.34. Capturas: `armor/pirateatelier/pirate_crimson_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_crimson_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.40, L=0.76, T=2.26. Capturas: `armor/pirateatelier/pirate_crimson_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_crimson_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.42, L=0.74, T=1.33. Capturas: `armor/pirateatelier/pirate_crimson_weathered_normal/{front,side,back}.jpg`.
  - `pirate_crimson_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.87, L=1.15, T=2.09. Capturas: `armor/pirateatelier/pirate_crimson_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_crimson_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.01, L=0.73, T=2.11. Capturas: `armor/pirateatelier/pirate_crimson_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_crimson_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_crimson_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_crimson_pristine_normal/`.

### `pirate_emerald` — paleta esmeralda
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_emerald_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_emerald_pristine_normal/{front,side,back}.jpg`.
  - `pirate_emerald_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.70, L=1.16, T=2.24. Capturas: `armor/pirateatelier/pirate_emerald_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_emerald_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.97, L=0.77, T=2.09. Capturas: `armor/pirateatelier/pirate_emerald_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_emerald_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.61, L=1.02, T=1.64. Capturas: `armor/pirateatelier/pirate_emerald_tattered_normal/{front,side,back}.jpg`.
  - `pirate_emerald_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.83, L=1.31, T=2.29. Capturas: `armor/pirateatelier/pirate_emerald_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_emerald_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.78, L=0.77, T=2.23. Capturas: `armor/pirateatelier/pirate_emerald_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_emerald_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.16, L=0.64, T=1.31. Capturas: `armor/pirateatelier/pirate_emerald_weathered_normal/{front,side,back}.jpg`.
  - `pirate_emerald_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.67, L=1.14, T=1.88. Capturas: `armor/pirateatelier/pirate_emerald_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_emerald_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.97, L=0.67, T=2.02. Capturas: `armor/pirateatelier/pirate_emerald_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_emerald_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_emerald_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_emerald_pristine_normal/`.

### `pirate_forest` — paleta verde floresta
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_forest_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_forest_pristine_normal/{front,side,back}.jpg`.
  - `pirate_forest_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.84, L=1.14, T=2.14. Capturas: `armor/pirateatelier/pirate_forest_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_forest_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.12, L=0.99, T=2.40. Capturas: `armor/pirateatelier/pirate_forest_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_forest_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.92, L=1.15, T=2.29. Capturas: `armor/pirateatelier/pirate_forest_tattered_normal/{front,side,back}.jpg`.
  - `pirate_forest_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.29, L=1.30, T=2.40. Capturas: `armor/pirateatelier/pirate_forest_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_forest_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.21, L=0.57, T=1.88. Capturas: `armor/pirateatelier/pirate_forest_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_forest_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.00, L=0.45, T=1.13. Capturas: `armor/pirateatelier/pirate_forest_weathered_normal/{front,side,back}.jpg`.
  - `pirate_forest_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.66, L=1.06, T=1.89. Capturas: `armor/pirateatelier/pirate_forest_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_forest_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.06, L=0.78, T=2.06. Capturas: `armor/pirateatelier/pirate_forest_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_forest_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_forest_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_forest_pristine_normal/`.

### `pirate_ivory` — paleta marfim/cinza-claro
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_ivory_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_ivory_pristine_normal/{front,side,back}.jpg`.
  - `pirate_ivory_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.02, L=1.39, T=2.65. Capturas: `armor/pirateatelier/pirate_ivory_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_ivory_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.26, L=1.08, T=2.96. Capturas: `armor/pirateatelier/pirate_ivory_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_ivory_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.86, L=1.41, T=3.01. Capturas: `armor/pirateatelier/pirate_ivory_tattered_normal/{front,side,back}.jpg`.
  - `pirate_ivory_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.56, L=1.79, T=3.32. Capturas: `armor/pirateatelier/pirate_ivory_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_ivory_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.41, L=1.16, T=3.14. Capturas: `armor/pirateatelier/pirate_ivory_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_ivory_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.15, L=0.73, T=1.75. Capturas: `armor/pirateatelier/pirate_ivory_weathered_normal/{front,side,back}.jpg`.
  - `pirate_ivory_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.79, L=1.39, T=2.81. Capturas: `armor/pirateatelier/pirate_ivory_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_ivory_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.02, L=0.98, T=2.86. Capturas: `armor/pirateatelier/pirate_ivory_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_ivory_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_ivory_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_ivory_pristine_normal/`.

### `pirate_navy` — paleta azul-marinho
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_navy_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_navy_pristine_normal/{front,side,back}.jpg`.
  - `pirate_navy_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.12, L=1.32, T=2.24. Capturas: `armor/pirateatelier/pirate_navy_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_navy_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.42, L=0.92, T=2.30. Capturas: `armor/pirateatelier/pirate_navy_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_navy_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.13, L=0.98, T=1.86. Capturas: `armor/pirateatelier/pirate_navy_tattered_normal/{front,side,back}.jpg`.
  - `pirate_navy_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.19, L=1.48, T=2.59. Capturas: `armor/pirateatelier/pirate_navy_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_navy_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.04, L=0.92, T=2.31. Capturas: `armor/pirateatelier/pirate_navy_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_navy_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.24, L=0.46, T=1.15. Capturas: `armor/pirateatelier/pirate_navy_weathered_normal/{front,side,back}.jpg`.
  - `pirate_navy_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.88, L=1.19, T=1.90. Capturas: `armor/pirateatelier/pirate_navy_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_navy_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.34, L=0.68, T=2.07. Capturas: `armor/pirateatelier/pirate_navy_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_navy_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_navy_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_navy_pristine_normal/`.

### `pirate_ochre` — paleta ocre
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_ochre_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_ochre_pristine_normal/{front,side,back}.jpg`.
  - `pirate_ochre_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.86, L=1.24, T=2.40. Capturas: `armor/pirateatelier/pirate_ochre_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_ochre_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.35, L=1.19, T=2.69. Capturas: `armor/pirateatelier/pirate_ochre_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_ochre_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.20, L=1.29, T=2.58. Capturas: `armor/pirateatelier/pirate_ochre_tattered_normal/{front,side,back}.jpg`.
  - `pirate_ochre_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.49, L=1.32, T=2.67. Capturas: `armor/pirateatelier/pirate_ochre_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_ochre_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.14, L=0.78, T=2.41. Capturas: `armor/pirateatelier/pirate_ochre_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_ochre_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.52, L=0.70, T=1.63. Capturas: `armor/pirateatelier/pirate_ochre_weathered_normal/{front,side,back}.jpg`.
  - `pirate_ochre_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.16, L=1.14, T=2.29. Capturas: `armor/pirateatelier/pirate_ochre_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_ochre_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.61, L=0.94, T=2.66. Capturas: `armor/pirateatelier/pirate_ochre_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_ochre_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_ochre_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_ochre_pristine_normal/`.

### `pirate_plum` — paleta ameixa/roxo
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_plum_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_plum_pristine_normal/{front,side,back}.jpg`.
  - `pirate_plum_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.49, L=1.21, T=2.10. Capturas: `armor/pirateatelier/pirate_plum_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_plum_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.84, L=0.92, T=2.37. Capturas: `armor/pirateatelier/pirate_plum_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_plum_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.01, L=1.03, T=2.27. Capturas: `armor/pirateatelier/pirate_plum_tattered_normal/{front,side,back}.jpg`.
  - `pirate_plum_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.15, L=1.51, T=2.36. Capturas: `armor/pirateatelier/pirate_plum_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_plum_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.40, L=0.66, T=2.24. Capturas: `armor/pirateatelier/pirate_plum_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_plum_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.29, L=0.61, T=1.65. Capturas: `armor/pirateatelier/pirate_plum_weathered_normal/{front,side,back}.jpg`.
  - `pirate_plum_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.85, L=1.22, T=2.15. Capturas: `armor/pirateatelier/pirate_plum_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_plum_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.92, L=0.83, T=2.43. Capturas: `armor/pirateatelier/pirate_plum_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_plum_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_plum_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_plum_pristine_normal/`.

### `pirate_sea_blue` — paleta azul-mar
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_sea_blue_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_sea_blue_pristine_normal/{front,side,back}.jpg`.
  - `pirate_sea_blue_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.16, L=1.24, T=1.95. Capturas: `armor/pirateatelier/pirate_sea_blue_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_sea_blue_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.16, L=0.81, T=2.08. Capturas: `armor/pirateatelier/pirate_sea_blue_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_sea_blue_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.68, L=1.01, T=1.94. Capturas: `armor/pirateatelier/pirate_sea_blue_tattered_normal/{front,side,back}.jpg`.
  - `pirate_sea_blue_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.44, L=1.44, T=2.76. Capturas: `armor/pirateatelier/pirate_sea_blue_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_sea_blue_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.37, L=0.74, T=3.06. Capturas: `armor/pirateatelier/pirate_sea_blue_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_sea_blue_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.55, L=0.64, T=1.40. Capturas: `armor/pirateatelier/pirate_sea_blue_weathered_normal/{front,side,back}.jpg`.
  - `pirate_sea_blue_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.16, L=1.19, T=2.24. Capturas: `armor/pirateatelier/pirate_sea_blue_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_sea_blue_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.45, L=0.65, T=2.37. Capturas: `armor/pirateatelier/pirate_sea_blue_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_sea_blue_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_sea_blue_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_sea_blue_pristine_normal/`.

### `pirate_teal` — paleta verde-azulado
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_teal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_teal_pristine_normal/{front,side,back}.jpg`.
  - `pirate_teal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.09, L=1.23, T=2.18. Capturas: `armor/pirateatelier/pirate_teal_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_teal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.29, L=0.95, T=2.33. Capturas: `armor/pirateatelier/pirate_teal_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_teal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.88, L=1.06, T=2.02. Capturas: `armor/pirateatelier/pirate_teal_tattered_normal/{front,side,back}.jpg`.
  - `pirate_teal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.19, L=1.28, T=2.06. Capturas: `armor/pirateatelier/pirate_teal_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_teal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.98, L=0.52, T=2.30. Capturas: `armor/pirateatelier/pirate_teal_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_teal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.22, L=0.52, T=1.41. Capturas: `armor/pirateatelier/pirate_teal_weathered_normal/{front,side,back}.jpg`.
  - `pirate_teal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.01, L=1.15, T=2.14. Capturas: `armor/pirateatelier/pirate_teal_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_teal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.39, L=0.73, T=2.36. Capturas: `armor/pirateatelier/pirate_teal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_teal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_teal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_teal_pristine_normal/`.

### `pirate_wine` — paleta vinho
- **Defeito visual específico:** chapéu/lenço de pirate forma um bloco que oculta o rosto; correias, lapelas e aba não projetam volume; costas excessivamente uniformes. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `pirate_wine_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/pirate_wine_pristine_normal/{front,side,back}.jpg`.
  - `pirate_wine_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.51, L=1.13, T=2.01. Capturas: `armor/pirateatelier/pirate_wine_pristine_peg_left/{front,side,back}.jpg`.
  - `pirate_wine_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.73, L=0.90, T=2.16. Capturas: `armor/pirateatelier/pirate_wine_pristine_peg_right/{front,side,back}.jpg`.
  - `pirate_wine_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.86, L=1.15, T=2.06. Capturas: `armor/pirateatelier/pirate_wine_tattered_normal/{front,side,back}.jpg`.
  - `pirate_wine_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.38, L=1.57, T=2.41. Capturas: `armor/pirateatelier/pirate_wine_tattered_peg_left/{front,side,back}.jpg`.
  - `pirate_wine_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.79, L=0.67, T=2.42. Capturas: `armor/pirateatelier/pirate_wine_tattered_peg_right/{front,side,back}.jpg`.
  - `pirate_wine_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.10, L=0.50, T=1.41. Capturas: `armor/pirateatelier/pirate_wine_weathered_normal/{front,side,back}.jpg`.
  - `pirate_wine_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.63, L=1.09, T=2.03. Capturas: `armor/pirateatelier/pirate_wine_weathered_peg_left/{front,side,back}.jpg`.
  - `pirate_wine_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.86, L=0.76, T=2.27. Capturas: `armor/pirateatelier/pirate_wine_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/pirate_wine_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/pirate_wine_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/pirate_wine_pristine_normal/`.

### `quartermaster_armor`
- **Defeito visual específico:** bandoleira diagonal e cintos aparecem pintados; não há bainha/bolso externo, com cabeça obstruída. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `quartermaster_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/quartermaster_armor_pristine_normal/{front,side,back}.jpg`.
  - `quartermaster_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.24, L=0.98, T=2.14. Capturas: `armor/pirateatelier/quartermaster_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `quartermaster_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.30, L=0.71, T=2.25. Capturas: `armor/pirateatelier/quartermaster_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `quartermaster_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.17, L=0.99, T=2.34. Capturas: `armor/pirateatelier/quartermaster_armor_tattered_normal/{front,side,back}.jpg`.
  - `quartermaster_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.57, L=1.47, T=2.50. Capturas: `armor/pirateatelier/quartermaster_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `quartermaster_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.40, L=0.96, T=2.55. Capturas: `armor/pirateatelier/quartermaster_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `quartermaster_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.36, L=0.59, T=1.35. Capturas: `armor/pirateatelier/quartermaster_armor_weathered_normal/{front,side,back}.jpg`.
  - `quartermaster_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.98, L=1.08, T=2.19. Capturas: `armor/pirateatelier/quartermaster_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `quartermaster_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.00, L=0.73, T=2.17. Capturas: `armor/pirateatelier/quartermaster_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/quartermaster_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/quartermaster_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/quartermaster_armor_pristine_normal/`.

### `rogue_navigator`
- **Defeito visual específico:** faixa de peito/objetos cartográficos sem relevo; gorro/capuz quadrado não preserva a leitura facial. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `rogue_navigator_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/rogue_navigator_pristine_normal/{front,side,back}.jpg`.
  - `rogue_navigator_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.15, L=1.06, T=1.76. Capturas: `armor/pirateatelier/rogue_navigator_pristine_peg_left/{front,side,back}.jpg`.
  - `rogue_navigator_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.28, L=0.64, T=1.58. Capturas: `armor/pirateatelier/rogue_navigator_pristine_peg_right/{front,side,back}.jpg`.
  - `rogue_navigator_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.81, L=0.62, T=1.46. Capturas: `armor/pirateatelier/rogue_navigator_tattered_normal/{front,side,back}.jpg`.
  - `rogue_navigator_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.96, L=0.93, T=2.26. Capturas: `armor/pirateatelier/rogue_navigator_tattered_peg_left/{front,side,back}.jpg`.
  - `rogue_navigator_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.15, L=0.65, T=2.43. Capturas: `armor/pirateatelier/rogue_navigator_tattered_peg_right/{front,side,back}.jpg`.
  - `rogue_navigator_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.33, L=0.66, T=1.25. Capturas: `armor/pirateatelier/rogue_navigator_weathered_normal/{front,side,back}.jpg`.
  - `rogue_navigator_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.93, L=1.17, T=1.85. Capturas: `armor/pirateatelier/rogue_navigator_weathered_peg_left/{front,side,back}.jpg`.
  - `rogue_navigator_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.11, L=0.56, T=1.78. Capturas: `armor/pirateatelier/rogue_navigator_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/rogue_navigator_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/rogue_navigator_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/rogue_navigator_pristine_normal/`.

### `sailor_charcoal` — paleta carvão/preto
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_charcoal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_charcoal_pristine_normal/{front,side,back}.jpg`.
  - `sailor_charcoal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.10, L=1.15, T=1.97. Capturas: `armor/pirateatelier/sailor_charcoal_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_charcoal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.47, L=0.72, T=2.04. Capturas: `armor/pirateatelier/sailor_charcoal_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_charcoal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.13, L=0.64, T=1.55. Capturas: `armor/pirateatelier/sailor_charcoal_tattered_normal/{front,side,back}.jpg`.
  - `sailor_charcoal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.33, L=1.05, T=2.23. Capturas: `armor/pirateatelier/sailor_charcoal_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_charcoal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.58, L=0.79, T=2.42. Capturas: `armor/pirateatelier/sailor_charcoal_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_charcoal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.18, L=0.86, T=1.44. Capturas: `armor/pirateatelier/sailor_charcoal_weathered_normal/{front,side,back}.jpg`.
  - `sailor_charcoal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.80, L=1.30, T=2.05. Capturas: `armor/pirateatelier/sailor_charcoal_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_charcoal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.09, L=0.75, T=2.28. Capturas: `armor/pirateatelier/sailor_charcoal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_charcoal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_charcoal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_charcoal_pristine_normal/`.

### `sailor_copper` — paleta cobre
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_copper_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_copper_pristine_normal/{front,side,back}.jpg`.
  - `sailor_copper_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.81, L=0.96, T=1.87. Capturas: `armor/pirateatelier/sailor_copper_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_copper_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.23, L=0.76, T=2.22. Capturas: `armor/pirateatelier/sailor_copper_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_copper_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.14, L=1.00, T=2.11. Capturas: `armor/pirateatelier/sailor_copper_tattered_normal/{front,side,back}.jpg`.
  - `sailor_copper_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.43, L=1.27, T=2.50. Capturas: `armor/pirateatelier/sailor_copper_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_copper_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.87, L=0.97, T=2.50. Capturas: `armor/pirateatelier/sailor_copper_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_copper_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.23, L=0.85, T=1.64. Capturas: `armor/pirateatelier/sailor_copper_weathered_normal/{front,side,back}.jpg`.
  - `sailor_copper_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.78, L=1.19, T=2.20. Capturas: `armor/pirateatelier/sailor_copper_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_copper_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.76, L=0.75, T=2.28. Capturas: `armor/pirateatelier/sailor_copper_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_copper_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_copper_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_copper_pristine_normal/`.

### `sailor_crimson` — paleta carmesim
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_crimson_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_crimson_pristine_normal/{front,side,back}.jpg`.
  - `sailor_crimson_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.97, L=0.98, T=1.68. Capturas: `armor/pirateatelier/sailor_crimson_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_crimson_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.95, L=0.62, T=1.80. Capturas: `armor/pirateatelier/sailor_crimson_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_crimson_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.81, L=0.69, T=2.17. Capturas: `armor/pirateatelier/sailor_crimson_tattered_normal/{front,side,back}.jpg`.
  - `sailor_crimson_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.32, L=1.02, T=2.79. Capturas: `armor/pirateatelier/sailor_crimson_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_crimson_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.17, L=0.88, T=2.86. Capturas: `armor/pirateatelier/sailor_crimson_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_crimson_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.06, L=0.84, T=1.67. Capturas: `armor/pirateatelier/sailor_crimson_weathered_normal/{front,side,back}.jpg`.
  - `sailor_crimson_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.60, L=1.21, T=2.20. Capturas: `armor/pirateatelier/sailor_crimson_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_crimson_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.62, L=0.69, T=2.00. Capturas: `armor/pirateatelier/sailor_crimson_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_crimson_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_crimson_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_crimson_pristine_normal/`.

### `sailor_emerald` — paleta esmeralda
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_emerald_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_emerald_pristine_normal/{front,side,back}.jpg`.
  - `sailor_emerald_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.06, L=1.09, T=1.94. Capturas: `armor/pirateatelier/sailor_emerald_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_emerald_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.28, L=0.71, T=1.95. Capturas: `armor/pirateatelier/sailor_emerald_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_emerald_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.77, L=0.61, T=1.23. Capturas: `armor/pirateatelier/sailor_emerald_tattered_normal/{front,side,back}.jpg`.
  - `sailor_emerald_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.81, L=1.08, T=2.44. Capturas: `armor/pirateatelier/sailor_emerald_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_emerald_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.61, L=0.94, T=2.38. Capturas: `armor/pirateatelier/sailor_emerald_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_emerald_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.02, L=0.80, T=1.45. Capturas: `armor/pirateatelier/sailor_emerald_weathered_normal/{front,side,back}.jpg`.
  - `sailor_emerald_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.84, L=1.26, T=2.13. Capturas: `armor/pirateatelier/sailor_emerald_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_emerald_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.96, L=0.70, T=2.09. Capturas: `armor/pirateatelier/sailor_emerald_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_emerald_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_emerald_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_emerald_pristine_normal/`.

### `sailor_forest` — paleta verde floresta
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_forest_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_forest_pristine_normal/{front,side,back}.jpg`.
  - `sailor_forest_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.91, L=1.09, T=2.04. Capturas: `armor/pirateatelier/sailor_forest_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_forest_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.25, L=0.78, T=2.22. Capturas: `armor/pirateatelier/sailor_forest_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_forest_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.29, L=0.76, T=2.12. Capturas: `armor/pirateatelier/sailor_forest_tattered_normal/{front,side,back}.jpg`.
  - `sailor_forest_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.03, L=1.31, T=2.32. Capturas: `armor/pirateatelier/sailor_forest_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_forest_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.95, L=0.97, T=2.51. Capturas: `armor/pirateatelier/sailor_forest_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_forest_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.28, L=0.90, T=1.62. Capturas: `armor/pirateatelier/sailor_forest_weathered_normal/{front,side,back}.jpg`.
  - `sailor_forest_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.69, L=1.30, T=2.06. Capturas: `armor/pirateatelier/sailor_forest_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_forest_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.83, L=0.72, T=2.32. Capturas: `armor/pirateatelier/sailor_forest_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_forest_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_forest_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_forest_pristine_normal/`.

### `sailor_ivory` — paleta marfim/cinza-claro
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_ivory_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_ivory_pristine_normal/{front,side,back}.jpg`.
  - `sailor_ivory_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.72, L=1.32, T=2.69. Capturas: `armor/pirateatelier/sailor_ivory_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_ivory_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.81, L=1.03, T=2.92. Capturas: `armor/pirateatelier/sailor_ivory_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_ivory_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.65, L=1.28, T=3.02. Capturas: `armor/pirateatelier/sailor_ivory_tattered_normal/{front,side,back}.jpg`.
  - `sailor_ivory_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.55, L=1.84, T=3.44. Capturas: `armor/pirateatelier/sailor_ivory_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_ivory_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.64, L=1.25, T=3.39. Capturas: `armor/pirateatelier/sailor_ivory_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_ivory_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.50, L=1.03, T=2.19. Capturas: `armor/pirateatelier/sailor_ivory_weathered_normal/{front,side,back}.jpg`.
  - `sailor_ivory_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.39, L=1.49, T=2.95. Capturas: `armor/pirateatelier/sailor_ivory_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_ivory_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.34, L=0.99, T=2.92. Capturas: `armor/pirateatelier/sailor_ivory_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_ivory_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_ivory_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_ivory_pristine_normal/`.

### `sailor_navy` — paleta azul-marinho
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_navy_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_navy_pristine_normal/{front,side,back}.jpg`.
  - `sailor_navy_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.98, L=0.95, T=1.58. Capturas: `armor/pirateatelier/sailor_navy_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_navy_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.50, L=0.38, T=1.92. Capturas: `armor/pirateatelier/sailor_navy_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_navy_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.74, L=0.91, T=2.36. Capturas: `armor/pirateatelier/sailor_navy_tattered_normal/{front,side,back}.jpg`.
  - `sailor_navy_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.29, L=1.81, T=2.96. Capturas: `armor/pirateatelier/sailor_navy_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_navy_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.30, L=1.19, T=3.02. Capturas: `armor/pirateatelier/sailor_navy_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_navy_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.45, L=0.93, T=1.63. Capturas: `armor/pirateatelier/sailor_navy_weathered_normal/{front,side,back}.jpg`.
  - `sailor_navy_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.13, L=1.31, T=2.14. Capturas: `armor/pirateatelier/sailor_navy_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_navy_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.98, L=0.71, T=1.81. Capturas: `armor/pirateatelier/sailor_navy_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_navy_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_navy_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_navy_pristine_normal/`.

### `sailor_ochre` — paleta ocre
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_ochre_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_ochre_pristine_normal/{front,side,back}.jpg`.
  - `sailor_ochre_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.22, L=1.07, T=2.31. Capturas: `armor/pirateatelier/sailor_ochre_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_ochre_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.43, L=0.84, T=2.51. Capturas: `armor/pirateatelier/sailor_ochre_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_ochre_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.19, L=1.05, T=2.41. Capturas: `armor/pirateatelier/sailor_ochre_tattered_normal/{front,side,back}.jpg`.
  - `sailor_ochre_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.03, L=1.41, T=3.02. Capturas: `armor/pirateatelier/sailor_ochre_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_ochre_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.36, L=1.17, T=3.11. Capturas: `armor/pirateatelier/sailor_ochre_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_ochre_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.53, L=1.05, T=2.17. Capturas: `armor/pirateatelier/sailor_ochre_weathered_normal/{front,side,back}.jpg`.
  - `sailor_ochre_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.01, L=1.44, T=2.43. Capturas: `armor/pirateatelier/sailor_ochre_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_ochre_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.60, L=0.91, T=2.51. Capturas: `armor/pirateatelier/sailor_ochre_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_ochre_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_ochre_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_ochre_pristine_normal/`.

### `sailor_plum` — paleta ameixa/roxo
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_plum_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_plum_pristine_normal/{front,side,back}.jpg`.
  - `sailor_plum_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.05, L=0.99, T=2.18. Capturas: `armor/pirateatelier/sailor_plum_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_plum_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.08, L=0.70, T=2.28. Capturas: `armor/pirateatelier/sailor_plum_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_plum_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.37, L=1.04, T=2.18. Capturas: `armor/pirateatelier/sailor_plum_tattered_normal/{front,side,back}.jpg`.
  - `sailor_plum_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.15, L=1.48, T=2.71. Capturas: `armor/pirateatelier/sailor_plum_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_plum_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.97, L=1.07, T=2.37. Capturas: `armor/pirateatelier/sailor_plum_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_plum_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.16, L=0.85, T=1.56. Capturas: `armor/pirateatelier/sailor_plum_weathered_normal/{front,side,back}.jpg`.
  - `sailor_plum_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.66, L=1.19, T=2.04. Capturas: `armor/pirateatelier/sailor_plum_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_plum_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.14, L=0.73, T=2.39. Capturas: `armor/pirateatelier/sailor_plum_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_plum_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_plum_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_plum_pristine_normal/`.

### `sailor_sea_blue` — paleta azul-mar
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_sea_blue_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_sea_blue_pristine_normal/{front,side,back}.jpg`.
  - `sailor_sea_blue_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.99, L=1.08, T=2.05. Capturas: `armor/pirateatelier/sailor_sea_blue_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_sea_blue_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.39, L=0.76, T=2.52. Capturas: `armor/pirateatelier/sailor_sea_blue_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_sea_blue_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.92, L=1.25, T=2.60. Capturas: `armor/pirateatelier/sailor_sea_blue_tattered_normal/{front,side,back}.jpg`.
  - `sailor_sea_blue_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.88, L=1.86, T=3.30. Capturas: `armor/pirateatelier/sailor_sea_blue_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_sea_blue_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.69, L=1.17, T=2.92. Capturas: `armor/pirateatelier/sailor_sea_blue_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_sea_blue_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.51, L=0.81, T=1.53. Capturas: `armor/pirateatelier/sailor_sea_blue_weathered_normal/{front,side,back}.jpg`.
  - `sailor_sea_blue_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.15, L=1.18, T=2.09. Capturas: `armor/pirateatelier/sailor_sea_blue_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_sea_blue_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.46, L=0.68, T=2.50. Capturas: `armor/pirateatelier/sailor_sea_blue_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_sea_blue_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_sea_blue_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_sea_blue_pristine_normal/`.

### `sailor_teal` — paleta verde-azulado
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_teal_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_teal_pristine_normal/{front,side,back}.jpg`.
  - `sailor_teal_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.19, L=1.16, T=2.01. Capturas: `armor/pirateatelier/sailor_teal_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_teal_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.89, L=0.99, T=2.43. Capturas: `armor/pirateatelier/sailor_teal_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_teal_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.07, L=1.20, T=2.83. Capturas: `armor/pirateatelier/sailor_teal_tattered_normal/{front,side,back}.jpg`.
  - `sailor_teal_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.66, L=1.70, T=2.39. Capturas: `armor/pirateatelier/sailor_teal_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_teal_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.25, L=0.92, T=2.04. Capturas: `armor/pirateatelier/sailor_teal_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_teal_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.41, L=0.43, T=1.10. Capturas: `armor/pirateatelier/sailor_teal_weathered_normal/{front,side,back}.jpg`.
  - `sailor_teal_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.06, L=1.02, T=2.06. Capturas: `armor/pirateatelier/sailor_teal_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_teal_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.49, L=0.79, T=2.39. Capturas: `armor/pirateatelier/sailor_teal_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_teal_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_teal_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_teal_pristine_normal/`.

### `sailor_wine` — paleta vinho
- **Defeito visual específico:** roupa de sailor reduzida ao molde da armadura vanilla; avental, tira, bolso, gola ou barra são texturas sem relevo, e a cabeça fica encoberta. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `sailor_wine_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/sailor_wine_pristine_normal/{front,side,back}.jpg`.
  - `sailor_wine_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.52, L=1.21, T=2.12. Capturas: `armor/pirateatelier/sailor_wine_pristine_peg_left/{front,side,back}.jpg`.
  - `sailor_wine_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.81, L=0.96, T=2.34. Capturas: `armor/pirateatelier/sailor_wine_pristine_peg_right/{front,side,back}.jpg`.
  - `sailor_wine_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.07, L=1.01, T=2.00. Capturas: `armor/pirateatelier/sailor_wine_tattered_normal/{front,side,back}.jpg`.
  - `sailor_wine_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.02, L=1.14, T=2.69. Capturas: `armor/pirateatelier/sailor_wine_tattered_peg_left/{front,side,back}.jpg`.
  - `sailor_wine_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.82, L=0.64, T=2.45. Capturas: `armor/pirateatelier/sailor_wine_tattered_peg_right/{front,side,back}.jpg`.
  - `sailor_wine_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.05, L=0.64, T=1.50. Capturas: `armor/pirateatelier/sailor_wine_weathered_normal/{front,side,back}.jpg`.
  - `sailor_wine_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.76, L=1.05, T=2.49. Capturas: `armor/pirateatelier/sailor_wine_weathered_peg_left/{front,side,back}.jpg`.
  - `sailor_wine_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.24, L=0.75, T=2.56. Capturas: `armor/pirateatelier/sailor_wine_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/sailor_wine_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/sailor_wine_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/sailor_wine_pristine_normal/`.

### `ships_cook`
- **Defeito visual específico:** touca quadrada sem copa saliente nem aba; uniforme branco e botões parecem aplicação 2D. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `ships_cook_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/ships_cook_pristine_normal/{front,side,back}.jpg`.
  - `ships_cook_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.53, L=1.23, T=2.98. Capturas: `armor/pirateatelier/ships_cook_pristine_peg_left/{front,side,back}.jpg`.
  - `ships_cook_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.63, L=1.30, T=3.15. Capturas: `armor/pirateatelier/ships_cook_pristine_peg_right/{front,side,back}.jpg`.
  - `ships_cook_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=3.05, L=1.59, T=3.12. Capturas: `armor/pirateatelier/ships_cook_tattered_normal/{front,side,back}.jpg`.
  - `ships_cook_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.29, L=1.56, T=3.79. Capturas: `armor/pirateatelier/ships_cook_tattered_peg_left/{front,side,back}.jpg`.
  - `ships_cook_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.17, L=1.21, T=3.54. Capturas: `armor/pirateatelier/ships_cook_tattered_peg_right/{front,side,back}.jpg`.
  - `ships_cook_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.28, L=1.03, T=2.36. Capturas: `armor/pirateatelier/ships_cook_weathered_normal/{front,side,back}.jpg`.
  - `ships_cook_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.68, L=1.47, T=2.95. Capturas: `armor/pirateatelier/ships_cook_weathered_peg_left/{front,side,back}.jpg`.
  - `ships_cook_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.03, L=1.42, T=3.17. Capturas: `armor/pirateatelier/ships_cook_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/ships_cook_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/ships_cook_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/ships_cook_pristine_normal/`.

### `smuggler`
- **Defeito visual específico:** alça diagonal e bolsos do contrabandista não têm espessura; cabeça coberta por camada preta uniforme. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `smuggler_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/smuggler_pristine_normal/{front,side,back}.jpg`.
  - `smuggler_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=1.99, L=1.09, T=2.05. Capturas: `armor/pirateatelier/smuggler_pristine_peg_left/{front,side,back}.jpg`.
  - `smuggler_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.38, L=0.90, T=2.33. Capturas: `armor/pirateatelier/smuggler_pristine_peg_right/{front,side,back}.jpg`.
  - `smuggler_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.39, L=1.15, T=1.93. Capturas: `armor/pirateatelier/smuggler_tattered_normal/{front,side,back}.jpg`.
  - `smuggler_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.63, L=1.58, T=2.35. Capturas: `armor/pirateatelier/smuggler_tattered_peg_left/{front,side,back}.jpg`.
  - `smuggler_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.50, L=0.94, T=2.20. Capturas: `armor/pirateatelier/smuggler_tattered_peg_right/{front,side,back}.jpg`.
  - `smuggler_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.42, L=0.55, T=0.92. Capturas: `armor/pirateatelier/smuggler_weathered_normal/{front,side,back}.jpg`.
  - `smuggler_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.17, L=1.01, T=2.11. Capturas: `armor/pirateatelier/smuggler_weathered_peg_left/{front,side,back}.jpg`.
  - `smuggler_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.39, L=0.64, T=2.18. Capturas: `armor/pirateatelier/smuggler_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/smuggler_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/smuggler_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/smuggler_pristine_normal/`.

### `tavern_raider`
- **Defeito visual específico:** colete vinho, botões e gola são textura de torso sem separação; rosto encoberto. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `tavern_raider_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/tavern_raider_pristine_normal/{front,side,back}.jpg`.
  - `tavern_raider_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.10, L=1.23, T=2.12. Capturas: `armor/pirateatelier/tavern_raider_pristine_peg_left/{front,side,back}.jpg`.
  - `tavern_raider_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.43, L=0.96, T=2.21. Capturas: `armor/pirateatelier/tavern_raider_pristine_peg_right/{front,side,back}.jpg`.
  - `tavern_raider_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.17, L=0.99, T=1.71. Capturas: `armor/pirateatelier/tavern_raider_tattered_normal/{front,side,back}.jpg`.
  - `tavern_raider_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.19, L=1.12, T=2.13. Capturas: `armor/pirateatelier/tavern_raider_tattered_peg_left/{front,side,back}.jpg`.
  - `tavern_raider_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=1.91, L=0.68, T=2.01. Capturas: `armor/pirateatelier/tavern_raider_tattered_peg_right/{front,side,back}.jpg`.
  - `tavern_raider_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.18, L=0.56, T=1.17. Capturas: `armor/pirateatelier/tavern_raider_weathered_normal/{front,side,back}.jpg`.
  - `tavern_raider_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.07, L=1.10, T=2.07. Capturas: `armor/pirateatelier/tavern_raider_weathered_peg_left/{front,side,back}.jpg`.
  - `tavern_raider_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.30, L=0.74, T=2.19. Capturas: `armor/pirateatelier/tavern_raider_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/tavern_raider_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/tavern_raider_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/tavern_raider_pristine_normal/`.

### `treasure_keeper_armor`
- **Defeito visual específico:** ornamentos e ombreiras dourados sem volume 3D; cabeça e rosto cobertos pelo bloco do elmo. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `treasure_keeper_armor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/treasure_keeper_armor_pristine_normal/{front,side,back}.jpg`.
  - `treasure_keeper_armor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.28, L=1.39, T=2.29. Capturas: `armor/pirateatelier/treasure_keeper_armor_pristine_peg_left/{front,side,back}.jpg`.
  - `treasure_keeper_armor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.50, L=0.88, T=2.30. Capturas: `armor/pirateatelier/treasure_keeper_armor_pristine_peg_right/{front,side,back}.jpg`.
  - `treasure_keeper_armor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.05, L=0.94, T=2.56. Capturas: `armor/pirateatelier/treasure_keeper_armor_tattered_normal/{front,side,back}.jpg`.
  - `treasure_keeper_armor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.99, L=1.50, T=3.36. Capturas: `armor/pirateatelier/treasure_keeper_armor_tattered_peg_left/{front,side,back}.jpg`.
  - `treasure_keeper_armor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=3.12, L=1.21, T=3.33. Capturas: `armor/pirateatelier/treasure_keeper_armor_tattered_peg_right/{front,side,back}.jpg`.
  - `treasure_keeper_armor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.15, L=1.14, T=2.15. Capturas: `armor/pirateatelier/treasure_keeper_armor_weathered_normal/{front,side,back}.jpg`.
  - `treasure_keeper_armor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=3.01, L=1.48, T=2.42. Capturas: `armor/pirateatelier/treasure_keeper_armor_weathered_peg_left/{front,side,back}.jpg`.
  - `treasure_keeper_armor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.68, L=0.92, T=2.26. Capturas: `armor/pirateatelier/treasure_keeper_armor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/treasure_keeper_armor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/treasure_keeper_armor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/treasure_keeper_armor_pristine_normal/`.

### `weathered_sailor`
- **Defeito visual específico:** faixas, cinto e gola quase sem profundidade, cabeça/face coberta por camada azul escura. Nas vistas lateral/traseira, a silhueta permanece a de uma armadura de cubos convencionais, não a das peças da referência 3D.
- **Falhas transversais:** A1, A2, A3. Nas versões `_peg_left`/`_peg_right`, acrescentar P1 e ensaio dinâmico P2. Validar separadamente todas as superfícies/UV de cabeça, ombros, tórax, mangas, cintura, pernas e calçados.
- **Variações individualmente indexadas (vista frontal/lateral/traseira):**
  - `weathered_sailor_pristine_normal`: **limpo**, sem prótese; RGB médio Δ vs pristine_normal: F=0.00, L=0.00, T=0.00. Capturas: `armor/pirateatelier/weathered_sailor_pristine_normal/{front,side,back}.jpg`.
  - `weathered_sailor_pristine_peg_left`: **limpo**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.32, L=1.05, T=1.95. Capturas: `armor/pirateatelier/weathered_sailor_pristine_peg_left/{front,side,back}.jpg`.
  - `weathered_sailor_pristine_peg_right`: **limpo**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.29, L=0.70, T=1.98. Capturas: `armor/pirateatelier/weathered_sailor_pristine_peg_right/{front,side,back}.jpg`.
  - `weathered_sailor_tattered_normal`: **rasgado**, sem prótese; RGB médio Δ vs pristine_normal: F=2.07, L=1.01, T=2.12. Capturas: `armor/pirateatelier/weathered_sailor_tattered_normal/{front,side,back}.jpg`.
  - `weathered_sailor_tattered_peg_left`: **rasgado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.64, L=1.54, T=2.61. Capturas: `armor/pirateatelier/weathered_sailor_tattered_peg_left/{front,side,back}.jpg`.
  - `weathered_sailor_tattered_peg_right`: **rasgado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.55, L=1.06, T=2.42. Capturas: `armor/pirateatelier/weathered_sailor_tattered_peg_right/{front,side,back}.jpg`.
  - `weathered_sailor_weathered_normal`: **desgastado**, sem prótese; RGB médio Δ vs pristine_normal: F=1.76, L=0.59, T=1.30. Capturas: `armor/pirateatelier/weathered_sailor_weathered_normal/{front,side,back}.jpg`.
  - `weathered_sailor_weathered_peg_left`: **desgastado**, prótese esquerda; investigar máscara e pivô left_leg; RGB médio Δ vs pristine_normal: F=2.30, L=1.05, T=1.94. Capturas: `armor/pirateatelier/weathered_sailor_weathered_peg_left/{front,side,back}.jpg`.
  - `weathered_sailor_weathered_peg_right`: **desgastado**, prótese direita; investigar máscara e pivô right_leg; RGB médio Δ vs pristine_normal: F=2.38, L=0.50, T=1.64. Capturas: `armor/pirateatelier/weathered_sailor_weathered_peg_right/{front,side,back}.jpg`.
- **Arquivo de definição:** `assets/pirateatelier/equipment/weathered_sailor_pristine_normal.json`; **atlas:** `assets/pirateatelier/textures/entity/equipment/humanoid/weathered_sailor_pristine_normal.png`; **modelo de referência:** `development_assets/cem_wardrobe/weathered_sailor_pristine_normal/`.

## Critérios de aceite e limites de diagnóstico
1. Nenhum capacete/touca/chapéu não fechado pode obliterar olhos, nariz e face; chapéu 3D acompanha rotação da cabeça.
2. Vestimentas apresentam geometria condizente com referência (abas, lapelas, ombreiras, cintos, saias, bainhas); comparações nas 3 vistas sem clipping.
3. Nas 20 skins sem vestimenta, a captura preserva as cores, detalhes e transparência esperados do atlas individual, em cabeça/corpo/pernas.
4. Próteses devem substituir **somente** o membro selecionado; não deve restar pé humano sobreposto; rotação, translação, escala seguem pose pós-animação.
5. Testar `normal`, `peg_left`, `peg_right` para os 118 arquétipos, 3 estados de desgaste, slots completos/parciais, slim/default, MCA masculino/feminino e FA+Player ativo/desativado.
6. Capturas de referência devem incluir estado da armadura, roupa MCA, skin selecionada, ordem real dos resource packs e `latest.log`. Sem isso, não se deve afirmar causa única ou compatibilidade total.
