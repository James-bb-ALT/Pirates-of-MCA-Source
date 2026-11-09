# Auditoria visual dos assets atuais — 12.0.0-alpha.18

**Estado: trabalho pendente.** A matriz in-game foi executada; a aprovação visual geral falha porque a perna de pau continua pequena demais em relação ao concept. Os testes de persistência, UV visual completo e comparação Fresh Animations ON/OFF também continuam pendentes.

## Execução observável

Minecraft 26.2, Java 25, Fabric Loader 0.20.1, MCA 8.1.11+26.2. O cliente carregou EMF 3.3.11, ETF 7.2.5, Fresh Animations 1.11.2, FA+All Extensions 1.10.1, FA+Player 1.1, 3D Armor 0.9.4.2, SkinLayers3D 1.11.4 e Better Combat 3.2.2. O log confirma os três resource packs ativos durante as capturas. O ensaio foi feito num mundo integrado isolado com dois adultos MCA; todos os quadros vêm do renderer ao vivo.

Foram capturadas **434 imagens únicas**: 20 skins corporais nativas MCA, 20 skins extras de pirata, 10 famílias conceituais de armadura e as pernas esquerda/direita sem armadura superior. Cada amostra contém frente, costas, lado esquerdo, lado direito, caminhada, corrida e ataque. Para as pernas, a cena dedicada usa somente o item necessário para selecionar o lado; não equipa capacete, peitoral nem botas. O cliente confirmou `walkAnimation`, sprint e `attackAnim` antes de salvar cada pose. O estado real do renderer mostrou uma prótese ativa em cada lado. A contagem soma 140 + 140 + 70 + 14 quadros; as capturas de prótese da matriz principal foram excluídas para preservar a comparação isolada.

Comandos usados:

```powershell
.\gradlew.bat runProductionClientTest -PvisualAssetAudit -PcompatMods=qa/fixtures/mca-skin-3d/mods
.\gradlew.bat runProductionClientTest -PvisualPegAudit -PcompatMods=qa/fixtures/mca-skin-3d/mods
python scripts/pmca/validate_wardrobe.py
python scripts/pmca/validar_assets.py --root .
```

Os dois game tests terminaram `BUILD SUCCESSFUL`. A validação de geometria reportou 1.091 exports, 4.349 slots, 54.098 caixas e 324.588 faces; os quatro UVs de cada face estão dentro das texturas e há cinco máscaras fechadas intencionais. O inventário do catálogo também passou, mas **não é evidência de render**. A matriz visual e os logs da execução estão no [ZIP de evidências](visual-audit-12.0.0-alpha.18/piratesmca-visual-audit-current-assets.zip), com PNGs crus, recortes centralizados, hashes por captura e folhas de contato.

## Resultado visual por área

| ID interno | Resultado | Evidência e erro remanescente |
|---|---|---|
| VISUAL-MCA-SKIN-01 | PASS, escopo de render | As 20 skins corporais MCA foram renderizadas sem roupa nem armadura, nos quatro ângulos e três poses, com o gênero correspondente. Isto não cobre as 322 texturas de roupa como variações separadas. |
| VISUAL-PIRATE-SKIN-01 | PASS de captura; FAIL de cobertura | As 20 skins extras renderizaram nos mesmos ângulos e poses. `skins.json` contém 19 masculinas e apenas uma feminina (`deckhand_f`): faltam opções femininas para a maioria das famílias visuais do concept. As skins extras pintam rosto/cabelo no próprio PNG e o mixin cancela `FaceLayer`/`HairLayer`; a cabeça fica chapada em vez de usar cabelo volumétrico/expressões MCA. É uma diferença visível frente às referências, ainda que evite faces duplicadas. |
| VISUAL-ARMOR-FAMILY-01 | PASS de captura das 10 amostras | Capitão, imediato, intendente, invasor, duelista, artilheiro, brutamontes, oficial naval, fantasma amaldiçoado e guardião do tesouro renderizaram com o set isolado, quatro vistas e três poses. A amostra usa variante `pristine_normal` e NPC masculino. As 1.086 combinações completas e cinco capacetes extra não foram todas repetidas nesta matriz de movimento; desgaste, cores e ajuste feminino ficam pendentes. |
| VISUAL-PEG-FA-01 | **FAIL visual** | Fresh Animations ativo; ambos os lados têm item selecionado e parte prostética no estado de render. Mesmo sem botas ou armadura sobreposta, a madeira aparece como um toco muito curto e estreito sob a barra da calça, em vez de percorrer a parte inferior da perna como no concept `03_acessorios_proteses.png`. Falha visível parada, andando e correndo. O quadro prova o resultado; ainda não distingue se a escala/pivô errado vem do modelo `.bbmodel` ou da transformação `FIXED` usada pelo renderer. Não declarar a prótese aprovada até corrigir e recapturar. |
| VISUAL-UV-01 | PASS estrutural; BLOCKED visual exaustivo | A validação verificou limites de UV e seis faces por caixa. As amostras renderizadas não mostram textura ausente nem face magenta. Não houve inspeção manual pixel a pixel/seam a seam de 324.588 faces; a validação estrutural não prova alinhamento artístico sem emendas. |
| VISUAL-ANIM-01 | PASS de pose controlada | Estados de caminhada, corrida e ataque estavam ativos no cliente sob EMF/ETF/Fresh. O movimento foi dirigido por fixture em ticks, não por pathfinding autônomo. Ataque foi swing MCA desarmado; compatibilidade de ataques com Better Combat não foi reavaliada aqui. |

Folhas de contato geradas dos quadros reais:

![Skins nativas MCA](visual-audit-12.0.0-alpha.18/final/contact-sheets/mca-skin-front.jpg)

![Skins extras de pirata](visual-audit-12.0.0-alpha.18/final/contact-sheets/pirate-skin-front.jpg)

![Famílias de armadura](visual-audit-12.0.0-alpha.18/final/contact-sheets/armor-front.jpg)

![Perna de pau esquerda e direita em vistas e poses](visual-audit-12.0.0-alpha.18/final/contact-sheets/peg-all-views-and-poses.jpg)

## IDs do plano de testes

O plano `docs/01_PLANO_DE_TESTES.md` não tem casos próprios de Blockbench/armaduras. Para evitar transformar esta captura em aprovação de casos mais amplos:

- **TC-SPAWN-05 — BLOCKED nesta auditoria:** não houve viagem de 128 blocos nem comparação de UUID/roupa/cabelo antes e depois.
- **TC-SPAWN-06 — BLOCKED:** não houve fechamento/reinício/relogin.
- **TC-SPAWN-07 — PARTIAL:** Fresh e EMF estiveram ativos durante render real, mas não foi feito o ciclo 5/30/120 s nem `F3+T`.
- **TC-SPAWN-08 — BLOCKED:** o cenário ficou em dois espécimes controlados; não foi ensaiada diversidade aleatória em 48 NPCs.

Os IDs internos VISUAL-* acima acompanham somente a nova matriz visual. O `BUILD SUCCESSFUL` não altera os estados BLOCKED/FAIL do plano.

## Limitações abertas

1. Ajustar comprimento, largura, pivô e transformação da perna de pau; repetir a matriz esquerda/direita com Fresh Animations ativo e confirmar os quadros de caminhada/corrida/ataque visualmente.
2. Expandir o catálogo de skins femininas e decidir se os rostos/cabelos extras devem continuar pintados no PNG ou ganhar camada animada sem duplicar a face.
3. Inspecionar UVs e seams visualmente por todas as famílias, variantes desgastadas e corpos femininos; o teste atual só valida limites/estruturas de UV.
4. Capturar as variantes de armadura restantes em movimento, comparar Fresh Animations desligado e testar persistência após unload/relog/restart.
5. A rodada local não autenticou nos serviços online Mojang/Realms (401); o mundo integrado e as renderizações locais funcionaram. Isso não certifica um perfil online nem multiplayer.
