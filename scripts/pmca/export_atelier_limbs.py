"""Convert supplied CEM cuboids into FIXED-context attachment meshes; GUI/held originals retained."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[2];I=R/'imports/pirate_atelier_v5';O=R/'source/src/main/resources';entries=json.loads((O/'data/piratesmca/atelier/items.json').read_text())
for e in entries:
 if e['kind'] not in ['hook','peg']:continue
 ns,name=e['id'].split(':');kind=e['kind'];material=name.rsplit('_',1)[-1] if name.startswith('atelier_') else name.split('_')[0]
 if kind=='peg':source='peg_leg_'+material
 else:source='hook_'+({'blacksteel':'naval','silver':'iron','copper':'brass'}.get(material,material))
 path=I/'development_assets/cem_accessories'/f'{source}.jem'
 if not path.exists():source='hook_iron' if kind=='hook' else 'peg_leg_wood';path=I/'development_assets/cem_accessories'/f'{source}.jem'
 cem=json.loads(path.read_text());elements=[]
 for model in cem['models']:
  for b in model.get('boxes',[]):
   x,y,z,w,h,d=b['coordinates'];y-=4 if kind=='hook' else 1.7
   # Source Java positive-down limb space -> centered item FIXED space.
   faces={f:{'uv':[v/8 for v in b['uv'+key]],'texture':'#material'} for f,key in [('north','South'),('south','North'),('up','Down'),('down','Up'),('west','West'),('east','East')]}
   elements.append({'from':[8+x,8-y-h,8-z-d],'to':[8+x+w,8-y,8-z],'faces':faces})
 source_tex=cem['texture'];tn,tp=source_tex.split(':');source_png=O/f'assets/{tn}/{tp}.png'
 target=O/f'assets/{tn}/textures/item/attachments/{source}.png';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(source_png.read_bytes())
 tex=f'{tn}:item/attachments/{source}'
 editable=R/f'models/blockbench/atelier/{name}.bbmodel'
 if editable.exists():
  bb=json.loads(editable.read_text());assert bb['resolution']=={'width':128,'height':128}
  elements=[{'from':[round(x,6) for x in c['from']],'to':[round(x,6) for x in c['to']],'faces':{face:{'uv':[v/8 for v in data['uv']],'texture':'#material'} for face,data in c['faces'].items()}} for c in bb['elements']]
 else:
  elements[0]['to'][1]=round(elements[0]['to'][1]+.04,6)
 mesh={'textures':{'material':tex,'particle':tex},'elements':elements,'display':{'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]}}}
 out=O/f'assets/{ns}/models/item/attachments/{name}.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(mesh,indent=2)+'\n')
 dedicated=O/f'assets/{ns}/items/attachments/{name}.json';dedicated.parent.mkdir(parents=True,exist_ok=True);dedicated.write_text(json.dumps({'model':{'type':'minecraft:model','model':f'{ns}:item/attachments/{name}'}},indent=2)+'\n')
 item=O/f'assets/{ns}/items/{name}.json';original=json.loads((I/f'assets/{ns}/items/{name}.json').read_text())
 original['model']={'type':'minecraft:select','property':'minecraft:display_context','cases':[{'when':'fixed','model':{'type':'minecraft:model','model':f'{ns}:item/attachments/{name}'}}],'fallback':original['model']};item.write_text(json.dumps(original,indent=2)+'\n')

 # Bake a reflected mesh instead of using a negative render scale (stable winding/normals).
 if kind=='hook':
  import copy
  left=copy.deepcopy(mesh)
  for c in left['elements']:
   a,b=c['from'][0],c['to'][0];c['from'][0]=round(16-b,6);c['to'][0]=round(16-a,6)
   c['faces']['west'],c['faces']['east']=c['faces']['east'],c['faces']['west']
  p=O/f'assets/{ns}/models/item/attachments/{name}_left.json';p.write_text(json.dumps(left,indent=2)+'\n')
  p=O/f'assets/{ns}/items/attachments/{name}_left.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps({'model':{'type':'minecraft:model','model':f'{ns}:item/attachments/{name}_left'}},indent=2)+'\n')

print('Exported closed-cuff right/left limb meshes from editable sources')
