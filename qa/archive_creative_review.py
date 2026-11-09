"""Archive the two creative inventory smoke screenshots with the tested JAR hash."""
from pathlib import Path
import hashlib,json,shutil,sys
R=Path(__file__).resolve().parents[1];kind=sys.argv[1];assert kind in {'off','extensions'}
log=R/f'qa/results/creative-tab-client-{kind}.log';text=log.read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in text and 'QA_CREATIVE_COMPLETE eggs=2 adult_genders=2 real_screen=true' in text
assert ('QA_CREATIVE_EMF_EXTENSIONS packs=' in text)==(kind=='extensions')
meta=json.loads((R/'source/src/main/resources/fabric.mod.json').read_text());jar=R/f'build/libs/piratesmca-{meta["version"]}.jar'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert f'QA_ARTIFACT_SHA256 {sha(jar)} ' in text
d=R/'reports/creative-tab'/kind;d.mkdir(parents=True,exist_ok=True)
names=['0000_CREATIVE-EGG-NPCS.png','0001_CREATIVE-TAB.png']
for name in names:shutil.copy2(R/'run/creative-tab-test/screenshots'/name,d/name)
shutil.copy2(log,d/'execution.log')
(d/'manifest.json').write_text(json.dumps({'artifact_sha256':sha(jar),'files':{name:sha(d/name) for name in names+['execution.log']}},indent=2)+'\n')
print(f'PASS {kind}: real screen, two egg-spawned adults, SHA256 manifest')
