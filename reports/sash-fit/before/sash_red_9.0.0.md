# Faixa vermelha 3D — candidata 9.0.0-alpha.12 — Lote 1 parcial

Data: 2026-11-04. **Certificação visual BLOCKED: encaixe em todos os corpos ainda pendente.**
O código e os testes de estado passaram; isso não elimina a falha visual registrada.
Não é uma declaração de conclusão do addon nem dos conjuntos de armadura.

## Entregue e arquitetura

- Fonte: models/blockbench/torso/sash_red.bbmodel, oito cubos editáveis.
- PNG: textures/wearables/waist/sash_red.png, 64×64, original, alfa preservado.
- Export: exports/fabric_renderer/sash_red.json.
- Runtime: assets/piratesmca/{models/item,items,textures/item}/sash_red.
- Preset: definitions/presets/sash_mvp.json.
- Auditoria: renderer-audit/SASH_RENDER_TARGET.md e dumps reais MCA/Fabric/26.2.

SashLayer é registrada pela API Fabric apenas em renderers MCA e AvatarRenderer.
Segue root e body reais dentro da transformação de corpo/genética do renderer;
nunca usa a cabeça como âncora. Geometria vem do modelo FIXED do item, resolvido
em extractRenderState e guardado no snapshot do frame. submit não consulta entidade,
mundo ou aleatoriedade. Sem OpenGL direto, substituição de classes MCA ou modelo Fresh.

Item CHEST sem equipment asset de armadura e sem atributos de defesa. Preset nega
peitoral ocupado. A faixa convive com tricórnio/tapa-olho em HEAD; retirar a faixa
preserva a cabeça. Não existe slot de cintura nativo nem compatibilidade simultânea
com qualquer peitoral: CHEST é o mapeamento limitado deste MVP, documentado no preset.

## Resultados

| ID | Estado | Alcance/evidência |
|---|---|---|
| SASH-01 | PASS | Registro CHEST, equipment asset ausente e nenhum modificador de defesa |
| SASH-02 | PASS | Preset com cabeça/rosto; equipar/retirar sem alterar nome, roupa, cabelo, pele ou gênero |
| SASH-03 | PASS | NBT nativo preserva HEAD/CHEST e tags; sem re-equipamento depois de clear |
| SASH-04 | PASS | Peitoral de ferro e criança MCA comum preservados |
| SASH-05 | PASS limitado | Snapshot real resolve modelo em duas entidades; seis vistas e nove poses controladas, OFF/ON |
| SASH-06 / TC-SAVE-03 | PASS OFF/ON | Ida/volta Nether preserva identidade e CHEST no servidor |
| SASH-07 / TC-SPAWN-05 | PASS OFF/ON | UUIDs ausentes confirmam unload; recarga preserva identidade/HEAD no cliente e CHEST no servidor; capturas reais |
| SASH-08 / TC-SPAWN-06 | PASS limitado OFF/ON | Mundo reaberto no servidor integrado; identidade e equipamento preservados, capturas; não é restart do processo Minecraft |
| SASH-09 | PASS limitado ON | Jogador visível, faixa e cabeça equipadas; ataque Better Combat ativo com FA Player |
| SASH-10 | PASS | Blockbench Web 5.2.2, Playwright headless isolado, oito cubos/textura carregados |
| SASH-FIT-01 | FAIL observado / BLOCKED certificação | Parte frontal ficou encoberta numa NPC feminina do fixture ON; sem correção confirmada para toda morfologia |
| SASH-11 | BLOCKED | Todos os corpos/roupas, shaders, multiplayer, FO completo e profiling 4/12/24/48 não certificados |

Build real: **35 JUnit, 64 GameTests obrigatórios, sete testes Python e validador
de assets PASS**. Logs qa/results/sash-*.log e XML em sash-junit. O validador só
comprova integridade do catálogo; a biblioteca mantém exported/animation_safe=false.

OFF: sem EMF/ETF, pack vanilla. ON: EMF 3.3.11, ETF 7.2.5, EMF Compat Core 2.3.2,
EMF Compat Better Combat 2.1.0, Better Combat 3.2.2, Player Animation Library 1.2.8,
Cloth Config 26.2.155; Fresh base 1.11.2 + All Extensions 1.10.1 + Player 1.1 ativos.
O gate exige esses mods/packs carregados. Não é teste de todo o perfil pessoal.

Manifests em sash/off (18 capturas) e sash/extensions (21), com hashes e logs.
O frame 0010_SASH-0-front.png da rodada ON documenta a oclusão. A roupa MCA continua
presente e não foi apagada; a geometria é encoberta parcialmente. Na captura após
reabertura, a banda é visível. O encaixe depende de enquadramento/pose/morfologia;
a causa completa dessa variação não foi isolada, portanto não há correção confirmada.
Não desativei volumes do corpo, roupas ou packs para fabricar um PASS.

A [skill aplicada](../.agents/skills/pirates-mca-blockbench/SKILL.md) exige relatório
com resultado e limitações. Seu [quality gate](../.agents/skills/pirates-mca-blockbench/references/quality-gates.md)
determina “Registrar qualquer falha in-game e marcar release BLOQUEADA.” Por isso
esta entrega é candidata experimental, bloqueada para certificação visual completa;
os testes PASS acima mantêm apenas seu escopo, não aprovam o encaixe pendente.

## Instalar, testar e editar

Substituir somente o JAR antigo do addon, sem duas versões; não instalar GameTests.
Nova peça aparece automaticamente na aba Pirates of MCA. Não modifiquei o perfil
pessoal nem redistribuí os packs/JARs opcionais. Não foram usados cliques no desktop.

```text
/give @s piratesmca:sash_red
/pirates preset nearest sash_red
/pirates preset nearest sash_clear
```

Preset exige capitão/OP e pirata adulto próximo/visível, CHEST vazio. sash_clear
remove só a faixa. O comando clear anterior continua removendo apenas HEAD/rosto.
Sem receitas, defesa ou geração aleatória da peça. Player pode equipar no CHEST.

```powershell
python scripts/pmca/build_sash.py
python scripts/pmca/validar_assets.py --root .
.\gradlew.bat build --console=plain
.\gradlew.bat runProductionClientTest -PsashReview --console=plain
.\gradlew.bat runProductionClientTest -PsashReview -PextensionsReview -PcompatMods=qa/fixtures/tricorne-emf-extensions/mods --console=plain
```

Sequencialmente. OFF após ON: só vanilla em run/sash-test/options.txt. Fixture
opcional é preparado por qa/prepare_emf_extensions_fixture.py com os arquivos do
usuário. Gerador cria fonte apenas se ausente; preserva edições existentes.
Blockbench: scripts/pmca/review_blockbench.py --model models/blockbench/torso/sash_red.bbmodel
--count 8 --name sash --out reports/sash/blockbench, com Python/Playwright/Chromium.

JAR testado: `1f1987071ea1c8aaca05d0a6acb3755a1f97845267d4d4a1f1c1be512bbc3d25`.
JAR/sources reempacotados com mesmos hashes; manifesto na entrega e log
sash-reproducible.log. Dados sociais, comércio e recursos existentes preservados
por comparação binária; sem alteração da hitbox. Sem Git/remoto neste diretório.
Três roupas base e conjuntos completos de armadura continuam pendentes.
