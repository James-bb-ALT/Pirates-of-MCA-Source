"""Non-executable asset import: validate source paths, retain original namespaces, write explicit registry metadata."""
from pathlib import Path
import json,shutil,hashlib,re,collections
R=Path(__file__).resolve().parents[2];I=R/'imports/pirate_atelier_v5';O=R/'source/src/main/resources'
def write(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
entries=[];counts=collections.Counter();assets=[]
for ns in ['lfpirates','pirateatelier']:
 src=I/'assets'/ns;shutil.copytree(src,O/'assets'/ns,dirs_exist_ok=True)
 equipment={p.stem for p in (src/'equipment').glob('*.json')}
 for p in sorted((src/'items').glob('*.json')):
  name=p.stem;entry={'id':ns+':'+name,'kind':'accessory'}
  for slot in ['helmet','chestplate','leggings','boots']:
   if name.endswith('_'+slot):
    base=name[:-len(slot)-1]
    if base not in equipment:
     model=json.loads((src/'models/item'/f'{name}.json').read_text());texture=str(model.get('textures',{}));matches=[k for k in equipment if k in texture];assert matches,(name,texture);base=max(matches,key=len)
    entry.update(kind='armor',slot=slot,asset=ns+':'+base);break
  if entry['kind']=='accessory' and any(s in name for s in ['saber','cutlass','rapier','falchion','sword']):entry['kind']='weapon'
  if entry['kind']=='accessory' and ('hook' in name and 'lantern' not in name):entry['kind']='hook'
  if entry['kind']=='accessory' and 'peg_leg' in name:entry['kind']='peg'
  entries.append(entry);counts[entry['kind']]+=1
write(O/'data/piratesmca/atelier/items.json',entries)
# Explicit opt-in: only real registered armor/peg items enter randomized loadouts.
write(O/'data/piratesmca/tags/item/spawn_equipment.json',{'replace':False,'values':[e['id'] for e in entries if e['kind'] in ['armor','peg']]})
skins=json.loads((I/'manifests/v4_skins.json').read_text())['skins'];body=[]
for s in skins:
 s['id']='pirateatelier:textures/skins/'+s['name']+'.png';s['gender']='female' if s['name'].endswith('_f') else 'male'
 body.append({'id':s['id'],'gender':s['gender'],'chance':0.0})
write(O/'data/piratesmca/skins/body/atelier.json',body);write(O/'data/piratesmca/atelier/skins.json',skins)
for language in ['en_us','pt_br']:
 path=O/'assets/piratesmca/lang'/f'{language}.json';values=json.loads(path.read_text(encoding='utf-8'))
 for e in entries:values['item.'+e['id'].replace(':','.')]=e['id'].split(':')[1].replace('_',' ').title()
 write(path,values)
report={'source_sha256':(I/'INPUT_SHA256.txt').read_text().split()[0],'counts':dict(counts),'items':len(entries),'skins':len(skins),'equipment_assets':sum(len(list((I/'assets'/n/'equipment').glob('*.json'))) for n in ['lfpirates','pirateatelier']),'recipes':0}
write(R/'reports/atelier/import.json',report);print(json.dumps(report))

for suffix,tag in [('helmet','head_armor'),('chestplate','chest_armor'),('leggings','leg_armor'),('boots','foot_armor')]:
 write(O/f'data/minecraft/tags/item/{tag}.json',{'replace':False,'values':[e['id'] for e in entries if e['kind']=='armor' and e['slot']==suffix]})
write(O/'data/minecraft/tags/item/swords.json',{'replace':False,'values':[e['id'] for e in entries if e['kind']=='weapon']})
import subprocess,sys
subprocess.run([sys.executable,str(R/'scripts/pmca/export_atelier_limbs.py')],check=True)

for e in entries:
 if e['kind']=='weapon':
  ns,name=e['id'].split(':');write(O/f'data/{ns}/weapon_attributes/{name}.json',{'parent':'bettercombat:rapier' if 'rapier' in name else 'bettercombat:cutlass'})

# Keep the vanilla handheld fix after a fresh import of the original voxel pack.
subprocess.run([sys.executable,str(R/'scripts/pmca/export_atelier_weapons.py')],check=True)
subprocess.run([sys.executable,str(R/'scripts/pmca/build_layered_pirate_skins.py')],check=True)
