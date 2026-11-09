# PROMPT MESTRE — Codex / Pirates of MCA

Você é um agente sênior de renderização Minecraft Fabric 26.2, modelos Blockbench, EMF/CEM, MCA, UV, biped animation, equipamentos e QA visual. Corrija **o mod existente** `Pirates of MCA`, preservando jogabilidade, IDs, assets, variantes e recursos já aprovados. Não declare correção sem compilar, iniciar o jogo, capturar e comparar visuais reais.

## Materiais obrigatórios
- `piratesmca-real-assets-v11.0.0-alpha.15.zip`: assets reais atuais. V11 tem 20.133 arquivos de runtime, 17 `.bbmodel` editáveis, 1.091 definições de equipamento, 20 skins. A pasta Java do renderer não foi fornecida nesta auditoria — localize a implementação no repositório.
- `Pirate_Atelier_CleanRoom_Player_3D_EMF_26.2_v5.zip`: referência da produção anterior, incluindo `development_assets/cem_wardrobe/`, 8.565 `.jem` de desenvolvimento e atlases de materiais.
- `piratesmca-armor-0{1..4}-of-04.zip`: 3.273 screenshots da versão em execução; 1.091 conjuntos × 3 perspectivas.
- `piratesmca-skins.zip`: 60 screenshots de 20 skins.
- `resourcepacks(1).zip`: coleção de 45 resource packs. A coleção não informa seleção e precedência ativa: obtenha arquivo/configuração reais do launcher antes de concluir conflitos.
- `DIAGNOSTICO_COMPLETO.md` e `inventario_diagnostico_3333_imagens.csv`: diagnóstico rastreável por skin, armadura, variante e screenshot.

## Achados auditados — não ignore
1. As texturas PNG em geral **existem**. A checagem das 1.091 definições `assets/lfpirates/equipment/*.json` e `assets/pirateatelier/equipment/*.json` encontrou **0 referências a PNG ausentes**. Não perder tempo inventando correções de caminhos sem reproduzir um erro de atlas.
2. Modelos 3D CEM estão na biblioteca `development_assets` do pacote original e **não** estão disponíveis como `.jem` no assets runtime v11. As camadas `humanoid`/`humanoid_leggings` em `equipment/*.json` são mapas 2D, não modelos vestíveis. Antes de alterar texturas, instrumente a renderização e confira a seleção efetiva da geometria de chapéus, casacas, ombreiras, cinturões, botas, ganchos e pernas de pau.
3. O pacote v11 preserva 19978 arquivos comuns idênticos ao Atelier v5 e altera somente 17 descritores JSON comuns. Não alegue que todos os modelos e PNGs foram recriados; compare art/UV diretamente com os assets.
4. Em **19/20** capturas de skin, a roupa observada é o mesmo uniforme vermelho com emblema, embora os 20 PNGs sejam distintos. A exceção `deckhand_f` aparece listrada, mas não comprova mapeamento integral da skin. Audite camadas MCA, armadura inicial do catálogo, humanoid render layer, seleção de textura e ausência de troca de skin em runtime. Capturar cada skin sem roupas/armaduras e depois adicionar as camadas uma por vez.
5. Capturas de armadura mostram tampas/capacetes cúbicos cobrindo a face, casacos/ombros sem espessura adequada, falta de aba/volume de chapéu e acessórios aplicados apenas como textura. Leia **cada entrada** do CSV e sua referência `front,side,back`; use `DIAGNOSTICO_COMPLETO.md` para apontamentos individuais por 118 arquétipos e 29 conjuntos legados.
6. `FA+Player-v1.1.zip` contém `assets/minecraft/emf/cem/player.jem`, `player_slim.jem`, `a_player_equipment.jpm` e overrides de camadas de armadura. Esses recursos podem mudar transformações do personagem. Investigue integração com as poses pós-EMF; não declare que o pack é culpado sem teste A/B. Fresh Animations base e FA+All Extensions também estão no acervo, mas não comprovadamente ativados.

## Trabalho obrigatório em fases, com commit por fase

### Fase 1 — Auditoria reprodutível, sem correções especulativas
- Inventariar src Java/resources, mixins, registro de skins de MCA, `ArmorRenderer`, `EquipmentLayerRenderer`, `PirateLimbsLayer`, classes de entidades, seleção de `equippable` e model parts de player. Localizar o caminho real de cada peça de armadura desde ItemStack → model/material/equipment asset → render layer → pose e atlas.
- Conferir slots individuais `head`, `chest`, `legs`, `feet` e ID do namespace `lfpirates`/`pirateatelier`; proibir substituição genérica de textura que atinja todos os itens.
- Reproduzir 2 grupos: `captain_armor_pristine_normal` e `fisherman_teal_pristine_peg_left`, mais skin `boatswain` e `deckhand_f`.
- Registrar logs com item ID, equipamento resolvido, arquivo/modelo, texture ID, pivô/pose usado, layer ativo, skin de MCA, player model default/slim, mod/pack efetivos e assertivas sobre cada etapa.
- Gerar relatório de causas com código/linha e prova. Não alterar os 4.000+ itens por substituição cega.

### Fase 2 — Fix geométrico e de UV de TODAS as armaduras
- Integrar geometria do Atelier como malhas corretas de runtime, a partir dos modelos `.jem`/`bbmodel` realmente correspondentes, sem posicionar dados de desenvolvimento em pasta inativa. Se EMF não oferecer seleção por item e slot com segurança, usar render layer Fabric própria que associe modelo a cada equipment ID.
- Articular chapéu e viseira ao osso `head`, tronco/casaca/cinto ao `body`, ombreiras e punhos aos ossos `left_arm`/`right_arm`, barras inferiores às pernas somente se justificado. Corrigir offsets/rotations e normalizar coordenadas de pivô; exportar UV com atlas específico e caixas sem faces invertidas/faltantes.
- Distinguir chapéu aberto de elmo fechado. Para todos os chapéus, deixar olhos/rosto visíveis sem z-fighting ou interseção; incluir abas com espessura e brim acima da testa. Revisar geometria 3D dos casacos, coletes, cinturões, bolsos, bainhas, jóias, botas, ombreiras, chapéus, bandanas e tapa-olhos, com nível de detalhe compatível com Minecraft.
- Não introduzir geometria exagerada no estilo de proporções impossíveis nem quebrar Fresh Animations ou skins slim. Normalizar UV e preservar cores/estados `pristine`, `tattered`, `weathered` em todos os 118 arquétipos e suas 9 combinações.

### Fase 3 — Perna de pau e gancho realmente articulados
- A prótese **não** deve ser um item solto no mundo nem fixada ao `player.yaw`/world-space. Vincule a matriz dos ossos apropriados **depois de atualizada a pose por EMF/FA+Player**, inclusive em MCA. Se `ModelPart` vanilla não for o modelo efetivamente animado, obter/adaptar o part efetivo do modelo renderizado; não copiar somente ângulos Euler do bípede vanilla.
- `peg_left` = esconder exatamente `left_leg` anatômica, skin overlay, roupa, camada de armadura/boot daquele lado e anexar prótese ao pivot `left_leg`. `peg_right` idem para a direita. `normal` não altera membros. A calça acima da amputação deve conservar transição visual e encaixe; o pé humano não pode sobrar. Corrigir ground contact e camada de profundidade.
- Ganchos substituem mão, não ficam apenas sobrepostos. Suporte lado direito/esquerdo, punho rotacionando em caminhada/ataque, holding items, bows e primeira pessoa quando viável. Preservar qualidade 3D/UV dos 17 `.bbmodel` reais.
- Ensaiar em idle, walk, sprint, crouch, jump, swim, crawl, boat seat, horse, attack, mine, eat, block, emote e sleep, com câmera e poses de FA+Player. Capturar série temporal, não apenas screenshot parado.

### Fase 4 — Corrigir as 20 skins individualmente
- Ler `data/piratesmca/atelier/skins.json` e as 20 imagens `assets/pirateatelier/textures/skins/*.png`. Inspecionar registro MCA e binding de textura; garantir que cada NPC receba a skin de referência sem uniforme/camadas de equipamento impostas por fallback.
- Renderizar cada skin nua em `front`, `side`, `back` e cabeça em close-up. Conferir coordenadas UV `64×64`, braços/segunda camada/slim e os quatro lados do rosto/cabelo. Na sequência, vestir peças MCA e armaduras do Atelier, sem perder face/cores/identidade.
- Registros prioritários: `boatswain`, `captain_armor_skin`, `captain_civilian`, `deckhand`, `deckhand_f`, `fisherman_turned_pirate`, `gunner`, `harbor_merchant`, `naval_officer_skin`, `rogue_navigator`, `ships_cook`, `smuggler`, `tavern_raider`, `the_cabin_boy`, `the_corsair`, `the_cursed_sailor`, `the_dock_rat`, `the_naval_deserter`, `the_privateer`, `weathered_sailor`. NÃO agrupar a correção somente em uma roupa universal.

### Fase 5 — Matriz de testes e resultado final
- Validar todas as 1.091 combinações de armadura em 3 vistas (3.273 imagens), mais 20 skins sem e com equipamento (mínimo 60 screenshots nua), com asserts de ID de render/texture/geometry. Comparar a screenshot final com a original e uma referência Blockbench do modelo esperado; destacar clipping, UV errado, faces faltantes, interseção de brim/rosto, cores alteradas e perna dupla.
- A/B do loader com `FA+Player` ativado/desativado e gameplay em servidor/cliente, com modelos MCA e player. Preservar compatibilidade MC26.2 Fabric e versão exata das dependências do projeto; não atualizar loader sem teste.
- Entregar `.jar` compilado e fontes, diffs por arquivo, relatório de problemas por outfit/skin, capturas antes/depois, testes automatizados, log de boot e lista de defeitos ainda abertos. Não marcar um item como corrigido só por iniciar o jogo.

## Restrições de confiabilidade
**Seja rigoroso:** classifique cada constatação como evidência visual, evidência estática de assets, relato do usuário, inferência, ou confirmação via jogo/código. Captura estática não prova sincronização de animação. ZIP de resource packs não prova prioridade ativa. Textura PNG válida não prova UV correto. Não alterar os 20 atlases base quando o problema está na roupa sobreposta. Faça a correção por causa raiz, não via remendo cosmético.
