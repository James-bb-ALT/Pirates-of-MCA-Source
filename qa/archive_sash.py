"""Exact final torso frames, separate from stale or unrelated screenshots."""
from pathlib import Path
import hashlib,json,shutil,sys
R=Path(__file__).resolve().parents[1];kind=sys.argv[1];assert kind in {'off','extensions'}
prefix=sys.argv[3] if len(sys.argv)>3 else 'sash'
log=R/f'qa/results/{prefix}-client-{kind}.log';text=log.read_text(encoding='utf-8')
for marker in ['BUILD SUCCESSFUL','QA_SASH_VIEWS count=6 genders=2','QA_EYEPATCH_DIMENSION_PASS','QA_EYEPATCH_CHUNK_PASS','QA_EYEPATCH_REOPEN_PASS']:
    assert marker in text,marker
assert text.count('QA_SASH_RENDER_SNAPSHOT stage=')>=20
assert ('QA_HAT_EMF_EXTENSIONS active_packs=' in text)==(kind=='extensions')
tags=['HAT-POSE-'+s for s in ['idle','head_turn','walk','run','attack','hurt','swim']]+['HAT-FIT-swim-dry','HAT-POSE-sit','HAT-POSE-sleep']
tags += [f'SASH-{i}-{view}' for i in range(2) for view in ['front','side','back']]
if kind=='extensions':tags += ['HAT-PLAYER-no-hat-idle','HAT-PLAYER-hat-idle','HAT-PLAYER-bettercombat-attack']
tags += ['EYE-after-chunk-reload','EYE-after-reopen']
d=R/(sys.argv[2] if len(sys.argv)>2 else 'reports/sash')/kind;d.mkdir(parents=True,exist_ok=True)
names=[f'{i:04}_{tag}.png' for i,tag in enumerate(tags)]
for name in names:shutil.copy2(R/'run/sash-test/screenshots'/name,d/name)
shutil.copy2(log,d/'execution.log')
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
version=json.loads((R/'source/src/main/resources/fabric.mod.json').read_text())['version']
(d/'manifest.json').write_text(json.dumps({'version':version,'frames':len(names),'files':{n:sha(d/n) for n in names+['execution.log']}},indent=2)+'\n')
print(f'PASS {kind}: {len(names)} real frames and lifecycle assertions; fit limits remain in report')
