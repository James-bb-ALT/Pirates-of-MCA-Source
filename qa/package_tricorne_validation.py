"""Package the isolated-editor/pose validation checkpoint without changing old releases."""
from pathlib import Path
import hashlib,json,shutil,zipfile
import xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1]
V='6.0.1-alpha.9'
O=R/'delivery'/V
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar'
sources=jar.with_name(f'piratesmca-{V}-sources.jar')
assert 'BUILD SUCCESSFUL' in (R/'qa/results/tricorne-validation-build.log').read_text(encoding='utf-8')
assert 'All 53 required tests passed' in (R/'qa/results/tricorne-validation-build.log').read_text(encoding='utf-8')
assert sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))==35
editor=json.loads((R/'reports/blockbench-review/execution.json').read_text())
assert editor['isolated'] and editor['headless'] and not editor['personal_profile_used'] and len(editor['results'])==2
assert not editor['page_errors']
assert 'BUILD SUCCESSFUL' in (R/'qa/results/tricorne-validation-reproducible.log').read_text(encoding='utf-8')
assert sha(jar)=='f41a706efb6e60762cb8d31ddc1faa5369f96ad477053424808749c4d35ebb71'
assert sha(sources)=='81cc642912b6d8602aa635b8013ea33286f9a81e0ab27e822d8db1cbffad3f0b'
phases=['idle','head_turn','walk','run','attack','hurt','swim','sit','sleep']
for kind in ['off','fresh']:
    d=R/f'reports/pose-review/{kind}'
    log=(d/'execution.log').read_text(encoding='utf-8')
    assert 'BUILD SUCCESSFUL' in log and 'QA_HAT_POSES_COMPLETE count=9 entities=2 fixture_only=true' in log
    assert f'QA_ARTIFACT_SHA256 {sha(jar)} ' in log
    for i,phase in enumerate(phases):
        assert f'QA_HAT_POSE phase={phase} ' in log
        assert (d/f'{i:04}_HAT-POSE-{phase}.png').is_file()
    manifest={'version':V,'artifact_sha256':sha(jar),'controlled_fixture':True,'phases':phases,
              'files':{p.name:sha(p) for p in sorted(d.iterdir()) if p.is_file() and p.name!='manifest.json'}}
    (d/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
old=R/'delivery/6.0.0-alpha.9/piratesmca-6.0.0-alpha.9.jar'
with zipfile.ZipFile(old) as a,zipfile.ZipFile(jar) as b:
    assert set(a.namelist())==set(b.namelist())
    changed=[n for n in a.namelist() if a.read(n)!=b.read(n)]
    assert changed==['fabric.mod.json'],changed
(R/'reports/pose-review/production-diff.json').write_text(json.dumps({'before':'6.0.0-alpha.9','after':V,'changed_entries':changed},indent=2)+'\n')
O.mkdir(parents=True,exist_ok=True)
for p in [jar,sources,R/'reports/tricorne_mvp.md',R/'reports/tricorne_validation_6.0.1.md',R/'CHANGELOG.md']:
    shutil.copy2(p,O/p.name)
(O/'LEIA-ME.md').write_text(f'''# Pirates of MCA {V}

Validação incremental do tricórnio. Leia tricorne_mvp.md para limites e evidências.
Substitua o JAR antigo do addon, sem manter duas versões. Minecraft 26.2,
Java 25, Fabric Loader 0.20.1, MCA 8.1.11+26.2 e Fish 26.2.2.0.

```text
/give @s piratesmca:tricorne_classic
/pirates preset nearest tricorne_classic
/pirates preset nearest clear
```

Preset exige pirata adulto próximo/visível, cabeça vazia e capitão da crew ou OP.
Blockbench validado com Playwright headless, sem perfil pessoal nem cliques no desktop.
Nove poses controladas em dois NPCs, OFF e EMF/ETF/Fresh ON. Isso não certifica IA,
todos os cabelos, shaders, modpack completo nem desempenho. Nenhum novo asset além
do tricórnio foi iniciado. EMF/ETF/Fresh não são redistribuídos neste pacote.
''',encoding='utf-8')
def zipfiles(path,files):
    with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(set(files)):
            info=zipfile.ZipInfo(p.relative_to(R).as_posix(),(2026,11,4,0,0,0))
            info.compress_type=zipfile.ZIP_DEFLATED
            info.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16
            z.writestr(info,p.read_bytes())
def collect(names):
    return [p for name in names for p in (R/name).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.class','.pyc'}]
assets=collect(['models','textures','definitions','exports','scripts','reports','art'])
zipfiles(O/f'piratesmca-{V}-assets.zip',assets)
files=assets+collect(['.agents','source','src','qa','docs','gradle','reference-jars','baseline'])
files += [R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md','SHA256SUMS']]
zipfiles(O/f'piratesmca-{V}-workspace.zip',files)
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.name}\n' for p in sorted(O.iterdir()) if p.is_file() and p.name!='SHA256SUMS.txt'))
print(json.dumps({'release':str(O),'jar_sha256':sha(jar),'production_changed_entries':changed}))
