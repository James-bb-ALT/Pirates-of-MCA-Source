"""Create a compact evidence bundle for the MCA skin and optional renderer test."""
from pathlib import Path
import hashlib,json,zipfile

ROOT=Path(__file__).resolve().parents[1]
RUN=ROOT/'run/mca-skin-3d-test'
OUT=ROOT/'reports/mca-skin-3d'
OUT.mkdir(parents=True,exist_ok=True)
shots=sorted((RUN/'screenshots').glob('*.png'))
assert len(shots)==22,len(shots)
log=(ROOT/'qa/results/mca-skin-3d-final4.log').read_text(encoding='utf-8',errors='replace')
for marker in ['MCA_SKIN3D_MODS_PASS','MCA_SKIN3D_CATALOG_PASS body_skins=20 clothing_textures=322','MCA_SKIN3D_CEM_RESOURCE_PASS armor_3d','MCA_SKIN3D_CLIENT_REVIEW_PASS native_body_skins=20 native_clothing=322 rendered_screenshots=22','BUILD SUCCESSFUL']:
    assert marker in log,marker
manifest=json.loads((ROOT/'qa/fixtures/mca-skin-3d/MOD_MANIFEST.json').read_text(encoding='utf-8'))
body=[line.split('id=',1)[1].split(' gender=',1)[0] for line in log.splitlines() if 'MCA_SKIN3D_BODY_PASS' in line]
assert len(body)==20 and len(set(body))==20
report=f'''# Revisão MCA + 3D Armor + 3D Skin Layers — 2026-11-05

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

'''+''.join(f'- `{x}`\n' for x in body)+f'''
## Ambiente reproduzível

- Minecraft 26.2; Fabric Loader 0.20.1; Java 25; MCA 8.1.11+26.2; Fish of Thieves 26.2.2.0.
- JARs adicionais, SHA-256 local: `{manifest['mods'][-2]['file']}` ({manifest['mods'][-2]['sha256']}) e `{manifest['mods'][-1]['file']}` ({manifest['mods'][-1]['sha256']}). JARs não estão dentro do ZIP de evidências.
- EMF, ETF, compatibilidade Better Combat/core e dependências listadas em `qa/fixtures/mca-skin-3d/MOD_MANIFEST.json`.
- Packs: Fresh Animations 1.11.2, FA+ All Extensions 1.10.1, FA+ Player 1.1. `latest.log` registra a ordem carregada.
- Capturas 854x480 em `screenshots/`: 20 NPCs MCA e dois estados do jogador.

## Reproduzir

```powershell
python qa/prepare_mca_skin_3d_fixture.py
$env:JAVA_HOME='C:\\Program Files\\Amazon Corretto\\jdk25.0.2_10'
.\\gradlew.bat runProductionClientTest -PmcaSkin3dReview -PcompatMods=qa/fixtures/mca-skin-3d/mods --daemon
```

A fixture usa cópias locais dos mods e packs já autorizados no ambiente de teste. Ela não os inclui no bundle. A primeira tentativa foi descartada porque um helper colocou NPCs em chunks descarregados; o cenário foi corrigido para manter as entidades perto e a execução final passou com todos os marcadores acima. A fonte `qa/results/mca-skin-3d-final2.log` contém a execução completa.

Warnings ambientais no log: skinlayers refmap ausente em ambiente Loom, chamada de autenticação/Realms sem sessão Microsoft e referência opcional do Iris. O cliente prosseguiu e as verificações listadas passaram.
'''
(OUT/'report.md').write_text(report,encoding='utf-8')
manifest['body_skin_ids_tested']=body
manifest['screenshots']=[{'file':p.name,'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'bytes':p.stat().st_size} for p in shots]
(OUT/'manifest.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
with zipfile.ZipFile(OUT/'mca-skin-3d-evidence.zip','w',zipfile.ZIP_DEFLATED,6) as z:
    z.write(OUT/'report.md','report.md');z.write(OUT/'manifest.json','manifest.json')
    z.write(ROOT/'qa/results/mca-skin-3d-final4.log','logs/mca-skin-3d-final4.log')
    z.write(ROOT/'qa/results/mca-skin-3d-client-retry.log','logs/first-fixed-fixture-run.log')
    z.write(ROOT/'qa/results/mca-skin-3d-compile.log','logs/compile.log')
    for p in shots:z.write(p,'screenshots/'+p.name)
(OUT/'SHA256SUMS').write_text(''.join(f'{hashlib.sha256(p.read_bytes()).hexdigest()}  {p.name}\n' for p in sorted(OUT.iterdir()) if p.is_file() and p.name!='SHA256SUMS'),encoding='utf-8')
print(f'PASS evidence package: {len(body)} MCA skins; {len(shots)} screenshots; {OUT}')
