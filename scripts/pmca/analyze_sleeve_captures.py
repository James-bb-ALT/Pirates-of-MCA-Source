"""Measure suspicious red exposure in real screenshots; flags require visual review.
No image generation and no automatic artistic PASS. PNG originals remain untouched.
"""
from pathlib import Path
import argparse,csv,colorsys,re
from PIL import Image
ROOT=Path(__file__).resolve().parents[2]
FAMILIES=('first_mate_armor','cutlass_duelist_armor','gunner_armor','cursed_ghost_armor')
parser=argparse.ArgumentParser();parser.add_argument('stages',nargs='+');args=parser.parse_args()
rows=[]
for stage in args.stages:
 directory=ROOT/'run'/('visual-fix-'+stage)/'screenshots'
 for png in sorted(directory.glob('*_AUDIT-ARMOR-*-RUN*.png')):
  family=next((f for f in FAMILIES if 'ARMOR-'+f+'-' in png.name),None)
  if family is None:continue
  with Image.open(png) as im:
   im=im.convert('RGB');count=0
   for box in ((290,215,394,330),(456,215,565,330)):
    for rgb in im.crop(box).getdata():
     h,s,v=colorsys.rgb_to_hsv(*(c/255 for c in rgb))
     count+=int((h<.045 or h>.98) and s>.58 and v>.10)
  rows.append((stage,family,png.name,count,'REVIEW' if count>40 else 'NO_LARGE_RED_PATCH_DETECTED'))
target=ROOT/'reports/visual-fix/red-sleeve-pixels.csv';target.parent.mkdir(parents=True,exist_ok=True)
with target.open('w',newline='',encoding='utf-8-sig') as f:
 writer=csv.writer(f);writer.writerow(('stage','family','original_png','red_candidate_pixels','screening_only'));writer.writerows(rows)
for stage in args.stages:
 selected=[r for r in rows if r[0]==stage]
 print(stage,'frames=',len(selected),'flagged=',sum(r[4]=='REVIEW' for r in selected),'max_pixels=',max((r[3] for r in selected),default=0))
print('Visual screening only. Inspect flagged PNGs; absence of this color does not certify geometry or animation.')
