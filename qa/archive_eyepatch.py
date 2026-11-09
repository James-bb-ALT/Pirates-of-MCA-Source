"""Keep exact final frames; never mix stale captures from the forehead-fit failure."""
from pathlib import Path
import hashlib,json,shutil,sys
R=Path(__file__).resolve().parents[1];kind=sys.argv[1];assert kind in {'off','extensions'}
source=R/'run/eyepatch-test/screenshots';d=R/'reports/eyepatch'/kind;d.mkdir(parents=True,exist_ok=True)
log=R/f'qa/results/eyepatch-client-{kind}.log';text=log.read_text(encoding='utf-8')
for marker in ['BUILD SUCCESSFUL','QA_EYEPATCH_VIEWS count=12 genders=2 states=2','QA_EYEPATCH_DIMENSION_PASS','QA_EYEPATCH_CHUNK_PASS','QA_EYEPATCH_REOPEN_PASS']:
    assert marker in text,marker
assert ('QA_HAT_EMF_EXTENSIONS active_packs=' in text)==(kind=='extensions')
tags=['HAT-POSE-'+p for p in ['idle','head_turn','walk','run','attack','hurt','swim','sit','sleep']]
tags += ['EYE-'+state+'-'+str(i)+'-'+view for state in ['alone','combined'] for i in range(2) for view in ['front','side','back']]
if kind=='extensions':tags += ['HAT-PLAYER-no-hat-idle','HAT-PLAYER-hat-idle','HAT-PLAYER-bettercombat-attack']
tags += ['EYE-after-chunk-reload','EYE-after-reopen']
names=[f'{i:04}_{tag}.png' for i,tag in enumerate(tags)]
if len(sys.argv)>2 and sys.argv[2]=='--from-run':
    for n in names:shutil.copy2(source/n,d/n)
    shutil.copy2(log,d/'execution.log')
else:assert (d/'execution.log').read_text(encoding='utf-8')==text
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
for p in d.glob('*.png'):
    if p.name not in names:p.rename(p.with_name('DIAGNOSTIC-'+p.name))
assert all((d/n).is_file() for n in names)
(d/'manifest.json').write_text(json.dumps({'version':'8.0.0-alpha.11','frames':len(names),'files':{n:sha(d/n) for n in names+['execution.log']}},indent=2)+'\n')
print(f'PASS {kind}: {len(names)} frames, lifecycle and identities verified')
