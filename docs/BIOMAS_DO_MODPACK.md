# Biomas do modpack — 2026-11-03

Perfil lido: `C:\Users\victo\AppData\Roaming\ModrinthApp\profiles\Fabulously Optimized`. Nenhum arquivo desse perfil foi alterado.

Inventário estático: **344 JARs ativos**, **294 IDs com definição JSON**. Mods desativados `.jar.disabled` foram excluídos.

| Namespace | Definições JSON |
|---|---:|
| betterend | 27 |
| betternether | 23 |
| cinderscapes | 4 |
| climaterivers | 3 |
| fishofthieves | 1 |
| incendium | 8 |
| ksbiome | 7 |
| minecraft | 66 |
| nullscape | 3 |
| rawlands | 25 |
| streamsreflowing | 1 |
| terrablender | 1 |
| terralith | 95 |
| wilderwild | 30 |

## Biomas presentes no inventário abrangidos pela regra

- `fishofthieves:tropical_island`
- `ksbiome:deeper_cold_ocean`
- `minecraft:beach`
- `minecraft:cold_ocean`
- `minecraft:deep_cold_ocean`
- `minecraft:deep_frozen_ocean`
- `minecraft:deep_lukewarm_ocean`
- `minecraft:deep_ocean`
- `minecraft:frozen_ocean`
- `minecraft:lukewarm_ocean`
- `minecraft:ocean`
- `minecraft:snowy_beach`
- `minecraft:stony_shore`
- `minecraft:warm_ocean`
- `rawlands:mist_coast`
- `terralith:deep_warm_ocean`
- `terralith:gravel_beach`
- `wilderwild:warm_beach`

As tags Minecraft `is_ocean`/`is_beach` e comuns `c:is_ocean`, `c:is_deep_ocean`, `c:is_shallow_ocean`, `c:is_beach`, `c:is_stony_shores` são aceitas. Costa rochosa, KS deeper_cold_ocean, Rawlands mist_coast e a ilha tropical Fish têm entradas explícitas. A ilha tropical é uma extensão temática de compatibilidade; não é uma praia vanilla. O datapack pode ajustar `piratesmca:pirate_waters`.

## Limites

Biomas registrados por código ou JARs aninhados podem não aparecer nesta lista. A ordem efetiva dos datapacks e quais datapacks de saves estão habilitados não foram reproduzidas. Estes são recursos instalados/candidatos, não um dump do registro vivo do seu mundo.

Referências em tags sem definição JSON encontrada (não confundir com biomas confirmados):

- `still_life:arctic_beach`
- `still_life:arctic_deep_ocean`
- `still_life:arctic_shallow_ocean`
- `still_life:arid_beach`
- `still_life:cold_deep_ocean`
- `still_life:cold_shallow_ocean`
- `still_life:cold_tundra_beach`
- `still_life:mediterranean_beach`
- `still_life:savanna_beach`
- `still_life:subtropical_deep_ocean`
- `still_life:subtropical_shallow_ocean`
- `still_life:taiga_beach`
- `still_life:temperate_beach`
- `still_life:temperate_deep_ocean`
- `still_life:temperate_shallow_ocean`
- `still_life:tropical_beach`
- `still_life:tropical_deep_ocean`
- `still_life:tropical_shallow_ocean`
- `still_life:tundra_beach`

Seu perfil contém **piratesmca 3.0.0-alpha.6**; não está usando a entrega 4.0 nem a nova 5.0. Não instale dois JARs do addon juntos.

Também foi identificado `tidal-towns-2.0.jar`, com estrutura `joshie:village_ocean` restrita a `minecraft:deep_ocean`. Ela é candidata à nova integração genérica; não houve rodada de jogo com o pack completo de 344 JARs.

Lista completa: `qa/results/pack-biomes/biomes.csv`; origem de mods, estruturas e tags: `qa/results/pack-biomes/inventory.json`.
