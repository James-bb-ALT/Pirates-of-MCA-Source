# Pirate Atelier – Clean-room asset set (Minecraft 26.2)

Original pixel art and voxel models, rebuilt independently using **only asset names,
category, texture dimensions, resource paths and conventions** from the supplied
Let's Forge Pirates archive as functional specifications. **No source image pixels,
JSON geometries, original PNGs, bytecode or proprietary models have been copied.**
The reference mod declares **All Rights Reserved**; this release contains only
original, newly authored artwork.

## What is included
- 107 new replacement sprites (all reference item PNG names, including eyepatch, hook and sabers).
- 58 new classic 64x32 armor layer atlases, corresponding to 29 original outfits/skulls.
- 24 new legacy 3D headwear overlay textures (hat textures only).
- Replacement item model JSONs for original wearable items plus **new 3D block-cuboid models** for hook and three original cutlasses. Modern display-context selectors show pixel-art icons in GUI, true voxel models in hands.
- 864 complete extended outfits: 8 themes x 12 colorways x 3 wear conditions x 3 leg configurations, each with 4 item icons, modern item JSONs, a 64x32 head/chest/arms atlas and a leggings atlas, plus 26.2-style equipment JSON.
- 5 additional sculpted 3D metal hooks, 7 additional 3D sabers, 8 eyepatches, 8 original modular 3D clothing/hat meshes, plus standalone geometry templates for pegs, hook hand, eyepatch and tricorn.
- Catalogue previews, variant metadata and the fully reproducible pixel-art source generator.

## Full outfit combinations
Styles: corsair, pirate, sailor, merchant, nordic, deckhand, fisherman, craftsman.
Palettes: crimson, navy, sea_blue, emerald, forest, plum, ochre, ivory, charcoal, copper, teal, wine.
Wear: pristine, weathered, tattered.
Leg variants: normal, peg_left, peg_right.
An example: `pirateatelier:sailor_plum_tattered_peg_right_leggings`.

## Directory conventions
`assets/lfpirates/` = drop-in resource *names* matching the old mod, where relevant.
`assets/pirateatelier/` = independent newly invented expansions. The code of the
running mod must register the latter as items and set its equipment asset ID.
`assets/*/models/item/` = legacy-style item geometry models.
`assets/*/items/` = 26.2 item model references.
`assets/*/textures/models/armor/` = legacy 64x32 armor sprite sheets.
`assets/*/textures/entity/equipment/humanoid/` and `humanoid_leggings/` = modern equipment textures.
`assets/*/equipment/<outfit>.json` = 26.2 equipment definitions.
`development_assets/geometries/` = Minecraft geometry templates intended for a future
player-feature attachment renderer, **not directly loaded by the resource pack**.

## HOW TO USE
### Reference mod (1.21.1 NeoForge)
This is a resource pack with `lfpirates` overrides, but `pack_format=88`
is targeted to Minecraft 26.2 (`min_format=max_format=88`). For 1.21.1,
remove those two fields and set `pack_format=34` first.
**Visual compatibility with the original NeoForge custom headwear renderer is not verified.**
Enable this pack ABOVE the mod's built-in resources. The original 3D hook/item
models are replaced with independently generated geometric alternatives.

### Minecraft 26.2 Fabric
The ZIP is a resource pack. It **does not port** the 1.21.1 NeoForge mod,
register new armor items or attach geometry to players by itself. The original
mod cannot be assumed compatible with 26.2 Fabric. In a new Fabric mod:
  1. Register `pirateatelier:` item IDs found in `manifests/variants.json`.
  2. For each piece set its `minecraft:equippable` component / equipment asset
     to the outfit `pirateatelier:<outfit>` and appropriate slot.
  3. Add a client-side player render layer for truly thin peg leg, arm mounted
     hook, 3D tricorn, and eyepatch; use the geometry templates as starting points.
  4. For held hook items the 3D JSON works with item rendering; this does NOT
     remove the player's real hand or permanently replace its geometry.
  5. Test in game and adjust display pose as needed (has not been runtime tested).

### Limits / deliberate exclusions
- No mobs, mob textures, cannons, muskets, decorative blocks, or special-world items.
- Peg variants have dedicated item sprites and geometry templates, but the
  legacy 64x32 leggings atlas uses standard pants: vanilla mirrors the same
  UV on both legs. A **one-sided peg leg** requires client renderer/model code
  to avoid accidentally giving both legs a wooden texture.
- Dynamic changes (torn variants, cosmetic swaps) and new inventory items require
  mod code or data-driven item registration; new assets alone cannot spawn items.
- All artwork is procedurally designed by shape/category and uses custom palette
  variations; hand-polishing selected styles in Blockbench is still worthwhile.
