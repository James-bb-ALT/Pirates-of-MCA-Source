"""Package only after real build/client gates. Never bundles the optional user packs."""
from pathlib import Path
import hashlib,json,shutil,zipfile,xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='10.0.0-alpha.13';O=R/'delivery'/V
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';src=jar.with_name(f'piratesmca-{V}-sources.jar')
log=(R/'qa/results/outfits-final-build.log').read_text(encoding='utf-8');assert 'BUILD SUCCESSFUL' in log and 'All 68 required tests passed' in log
assert sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))==35
assert 'BUILD SUCCESSFUL' in (R/'qa/results/outfits-reproducible.log').read_text(encoding='utf-8')
first=json.loads((R/'qa/results/outfits-first-hashes.json').read_text());assert all(first[p.name]==sha(p) for p in [jar,src])
assert 'Ran 9 tests' in (R/'qa/results/outfits-python.log').read_text(encoding='utf-8')
assert 'RESULTADO: PASS' in (R/'qa/results/outfits-assets.log').read_text(encoding='utf-8')
for mode in ['off','extensions']:
 d=R/'reports/outfits/deckhand'/mode;l=(d/'execution.log').read_text(encoding='utf-8')
 for marker in ['BUILD SUCCESSFUL','QA_OUTFIT_VIEWS count=6','QA_HAT_POSES_COMPLETE count=9','QA_EYEPATCH_DIMENSION_PASS','QA_EYEPATCH_CHUNK_PASS','QA_EYEPATCH_REOPEN_PASS',f'QA_ARTIFACT_SHA256 {sha(jar)} ']:assert marker in l,marker
 if mode=='extensions':assert 'QA_HAT_EMF_EXTENSIONS active_packs=' in l and 'QA_BETTERCOMBAT_PLAYER_SWING' in l
 m=json.loads((d/'manifest.json').read_text());assert all(sha(d/n)==h for n,h in m['files'].items())
editor=json.loads((R/'reports/outfits/deckhand/blockbench/execution.json').read_text());assert editor['headless'] and not editor['page_errors'] and editor['results'][0]['status']=='PASS'
with zipfile.ZipFile(R/'delivery/9.0.1-alpha.12/piratesmca-9.0.1-alpha.12.jar') as a,zipfile.ZipFile(jar) as b:
 removed=sorted(set(a.namelist())-set(b.namelist()));assert not removed,removed
 changed=sorted(n for n in a.namelist() if a.read(n)!=b.read(n));added=sorted(set(b.namelist())-set(a.namelist()))
 allowed={'fabric.mod.json','dev/arandu/piratesmca/PiratesOfMCA.class','dev/arandu/piratesmca/PirateCommands.class','dev/arandu/piratesmca/PirateBehavior.class','assets/piratesmca/lang/en_us.json','assets/piratesmca/lang/pt_br.json'}
 assert set(changed)<=allowed,changed
 (R/'reports/outfits/deckhand/production-diff.json').write_text(json.dumps({'changed':changed,'added':added,'removed':removed},indent=2)+'\n')
O.mkdir(parents=True,exist_ok=True)
for p in [jar,src,R/'reports/deckhand.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
(O/'STATUS.json').write_text(json.dumps({'version':V,'scope':'deckhand overlay only; Lote 1 partial','result':'PASS_EXECUTED_SCENARIOS','universal_certification':False,'initial_client_failure_cause':'unconfirmed; retained in report','jar_sha256':sha(jar)},indent=2)+'\n')
(O/'LEIA-ME.md').write_text(f"Pirates of MCA {V} — uniforme de marinheiro.\nSubstitua só o JAR antigo, mantendo as dependências anteriores; não instale sources/gametest.\nMC26.2, Java25, Loader0.20.1, MCA8.1.11+26.2 e Fish26.2.2.0.\n/give @s piratesmca:deckhand_outfit\n/pirates outfit nearest deckhand\n/pirates outfit nearest restore\nMolde reutilizável em pirata adulto da própria tripulação ou OP, até6 blocos/visível.\nRoupa cosmética NPC, não é armadura nem veste jogadores.\nVeja deckhand.md para limites e a falha inicial de QA não reproduzida.\n",encoding='utf-8')
def collect(names):return [p for n in names for p in (R/n).rglob('*') if p.is_file() and 'fixtures' not in p.parts and '__pycache__' not in p.parts and p.suffix not in {'.pyc','.class'}]
def archive(dest,files):
 with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
  for p in sorted(set(files)):
   i=zipfile.ZipInfo(p.relative_to(R).as_posix(),(2026,11,4,0,0,0));i.compress_type=zipfile.ZIP_DEFLATED;i.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16;z.writestr(i,p.read_bytes())
assets=collect(['models','textures','definitions','exports','scripts','reports','art'])
archive(O/f'piratesmca-{V}-assets.zip',assets)
files=assets+collect(['.agents','source','src','qa','docs','gradle','reference-jars','baseline'])+[R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md']]
archive(O/f'piratesmca-{V}-workspace.zip',files)
(O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.name}\n' for p in sorted(O.iterdir()) if p.is_file() and p.name!='SHA256SUMS.txt'))
print(V,sha(jar))
