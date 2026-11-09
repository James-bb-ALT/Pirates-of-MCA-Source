# Pirates of MCA — Pipeline visual com Blockbench + Codex

**Versão do guia:** 1.0 • **Projeto alvo:** Minecraft Java 26.2 / Fabric / MCA Reborn 8.1.11+26.2 / EMF + ETF / Fresh Animations  
**Objetivo:** criar skins, roupas, armaduras e próteses piratas **modulares, editáveis, testáveis e animáveis**.

> **Estado dos materiais:** as três imagens em `concepts/` são *concept art*; **não** são modelos nem texturas prontas para uso. Os 3D, exports, compatibilidade com o renderer do MCA e validação in-game ainda precisam ser implementados.
>
> **Regra de engenharia:** trabalhar a partir do JAR/fonte real do MCA e do pipeline de renderização efetivo. EMF/Fresh Animations não tornam automaticamente renderizáveis as roupas e próteses de um NPC customizado. Verificar o alvo antes de escolher a rota de exportação.

## 1. Estratégia de produção

**Recomendação:** Codex como autor da infraestrutura, automação, modelos procedurais simples, validadores e integração; **Blockbench como editor e fonte de verdade visual** de cada `.bbmodel`. Trabalhar em lotes pequenos com aceitação visual e técnica.

### Quatro camadas

| Camada | Conteúdo | Fonte | Saída em jogo |
|---|---|---|---|
| A — Identidade | pele, rostos, cabelos, olhos, cicatrizes, barba | texturas nativas/MCA e variantes | renderização da pele/cabelo original, sem substituição global |
| B — Roupas 2D | camisas, calças, vestes finas, coletes leves | PNG com transparência/UV conforme MCA | roupas/overlays MCA, sem ocupar geometria adicional |
| C — Acessórios 3D | tricórnio, tapa-olho, ombreiras, gancho, perna de pau, bainha, papagaio | `.bbmodel` + texturas PNG | modelos vinculados a ossos/pivôs e render layers apropriadas |
| D — Armaduras 3D | 10 conjuntos completos e misturáveis | `.bbmodel`, texturas, definições de peças | renderização + atributos de armadura apenas quando houver item/equipamento real |

**Nunca** usar uma textura opaca de roupa para apagar a pele do MCA. Skins, overlays e geometria são assets distintos.

## 2. Ferramentas e formatos

- **Blockbench Desktop**: modelagem voxel, UV, pivôs, revisão do resultado e manutenção de projetos editáveis `.bbmodel`.
- **Codex CLI/IDE**: código de mod, registries, geração controlada de assets, revisão de UV, scripts, testes e relatórios.
- **VS Code/IDE Java**: modificações no Fabric mod (Loom real), testes e depuração do renderer.
- **Python 3 (opcional)**: script de inventário/validação fornecido neste kit (somente biblioteca padrão).
- **OptiFine Entity Model / CEM** no Blockbench: `.jem` / `.jpm` quando o alvo puder efetivamente ser tratado pelo EMF.
- **Modded Entity / Java e renderer Fabric**: alternativa para modelos específicos do MCA que não podem ser acrescentados com CEM sem sobrescrever o modelo/animador.
- **Java Block/Item Model**: adequado para itens/ícones/modelos de item; **não** transforma automaticamente um item em prótese vinculada ao braço ou perna de um NPC.

`.bbmodel` é um projeto interno JSON do Blockbench, **não é o JSON de modelo de Minecraft**. Na versão 5, o arquivo `groups` e a hierarquia `outliner` têm formato próprio. O parser deste kit só valida invariantes básicos: não o interpreta como formato de exportação estável.

### Rota de exportação (decisão obrigatória)

1. Examinar as classes de renderer e de modelo da entidade do **MCA 8.1.11+26.2**; identificar nomes das partes, variantes de gênero, roupas e pontos de encaixe.
2. Examinar os logs/exportação do **EMF** para conferir **se** as entidades MCA são reconhecidas e se existe template `.jem` correto para elas.
3. Se a substituição CEM for possível, criar um **pack de compatibilidade específico para MCA**, preservando os pivôs e as expressões das animações; evitar sobrescrever arquivos Fresh Animations não relacionados.
4. Se o MCA não expuser esse caminho, implementar **renderer de acessórios no Fabric** que usa os ângulos/transformações reais do modelo de NPC; não prometer compatibilidade automática com CEM.
5. Testar em jogo com EMF/ETF/Fresh ligados e desligados. A simples existência de um `.jem` não prova compatibilidade.

**Restrição:** modelos CEM podem substituir partes/animadores dependendo do target. Um pack genérico `villager.jem` não é uma solução universal para os NPCs customizados do MCA e pode gerar modelos duplicados ou quebrar animações.

Referências técnicas (consultar antes de automatizar):
- Blockbench `.bbmodel`: https://blockbench.net/wiki/docs/bbmodel/
- Formatos do Blockbench: https://blockbench.net/wiki/blockbench/formats/
- Plugins Blockbench: https://blockbench.net/wiki/docs/plugin/
- EMF: https://modrinth.com/project/entity-model-features
- EMF features/exports: https://github.com/Traben-0/Entity_Model_Features/blob/master/FEATURES.md
- CEM `.jem/.jpm`: https://optifine.readthedocs.io/cem.html
- Skills do Codex: https://learn.chatgpt.com/docs/build-skills

## 3. Arquitetura modular de vestuário

### Slots lógicos (propostos, não são slots nativos do MCA)

```text
head, face, neck,
shoulder_left, shoulder_right,
torso, back, waist,
hand_left, hand_right,
leg_left, leg_right, feet
```

Implementar **um registry de slots no addon** e uma camada de mapeamento para os pivôs e o equipamento renderizado de fato. Não assumir que o MCA ou EMF fornecem esses slots.

### Categorias

`civilian_outfit`, `crew_outfit`, `armor_set`, `prosthetic`, `accessory`, `cursed_variant`, `base_skin`.

### Distribuição (presets, não sorteio descontrolado)

`common`, `crew`, `officer`, `captain`, `elite`, `cursed`. Uma profissão Pirata pode ter visuais variados. **Cargo (capitão/imediato), perfil (briguento etc.), gênero, aparência e armadura são dimensões independentes.** Não atrelar gênero a categoria de roupa e não gerar todos os NPCs com a mesma skin base.

### Esquema mínimo de cada peça (contrato do addon, não formato nativo)

```json
{
  "id": "piratesmca:captain_tricorne_red",
  "category": "accessory",
  "slot": "head",
  "rarity": "captain",
  "model_source": "models/blockbench/head/captain_tricorne_red.bbmodel",
  "export_target": "custom_renderer",
  "export_path": "exports/placeholder/captain_tricorne_red",
  "texture": "textures/wearables/head/captain_tricorne_red.png",
  "variants": ["red", "navy", "black"],
  "requires": ["adult"],
  "tags": ["pirate", "tricorne"],
  "animation_safe": false,
  "status": "modeling"
}
```

O `export_target` somente será preenchido definitivamente após a análise do renderer; o JSON acima é **um exemplo**, não um arquivo válido do EMF/MCA. Marcar `animation_safe=true` só após testar animações e clipping.

### Presets compostos

```text
captain_preset_01 = skin_MCA + coat_burgundy + tricorne_gold + sash_red + boots_leather
boarding_raider_02 = skin_MCA + vest_rough + shoulder_plate + cutlass_scabbard
veteran_03 = skin_MCA + coat_weathered + eyepatch + hook_iron + pegleg_wood
```

**Hierarquia de camadas**: pele/genética/cabelo do MCA → roupa 2D transparente → itens/equipamento → peças 3D com pivôs → acessórios especiais. Resolver conflitos de slot/camadas por prioridade determinística. Persistir IDs/variantes, **não** randomizar roupa, pele ou gênero a cada reload.

## 4. Catálogo visual completo para o MVP

### Roupas civis e de tripulação (12 bases)

| ID sugerido | Conceito |
|---|---|
| `harbor_merchant` | comerciante do porto; colete vinho, bolsas, camisa clara |
| `smuggler` | contrabandista; casaco escuro, bandana, múltiplas correias |
| `rogue_navigator` | navegador; casaco azul, mapa e luneta |
| `tavern_raider` | briguento de taverna; colete gasto, faixa vermelha |
| `weathered_sailor` | veterano; remendos, gorro e capote azul escuro |
| `fisherman_pirate` | pescador que virou pirata; listras, rede, avental |
| `captain_civilian` | capitão à paisana; casaco longo e tricórnio |
| `deckhand` | marinheiro; bandana, camisa e calça remendada |
| `deckhand_alt` | variação com lenço e faixa de cintura |
| `ships_cook` | cozinheiro; avental, lenço e bolsas |
| `gunner` | artilheiro; correias, botas e luvas |
| `boatswain` | contramestre; cinto de ferramentas e casaco náutico |

Criar variantes de cor/corte para todos; **não** duplicar automaticamente texturas por gênero. Adaptar conforme UV, corpo e roupas reais do MCA. Para as skins base: variar rosto, cabelo, barba, cicatriz, tapa-olho e tons de pele sem substituir as bibliotecas de genética do MCA.

### Armaduras (10 conjuntos)

| Conjunto | Silhueta e peças 3D principais |
|---|---|
| `captain_armor` | tricórnio ornado, ombreiras, casaco vinho e dourado |
| `first_mate_armor` | casaco azul, ombro reforçado, cinturão e mapa |
| `quartermaster_armor` | couro, bolsos, livro-caixa, reforços discretos |
| `boarding_raider_armor` | jaqueta curta, placas de ombro, faixa vermelha |
| `duelist_armor` | roupa elegante, luvas, bainha, babados voxel discretos |
| `gunner_armor` | bandoleira, luvas, municiamento e proteção no braço |
| `bruiser_armor` | placas reforçadas, joelheiras e grandes manoplas |
| `naval_officer_armor` | uniforme azul-marinho, galões e insígnias |
| `cursed_ghost_armor` | metal/coral desgastado, runas e detalhes emissivos |
| `treasure_keeper_armor` | dourados, medalhões, placas e capa ornamentada |

Cada set deve ser subdividido em `head`, `torso`, `arms`, `waist`, `legs`, `feet`; visuais podem usar slots adicionais como ombro e costas. Atributos de defesa e dano dependem de registro de itens no mod (se desejado).

### Acessórios e próteses

- **Ganchos:** latão, ferro, naval, ornado, osso, amaldiçoado; mão esquerda e direita.
- **Pernas de pau:** madeira, ferro, reforçada, osso, amaldiçoada; perna esquerda e direita.
- **Cabeça/rosto:** tapa-olho, bandanas, lenços, tricórnios, gorros, chapéus emplumados, máscaras.
- **Ombros:** papagaio, ombreiras, medalhas, proteção de coral, porta-lanterna.
- **Torso/costas/cintura:** casacos, bandoleiras, bolsas, mochilas, bainhas, faixas, correntes, cinturões.
- **Utilitários:** lunetas, cantis, mapas, pingentes e medalhões; itens de mão com uso real requerem registro separado.
- **Sobrenaturais:** olhos emissivos, correntes, amuletos e detalhes de coral; aplicar emissivo apenas onde suportado.

**Próteses não são simples itens de mão.** Para gancho e perna de pau, fixar geometria à parte articulada correta e ocultar seletivamente a malha da mão/perna/roupa correspondente, sem apagar o restante da pele. Evitar duplicação de membros e preservar hitbox/colisão vanilla a menos que exista requisito contrário.

## 5. Modelagem no Blockbench

1. Criar **um exemplar oficial** por tipo de peça em Blockbench: `.bbmodel` válido e export compatível com o renderer escolhido.
2. Registrar **pivôs, escala, nome das partes, UV e textura** do exemplar validado (fonte de verdade). Não inventar coordenadas de corpo por suposição.
3. Construir cubos com estética Minecraft e baixo overhang, privilegiando poucos elementos, evitando geometria de centenas de cubos para acessórios triviais.
4. Manter grupos lógicos: `root`, `head`, `body`, `left_arm`, `right_arm`, `left_leg`, `right_leg` somente **quando corresponderem ao modelo real da entidade**; grupos adicionais seguem os pivôs corretos.
5. Salvar sempre `.bbmodel` editável; exportar em pasta separada; salvar PNG de textura com transparência válida.
6. Gerar preview de frente/costas/lado, comparar com concept art; validar com animações do jogo.
7. Não tratar o export de uma ferramenta como garantia de que o Minecraft consegue carregá-lo.

### Critérios geométricos

- Tricórnio: aba pequena, volumes cúbicos e espaço para cabelo.
- Ombreiras: não travar rotação do braço; largura configurável.
- Casacos: separação corpo/coxa; evitar atravessar pernas ao caminhar.
- Gancho: pivô do punho, alternativa left/right e teste de ataque/natação.
- Perna de pau: pivô correto de perna, sem duplicar a bota/malha original.
- Papagaio: suporte fixo em ombro, respeitando rotação do corpo.
- Mochila: profundidade suficiente para aparência, sem clip excessivo com capa.

## 6. Arquitetura de repositório recomendada

```text
project-root/
  .agents/skills/pirates-mca-blockbench/SKILL.md
  art/concepts/                  # referências PNG
  models/blockbench/             # FONTE: .bbmodel
    head/ face/ torso/ hand/ leg/ back/ armor/
  textures/                      # PNG fonte editável
    skins/ outfits/ wearables/ armor/
  definitions/
    asset_catalog.json           # inventário de produção
    outfits/ presets/ armor_sets/
  exports/                       # artefatos compiláveis, escolhidos após auditoria
    emf/ fabric_renderer/ items/
  reports/                       # previews, logs, clipping e validação
  scripts/pmca/                  # validadores e auxiliares
  src/main/...                   # código Java Fabric (projeto real existente)
```

O script `CONFIGURAR.ps1` instala uma skill no repositório e cria essas pastas **sem apagar arquivos existentes**. Selecione a raiz real do projeto fornecido ao Codex; não criar um segundo projeto Java em paralelo sem necessidade.

## 7. Backlog incremental

### Lote 0 — Auditoria do alvo (bloqueante)
- Mapear renderer, modelo, partes, camada de roupa e lifecycle do MCA.
- Confirmar se EMF reconhece a entidade e o caminho efetivo; comparar com Fresh Animations habilitado.
- Selecionar export target e criar um `reference_adult_mca.bbmodel` **a partir de referência real**, não gerado no escuro.
- Testar um chapéu simples sem substituir rosto/cabelo/roupa do MCA.

### Lote 1 — MVP visual
- Manifest/slots/loaders/presets e comandos de debug.
- 3 roupas 2D: `smuggler`, `deckhand`, `captain_civilian`.
- 3 acessórios 3D: tricórnio, tapa-olho, faixa.
- Testar gênero, idade adulta, skin base e persistência.

### Lote 2 — Diversidade de tripulação
- 9 roupas restantes e variantes, skins base sem destruir genética MCA.
- Gancho esquerdo/direito, perna de pau esquerda/direita, bainha, bolsa, lanterna, ombreira, bandana e mochila.
- Randomização determinística por UUID/seed e distribuição por cargo/perfil.

### Lote 3 — Armaduras modulares
- 10 conjuntos do catálogo, começando por capitão, imediato, artilheiro, brutamontes e amaldiçoado.
- Render/itens/atributos configuráveis, conforme arquitetura já validada.
- Compatibilidade real com EMF/Fresh, mix & match de slots, transparência e emissivos.

### Lote 4 — Polimento e QA
- Clipping e animação, benchmarks, textura e persistência em relog/reload/chunk unload.
- Migração de roupas antigas do mod sem restaurar a skin genérica e sem remover equipamento do jogador.
- Distribuição por raridade e tripulações; testes de regressão no Fabulously Optimized 14.1.1.

## 8. Critérios de aceitação — NÃO NEGOCIÁVEIS

1. Rosto, cabelo, gênero e skin MCA permanecem corretos imediatamente ao spawn e após afastar-se/voltar.
2. NPCs não aparecem invisíveis nem sem roupas depois do recarregamento.
3. Uniformes variam por NPC; mesma classe não impõe mesma aparência base.
4. Brigas, recrutamento e profissão não alteram roupa/skin por engano.
5. Gancho e perna de pau acompanham os pivôs durante idle, walk, run, attack, hurt, swim, sit e sleep.
6. Testar combinações em ambos os lados, incluindo próteses simultâneas; sem duplicação de braço/perna.
7. Chapéu compatível com cabelo e com diferentes formatos de cabeça/rosto.
8. Roupas não tornam a pele invisível (textura alfa e render layer corretos).
9. Em jogo **com e sem** EMF/ETF/Fresh Animations; declarar suporte real por alvo, não por intenção.
10. Save/reload, saída do chunk, respawn do NPC, restart, trocas de dimensão e atualização do recurso preservam o visual.
11. Sem atribuir gênero/idade aleatória novamente durante render/sync.
12. Medir performance com 4/12/24/48 NPCs. Evitar centenas de cubos/updates por tick em cada acessório.
13. Debug: listar entity UUID, gênero, idade/estado, skin, roupa, preset e slots/renderer usados.
14. Build real do mod com dependências reais do Minecraft 26.2 — **sem stubs como certificação**.
15. Não declarar compatibilidade com Fresh Animations sem capturas no jogo ou testes do renderer.

## 9. Instalação do kit no Windows

1. Extraia `PiratesMCA_Blockbench_Codex_Kit.zip` em qualquer pasta.
2. Em PowerShell, abra a pasta extraída e rode:

```powershell
.\CONFIGURAR.ps1 -Projeto "C:\CAMINHO\PARA\PiratesMCA"
```

3. Se Blockbench não estiver instalado, a instalação é **opcional e explícita**:

```powershell
.\CONFIGURAR.ps1 -Projeto "C:\CAMINHO\PARA\PiratesMCA" -InstallBlockbench
```

4. Para tornar a skill global (também disponível em outros repositórios):

```powershell
.\CONFIGURAR.ps1 -Projeto "C:\CAMINHO\PARA\PiratesMCA" -GlobalSkill
```

5. Abra o projeto no Codex e invoque a skill explicitamente:

```text
$pirates-mca-blockbench
```

6. Faça a validação local do inventário (Python 3):

```powershell
py -3 .\scripts\pmca\validar_assets.py --root .
```

A instalação do Blockbench usa `winget` com o ID `JannisX11.Blockbench` e não modifica o Java/Minecraft automaticamente. O script não instala plugins não auditados, não modifica arquivos do MCA e não faz commit/push.

## 10. Como delegar ao Codex

Antes de qualquer modelagem, peça ao agente para **executar o Lote 0**. Forneça o código-fonte atual do mod, o MCA e os JARs EMF/Fresh que você utiliza, além dos arquivos PNG deste kit. O comando `codex` não substitui a inspeção visual no Blockbench ou os testes de Minecraft.

### Prompt mestre (copiar para Codex)

> Utilize `$pirates-mca-blockbench` e leia `docs/GUIA_PIRATES_MCA_BLOCKBENCH_CODEX.md`, as três concept sheets e o catálogo. Crie um pipeline modular de roupas, armaduras e próteses 3D para Pirates of MCA no Minecraft 26.2. Comece auditando o renderer real do MCA e comprovando se os NPCs são alvo viável de CEM/EMF sem sobrescrever Fresh Animations; se não forem, use render layers Fabric compatíveis. Não altere a skin base/genética do MCA. Produza primeiro um modelo de tricórnio `.bbmodel` editável, export efetivo, textura PNG e preset de NPC; valide spawn adulto masculino/feminino, animação e persistência. Execute `scripts/pmca/validar_assets.py` e entregue relatório PASS/FAIL/BLOCKED. Somente depois avance para o Lote 1. Não invente evidência de testes no jogo.

## 11. Como usar a skill

- **Nome:** `pirates-mca-blockbench`.
- **Local no repositório:** `.agents/skills/pirates-mca-blockbench/SKILL.md`.
- **Local global opcional:** `$HOME/.agents/skills/pirates-mca-blockbench/SKILL.md`.
- **Quando acionar:** criar/editar `.bbmodel`, export de wearables, roupas MCA, armaduras, próteses, troubleshooting EMF/Fresh e validação de assets.
- A skill contém procedimentos específicos e um contrato de qualidade para impedir que o agente entregue modelos não renderizáveis como “prontos”.

## 12. Limitações assumidas

- As concept sheets são direção visual, não assets de produção.
- O sistema de slots e o JSON de exemplo são **contratos propostos para o addon**, não APIs nativas garantidas de Blockbench, MCA ou EMF.
- A compatibilidade EMF/ETF/Fresh para os NPCs próprios do MCA **depende da inspeção em runtime**.
- A geração procedural de `.bbmodel` precisa ser validada pelo Blockbench e por um modelo-base real; não existe conversão universal de PNG conceitual para armadura funcional.
- O kit prepara a infraestrutura de trabalho no computador do usuário; não executa Minecraft nem instala o mod por conta própria.
