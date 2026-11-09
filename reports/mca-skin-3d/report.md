# Revisão MCA + 3D Armor + 3D Skin Layers — 2026-11-05

## Resultado

| Item | Estado | Evidência |
|---|---|---|
| Inicialização integrada no cliente Fabric/Minecraft | PASS | Minecraft 26.2, Loader 0.20.1; execução real terminou `BUILD SUCCESSFUL` |
| JAR `3d-armor` carregado | PASS | mod id `armor_3d`, versão 0.9.4.2 |
| JAR `skinlayers3d` carregado | PASS | mod id `skinlayers3d`, versão 1.11.4 |
| EMF/ETF e Fresh Animations ativos junto dos dois JARs | PASS | `latest.log` lista recursos dos mods e os três packs Fresh |
| Catálogo nativo de skins corporais MCA | PASS | 20/20 renderizados; masculino/feminino; verificados contra `SkinVisualIds` e textura escolhida pelo `SkinLayer` real |
| Roupas MCA nativas | PASS | 322/322 IDs com gênero válido e PNG disponível no resource manager |
| Armadura pirata em corpos MCA | PASS | set de capitão equipado durante todas as 20 capturas; geometria e sync conferidos |
| Modelo CEM 3D Armor | PASS | `assets/minecraft/optifine/cem/outer_armor.jem` resolvido pelo resource manager EMF, origem `armor_3d`; jogador vestido com ferro foi capturado |
| Camadas 3D de pele do jogador | PASS com escopo limitado | captura sem armadura mostra o jogador e as camadas de camisa/mangas/calça com skinlayers3d ativo. Só a skin padrão da conta de teste foi revista; não cobre skins autenticadas personalizadas |
| Efeito dos dois mods no renderer próprio dos aldeões MCA | BLOCKED / fora do alvo | SkinLayers modifica PlayerRenderer. CEM da 3D Armor atende modelos vanilla via EMF; isso não substitui o renderer de VillagerEntityMCA. Os aldeões foram verificados com o renderer MCA e os itens de armadura do addon |

## Skins percorridas

- `mca:skins/skin/female/0.png`
- `mca:skins/skin/female/0_classic.png`
- `mca:skins/skin/female/1.png`
- `mca:skins/skin/female/1_classic.png`
- `mca:skins/skin/female/2.png`
- `mca:skins/skin/female/2_classic.png`
- `mca:skins/skin/female/3.png`
- `mca:skins/skin/female/3_classic.png`
- `mca:skins/skin/female/4.png`
- `mca:skins/skin/female/4_classic.png`
- `mca:skins/skin/male/0.png`
- `mca:skins/skin/male/0_classic.png`
- `mca:skins/skin/male/1.png`
- `mca:skins/skin/male/1_classic.png`
- `mca:skins/skin/male/2.png`
- `mca:skins/skin/male/2_classic.png`
- `mca:skins/skin/male/3.png`
- `mca:skins/skin/male/3_classic.png`
- `mca:skins/skin/male/4.png`
- `mca:skins/skin/male/4_classic.png`

## Ambiente reproduzível

- Minecraft 26.2; Fabric Loader 0.20.1; Java 25; MCA 8.1.11+26.2; Fish of Thieves 26.2.2.0.
- JARs adicionais, SHA-256 local: `PlayerAnimationLibMerged-1.2.8+mc.26.2.jar` (644aa199c5e905f098ead8b3d857e39a04712201871367129b36b144b12d13c8) e `skinlayers3d-fabric-1.11.4-mc26.2.jar` (a78a3964e3f6baa87c52cc2dec5d10cca13f3fdca2fd677eb769fcf3eb3684d6). JARs não estão dentro do ZIP de evidências.
- EMF, ETF, compatibilidade Better Combat/core e dependências listadas em `qa/fixtures/mca-skin-3d/MOD_MANIFEST.json`.
- Packs: Fresh Animations 1.11.2, FA+ All Extensions 1.10.1, FA+ Player 1.1. `latest.log` registra a ordem carregada.
- Capturas 854x480 em `screenshots/`: 20 NPCs MCA e dois estados do jogador.

## Reproduzir

```powershell
python qa/prepare_mca_skin_3d_fixture.py
$env:JAVA_HOME='C:\Program Files\Amazon Corretto\jdk25.0.2_10'
.\gradlew.bat runProductionClientTest -PmcaSkin3dReview -PcompatMods=qa/fixtures/mca-skin-3d/mods --daemon
```

A fixture usa cópias locais dos mods e packs já autorizados no ambiente de teste. Ela não os inclui no bundle. A primeira tentativa foi descartada porque um helper colocou NPCs em chunks descarregados; o cenário foi corrigido para manter as entidades perto e a execução final passou com todos os marcadores acima. A fonte `qa/results/mca-skin-3d-final2.log` contém a execução completa.

Warnings ambientais no log: skinlayers refmap ausente em ambiente Loom, chamada de autenticação/Realms sem sessão Microsoft e referência opcional do Iris. O cliente prosseguiu e as verificações listadas passaram.
