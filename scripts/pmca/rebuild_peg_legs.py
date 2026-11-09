"""Re-author only prosthetic cuboids from a shared limb-local shape, preserving UV/materials."""
from pathlib import Path
import json,copy
from export_wardrobe import export,write_json,PIVOTS
ROOT=Path(__file__).resolve().parents[2]
shape=json.loads((ROOT/'definitions/prosthetic_leg_shape.json').read_text())
changed=0
for source in (ROOT/'models/blockbench/wardrobe/pirateatelier').glob('*_peg_*.bbmodel'):
 bb=json.loads(source.read_text());bone='left_leg' if '_peg_left' in source.stem else 'right_leg'
 cubes=[c for c in bb['elements'] if c['name'].startswith('leggings__'+bone+'__peg_')]
 if len(cubes)!=4:raise ValueError((source,len(cubes)))
 px,py=PIVOTS[bone]
 for cube,coords in zip(cubes,shape['components']):
  x,y,z,w,h,d=coords;cube['from']=[px+x,24-py-y-h,-z-d];cube['to']=[px+x+w,24-py-y,-z]
 bb.setdefault('notes',{})['prosthetic_shape_version']=shape['version']
 if 'peg_ground_normalized' in bb['notes']:
  bb['notes']['peg_ground_normalized'].update(cuff_top=shape['components'][0][1],anatomical_cut=shape['anatomical_cut'],ground=12,shape_version=shape['version'])
 write_json(source,bb);export('pirateatelier:'+source.stem,source);changed+=1

# Item FIXED space is centered at (8,8,8); the render layer flips Y/Z once.
for source in (ROOT/'models/blockbench/atelier').glob('atelier_peg_leg_*.bbmodel'):
 bb=json.loads(source.read_text());original=bb['elements'];out=[]
 material=source.stem.removeprefix('atelier_peg_leg_')
 reference=json.loads((ROOT/f'imports/pirate_atelier_v5/development_assets/cem_accessories/peg_leg_{material}.jem').read_text())['models'][0]['boxes']
 face_map={'north':'South','south':'North','up':'Down','down':'Up','west':'West','east':'East'}
 for i,coords in enumerate(shape['components']):
  cube=copy.deepcopy(original[min(i,len(original)-1)]);x,y,z,w,h,d=coords
  import uuid
  cube['name']=source.stem+'_'+str(i);cube['uuid']=str(uuid.uuid5(uuid.NAMESPACE_URL,'piratesmca:peg-v2:'+cube['name']))
  cube['from']=[8+x,8-y-h,8-z-d];cube['to']=[8+x+w,8-y,8-z]
  reference_part=reference[{0:0,1:2,2:1,3:2}[i]]
  for face,key in face_map.items():cube['faces'][face]['uv']=reference_part['uv'+key]
  out.append(cube)
 bb['elements']=out;bb['outliner']=[c['uuid'] for c in out]
 if not isinstance(bb.get('notes'),dict):bb['notes']={'original':bb.get('notes','')}
 bb['notes']['prosthetic_shape_version']=shape['version'];write_json(source,bb)
print('Rebuilt peg sources:',changed,'armor variants + 5 standalone materials; retained per-face UV')
