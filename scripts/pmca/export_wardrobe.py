"""Author editable full-set BB sources from the corresponding supplied CEMs, then export runtime meshes."""
from pathlib import Path
import base64,copy,json,uuid

ROOT=Path(__file__).resolve().parents[2]
RES=ROOT/'source/src/main/resources'
LIB=ROOT/'imports/pirate_atelier_v5/development_assets/cem_wardrobe'
SOURCE=ROOT/'models/blockbench/wardrobe'
PIVOTS={'head':(0,0),'body':(0,0),'right_arm':(-6,2),'left_arm':(6,2),'right_leg':(-2,12),'left_leg':(2,12)}
FACES={'north':'South','south':'North','up':'Down','down':'Up','west':'West','east':'East'}
SLOTS=('helmet','chestplate','leggings','boots')
LEGACY_ROLES={'korsarz':'corsair','kupiec':'merchant','majtek':'deckhand','marynarkablue':'naval','marynarka':'naval','neutralny':'sailor','nord':'nordic','pirat':'pirate','rybak':'fisherman','rzemieslnik':'craftsman'}
uid=lambda s:str(uuid.uuid5(uuid.NAMESPACE_URL,'piratesmca:wardrobe:'+s))

def write_json(path,data):
 text=json.dumps(data,ensure_ascii=False,separators=(',',':'))+'\n'
 if path.is_file() and path.read_text(encoding='utf8')==text:return
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text,encoding='utf8')

def bbmodel(asset,texture,size,slots,provenance):
 elements=[];outliner=[]
 for slot,parts in slots.items():
  for part in parts:
   bone=part['part'];px,py=PIVOTS[bone];children=[]
   for i,box in enumerate(part['boxes']):
    x,y,z,w,h,d=box['coordinates'];name=f'{slot}__{bone}__{i}';ident=uid(asset+'/'+name);children.append(ident)
    elements.append({'name':name,'type':'cube','uuid':ident,'from':[px+x,24-py-y-h,-z-d],
                     'to':[px+x+w,24-py-y,-z],'origin':[px,24-py,0],
                     'faces':{face:{'uv':box['uv'+cemface],'texture':0} for face,cemface in FACES.items()}})
   outliner.append({'name':slot+'__'+bone,'uuid':uid(asset+'/'+slot+'/'+bone),'origin':[px,24-py,0],'children':children})
 ns,path=texture.split(':');png=RES/'assets'/ns/path
 assert png.is_file(),png
 return {'meta':{'format_version':'5.0','model_format':'free','box_uv':False},'name':asset,
         'resolution':{'width':size[0],'height':size[1]},'elements':elements,'outliner':outliner,
         'textures':[{'id':'0','uuid':uid(asset+'/texture'),'name':png.name,'width':size[0],'height':size[1],
                      'source':'data:image/png;base64,'+base64.b64encode(png.read_bytes()).decode()}],
         'notes':{'equipment':asset,'texture':texture,'source':provenance,'coordinates':'BB y-up: y=24-(bonePivotY+JavaLocalY); z=-JavaLocalZ; arm pivots are centered. Element names retain slot__bone__index.'}}

def export(asset,source):
 bb=json.loads(source.read_text(encoding='utf8'));slots={};size=[bb['resolution']['width'],bb['resolution']['height']]
 for cube in bb['elements']:
  slot,bone,_=cube['name'].split('__',2);px,py=PIVOTS[bone];a,b=cube['from'],cube['to'];faces=cube['faces']
  box={'coordinates':[a[0]-px,24-py-b[1],-b[2],b[0]-a[0],b[1]-a[1],b[2]-a[2]]}
  assert all(d>0 for d in box['coordinates'][3:]),(asset,cube['name'])
  for face,key in FACES.items():box['uv'+key]=faces[face]['uv']
  part=next((p for p in slots.setdefault(slot,[]) if p['part']==bone),None)
  if part is None:part={'part':bone,'boxes':[]};slots[slot].append(part)
  if cube['name'].split('__',2)[2].startswith('peg_'):part.setdefault('prostheticBoxes',[]).append(box)
  else:part['boxes'].append(box)
 ns,name=asset.split(':');out=RES/f'assets/piratesmca/wardrobe/{ns}/{name}.json'
 # Blockbench may omit application-specific notes on save. Existing runtime
 # material bindings are the independent manifest, not editor-only metadata.
 notes=bb.get('notes',{});notes=notes if isinstance(notes,dict) else {}
 previous=json.loads(out.read_text()) if out.exists() else {}
 texture=notes.get('texture',previous.get('texture'))
 assert texture,(asset,'material binding unavailable')
 data={'texture':texture,'textureSize':size,'source':str(source.relative_to(ROOT)).replace('\\','/'),'slots':slots}
 textures=notes.get('slotTextures',previous.get('slotTextures'))
 if textures:data['slotTextures']=textures
 write_json(out,data)
 return {'asset':asset,'runtime':str(out.relative_to(RES)).replace('\\','/'),'source':str(source.relative_to(ROOT)).replace('\\','/'),
         'parts':sum(len(p) for p in slots.values()),'boxes':len(bb['elements']),'slots':sorted(slots)}

def normalize_peg_source(asset,source):
 if not asset.endswith(('_peg_left','_peg_right')):return
 bb=json.loads(source.read_text(encoding='utf8'));notes=bb.get('notes',{})
 if not isinstance(notes,dict):notes={};bb['notes']=notes
 if notes.get('peg_ground_normalized') or any('__peg_' in c['name'] for c in bb['elements']):return
 bone='left_leg' if asset.endswith('_peg_left') else 'right_leg';py=PIVOTS[bone][1]
 selected=[c for c in bb['elements'] if c['name'].startswith('leggings__'+bone+'__') and 24-py-c['to'][1]>=7]
 assert len(selected)>=3,(asset,'prosthesis components missing')
 low=min(24-py-c['to'][1] for c in selected);high=max(24-py-c['from'][1] for c in selected)
 scale=(12-6.26)/(high-low)
 for c in bb['elements']:
  slot,b,_=c['name'].split('__',2)
  if b!=bone or slot not in ['leggings','boots']:continue
  a,z=c['from'],c['to'];minimum=24-py-z[1];maximum=24-py-a[1]
  if c in selected or slot=='boots':
   newmin=6.26+(minimum-low)*scale;newmax=6.26+(maximum-low)*scale
   a[1]=24-py-newmax;z[1]=24-py-newmin;c['name']=slot+'__'+bone+'__peg_'+c['name'].split('__',2)[2]
  elif maximum>6.3:
   a[1]=24-py-6.3
   for face in ['north','south','west','east']:
    uv=c['faces'][face]['uv'];uv[3]=uv[1]+(uv[3]-uv[1])*(6.3-minimum)/(maximum-minimum)
 notes['peg_ground_normalized']={'cuff_top':6.26,'anatomical_cut':6.3,'ground':12,'original_range':[low,high]}
 write_json(source,bb)

def native_box(coords,uv):return {'coordinates':coords,**{'uv'+face:rect for face,rect in uv.items()}}

def legacy_model(asset,name):
 # Explicitly authored adaptations: no corresponding CEM source was supplied for these 29 IDs.
 eq=json.loads((RES/f'assets/lfpirates/equipment/{name}.json').read_text())
 def tex(layer):
  ns,path=eq['layers'][layer][0]['texture'].split(':');return f'{ns}:textures/entity/equipment/{layer}/{path}.png'
 texture=tex('humanoid');legtexture=tex('humanoid_leggings') if 'humanoid_leggings' in eq['layers'] else texture
 from PIL import Image
 ns,path=texture.split(':');size=list(Image.open(RES/'assets'/ns/path).size)
 head={'Up':[8,0,16,8],'Down':[16,0,24,8],'North':[8,8,16,16],'South':[24,8,32,16],'West':[0,8,8,16],'East':[16,8,24,16]}
 body={'Up':[20,16,28,20],'Down':[28,16,36,20],'North':[20,20,28,32],'South':[32,20,40,32],'West':[16,20,20,32],'East':[28,20,32,32]}
 arm={'Up':[44,16,48,20],'Down':[48,16,52,20],'North':[44,20,48,32],'South':[52,20,56,32],'West':[40,20,44,32],'East':[48,20,52,32]}
 leg={'Up':[4,16,8,20],'Down':[8,16,12,20],'North':[4,20,8,32],'South':[12,20,16,32],'West':[0,20,4,32],'East':[8,20,12,32]}
 swatch=lambda rect:{face:rect for face in FACES.values()}
 skull='czacha' in name
 if skull:slots={'helmet':[{'part':'head','boxes':[native_box([-4.6,-8.6,-4.6,9.2,9.2,9.2],head)]}]};role='closed_skull_mask'
 else:
  key=next(k for k in sorted(LEGACY_ROLES,key=len,reverse=True) if name==k or name.startswith(k+'_'));role=LEGACY_ROLES[key]
  hat=[native_box([-4.6,-8.2,-4.6,9.2,2,9.2],head),native_box([-5.5,-8.4,-5.1,11,0.85,10.2],swatch([8,0,16,8])),native_box([-4.2,-10.7,-3.3,8.4,2.8,6.6],swatch([8,8,16,10]))]
  if role in ('corsair','pirate','naval','merchant'):
   hat.extend(native_box(coords,swatch([8,0,16,8])) for coords in [[-6,-10,-3,1.7,2.8,6],[4.3,-10,-3,1.7,2.8,6]])
  torso=[native_box([-4.4,-.1,-2.4,8.8,12.4,4.8],body),native_box([-4.6,10,-2.7,9.2,1.2,5.4],swatch([20,30,28,32]))]
  for x in (-4.1,2.6):torso.append(native_box([x,.7,-2.95,1.5,9,.65],swatch([20,20,22,28])))
  if role in ('corsair','naval','pirate','merchant','nordic'):
   for x in (-4.3,1.3):torso.append(native_box([x,11.6,1.7,3,4.5,1],swatch([32,26,35,32])))
  arms=[{'part':bone,'boxes':[native_box([-2.3,-1.8,-2.3,4.6,11.8,4.6],arm),native_box([-2.5,8.8,-2.5,5,1.1,5],swatch([44,29,48,32])),native_box([-2.6,-2.2,-2.6,5.2,2.1,5.2],swatch([44,20,48,23]))]} for bone in ('right_arm','left_arm')]
  legs=[{'part':bone,'boxes':[native_box([-2.2,0,-2.2,4.4,9.5,4.4],leg)]} for bone in ('right_leg','left_leg')]
  boots=[{'part':bone,'boxes':[native_box([-2.4,8.7,-2.5,4.8,3.3,4.8],leg),native_box([-2.5,8.5,-2.6,5,.7,5],swatch([4,26,8,28]))]} for bone in ('right_leg','left_leg')]
  slots={'helmet':[{'part':'head','boxes':hat}],'chestplate':[{'part':'body','boxes':torso}]+arms,'leggings':legs,'boots':boots}
 bb=bbmodel(asset,texture,size,slots,{'kind':'authored_legacy_adaptation','role':role,'equipment_definition':f'assets/lfpirates/equipment/{name}.json','closed_helmet':skull})
 bb['notes']['slotTextures']={slot:legtexture if slot=='leggings' else texture for slot in slots}
 if legtexture!=texture:
  ns,path=legtexture.split(':');png=RES/'assets'/ns/path
  bb['textures'].append({'id':'1','uuid':uid(asset+'/leggings-texture'),'name':png.name,'width':size[0],'height':size[1],'source':'data:image/png;base64,'+base64.b64encode(png.read_bytes()).decode()})
  for c in bb['elements']:
   if c['name'].startswith('leggings__'):
    for face in c['faces'].values():face['texture']=1
 return bb

def main():
 entries=json.loads((RES/'data/piratesmca/atelier/items.json').read_text());assets=sorted({e['asset'] for e in entries if e['kind']=='armor'})
 report=[];missing=[]
 for asset in assets:
  ns,name=asset.split(':');directory=LIB/name
  source=SOURCE/ns/(name+'.bbmodel')
  if ns=='lfpirates':
   if not source.exists():write_json(source,legacy_model(asset,name))
   report.append(export(asset,source));continue
  if ns!='pirateatelier' or not directory.is_dir():missing.append(asset);continue
  if not source.exists():
   slots={};texture=None;size=None
   for slot in SLOTS:
    cem=json.loads((directory/(slot+'.jem')).read_text());assert all(set(m)<= {'part','id','attach','boxes'} for m in cem['models'])
    slots[slot]=cem['models'];tex=cem['texture']+'.png'
    assert texture is None or texture==tex;texture=tex;size=cem['textureSize']
   write_json(source,bbmodel(asset,texture,size,slots,str(directory.relative_to(ROOT)).replace('\\','/')))
  normalize_peg_source(asset,source)
  report.append(export(asset,source))
 write_json(ROOT/'definitions/wardrobe_geometry.json',{'exported':report,'missing_reference_geometry':missing})
 print('Exported wardrobe equipment',len(report),'boxes',sum(r['boxes'] for r in report),'missing source geometry',len(missing),flush=True)

if __name__=='__main__':main()
