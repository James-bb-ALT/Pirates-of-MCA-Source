# Tapa-olho de couro — 8.0.0-alpha.11 — Lote 1 parcial

Data: 2026-11-04. Skill: `.agents/skills/pirates-mca-blockbench/SKILL.md`.
Novo asset entregue: tapa-olho, oito cubos, PNG original 64×64 e fonte editável.
Não são conjuntos de armadura. Faixa e três roupas base ainda pendentes do Lote 1;
armaduras completas pertencem ao Lote 3, conforme sequência do contrato inicial.

## Fontes e caminho real

- models/blockbench/face/eyepatch_leather.bbmodel — fonte MCA editável.
- textures/wearables/face/eyepatch_leather.png — fonte externa da textura.
- exports/fabric_renderer/eyepatch_leather.json — geometria MCA compilada.
- eyepatch_leather_player.json — export derivado com deslocamento Y de +2 unidades.
- tricorne_eyepatch.json — composição que referencia o tricórnio e o seletor de encaixe.
- Runtime: assets/piratesmca/{models/item,items,textures/item}.
- definitions/presets/eyepatch_mvp.json — mapeamento lógico e tags de spawn.

Rota: CustomHeadLayer real + adaptador de escala MCA já auditado, agora restrito
aos três itens cosméticos. Composite e context_entity_type são APIs nativas reais
do Minecraft 26.2, verificadas no bytecode. Seletor usa mca:male_villager e
mca:female_villager para o encaixe MCA; fallback de jogador mantém geometria própria.
Nenhuma skin base nem modelo Fresh foi copiado, substituído ou exportado no corpo.

HEAD e face são dois estados lógicos mapeados para um equipamento HEAD nativo.
Adicionar tapa-olho preserva o chapéu; retirar só o tapa-olho preserva o chapéu.
O composto contém as duas referências, sem duplicar malha do corpo/cabeça.
Isso não é inventário genérico de acessórios: não combina com capacete externo,
nem há suporte geral a ombros, próteses ou armaduras nesta entrega.

## Falha reproduzida e correção

EYE-08 inicialmente FAIL: patch na testa, apesar do build e da abertura no editor
passarem. A malha neutra de cabeça não revela a posição dos olhos na biblioteca de
skins MCA. Capturas em eyepatch/diagnostic-forehead preservam o erro e o log antigo.

A fonte MCA foi deslocada 2,6 unidades de modelo para baixo após inspeção do render.
O encaixe de jogador usa export derivado +2 em Y, selecionado pelo tipo de entidade.
Assim, o ajuste não desloca o tricórnio inteiro nem modifica o rosto. Regressão
automatizada verifica o centro da peça, o deslocamento entre exports, seleção dos
dois tipos MCA, UV opaco e composição sem duplicação. Capturas finais mostram um
olho coberto e o outro visível nos dois NPCs testados, sozinho e com tricórnio.

As câmeras iniciais também assumiam posições anteriores ao desmontar/dormir.
Fixture agora repõe os NPCs em coordenadas conhecidas antes dos close-ups; o cenário
limpa cama/água anterior. Isso corrige a evidência de QA, sem alterar o addon.

## Execução

| ID | Estado | Evidência/alcance |
|---|---|---|
| EYE-01 | PASS | Estados face/head, adição em ambas as ordens, remoção da face preserva chapéu; sem duplicar item |
| EYE-02 / TC-SPAWN-01/02/03 | PASS | Dois adultos/gêneros no cliente, identidade comparada antes/depois, pele/cabelo/roupa/nome preservados |
| EYE-03 | PASS | Preset inicial composto, NBT save/load e ausência de re-equipamento após clear |
| EYE-04 | PASS | Capacete de ferro e criança MCA comum não alterados |
| EYE-05 / TC-SAVE-03 | PASS OFF/ON | NPC equipado vai ao Nether e volta; identidade e composto preservados |
| EYE-06 / TC-SPAWN-05 | PASS OFF/ON | Unload confirmado por ausência dos UUIDs; recarga e snapshot do cliente iguais |
| EYE-07 / TC-SPAWN-06 | PASS limitado OFF/ON | Mundo reaberto em servidor integrado; dois UUIDs/identidades/HEAD iguais; não é restart do processo Minecraft |
| EYE-08 | PASS após FAIL | 12 vistas por configuração: dois gêneros × sozinho/composto × frente/perfil/costas; nove poses controladas do composto |
| EYE-09 | PASS | Blockbench Web 5.2.2 via Playwright headless, oito cubos e textura carregada; eyepatch/blockbench |
| EYE-10 | PASS limitado ON | Jogador visível sem/com composto e ataque Better Combat ativo, com FA Player |
| EYE-11 | BLOCKED | Todos os cabelos/skins, avatar MCA customizado do jogador, shaders, multiplayer, FO completo e profiling não certificados |

Minecraft 26.2 / Java 25 / Loader 0.20.1 / MCA 8.1.11+26.2 / Fish 26.2.2.0.
Build: **35 JUnit, 61 GameTests obrigatórios, cinco testes Python e validador PASS**.
Logs em qa/results/eyepatch-*.log; XML JUnit em eyepatch-junit. O validador do catálogo
comprova integridade de arquivos, não renderização. Catálogo continua exported e
animation_safe=false; não se infere certificação universal a partir dos fixtures.

OFF: sem EMF/ETF e só vanilla. ON: EMF 3.3.11, ETF 7.2.5, EMF Compat Core 2.3.2,
EMF Compat Better Combat 2.1.0, Better Combat 3.2.2, Player Animation Library 1.2.8,
Cloth Config 26.2.155; Fresh 1.11.2 + All Extensions 1.10.1 + Player 1.1 ativos.
Essa é a combinação completa solicitada, isolada do restante do perfil pessoal.
Arquivos opcionais não redistribuídos. Sem cliques no desktop nem uso do perfil
pessoal do navegador. Reabertura, poses e close-ups são controlados pela API.

Manifestos finais: eyepatch/off (23 imagens) e eyepatch/extensions (26 imagens).
Diagnósticos de testa estão separados; não contam como evidência final.
JAR testado: `588d26c84c06c8291ecc7b242981b6680e6b212128df17b83b75a89bc7181b2c`.
JAR e sources reempacotados com hashes idênticos; SHA256SUMS.txt na entrega.

## Instalar, editar e reproduzir

Substituir o JAR antigo, sem duas versões. Itens novos entram automaticamente na
aba Pirates of MCA. Equipar manualmente no jogador usa HEAD. Para NPC, presets
exigem capitão/OP e pirata adulto próximo/visível, sem substituir outro capacete.

```text
/give @s piratesmca:eyepatch_leather
/give @s piratesmca:tricorne_eyepatch
/pirates preset nearest eyepatch_leather
/pirates preset nearest eyepatch_clear
/pirates preset nearest clear
```

clear remove todos os cosméticos; eyepatch_clear remove apenas o rosto e preserva
o tricórnio. Não há receitas de composição/desmontagem nem defesa adicional.
Presets são a mesma ferramenta cosmética já existente, não consomem inventário.

```powershell
python scripts/pmca/build_eyepatch.py
python scripts/pmca/validar_assets.py --root .
.\gradlew.bat build --console=plain
.\gradlew.bat runProductionClientTest -PeyepatchReview --console=plain
.\gradlew.bat runProductionClientTest -PeyepatchReview -PextensionsReview -PcompatMods=qa/fixtures/tricorne-emf-extensions/mods --console=plain
```

Executar sequencialmente. Para OFF depois de ON, selecionar apenas vanilla em
run/eyepatch-test/options.txt. Fixture não distribuído é preparado pelo script
qa/prepare_emf_extensions_fixture.py a partir dos arquivos do usuário.
O gerador cria fonte apenas se ausente; edições existentes são preservadas no export.
Abrir fonte no editor: scripts/pmca/review_blockbench.py --model
models/blockbench/face/eyepatch_leather.bbmodel --count 8 --name eyepatch --out
reports/eyepatch/blockbench, com Python/Playwright/Chromium disponíveis.

Não houve migração do estado social nem alterações de comércio/profissão. Comparação
binária com 7.0.0 preserva os recursos e classes fora da alteração de equipamento.
Não há Git/remoto neste diretório; entrega local, sem commit/push/release remota.
