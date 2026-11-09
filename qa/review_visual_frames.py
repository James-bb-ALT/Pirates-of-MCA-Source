"""Build contact sheets from unmodified real PNGs, with a source/hash manifest.
No generated scenery or automatic visual approval. Middle animation samples let
the reviewer compare four angles; all original ticks remain in the gallery.
"""
from pathlib import Path
import argparse,csv,hashlib,re
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('stage');args=p.parse_args()
directory=ROOT/f'run/visual-fix-{args.stage}/screenshots'
out=ROOT/'reports/visual-fix/review'/args.stage;out.mkdir(parents=True,exist_ok=True)
groups={};latest={}
for path in directory.glob('*.png'):
 m=re.fullmatch(r'(\d+)_(AUDIT-.+)\.png',path.name)
 if m and (m[2] not in latest or path.stat().st_mtime_ns>latest[m[2]][0]):latest[m[2]]=(path.stat().st_mtime_ns,path)
for _,path in sorted(latest.values()):
 match=re.fullmatch(r'\d+_AUDIT-(ARMOR|PEG|WEAPON|PROTOTYPE)-(.+)\.png',path.name)
 if not match:continue
 label=match[2]
 if '-TURN-' in label or label.endswith('-0') or label.endswith('-2'):continue
 key=re.split(r'-(?:male|female|FRONT|BACK|LEFT|RIGHT|WALK|RUN|ATTACK)',label,maxsplit=1)[0]
 groups.setdefault(key,[]).append((label,path))
rows=[]
for key,frames in groups.items():
 width,height,cols=220,230,6
 board=Image.new('RGB',(width*cols,height*((len(frames)+cols-1)//cols)),(232,232,232));draw=ImageDraw.Draw(board)
 for i,(label,path) in enumerate(frames):
  x,y=(i%cols)*width,(i//cols)*height
  with Image.open(path) as im:
   im=im.convert('RGB').crop((247,115,607,452));im.thumbnail((220,205));board.paste(im,(x+(width-im.width)//2,y))
  suffix=label.removeprefix(key+'-');draw.text((x+3,y+207),suffix,fill='black')
  rows.append({'sheet':key+'.jpg','label':label,'original':path.relative_to(ROOT).as_posix(),'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
 board.save(out/(key+'.jpg'),quality=95)
with (out/'sources.csv').open('w',newline='',encoding='utf-8-sig') as f:
 writer=csv.DictWriter(f,fieldnames=['sheet','label','original','sha256']);writer.writeheader();writer.writerows(rows)
print('Prepared',len(groups),'contact sheets,',len(rows),'frames; inspect before assigning a result')
