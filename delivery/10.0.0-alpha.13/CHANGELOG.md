# Changelog

## 10.0.0-alpha.13 — 2026-11-04 — uniforme de marinheiro / Lote 1 parcial

Nova etapa de release conforme classificação do usuário: nova roupa e presets reversíveis.

- Camisa listrada, calça remendada e botas como overlay original 64×64 na camada
  nativa MCA; gênero neutro, preservando pele, genética, rosto, cabelo e equipamento.
- Molde reutilizável `deckhand_outfit` na aba criativa; uso apenas em pirata adulto
  da própria tripulação ou por OP, próximo e visível.
- Comandos `pirates outfit nearest deckhand|restore`; roupa anterior e seu bloqueio
  persistem em tags nativas. Reparo limitado à roupa inválida/ausente; escolha válida
  posterior do editor MCA prevalece. Sem nova versão de SavedData.
- Fonte Blockbench editável, PNG com cabeça/mãos transparentes e export MCA;
  testes de gênero, identidade, restauração, NBT e proteção de villagers/crianças.
- Sem substituir classes MCA, packs Fresh, armaduras ou as 48 roupas históricas.
- Evidência e limites desta peça: `reports/deckhand.md`. Demais roupas e armaduras pendentes.


## 9.0.1-alpha.12 — 2026-11-04 — correção do encaixe da faixa

Minor conforme classificação do usuário: correção incremental.

- Causa isolada com os mesmos UUIDs, roupas, câmera e genética: depois de acordar,
  o offset/pitch transitório do torso levava a faixa para dentro da região das pernas.
  A hipótese inicial de problema específico do corpo feminino não se confirmou.
- Faixa segue o centro dos pivôs reais do quadril e mantém o yaw do torso, sem
  herdar seu deslocamento/pitch de respiração/despertar. Malha e textura preservadas.
- Regressão usa os vértices reais do item resolvido: compara a âncora antiga com
  a corrigida, exige margem acima do quadril e reproduz a pose medida, sem depender de FPS.
- Replay do mundo QA antigo confirma SASH-FIT-01 corrigido no caso reproduzido;
  poses, Nether, chunks e reabertura testados OFF e com o stack EMF/Fresh completo.
- Não modifica genética, dados sociais, hitbox, roupa ou equipamentos persistidos.
- Limites de shaders, FO completo, todos os corpos/avatares e profiling permanecem.

## 9.0.0-alpha.12 — 2026-11-04 — faixa 3D / Lote 1 parcial

Nova etapa de release conforme classificação do usuário: novo modelo e render layer.

- Faixa vermelha original de oito cubos, fonte Blockbench/PNG editáveis e export.
- Layer Fabric vinculada ao root/body reais de MCA e AvatarRenderer; snapshot do
  item resolvido em extractRenderState, sem leitura do mundo durante submit.
- Equipamento CHEST cosmético, sem defesa e sem substituir peitoral ocupado;
  convive com tricórnio/tapa-olho no HEAD. Novo item na aba criativa automática.
- Presets sash_red/sash_clear preservam cabeça, rosto e identidade MCA.
- Três GameTests e dois testes Python novos, além de editor e cliente com stack
  EMF Compat/Fresh completo, poses e persistência.
- Limite visual identificado: parte frontal da faixa pode ficar encoberta em
  corpos MCA com maior projeção de peito. Asset exportado, sem certificação de fit.
- Três roupas base e conjuntos completos de armadura continuam pendentes.

## 8.0.0-alpha.11 — 2026-11-04 — tapa-olho modular / Lote 1 parcial

Nova etapa de release conforme classificação do usuário: novo modelo/acessório.

- Tapa-olho de couro original, oito cubos, fonte Blockbench editável e PNG 64×64.
- Uso sozinho ou junto do tricórnio através de item model composite nativo;
  nenhum modelo de cabeça/skin/Fresh copiado ou substituído.
- Encaixes distintos para NPC MCA e jogador vanilla, selecionados pela propriedade
  real context_entity_type. Corrigida posição inicial na testa, reproduzida em captura.
- Presets mantêm cabeça e rosto logicamente separados; adicionar/remover tapa-olho
  preserva o chapéu. Não sobrescreve capacetes de outros mods. HEAD/NBT persistente.
- Novos itens entram automaticamente na aba criativa Pirates of MCA.
- Modelo aberto no Blockbench via Playwright isolado; três GameTests e dois testes
  Python novos, poses e vistas de dois gêneros, stack completo EMF/Fresh e lifecycle.
- Lote 1 ainda parcial: faixa e três roupas base pendentes; conjuntos de armadura
  completos continuam no Lote 3. Sem atributos de defesa para os cosméticos.

## 7.0.0-alpha.10 — 2026-11-04 — aba criativa e ovos de pirata

Nova etapa de release conforme classificação do usuário: novas features.

- Aba “Pirates of MCA”, com bandeira, tricórnio e todos os itens registrados no
  namespace do addon; novos itens futuros entram automaticamente, sem duplicação.
- Ovos de pirata adulto masculino e feminino, com nomes em português/inglês,
  também disponíveis na aba vanilla de ovos. Reutilizam o modelo vanilla de ovo.
- Spawn nativo MCA: idade adulta, gênero explícito e marca de pirata; identidade,
  roupa e profissão seguem a inicialização existente. Dispensers preservam os dados.
- Correção específica dos ovos do addon ao clicar num adulto: o caminho vanilla
  de filhotes ignorava ENTITY_DATA. Usado spawn normal do item; ovos comuns mantêm
  comportamento vanilla. Teste reproduzível antes/depois e regressão de isolamento.
- Testes de registro/conteúdo, uso dos dois ovos, interação com adulto, dispenser,
  isolamento vanilla e tela criativa real, incluindo o stack EMF completo solicitado.
- Não modifica MCA, packs Fresh, dados de tripulações ou registros existentes.

## 6.0.2-alpha.9 — 2026-11-04 — cabelos e stack EMF do usuário

Minor conforme classificação do usuário: melhoria incremental de validação.

- Matriz de oito texturas de cabelo reais, dois gêneros e três vistas; snapshot
  do renderer confirma a camada efetiva, além do ID sincronizado.
- Corrigido falso positivo do teste: setHair(String) não atualiza as cinco camadas
  desenhadas no MCA 8.1.11; fixture usa HairStyle.singleLayer/setHairStyle e restaura
  as camadas originais. Rodadas anteriores reclassificadas como diagnóstico.
- Cenário isolado dos três packs Fresh base/All Extensions/Player e dos mods
  EMF Compat Core/Better Combat, com dependências reais e configurações copiadas.
- Gate de packs ativos, captura de encaixe seco na pose de natação e câmera de sono.
- Jogador testado sem/com chapéu e em golpe ativo do Better Combat com FA Player;
  corrigidas invisibilidade do fixture e espera que aceitava progresso de repouso.
- Arquivamento exclui screenshots antigas e gera manifestos SHA256.
- Nenhuma nova mecânica ou asset de produção neste incremento. Classes/assets
  preservados; apenas metadados do JAR mudam. Compatibilidade completa continua
  dependente de interações, todos os cabelos, shaders e profiling.

## 6.0.1-alpha.9 — 2026-11-04 — validação isolada do tricórnio

Minor conforme classificação do usuário: melhoria incremental de QA.

- Blockbench Web 5.2.2 em Chromium headless/Playwright; tricórnio e referência
  abertos com textura, sem usar perfil pessoal nem controlar o desktop.
- Teste de cliente dedicado: nove poses controladas em dois NPCs, com e sem
  EMF/ETF/Fresh; snapshots de identidade e equipamento preservados.
- Cenário isolado de poses com limpeza de água/camas entre fases e capturas reais.
- Build real: 35 JUnit, 53 GameTests, três testes Python e validador de assets.
- Código/assets de produção preservados; no JAR muda apenas a versão nos metadados.
- Todos os cabelos, IA autônoma, shaders, modpack completo e profiling permanecem
  sem certificação. Capturas de natação/sono não bastam para excluir todo clipping.

## 6.0.0-alpha.9 — 2026-11-03 — tricórnio MVP / Lote 0

Nova etapa de release segundo a classificação do usuário; checkpoint experimental,
sem certificação visual completa. Não representa conclusão dos lotes 0–4.

- Auditoria do renderer MCA real; reaproveitamento de CustomHeadLayer com
  adaptação client-only à escala da cabeça, sem substituir o corpo ou Fresh.
- Tricórnio original de 15 cubos: fonte Blockbench 5.0, PNG 64×64, export e preset.
- Equipamento nativo HEAD persistente; preset adulto, comandos autorizados ao
  capitão/operador e proteção de capacetes preexistentes. Sem re-equipar por tick.
- Regressões de NBT, gênero/identidade, crianças, slot e export/UV/atlas.
- Falha gráfica de atlas encontrada no primeiro cliente e corrigida por usar
  textures/item; capturas preliminares não contam como aprovação gráfica.
- Consulte reports/tricorne_mvp.md para resultados efetivos, revisão no editor,
  animações pendentes e execução com/sem EMF/ETF/Fresh.

## 5.0.0-alpha.8 — 2026-11-03 — estruturas costeiras

Nova etapa de release segundo a classificação do usuário. Extensão funcional
solicitada após a especificação v2.0; o contrato histórico permanece preservado.

- Villagers criados durante a geração de estruturas registradas em praias/oceanos
  recebem a integração MCA Pirata. Abrange templates e villagers diretos via
  ServerLevel/WorldGenRegion, sem procurar estruturas a cada tick.
- Tags minecraft e c de praias/oceanos, costa rochosa e entradas opcionais do
  modpack auditado: KS Biomes, Rawlands e ilha tropical Fish.
- Tripulações separadas por dimensão, ID de estrutura e chunk de origem.
  O adaptador original de seapost preserva suas tags e suas tripulações existentes.
- Contexto limitado à geração, com restauração em finally: villagers comuns,
  reprodução, comandos e estruturas no interior continuam fora da conversão.
- Regressões novas cobrem biomas, origens separadas e isolamento. Resultados e
  limitações em docs/RELATORIO_COSTEIRO_5.0.md. FO completo não certificado.

## 4.0.0-alpha.7 — 2026-11-03 — bandeira e fundação

Nova etapa de release segundo a classificação do usuário; entrega parcial da
especificação, preservando os comandos e as funções anteriores.

- Bandeira colocável com modelo, receita, drop e tradução PT/EN.
- Menu nativo para fundar tripulação com dois adultos voluntários, vincular uma
  tripulação existente e emitir sete ordens coletivas.
- Ações revalidadas no servidor: alcance, visibilidade, bloco, autoridade e
  consentimento; uma bandeira não pode pertencer a duas tripulações.
- RALLY/GUARD usam a bandeira vinculada, com limites de distância e dimensão.
- SavedData v4 preserva a âncora e migra v3 com backup pré-migração separado.
- Novas regressões de domínio, servidor e cliente real. Resultados e limitações
  em `docs/RELATORIO_BANDEIRA_4.0.md`; não certifica FO nem profiling completo.

## 3.0.0-alpha.6 — 2026-11-03 — teste jogável parcial

Nova etapa de release segundo a classificação do usuário: implementação dos
requisitos funcionais pendentes. O checkpoint 2.1.0-alpha.6 permanece preservado.

- Diretivas de ordens com alvo e âncora persistentes; seleção individual, próxima
  ou de todos os membros carregados da própria crew.
- Imediato assume ordens rotineiras quando o capitão não está disponível, mantendo
  o cargo de imediato e a capitania definitiva original.
- SavedData/codec nativo, importação preservando Properties, escrita atômica e recuperação.
- Eventos com IDs e consequências persistentes; rumores por encontros e provocação causal.
- Rendição segura, fases/cadência de motim e retirada de feridos com destino do Brain.
- 31 métodos JUnit, 35 GameTests e cliente de produção com 48 NPCs passaram.
  Funcionalidades restantes, FO e profiling não foram certificados; ver relatório atual.

## 2.1.0-alpha.6 — 2026-11-03 — auditoria parcial

Classificação do usuário: **Major**, por mudanças amplas de comportamento em combate,
persistência e hierarquia. Não é uma declaração de conclusão da especificação v2.0.

- Build Gradle 9.5.1 / Loom 1.17.21 / Java 25 com Minecraft 26.2 real,
  Loader 0.20.1, dependências fornecidas verificadas por SHA256 e YACL obrigatório.
  Scripts legados de release agora encaminham ao build real; stubs não validam release.
- Apoio ao motim exclui capitão NPC, mortos e crianças. Conspiração começa antes
  do limiar necessário para ultimato; relógio do ultimato pausa com capitão ausente.
- Oficiais não acumulam posições; transferência, morte e promoção limpam o cargo.
  Sucessão determinística considera relações e vantagem institucional do imediato.
- Candidatura de sobrevivência exige vaga e apoio de metade dos adultos da crew;
  não substitui um capitão definitivo. `force` e `debug` exigem OP.
- Brigas limitam o golpe antes do dano a 25% da vida; fogo e efeitos nocivos da
  própria arma são neutralizados no contexto do duelo. Dano externo continua letal.
  Guarda não transforma disputa comum em fogo amigo; contramestre separa ambos.
- Estado de runtime separado por servidor e limpo no unload/stop. Morte ambiental
  retira NPC do roster e dispara sucessão; capitão descarregado não é tratado como morto.
- Reparo visual não rerandomiza genética válida, não sobrescreve roupa MCA bloqueada,
  nem reaplica personalidade/humor a cada segundo. Relações MCA existentes são preservadas.
  Roupas piratas ficam restritas à profissão própria, sem contaminar pescadores comuns.
- Persistência grava por arquivo temporário com flush, substituição atômica quando
  suportada e backups. Falha de escrita não marca estado como salvo; schemas e
  referências inconsistentes são rejeitados preservando o original.
- Testes JUnit, regressões originais, GameTests reais de servidor e cliente,
  evidências visuais e fixture FO oficial com verificação de hashes.

As funcionalidades pendentes e cenários não executados estão no relatório de execução.
Os documentos originais e o JAR Alpha 5 permanecem como histórico.
