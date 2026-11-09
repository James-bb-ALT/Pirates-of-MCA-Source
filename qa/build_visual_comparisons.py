"""Pair current captures with the preserved baseline world, without image retouching."""
from pathlib import Path
import csv,hashlib,json,re
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
before='before-on';after='probe-comparison-on'
out=ROOT/'reports/visual-fix/comparisons';out.mkdir(parents=True,exist_ok=True)
old=(ROOT/f'reports/visual-fix-{before}.log').read_text(errors='replace')
new=(ROOT/f'reports/visual-fix-{after}.log').read_text(errors='replace')
assert 'BUILD SUCCESSFUL' in old and 'BUILD SUCCESSFUL' in new
def identities(text,marker):
 line=next(l for l in text.splitlines() if marker in l)
 return set(re.findall(r'\bid=([0-9a-f-]{36})',line))
baseline=json.loads((ROOT/'reports/visual-fix/baseline-saved-identities.json').read_text(encoding='utf-8'))
assert {p['id'] for p in baseline['pirates']}==identities(new,'QA_COMPARISON_SAVED_IDENTITIES'),'Different baseline NPCs'
def shots(stage):
 found={}
 for p in (ROOT/f'run/visual-fix-{stage}/screenshots').glob('*_AUDIT-*.png'):
  label=p.name.split('_AUDIT-',1)[1].removesuffix('.png')
  found[label]=p
 return found
a,b=shots(before),shots(after);rows=[]
for label in sorted(a.keys()&b.keys()):
 if not label.startswith(('ARMOR-','PEG-')):continue
 paths=[a[label],b[label]];board=Image.new('RGB',(720,370),'#e9e9e9');draw=ImageDraw.Draw(board)
 for i,p in enumerate(paths):
  with Image.open(p) as im:board.paste(im.convert('RGB').crop((247,115,607,452)),(i*360,30))
  draw.text((i*360+8,8),'ANTES — Alpha 18' if i==0 else 'DEPOIS — Alpha 19',fill='black')
 board.save(out/(label+'.png'))
 rows.append({'label':label,'before':paths[0].relative_to(ROOT).as_posix(),'after':paths[1].relative_to(ROOT).as_posix(),
              'before_sha256':hashlib.sha256(paths[0].read_bytes()).hexdigest(),'after_sha256':hashlib.sha256(paths[1].read_bytes()).hexdigest(),
              'scope':'same saved NPCs, camera, light, equipment and action; animation tick/pose is not pixel-identical'})
with (out/'manifest.csv').open('w',encoding='utf-8-sig',newline='') as f:
 w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader();w.writerows(rows)
print('Paired',len(rows),'actual captures from the same saved baseline NPCs. Original PNGs preserved.')
