"""Archive exactly the final 22 frames; reject stale frames from earlier runs."""
from pathlib import Path
import argparse,hashlib,json,shutil
R=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('kind',choices=['off','fresh','extensions']);p.add_argument('--from-run',action='store_true');a=p.parse_args()
d=R/'reports/hair-review'/a.kind;d.mkdir(parents=True,exist_ok=True)
tags=['POSE-'+x for x in ['idle','head_turn','walk','run','attack','hurt','swim']]+['FIT-swim-dry','POSE-sit','POSE-sleep']
tags += [f'HAIR-{style}-{view}' for style in [0,10,20,39] for view in ['front','profile','back']]
names=[f'{i:04}_HAT-{tag}.png' for i,tag in enumerate(tags)]
if a.from_run:
    log=R/f'qa/results/tricorne-hair-{a.kind}.log'
else:log=d/'execution.log'
text=log.read_text(encoding='utf-8')
assert 'BUILD SUCCESSFUL' in text and 'QA_HAT_HAIR_COMPLETE textures=8 views=12 entities=2 fixture_only=true' in text
assert 'QA_HAT_DRY_SWIM_FIT entities=2 synthetic=true' in text
assert text.count('QA_HAT_RENDER_HAIR style=')==24,'Renderer hair coverage was not verified'
assert ('QA_HAT_EMF_EXTENSIONS active_packs=' in text)==(a.kind=='extensions'),'Wrong fixture label'
if a.kind=='extensions':assert 'QA_BETTERCOMBAT_PLAYER_SWING progress=' in text
if a.from_run:
    for name in names:shutil.copy2(R/'run/tricorne-hair-test/screenshots'/name,d/name)
    shutil.copy2(log,d/'execution.log')
for f in d.glob('*_HAT-*.png'):
    if f.name not in names:
        # Keep diagnostic player captures outside the NPC manifest.
        if 'PLAYER' in f.name:f.rename(f.with_name('DIAGNOSTIC-'+f.name))
        else:f.unlink()
assert all((d/n).is_file() for n in names)
sha=lambda f:hashlib.sha256(f.read_bytes()).hexdigest()
(d/'manifest.json').write_text(json.dumps({'frames':len(names),'legacy_hair_textures':8,'controlled_fixture':True,'files':{n:sha(d/n) for n in names+['execution.log']}},indent=2)+'\n')
print(f'PASS {a.kind}: {len(names)} final frames, stale captures excluded')
