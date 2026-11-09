"""Package only after actual model/editor/client/lifecycle evidence is complete."""
from pathlib import Path
import hashlib,json,re,shutil,zipfile
import xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='8.0.0-alpha.11';O=R/'delivery'/V
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';src=jar.with_name(f'piratesmca-{V}-sources.jar')
build=(R/'qa/results/eyepatch-build.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in build and 'All 61 required tests passed' in build
assert sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))==35
assert 'Ran 5 tests' in (R/'qa/results/eyepatch-python.log').read_text(encoding='utf-8')
assert 'RESULTADO: PASS' in (R/'qa/results/eyepatch-assets.log').read_text(encoding='utf-8')
first=json.loads((R/'qa/results/eyepatch-first-hashes.json').read_text());assert all(sha(p)==first[p.name] for p in [jar,src])
assert 'BUILD SUCCESSFUL' in (R/'qa/results/eyepatch-reproducible.log').read_text(encoding='utf-8')
editor=json.loads((R/'reports/eyepatch/blockbench/execution.json').read_text());assert editor['headless'] and editor['isolated'] and not editor['page_errors'];assert editor['results'][0]['status']=='PASS' and len(editor['results'][0]['cubes'])==8
for kind in ['off','extensions']:
    d=R/'reports/eyepatch'/kind;m=json.loads((d/'manifest.json').read_text());assert m['frames']==(23 if kind=='off' else 26)
    text=(d/'execution.log').read_text(encoding='utf-8');assert f'QA_ARTIFACT_SHA256 {sha(jar)} ' in text and 'BUILD SUCCESSFUL' in text
    for name,digest in m['files'].items():assert sha(d/name)==digest,(kind,name)
    if kind=='extensions':
        progress=float(re.search(r'QA_BETTERCOMBAT_PLAYER_SWING progress=([0-9.]+)',text)[1]);assert 0<progress<1
with zipfile.ZipFile(R/'delivery/7.0.0-alpha.10/piratesmca-7.0.0-alpha.10.jar') as a,zipfile.ZipFile(jar) as b:
    assert not (set(a.namelist())-set(b.namelist()))
    changed=[n for n in a.namelist() if a.read(n)!=b.read(n)]
    allowed={'dev/arandu/piratesmca/PirateWearables.class','dev/arandu/piratesmca/mixin/client/TricorneExtractMixin.class','fabric.mod.json','assets/piratesmca/lang/en_us.json','assets/piratesmca/lang/pt_br.json'}
    assert set(changed)<=allowed,changed
    added=sorted(set(b.namelist())-set(a.namelist()))
(R/'reports/eyepatch/production-diff.json').write_text(json.dumps({'changed_existing':changed,'added':added,'removed':[]},indent=2)+'\n')
O.mkdir(parents=True,exist_ok=True)
for p in [jar,src,R/'reports/eyepatch_leather.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
(O/'LEIA-ME.md').write_text(f'''# Pirates of MCA {V} — Lote 1 parcial

Substituir somente o JAR antigo do addon, sem duas versões. Não instalar GameTests.
Minecraft 26.2, Java 25, Fabric Loader 0.20.1, MCA 8.1.11+26.2, Fish 26.2.2.0,
Fabric API e YACL preservados.

Tapa-olho de couro novo, sozinho ou com tricórnio, na aba Pirates of MCA.
Os dois cosméticos compartilham o HEAD nativo; não acumulam com capacete externo.
Não há receitas nem atributos de armadura. Não são conjuntos completos de armadura.

```text
/give @s piratesmca:eyepatch_leather
/give @s piratesmca:tricorne_eyepatch
/pirates preset nearest eyepatch_leather
/pirates preset nearest eyepatch_clear
```

Preset para capitão/OP, pirata adulto próximo/visível; não muda skin, cargo ou gênero.
eyepatch_clear preserva o tricórnio. clear remove todos os cosméticos.
Fonte editável, PNG e export no assets ZIP; código/testes/logs/capturas no workspace.
EMF Compat Core/Better Combat + os três packs fornecidos foram testados isoladamente.
Não significa certificação do perfil completo, todos os cabelos, shaders ou desempenho.
Leia eyepatch_leather.md. Perfil pessoal e packs não modificados nem redistribuídos.
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
print(json.dumps({'release':str(O),'sha256':sha(jar),'server_tests':61,'python_tests':5,'client_frames':49}))
