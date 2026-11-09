"""Static completeness gate, deliberately separate from in-game rendering proof."""
from pathlib import Path
import json,struct,hashlib,collections,sys
sys.path.insert(0,str(Path(__file__).resolve().parent/"tests"))
from test_tricorne_export import pixels
R=Path(__file__).resolve().parents[2];O=R/'source/src/main/resources';entries=json.loads((O/'data/piratesmca/atelier/items.json').read_text());ids={e['id'] for e in entries};assert len(ids)==len(entries)==4444
counts=collections.Counter(e['kind'] for e in entries);assert counts['armor']==4349 and counts['weapon']==10
errors=[];models=0
for e in entries:
 ns,name=e['id'].split(':');assert (O/f'assets/{ns}/items/{name}.json').is_file(),e['id']
 if e['kind']=='armor':
  a,b=e['asset'].split(':');p=O/f'assets/{a}/equipment/{b}.json';assert p.is_file(),p
  definition=json.loads(p.read_text())
  for layer,values in definition['layers'].items():
   for value in values:
    tn,tp=value['texture'].split(':');texture=O/f'assets/{tn}/textures/entity/equipment/{layer}/{tp}.png';assert texture.is_file(),texture
 if e['kind'] in ['hook','peg']:
  mesh=json.loads((O/f'assets/{ns}/models/item/attachments/{name}.json').read_text());assert mesh['textures']['particle']==mesh['textures']['material'];tn,tp=mesh['textures']['material'].split(':');assert tp.startswith('item/'),e['id']
  texture=O/f'assets/{tn}/textures/{tp}.png';assert texture.is_file();w,h,rows=pixels(texture);assert (R/f'models/blockbench/atelier/{name}.bbmodel').is_file()
  for element in mesh['elements']:
   assert set(element['faces'])=={'north','south','east','west','up','down'},'Open cube face'
   assert all(a<b for a,b in zip(element['from'],element['to'])),'Inverted/empty cube'
   assert all(-16<=x<=32 for x in element['from']+element['to'])
   for face in element['faces'].values():
    assert all(0<=v<=16 for v in face['uv']);u=face['uv'];x=int((u[0]+u[2])/2*w/16);y=int((u[1]+u[3])/2*h/16);assert rows[y][x*4+3]==255,'Empty material face'
  cut=6 if e['kind']=='hook' else 6.3
  cuff=mesh['elements'][0];top,bottom=8-cuff['to'][1],8-cuff['from'][1];assert top<cut<bottom,'Cuff must overlap the upper limb, no open seam'
  reached={0}
  while True:
   added={j for i in reached for j,b in enumerate(mesh['elements']) if all(min(mesh['elements'][i]['to'][k],b['to'][k])-max(mesh['elements'][i]['from'][k],b['from'][k])>=-1e-6 for k in range(3))}
   if added<=reached:break
   reached|=added
  assert len(reached)==len(mesh['elements']),'Disconnected prosthetic cuboids'
  if e['kind']=='hook':
   left=json.loads((O/f'assets/{ns}/models/item/attachments/{name}_left.json').read_text())
   for a,b in zip(mesh['elements'],left['elements']):assert abs(b['from'][0]-(16-a['to'][0]))<1e-5 and abs(b['to'][0]-(16-a['from'][0]))<1e-5,'Left mesh reflection changed winding'
  models+=1
 if e['kind']=='weapon':assert (O/f'data/{ns}/weapon_attributes/{name}.json').is_file()
skins=json.loads((O/'data/piratesmca/atelier/skins.json').read_text());assert len(skins)==38
assert collections.Counter(s['gender'] for s in skins)=={'male':19,'female':19}
for s in skins:
 ns,path=s['id'].split(':');p=O/f'assets/{ns}/{path}';data=p.read_bytes();assert struct.unpack('>II',data[16:24])==(64,64)
body=json.loads((O/'data/piratesmca/skins/body/atelier.json').read_text());assert len(body)==len(skins) and all(e['chance']==0 for e in body),'Extra skins must not enter ordinary MCA random pools'
for p in (O/'data').rglob('*.json'):
 if '/recipe/' in p.as_posix() or '/recipes/' in p.as_posix():
  assert not any(i in p.read_text() for i in ids),'Unexpected crafting recipe'
assert models==17
print('PASS ATELIER STRUCTURE: 4444 unique registered items, 4349 armor, 10 weapons, 38 skins (19 male/19 female), 17 atlas-safe editable attachments; no new crafting; render approval requires client evidence')
