# Pirate Atelier v2 — Modelos CEM 3D vestíveis / Wearable library

**Arquivos originais produzidos para este projeto; nenhum pixel de Let's Forge Pirates copiado.**

## O que realmente foi gerado
- 864 estilos de equipamento com 4 modelos CEM por conjunto (helmet/chestplate/leggings/boots) = 3.456 .jem.
- 864 x 4 modelos alternativos com acessórios (gancho esquerdo/direito e tapa-olho esquerdo/direito) = 3.456 .jem.
- 864 atlas de 128×128 de materiais específicos para CEM e modelos 3D com volumes de aba, chapéu, bainha, lapela, metal e madeira.
- Geometria referenciada pelos **ossos** head/body/right_arm/left_arm/right_leg/left_leg (juntas de animação do player).

## Compatibilidade EMF + FA Player Extension

A compatibilidade arquitetural foi planejada, mas **não validada em gameplay**. Não há `player.jem` nesta versão, então o arquivo de animação do FA Player Extension não é sobrescrito. Os modelos seguem o padrão CEM OptiFine/EMF: `texture`, `textureSize`, `models`, `part`, `boxes`.

Os .jem estão em `development_assets/cem_wardrobe/<nome>/...` como biblioteca. **NÃO são carregados automaticamente em jogo**: EMF procura `assets/minecraft/emf/cem/<slot>.jem` e precisa de lógica de seleção do modelo adequado ao item equipado para usar as 864 variações. Um cliente Fabric deve montar um renderer por slot ou utilizar uma solução de filtros verificadamente compatível com Minecraft 26.2. Os itens `pirateatelier:*` precisam ser registrados por um mod; um resourcepack sozinho não registra itens. Os nomes de arquivos per-slot são modelos candidatos, não rotas EMF ativadas automaticamente.

O arquivo `Pirate_Atelier_DEMO_EMF_FPA_26.2.zip` vem separado e fica em `development_assets/` para ser testado no Minecraft 26.2. Ele aplica UMA roupa 3D experimental a qualquer armadura equipada, por isso deve ser ativado APENAS para testar a renderização.

Limitações reais: o CEM de armadura não desativa automaticamente a geometria da mão/perna de skin de player. Para uma perna realmente substituída (sem perna humana duplicada) e gancho amputando a mão, é indispensável renderização/máscara do modelo do player por um mod Fabric. Modelos de capa/skins com animações FPA podem divergir nas poses de geometria sobreposta; validar correndo, nadando, atacando e em primeira pessoa.

## Entradas úteis

`development_assets/cem_wardrobe/pirate_crimson_weathered_peg_right/` — conjunto 3D de exemplo.
`assets/pirateatelier/textures/entity/cem/pirate_crimson_weathered_peg_right.png` — atlas da textura desse conjunto.
`manifests/variants.json` — ids do item e do equipamento correspondente.

## Documentação EMF
https://github.com/Traben-0/Entity_Model_Features/blob/master/FEATURES.md
https://github.com/Traben-0/Entity_Model_Features/blob/master/.github/emf_part.txt
