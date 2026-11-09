"""Package the real creative-tab release only after server and client evidence passes."""
from pathlib import Path
import hashlib,json,shutil,zipfile
import xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='7.0.0-alpha.10';O=R/'delivery'/V
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';src=jar.with_name(f'piratesmca-{V}-sources.jar')
log=(R/'qa/results/creative-tab-build.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in log and 'All 58 required tests passed' in log
assert sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))==35
assert 'RESULTADO: PASS' in log
for kind in ['off','extensions']:
    d=R/'reports/creative-tab'/kind;manifest=json.loads((d/'manifest.json').read_text());assert manifest['artifact_sha256']==sha(jar)
    for name,digest in manifest['files'].items():assert sha(d/name)==digest
    text=(d/'execution.log').read_text(encoding='utf-8');assert 'BUILD SUCCESSFUL' in text and 'QA_CREATIVE_COMPLETE eggs=2 adult_genders=2 real_screen=true' in text
first=json.loads((R/'qa/results/creative-tab-first-hashes.json').read_text());assert all(first[p.name]==sha(p) for p in [jar,src])
assert 'BUILD SUCCESSFUL' in (R/'qa/results/creative-tab-reproducible.log').read_text(encoding='utf-8')
with zipfile.ZipFile(R/'delivery/6.0.2-alpha.9/piratesmca-6.0.2-alpha.9.jar') as a,zipfile.ZipFile(jar) as b:
    removed=set(a.namelist())-set(b.namelist());assert not removed,removed
    changed=[n for n in a.namelist() if a.read(n)!=b.read(n)]
    allowed={'fabric.mod.json','piratesmca.mixins.json','dev/arandu/piratesmca/PiratesOfMCA.class','assets/piratesmca/lang/en_us.json','assets/piratesmca/lang/pt_br.json'}
    assert set(changed)<=allowed,changed
    added=sorted(set(b.namelist())-set(a.namelist()))
(R/'reports/creative-tab/production-diff.json').write_text(json.dumps({'before':'6.0.2-alpha.9','after':V,'changed':changed,'added':added,'removed':[]},indent=2)+'\n')
O.mkdir(parents=True,exist_ok=True)
for p in [jar,src,R/'reports/creative_tab_7.0.0.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
(O/'LEIA-ME.md').write_text(f'''# Pirates of MCA {V}

Substitua o JAR antigo do addon; não instale duas versões nem o JAR GameTest.
Minecraft 26.2 / Java 25 / Fabric Loader 0.20.1 / MCA 8.1.11+26.2 /
Fish 26.2.2.0 / Fabric API e YACL preservados.

No criativo, use as setas de páginas no topo do inventário para encontrar a aba
Pirates of MCA (ícone de tricórnio). Contém bandeira, tricórnio e dois ovos adultos.
Todos os futuros itens do addon entram automaticamente. Ovos também na aba vanilla.

```text
/give @s piratesmca:male_pirate_spawn_egg
/give @s piratesmca:female_pirate_spawn_egg
```

Os ovos criam NPCs MCA piratas, não tornam você capitão automaticamente.
Build e cliente OFF/stack EMF completo testados; relatório creative_tab_7.0.0.md.
Packs/JARs opcionais não redistribuídos; perfil pessoal não modificado.
Shaders, modpack completo, multiplayer e profiling continuam sem certificação.
Fontes, logs, capturas e testes no workspace ZIP. Hashes em SHA256SUMS.txt.
''',encoding='utf-8')
def collect(names):return [p for name in names for p in (R/name).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.class','.pyc'}]
files=collect(['.agents','source','src','qa','docs','gradle','reference-jars','baseline','models','textures','definitions','exports','scripts','reports','art'])
files += [R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md','SHA256SUMS']]
with zipfile.ZipFile(O/f'piratesmca-{V}-workspace.zip','w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
    for p in sorted(set(files)):
        i=zipfile.ZipInfo(p.relative_to(R).as_posix(),(2026,11,4,0,0,0));i.compress_type=zipfile.ZIP_DEFLATED;i.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16;z.writestr(i,p.read_bytes())
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.name}\n' for p in sorted(O.iterdir()) if p.is_file() and p.name!='SHA256SUMS.txt'))
print(json.dumps({'release':str(O),'sha256':sha(jar),'server_tests':58,'client_configs':['off','extensions']}))
