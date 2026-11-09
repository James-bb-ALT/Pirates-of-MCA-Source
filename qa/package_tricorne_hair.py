"""Package verified hair/EMF fixtures; never infer player rendering from NPC results."""
from pathlib import Path
import hashlib,json,re,shutil,zipfile
import xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='6.0.2-alpha.9';O=R/'delivery'/V
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';src=jar.with_name(f'piratesmca-{V}-sources.jar')
build=(R/'qa/results/tricorne-hair-build.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in build and 'All 53 required tests passed' in build
assert sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))==35
assert sha(jar)=='4161cf4d88cce2bf887a35be4ef0c768f6c4c8bc14106cc5cd3aec72ef21ad12'
assert sha(src)=='45b99bf8dd6549004dafccc4ce8d1020b9454ad673a5a18bfb835bca11d488f3'
assert 'BUILD SUCCESSFUL' in (R/'qa/results/tricorne-hair-reproducible.log').read_text(encoding='utf-8')
for kind in ['off','fresh','extensions']:
    d=R/'reports/hair-review'/kind;log=(d/'execution.log').read_text(encoding='utf-8');manifest=json.loads((d/'manifest.json').read_text())
    assert 'BUILD SUCCESSFUL' in log and f'QA_ARTIFACT_SHA256 {sha(jar)} ' in log
    assert log.count('QA_HAT_RENDER_HAIR style=')==24 and manifest['frames']==22
    for name,digest in manifest['files'].items():assert sha(d/name)==digest,(kind,name)
player=R/'reports/hair-review/player-extensions';log=(player/'execution.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in log and 'QA_PLAYER_RENDER_DIAGNOSTIC invisible=false spectator=false' in log
assert f'QA_ARTIFACT_SHA256 {sha(jar)} ' in log
progress=float(re.search(r'QA_BETTERCOMBAT_PLAYER_SWING progress=([0-9.]+)',log)[1]);assert 0<progress<1,progress
frames=['0000_HAT-PLAYER-no-hat-idle.png','0001_HAT-PLAYER-hat-idle.png','0002_HAT-PLAYER-bettercombat-attack.png']
assert all((player/n).is_file() for n in frames)
(player/'manifest.json').write_text(json.dumps({'active_swing_progress':progress,'files':{n:sha(player/n) for n in frames+['execution.log']}},indent=2)+'\n')
with zipfile.ZipFile(R/'delivery/6.0.1-alpha.9/piratesmca-6.0.1-alpha.9.jar') as a,zipfile.ZipFile(jar) as b:
    assert set(a.namelist())==set(b.namelist())
    changed=[n for n in a.namelist() if a.read(n)!=b.read(n)];assert changed==['fabric.mod.json'],changed
(R/'reports/hair-review/production-diff.json').write_text(json.dumps({'before':'6.0.1-alpha.9','after':V,'changed_entries':changed},indent=2)+'\n')
O.mkdir(parents=True,exist_ok=True)
for f in [jar,src,R/'CHANGELOG.md',R/'reports/tricorne_mvp.md',R/'reports/tricorne_validation_6.0.1.md',R/'reports/tricorne_validation_6.0.2.md']:shutil.copy2(f,O/f.name)
(O/'LEIA-ME.md').write_text(f'''# Pirates of MCA {V} — checkpoint de validação

Substitua o JAR antigo, sem manter duas versões. Minecraft 26.2, Java 25,
Fabric Loader 0.20.1, MCA 8.1.11+26.2, Fish 26.2.2.0 e dependências anteriores.
Nenhuma mecânica nova neste incremento; ampliada a prova de compatibilidade visual.

Validação isolada inclui EMF/ETF, EMF Compat Core/Better Combat, Fresh base,
FA All Extensions e FA Player, mais dependências. Não certifica o modpack completo,
todos os cabelos, shaders, multiplayer ou desempenho. Leia tricorne_validation_6.0.2.md.
Não há redistribuição dos packs/mods opcionais. Não instale o JAR GameTest.

```text
/give @s piratesmca:tricorne_classic
/pirates preset nearest tricorne_classic
/pirates preset nearest clear
```

Preset: pirata adulto próximo/visível, cabeça vazia, capitão da crew ou OP.
Fonts/editáveis/scripts/logs/capturas nos ZIPs; hashes em SHA256SUMS.txt.
''',encoding='utf-8')
def collect(names):return [p for n in names for p in (R/n).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.class','.pyc'}]
def archive(dest,files):
    with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(set(files)):
            i=zipfile.ZipInfo(p.relative_to(R).as_posix(),(2026,11,4,0,0,0));i.compress_type=zipfile.ZIP_DEFLATED;i.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16;z.writestr(i,p.read_bytes())
assets=collect(['models','textures','definitions','exports','scripts','reports','art'])
archive(O/f'piratesmca-{V}-assets.zip',assets)
files=assets+collect(['.agents','source','src','qa','docs','gradle','reference-jars','baseline'])+[R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md','SHA256SUMS']]
archive(O/f'piratesmca-{V}-workspace.zip',files)
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.name}\n' for p in sorted(O.iterdir()) if p.is_file() and p.name!='SHA256SUMS.txt'))
print(json.dumps({'release':str(O),'sha256':sha(jar),'player_active_swing_progress':progress,'production_changed_entries':changed}))
