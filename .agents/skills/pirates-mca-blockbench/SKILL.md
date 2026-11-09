---
name: pirates-mca-blockbench
description: Modelar, validar e integrar roupas, armaduras e acessórios 3D editáveis do Pirates of MCA usando Blockbench, Fabric e EMF/ETF/Fresh Animations. Use ao criar ou editar .bbmodel, PNG, CEM .jem/.jpm, wearables, próteses (gancho/perna de pau), skins MCA, presets e depurar clipping/renderização. Não use para mudanças exclusivamente na IA social ou comércio sem impacto visual.
---

# Pirates of MCA — Blockbench workflow

## Propósito
Construir assets modulares e reproduzíveis sem sacrificar skin base, gênero, nome, idade, roupas MCA nem animações. Os concepts em `art/concepts/` são apenas referências; **não** comprovam que existem modelos prontos.

## 0. Entrada obrigatória
1. Leia `docs/GUIA_PIRATES_MCA_BLOCKBENCH_CODEX.md` e `definitions/asset_catalog.json`.
2. Consulte `references/rendering-and-formats.md` antes de escolher `.jem`, Java renderer ou modelo de item.
3. Se ainda não existe evidência do renderer do MCA alvo, **PARE a implementação dos assets dependentes** e faça a auditoria do Lote 0.
4. Não substituir pacotes Fresh Animations existentes nem sobrescrever classe/renderer sem isolar a alteração.

## 1. Selecionar render target com evidência
- Inspecione MCA 8.1.11+26.2 e o loader/renderer real do projeto fornecido, não APIs de outras versões.
- Verifique se o EMF reconhece a entidade MCA (ex.: logs e modelos exportados pelo EMF). Descubra IDs, hierarquia, pivôs e nomes reais.
- **Se** CEM funcionar com MCA, exporte `.jem/.jpm` e teste com Fresh Animations em prioridade de packs controlada. **Senão**, implemente render layer do Fabric que acompanhe o modelo animado MCA.
- `.bbmodel` é a **fonte editável**; não assuma que o arquivo é consumido pelo jogo.
- Sem evidência, marque `BLOCKED: renderer alvo não validado`. Nunca invente export compatível.

## 2. Produzir um asset por vez
1. Abra/construa a referência adulta **real** do NPC com o Blockbench. Salve `.bbmodel` versionado.
2. Crie malha voxel enxuta em grupos/pivôs compatíveis; preserve corpo e cabeça originais.
3. Faça UV explícito e PNG com alfa correto; documente dimensões e material.
4. Separe modelo de equipamento, textura MCA e render runtime. Para ganchos/pernas, implemente ocultação seletiva do membro correspondente sem apagar a pele inteira.
5. Exporte no formato **validado no passo 1**. Não trate export de Java Block/Item como prótese renderizável.
6. Atualize catálogo e preset por ID estável. Sem randomização no tick/render; persistir aparência por UUID.
7. Gere imagens de frente, perfil e costas e evidências com/sem EMF/ETF/Fresh; não afirmar compatibilidade sem execução.
8. Rode `python scripts/pmca/validar_assets.py --root .`.

## 3. Regras de modelagem
- Realizar primeiras peças na ordem: tricórnio → tapa-olho → faixa → gancho → perna de pau → armaduras.
- Blocos/cubos pequenos em quantidade razoável. Ombreiras e casacos não devem bloquear rotação.
- Estruturas dos pivôs são determinadas pelo modelo MCA real; **não** deduzir ossos só por nomes genéricos `left_arm`.
- Próteses devem suportar esquerda/direita e animações idle/walk/attack/swim.
- Tricórnios testados com cabelos e gêneros diferentes; roupa nunca oculta rosto nem skin de base.
- Evite um CEM genérico `villager.jem` como solução para MCA customizado.
- Conflitos de acessórios são resolvidos por slots **propostos no addon**, não nativos.

## 4. Anti-regressões (bugs reais do projeto)
- Pele invisível no spawn ou depois de relog/chunk unload.
- Corpo genérico/nude depois de afastar-se do NPC e voltar.
- Todos os NPCs compartilhando a mesma skin.
- Spawn feminino ignorando masculino explícito; bebês em comandos de adultos.
- Nome ausente, profissão alterada indevidamente, troca/roupa perdida depois do load.
- Cabeça/roupa aparecendo duplicada com Fresh Animations ou partes flutuando/clipping.
- Registro/trade não devem ser modificados para resolver rendering.

## 5. Gate de entrega
Produza `reports/<asset-id>.md` com: caminho do `.bbmodel`, PNG(s), target de export, caminho runtime, testes PASS/FAIL/BLOCKED, capturas, ambiente (MC/Fabric/MCA/EMF/ETF/FA), limitações e próximo passo. Um `.bbmodel` que abre no Blockbench **não** constitui teste in-game.

Leia sob demanda:
- `references/rendering-and-formats.md` para escolha do target;
- `references/quality-gates.md` para critérios de QA;
- `references/asset-workflow.md` para lotes e nomenclatura.
