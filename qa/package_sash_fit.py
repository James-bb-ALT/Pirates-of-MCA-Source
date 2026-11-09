"""Ship the targeted anchor correction with old/new geometry and actual client evidence."""
from pathlib import Path
import hashlib,json,re,shutil,zipfile
import xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='9.0.1-alpha.12';O=R/'delivery'/V
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';src=jar.with_name(f'piratesmca-{V}-sources.jar')
text=(R/'qa/results/sash-fit-build.log').read_text(encoding='utf-8');assert 'BUILD SUCCESSFUL' in text and 'All 64 required tests passed' in text
assert sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))==35
assert 'Ran 7 tests' in (R/'qa/results/sash-fit-python.log').read_text(encoding='utf-8')
assert 'RESULTADO: PASS' in (R/'qa/results/sash-fit-assets.log').read_text(encoding='utf-8')
first=json.loads((R/'qa/results/sash-fit-first-hashes.json').read_text());assert all(first[p.name]==sha(p) for p in [jar,src])
assert 'BUILD SUCCESSFUL' in (R/'qa/results/sash-fit-reproducible.log').read_text(encoding='utf-8')
editor=json.loads((R/'reports/sash-fit/blockbench/execution.json').read_text());assert editor['headless'] and not editor['page_errors'] and editor['results'][0]['status']=='PASS'
for kind in ['off','extensions']:
    d=R/'reports/sash-fit/final'/kind;m=json.loads((d/'manifest.json').read_text());assert m['version']==V and m['frames']==(18 if kind=='off' else 21)
    log=(d/'execution.log').read_text(encoding='utf-8');assert 'BUILD SUCCESSFUL' in log and f'QA_ARTIFACT_SHA256 {sha(jar)} ' in log
    for name,digest in m['files'].items():assert sha(d/name)==digest
    bounds=re.findall(r'newMaxY=([0-9.eE+-]+) hipY=([0-9.eE+-]+)',log);assert len(bounds)>=20
    assert all(float(a)<=float(b)-.2 for a,b in bounds)
    if kind=='off':assert log.count('QA_SASH_DETERMINISTIC_WAKE ')==2
    else:assert 0<float(re.search(r'QA_BETTERCOMBAT_PLAYER_SWING progress=([0-9.]+)',log)[1])<1
replay=R/'reports/sash-fit/fixed-replay';log=(replay/'execution.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in log and 'legacy_front_clips=1 corrected_clips=0' in log
assert f'QA_ARTIFACT_SHA256 {sha(jar)} ' in log
for n,h in json.loads((replay/'manifest.json').read_text())['files'].items():assert sha(replay/n)==h
with zipfile.ZipFile(R/'delivery/9.0.0-alpha.12/piratesmca-9.0.0-alpha.12.jar') as a,zipfile.ZipFile(jar) as b:
    assert set(a.namelist())==set(b.namelist())
    changed=[n for n in a.namelist() if a.read(n)!=b.read(n)]
    assert set(changed)=={'dev/arandu/piratesmca/client/SashLayer.class','fabric.mod.json'},changed
(R/'reports/sash-fit/production-diff.json').write_text(json.dumps({'changed':changed,'added':[],'removed':[]},indent=2)+'\n')
O.mkdir(parents=True,exist_ok=True)
for p in [jar,src,R/'reports/sash_red.md',R/'reports/sash_fit_9.0.1.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
status={'release':V,'targeted_issue':'SASH-FIT-01','result':'PASS_REPRODUCED_CASE','universal_certification':False,'jar_sha256':sha(jar)}
(O/'STATUS.json').write_text(json.dumps(status,indent=2)+'\n')
(O/'LEIA-ME.md').write_text(f'''# Pirates of MCA {V}

SASH-FIT-01 corrigido: faixa segue o quadril, sem ser arrastada pelo torso ao acordar.
Mesmos atores/câmera/genética reproduziram a falha antiga e confirmaram a correção.
Malha, PNG, equipamentos persistidos e dados do mundo não foram alterados.

Substitua só o JAR antigo, sem duas versões ou JAR GameTest. MC 26.2 / Java 25 /
Loader 0.20.1 / MCA 8.1.11+26.2 / Fish 26.2.2.0 e dependências anteriores.

```text
/give @s piratesmca:sash_red
/pirates preset nearest sash_red
/pirates preset nearest sash_clear
```

CHEST cosmético, sem defesa, sem substituir peitoral. Preset para capitão/OP,
pirata adulto próximo/visível e CHEST vazio. Cabeça/rosto continuam independentes.
Leitura completa: sash_fit_9.0.1.md. Shaders, modpack completo, todos os corpos e
profiling ainda não certificados. Não confundir correção do caso com aprovação universal.
Packs opcionais não redistribuídos; perfil pessoal não modificado.
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
