"""Package a clearly BLOCKED visual candidate, preserving the actual test evidence."""
from pathlib import Path
import hashlib,json,re,shutil,zipfile
import xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='9.0.0-alpha.12';O=R/'delivery'/V
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';src=jar.with_name(f'piratesmca-{V}-sources.jar')
log=(R/'qa/results/sash-build.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in log and 'All 64 required tests passed' in log
assert sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))==35
assert 'Ran 7 tests' in (R/'qa/results/sash-python.log').read_text(encoding='utf-8')
assert 'RESULTADO: PASS' in (R/'qa/results/sash-assets.log').read_text(encoding='utf-8')
first=json.loads((R/'qa/results/sash-first-hashes.json').read_text());assert all(first[p.name]==sha(p) for p in [jar,src])
assert 'BUILD SUCCESSFUL' in (R/'qa/results/sash-reproducible.log').read_text(encoding='utf-8')
editor=json.loads((R/'reports/sash/blockbench/execution.json').read_text());assert editor['headless'] and not editor['page_errors'] and editor['results'][0]['status']=='PASS';assert len(editor['results'][0]['cubes'])==8
for kind in ['off','extensions']:
    d=R/'reports/sash'/kind;m=json.loads((d/'manifest.json').read_text());assert m['frames']==(18 if kind=='off' else 21)
    text=(d/'execution.log').read_text(encoding='utf-8');assert 'BUILD SUCCESSFUL' in text and f'QA_ARTIFACT_SHA256 {sha(jar)} ' in text
    for name,digest in m['files'].items():assert sha(d/name)==digest
    if kind=='extensions':assert 0<float(re.search(r'QA_BETTERCOMBAT_PLAYER_SWING progress=([0-9.]+)',text)[1])<1
with zipfile.ZipFile(R/'delivery/8.0.0-alpha.11/piratesmca-8.0.0-alpha.11.jar') as a,zipfile.ZipFile(jar) as b:
    assert not (set(a.namelist())-set(b.namelist()))
    changed=[n for n in a.namelist() if a.read(n)!=b.read(n)]
    allowed={'dev/arandu/piratesmca/PiratesOfMCA.class','dev/arandu/piratesmca/PirateWearables.class','fabric.mod.json','piratesmca.mixins.json','assets/piratesmca/lang/en_us.json','assets/piratesmca/lang/pt_br.json'}
    assert set(changed)<=allowed,changed
    added=sorted(set(b.namelist())-set(a.namelist()))
(R/'reports/sash/production-diff.json').write_text(json.dumps({'changed_existing':changed,'added':added,'removed':[]},indent=2)+'\n')
O.mkdir(parents=True,exist_ok=True)
for p in [jar,src,R/'reports/sash_red.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
status={'release':V,'certification':'BLOCKED','reason':'SASH-FIT-01 front occlusion observed; universal body fit not confirmed','automated_tests':'PASS','jar_sha256':sha(jar)}
(O/'STATUS.json').write_text(json.dumps(status,indent=2)+'\n')
(O/'LEIA-ME.md').write_text(f'''# Pirates of MCA {V} — candidata experimental

CERTIFICAÇÃO VISUAL BLOQUEADA: faixa parcialmente encoberta numa NPC MCA.
Leia sash_red.md; testes de estado PASS não aprovam o encaixe pendente.
Não é release final nem conjunto completo de armadura.

Para experimentar, substituir apenas o JAR antigo do addon; não manter duas versões.
Minecraft 26.2 / Java 25 / Loader 0.20.1 / MCA 8.1.11+26.2 / Fish 26.2.2.0.
Nova faixa na aba Pirates of MCA. Ocupa CHEST, sem defesa, sem sobrepor outro peitoral.
Pode coexistir com tricórnio/tapa-olho em HEAD. Não instalar o JAR GameTest.

```text
/give @s piratesmca:sash_red
/pirates preset nearest sash_red
/pirates preset nearest sash_clear
```

Preset para capitão/OP, pirata adulto próximo/visível e CHEST vazio.
clear continua limpando apenas cabeça/rosto; sash_clear limpa a cintura.
Fontes, PNG, export, testes, logs e 39 capturas finais nos ZIPs.
Packs/mods opcionais não redistribuídos; perfil pessoal não modificado.
Shaders, FO completo, todos os corpos, multiplayer e profiling não certificados.
''',encoding='utf-8')
def collect(names):return [p for name in names for p in (R/name).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.class','.pyc'}]
def archive(dest,files):
    with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(set(files)):
            i=zipfile.ZipInfo(p.relative_to(R).as_posix(),(2026,11,4,0,0,0));i.compress_type=zipfile.ZIP_DEFLATED;i.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16;z.writestr(i,p.read_bytes())
assets=collect(['models','textures','definitions','exports','scripts','reports','art'])
archive(O/f'piratesmca-{V}-assets.zip',assets)
files=assets+collect(['.agents','source','src','qa','docs','gradle','reference-jars','baseline'])+[R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md','SHA256SUMS']]
archive(O/f'piratesmca-{V}-workspace.zip',files)
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.name}\n' for p in sorted(O.iterdir()) if p.is_file() and p.name!='SHA256SUMS.txt'))
print(json.dumps(status))
