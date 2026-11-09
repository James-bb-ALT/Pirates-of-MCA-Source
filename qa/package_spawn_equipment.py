"""Release gate for the spawn-loadout policy; optional fixtures are never bundled."""
from pathlib import Path
import hashlib,json,shutil,zipfile,xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];V='10.1.0-alpha.14';O=R/'delivery'/V
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=R/f'build/libs/piratesmca-{V}.jar';src=jar.with_name(f'piratesmca-{V}-sources.jar')
log=(R/'qa/results/spawn-equipment-final-build.log').read_text(encoding='utf-8');assert 'BUILD SUCCESSFUL' in log and 'All 72 required tests passed' in log
assert sum(int(ET.parse(p).getroot().get('tests')) for p in (R/'build/test-results/test').glob('TEST-*.xml'))==35
assert 'RESULTADO: PASS' in (R/'qa/results/spawn-equipment-assets.log').read_text(encoding='utf-8')
first=json.loads((R/'qa/results/spawn-equipment-first-hashes.json').read_text());assert all(first[p.name]==sha(p) for p in [jar,src])
assert 'BUILD SUCCESSFUL' in (R/'qa/results/spawn-equipment-reproducible.log').read_text(encoding='utf-8')
d=R/'reports/spawn-equipment/client';l=(d/'execution.log').read_text(encoding='utf-8')
for marker in ['BUILD SUCCESSFUL','QA_SPAWN_EQUIPMENT_CHUNK_PASS count=12','QA_SPAWN_EQUIPMENT_REOPEN_PASS count=12','QA_SPAWN_EQUIPMENT_CLIENT_SYNC count=12','QA_SPAWN_EQUIPMENT_PACKS',f'QA_ARTIFACT_SHA256 {sha(jar)} ']:assert marker in l,marker
m=json.loads((d/'manifest.json').read_text());assert m['frames']==4 and all(sha(d/n)==h for n,h in m['files'].items())
with zipfile.ZipFile(R/'delivery/10.0.0-alpha.13/piratesmca-10.0.0-alpha.13.jar') as a,zipfile.ZipFile(jar) as b:
 removed=sorted(set(a.namelist())-set(b.namelist()));assert not removed,removed
 changed=sorted(n for n in a.namelist() if a.read(n)!=b.read(n));added=sorted(set(b.namelist())-set(a.namelist()))
 assert set(changed)<={'fabric.mod.json','piratesmca.mixins.json','dev/arandu/piratesmca/PirateBehavior.class'},changed
 assert not any(n.startswith(('net/','org/','com/')) for n in b.namelist())
 (R/'reports/spawn-equipment/production-diff.json').write_text(json.dumps({'changed':changed,'added':added,'removed':removed},indent=2)+'\n')
O.mkdir(parents=True,exist_ok=True)
for p in [jar,src,R/'reports/spawn_equipment.md',R/'CHANGELOG.md']:shutil.copy2(p,O/p.name)
(O/'STATUS.json').write_text(json.dumps({'version':V,'scope':'new pirate spawn equipment policy','result':'PASS_EXECUTED_SCENARIOS','universal_certification':False,'jar_sha256':sha(jar)},indent=2)+'\n')
(O/'LEIA-ME.md').write_text(f"Pirates of MCA {V}\nSubstitua somente o JAR antigo, sem duas versões e sem sources/gametest no mods.\nJava25 / MC26.2 / FabricLoader0.20.1 / MCA8.1.11+26.2 / Fish26.2.2.0 e dependências anteriores.\nPiratas novos recebem roupas piratas e equipamento aleatório por slot, incluindo vazio.\nHEAD: três itens atuais ou vazio; CHEST: faixa ou vazio; LEGS/FEET: vazios até adicionar peças.\nItens equippable do addon são descobertos automaticamente. Tag piratesmca:spawn_equipment inclui extras.\nNPCs já salvos e equipamento explícito não são sorteados novamente. Armas atuais mantidas.\nVeja spawn_equipment.md para testes e limitações.\n",encoding='utf-8')
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
