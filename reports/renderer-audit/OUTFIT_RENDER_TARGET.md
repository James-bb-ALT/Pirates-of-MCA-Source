# Native MCA overlay — deckhand

Target audited: MCA Fabric 8.1.11+26.2 (reference JAR hash pinned by Gradle).
`VillagerEntityBaseModelMCA.bodyData` uses `PlayerModel.createMesh(..., false)`:
4-pixel arms for both genders. Female breast cuboid uses texOffs(18,21), 6×3×3;
front UV is x21..27/y24..27. See clothing-model-full.txt and clothing-validation.txt.

Route: `mca_overlay`, through native `ClothingList`, `setClothes`,
`VillagerVisuals.clothes()` and MCA ClothingLayer. Resource ID is
`piratesmca:outfits/deckhand.png` (raw asset path used by MCA, not an item atlas).
Gender 3 is neutral; `exclude:true` keeps this preset out of random clothing pools.
No new renderer, body mesh override, player armor registration or generic CEM.

Editable six-part .bbmodel is a neutral UV reference for the original overlay;
it is not consumed by Minecraft. Runtime uses MCA's own genetic geometry.
The PNG leaves head, face, hair, hands and unused outer layers transparent.
Existing native skin and animated model provide those parts. Source and runtime
PNG equality, hand-bottom transparency and female chest coverage have regressions.

Blockbench headless/WebGL proof is editor-only. Real client proof with both genders,
9 poses, OFF and complete optional EMF/Fresh stack is recorded in reports/deckhand.md.
Default genetics in those client fixtures are not coverage of every possible body.
