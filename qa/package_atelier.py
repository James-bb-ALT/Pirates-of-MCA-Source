"""Gate/ship the real collection build, preserving full source provenance and actual test evidence."""
from pathlib import Path
import hashlib,json,shutil,zipfile,xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='11.0.0-alpha.15';O=R/'delivery'/V
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';src=jar.with_name(f'piratesmca-{V}-sources.jar')
log=(R/'qa/results/atelier-final-build.log').read_text(encoding='utf-8');assert 'BUILD SUCCESSFUL' in log and 'All 78 required tests passed' in log
assert sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))==35
assert 'PASS ATELIER:' in (R/'qa/results/atelier-static.log').read_text(encoding='utf-8')
assert 'RESULTADO: PASS' in (R/'qa/results/atelier-assets.log').read_text(encoding='utf-8')
assert 'Ran 9 tests' in (R/'qa/results/atelier-python.log').read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in (R/'qa/results/atelier-reproducible.log').read_text(encoding='utf-8')
first=json.loads((R/'qa/results/atelier-first-hashes.json').read_text());assert all(first[p.name]==sha(p) for p in [jar,src])
for mode in ['off','extensions']:
 d=R/'reports/atelier/release'/mode;m=json.loads((d/'manifest.json').read_text());l=(d/'execution.log').read_text(encoding='utf-8')
 for marker in ['BUILD SUCCESSFUL','QA_ATELIER_GUI_FIXED_MODELS count=8917','QA_ATELIER_PROSTHESES count=17','QA_ATELIER_SKINS_COMPLETE count=20','QA_SPAWN_EQUIPMENT_CHUNK_PASS count=12','QA_SPAWN_EQUIPMENT_REOPEN_PASS count=12',f'QA_ARTIFACT_SHA256 {sha(jar)} ']:assert marker in l,(mode,marker)
 assert all(sha(d/n)==h for n,h in m['files'].items())
 if mode=='extensions':assert 'QA_BETTERCOMBAT_PLAYER_SWING progress=' in l and 'QA_SPAWN_EQUIPMENT_PACKS' in l
 if mode=='off':assert 'QA_ATELIER_DIMENSION_PASS slots=6' in l
for folder in ['blockbench-hook-final','blockbench-peg']:
 e=json.loads((R/'reports/atelier'/folder/'execution.json').read_text());assert e['headless'] and not e['page_errors'] and e['results'][0]['status']=='PASS'
with zipfile.ZipFile(R/'delivery/10.1.0-alpha.14/piratesmca-10.1.0-alpha.14.jar') as a,zipfile.ZipFile(jar) as b:
 removed=sorted(set(a.namelist())-set(b.namelist()));assert not removed,removed
 assert not any(n.startswith(('net/','org/','com/')) for n in b.namelist())
 assert not any('GameTest' in n or '/stubs/' in n or n.endswith('.jem') for n in b.namelist())
 changed=sorted(n for n in a.namelist() if a.read(n)!=b.read(n));added=sorted(set(b.namelist())-set(a.namelist()))
 assert not any(n.startswith(('assets/piratesmca/skins/','assets/piratesmca/outfits/','data/piratesmca/structures/')) for n in changed)
 (R/'reports/atelier/production-diff.json').write_text(json.dumps({'changed':changed,'added':added,'removed':removed},indent=2)+'\n')
O.mkdir(parents=True,exist_ok=True)
for p in [jar,src,R/'reports/atelier_v5.md',R/'CHANGELOG.md',R/'imports/pirate_atelier_v5/LICENSE_NEW_ASSETS.txt']:shutil.copy2(p,O/p.name)
(O/'STATUS.json').write_text(json.dumps({'version':V,'result':'PASS_EXECUTED_SCENARIOS','universal_certification':False,'items':4444,'armor_pieces':4349,'weapons':10,'extra_skins':20,'prosthetic_items':17,'jar_sha256':sha(jar)},indent=2)+'\n')
(O/'LEIA-ME.md').write_text(f"Pirates of MCA {V}\nSubstitua somente o JAR antigo; não instale sources/gametest ou duas versões.\nMC26.2 / Java25 / Loader0.20.1 / MCA8.1.11+26.2 / Fish26.2.2.0; dependências anteriores mantidas.\nAssets embutidos; não precisa ativar o ZIP fonte nem o DEMO de armadura global.\n4349 peças de armadura; 10 armas; 20 skins extras; 12 ganchos e 5 pernas 3D.\nDefesa16/tenacidade2 por conjunto, durabilidade20% acima do ferro. Sem crafting/trades novos.\nNovos piratas sorteiam por slot (50% vazio), usam armas piratas e podem receber skin extra.\nGancho em qualquer mão; perna avulsa em FEET ou armadura com peg_left/peg_right.\nAntigos não recebem novo sorteio automático. Leia atelier_v5.md para testes e limites.\n",encoding='utf-8')
def collect(names):return [p for n in names for p in (R/n).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.pyc','.class'}]
def archive(dest,files):
 with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
  for p in sorted(set(files)):
   i=zipfile.ZipInfo(p.relative_to(R).as_posix(),(2026,11,4,0,0,0));i.compress_type=zipfile.ZIP_DEFLATED;i.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16;z.writestr(i,p.read_bytes())
assets=collect(['models','textures','definitions','exports','scripts','reports','art','imports'])
archive(O/f'piratesmca-{V}-assets.zip',assets)
files=assets+collect(['.agents','source','src','qa','docs','gradle','reference-jars','baseline'])+[R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md']]
archive(O/f'piratesmca-{V}-workspace.zip',files)
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.name}\n' for p in sorted(O.iterdir()) if p.is_file() and p.name!='SHA256SUMS.txt'))
print(V,sha(jar))
