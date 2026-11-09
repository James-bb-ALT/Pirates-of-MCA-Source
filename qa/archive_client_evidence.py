"""Archive only textures belonging to the cohort identified in the selected log."""
from pathlib import Path
import argparse, hashlib, json, re, shutil

p=argparse.ArgumentParser()
p.add_argument('log', type=Path)
p.add_argument('screenshots', type=Path)
p.add_argument('destination', type=Path)
p.add_argument('--allow-shutdown-hang', action='store_true', help='Archive functional assertions only; record incomplete process exit explicitly')
p.add_argument('--restart-log', type=Path)
a=p.parse_args()
log=a.log.read_text(encoding='utf-8', errors='replace')
line=next(line for line in log.splitlines() if 'QA_SERVER_IDENTITIES ' in line)
ids=set(re.findall(r'Identity\[id=([0-9a-f-]{36})', line))
if len(ids)!=48:
    raise SystemExit(f'Expected 48 identities, got {len(ids)}')
if ('BUILD SUCCESSFUL' not in log and not a.allow_shutdown_hang) or 'after-server-restart count=48' not in log:
    raise SystemExit('Not a complete successful client run')
a.destination.mkdir(parents=True, exist_ok=True)
stages=['TC-SPAWN-initial','TC-SPAWN-after-resource-reload','TC-SPAWN-after-travel','TC-SPAWN-after-restart']
if 'QA_FLAG_DIALOG_FOUNDED ' in log:
    stages[2:2]=['FLAG-before-founding','FLAG-captain-menu']
if 'QA_TRICORNE_VIEWS ' in log:
    stages[1:1]=['HAT-front','HAT-side','HAT-back']
files=[a.screenshots/f'{i:04d}_{stage}.png' for i,stage in enumerate(stages)]
files += [a.screenshots/f'texture-{id}.png' for id in sorted(ids)]
if a.restart_log:
    restart=a.restart_log.read_text(encoding='utf-8',errors='replace')
    if 'QA_FULL_PROCESS_RESTART_CONFIRMED count=48' not in restart or 'BUILD SUCCESSFUL' not in restart:
        raise SystemExit('Full-process restart not confirmed')
    files.append(a.screenshots/'0000_TC-SPAWN-after-full-process-restart.png')
manifest={}
for file in files:
    if not file.is_file():
        raise SystemExit(f'Missing evidence {file}')
    dest=a.destination/file.name
    shutil.copy2(file,dest)
    manifest[file.name]=hashlib.sha256(dest.read_bytes()).hexdigest()
(a.destination/'manifest.json').write_text(json.dumps({'log':str(a.log),'restart_log':str(a.restart_log) if a.restart_log else None,'complete_process_exit':'BUILD SUCCESSFUL' in log,'cohort':sorted(ids),'sha256':manifest},indent=2)+'\n',encoding='utf-8')
print(f'Archived {len(files)} evidence files for 48 NPCs in {a.destination}')
