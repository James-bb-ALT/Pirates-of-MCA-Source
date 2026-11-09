"""Summarize explicit execution gates; visual inspection remains a separate CSV."""
from pathlib import Path
import csv,hashlib,json,re
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'reports/visual-fix';OUT.mkdir(exist_ok=True)
JAR=ROOT/'build/libs/piratesmca-13.0.0-alpha.19.jar'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
gates=[('full-final-on','RECON_AVATAR_PASS'),('full-final-off','RECON_AVATAR_PASS'),
 ('weapons-on','QA_HELD_WEAPONS_COMPLETE'),('weapons-off','QA_HELD_WEAPONS_COMPLETE'),
 ('prototypes-on','QA_PROTOTYPES_COMPLETE'),('prototypes-off','QA_PROTOTYPES_COMPLETE'),
 ('variants-on','QA_VISUAL_VARIANTS_COMPLETE'),('heads-on','QA_FACE_CAPTURE_COMPLETE'),
 ('heads-off','QA_FACE_CAPTURE_COMPLETE'),('avatar-slim-on','RECON_AVATAR_PASS'),
 ('avatar-slim-off','RECON_AVATAR_PASS'),('probe-comparison-on','QA_COMPARISON_SAVED_IDENTITIES'),
 ('persistence','QA_CHUNK_UNLOAD_CONFIRMED count=48'),('persistence-reopen','QA_FULL_PROCESS_RESTART_CONFIRMED count=48'),
 ('server-gametests','required tests passed'),('build-regressions','BUILD SUCCESSFUL')]
rows=[]
proof=json.loads((OUT/'on-matrix-reuse.json').read_text())
for name,marker in gates:
 path=ROOT/f'reports/visual-fix-{name}.log';text=path.read_text(encoding='utf-8',errors='replace') if path.exists() else ''
 matches=re.findall(r'QA_ARTIFACT_SHA256 ([0-9a-f]{64}) ',text)
 tested=matches[-1] if matches else ''
 state='PASS_EXECUTION' if 'BUILD SUCCESSFUL' in text and marker in text else 'FAIL' if 'BUILD FAILED' in text or 'AssertionError:' in text else 'RUNNING_OR_INCOMPLETE' if text else 'NOT_RUN'
 reused=proof.get('reusable_gates',{}).get(name)==tested and proof['final_jar_sha256']==sha(JAR) and tested!=sha(JAR)
 if state=='PASS_EXECUTION' and tested and tested!=sha(JAR) and not reused:state='BLOCKED_OTHER_JAR'
 counts=re.findall(r'QA_(?:VISUAL_AUDIT|VISUAL_VARIANTS|HELD_WEAPONS|PROTOTYPES)_COMPLETE[^\n]*shots=(\d+)',text)
 captures=int(counts[-1]) if counts else 0
 if state=='PASS_EXECUTION' and 'RECON_AVATAR_PASS' in text:captures+=27
 if state=='PASS_EXECUTION' and 'QA_FACE_CAPTURE_COMPLETE' in text:captures=10
 rows.append({'gate':name,'result':state,'screenshots_logged':captures,'jar_sha256':tested,
              'reused_candidate':reused,'log':path.relative_to(ROOT).as_posix(),'log_sha256':sha(path) if path.exists() else '',
              'visual_approval':'SEPARATE_REVIEW_REQUIRED'})
with (OUT/'execution-gates.csv').open('w',encoding='utf-8-sig',newline='') as f:
 w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
(OUT/'execution-gates.json').write_text(json.dumps({'final_jar_sha256':sha(JAR),'gates':rows,'all_visual_areas_approved':False},indent=2)+'\n',encoding='utf-8')
for row in rows:print(row['gate'],row['result'],row['screenshots_logged'])
