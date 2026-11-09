"""Validate every exported equipment slot, source, face, material and face opening."""
from pathlib import Path
import json,math
from PIL import Image

R=Path(__file__).resolve().parents[2];RES=R/'source/src/main/resources'
index=json.loads((R/'definitions/wardrobe_geometry.json').read_text())
assert not index['missing_reference_geometry']
assert len(index['exported'])==1091
slots=boxes=faces=closed=0
for entry in index['exported']:
 d=json.loads((RES/entry['runtime']).read_text());bb=json.loads((R/entry['source']).read_text())
 assert len(bb['elements'])==entry['boxes']
 assert len(d['slots'])==(1 if 'czacha' in entry['asset'] else 4),entry['asset']
 for slot,parts in d['slots'].items():
  slots+=1;tex=d.get('slotTextures',{}).get(slot,d['texture']);ns,path=tex.split(':');png=RES/'assets'/ns/path
  assert png.is_file(),png
  with Image.open(png) as im:assert list(im.size)==d['textureSize'],(png,im.size)
  for part in parts:
   assert part['part'] in ['head','body','left_arm','right_arm','left_leg','right_leg']
   for b in part['boxes']+part.get('prostheticBoxes',[]):
    boxes+=1;c=b['coordinates'];assert len(c)==6 and all(math.isfinite(x) for x in c) and all(x>0 for x in c[3:]),(entry['asset'],c)
    for side in ['Up','Down','North','South','West','East']:
     faces+=1;uv=b['uv'+side];assert len(uv)==4
     assert all(0<=uv[i]<=d['textureSize'][i%2] for i in range(4)),(entry['asset'],side,uv)
    if slot=='helmet':
     if 'czacha' in entry['asset']:closed+=1;continue
     x,y,z,w,h,dep=c
     assert not(x<3 and x+w>-3 and y<-2 and y+h>-5 and z<-4 and z+dep<0),('eye obstruction',entry['asset'],c)
assert slots==4349,slots
print(f'PASS equipment=1091 slots={slots} boxes={boxes} faces={faces} intentional_closed_masks={closed}; all material PNGs/source files present; UV bounds and open-hat eyes clear')
