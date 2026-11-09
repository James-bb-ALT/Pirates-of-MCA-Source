"""Keep only the current run's exact expected screenshots; exclude stale prior iterations."""
from pathlib import Path
import sys,json,shutil,hashlib
R=Path(__file__).resolve().parents[1];mode=sys.argv[1];log=R/sys.argv[2];run=R/sys.argv[3];out=R/'reports/atelier/release'/mode;out.mkdir(parents=True,exist_ok=True)
labels=['ATELIER-SKIN-'+s['name'] for s in json.loads((R/'source/src/main/resources/data/piratesmca/atelier/skins.json').read_text())]
labels += [f'ATELIER-FIT-{i}-{v}' for i in range(4) for v in ['front','side','back']]
labels += ['ATELIER-PROSTH-'+e['id'].replace(':','_') for e in json.loads((R/'source/src/main/resources/data/piratesmca/atelier/items.json').read_text()) if e['kind'] in ['hook','peg']]
labels += ['ATELIER-LIMBS-'+p for p in ['idle','walk','attack','swim']]
if mode=='extensions':labels += ['HAT-PLAYER-no-hat-idle','HAT-PLAYER-hat-idle','HAT-PLAYER-bettercombat-attack']
labels += ['SPAWN-EQ-'+v for v in ['front','side','back','reopen']]
for i,label in enumerate(labels):
 name=f'{i:04d}_{label}.png';p=run/'screenshots'/name;assert p.is_file(),name
 assert p.stat().st_mtime>=log.stat().st_ctime-1,(name,'stale capture')
 shutil.copy2(p,out/name)
shutil.copy2(log,out/'execution.log');files={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in out.iterdir() if p.is_file() and p.name!='manifest.json'}
(out/'manifest.json').write_text(json.dumps({'version':'11.0.0-alpha.15','mode':mode,'frames':len(labels),'files':files},indent=2)+'\n')
print(mode,len(labels),'current frames archived')
