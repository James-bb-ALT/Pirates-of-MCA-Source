"""Read-only entity-region/NBT inventory for the preserved Alpha 18 baseline.
Standard unencrypted Anvil gzip/zlib/uncompressed records only; fail on unknown
compression instead of silently manufacturing an empty identity list.
"""
from pathlib import Path
import gzip,hashlib,io,json,struct,uuid,zlib

def read_nbt(data):
 stream=io.BytesIO(data)
 def number(fmt):return struct.unpack('>'+fmt,stream.read(struct.calcsize('>'+fmt)))[0]
 def string():return stream.read(number('H')).decode('utf-8',errors='replace')
 def payload(kind):
  if kind in range(1,7):return number({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[kind])
  if kind==7:return list(stream.read(number('i')))
  if kind==8:return string()
  if kind==9:
   member=number('B');length=number('i');return [payload(member) for _ in range(length)]
  if kind==10:
   out={}
   while (member:=number('B'))!=0:
    name=string();out[name]=payload(member)
   return out
  if kind in (11,12):return [number('i' if kind==11 else 'q') for _ in range(number('i'))]
  raise ValueError('Unsupported NBT tag '+str(kind))
 root=number('B');string();return payload(root)

def inventory(save):
 regions=[];pirates=[]
 for path in sorted(Path(save).glob('dimensions/minecraft/overworld/entities/*.mca')):
  raw=path.read_bytes();regions.append({'path':path.relative_to(save).as_posix(),'sha256':hashlib.sha256(raw).hexdigest()})
  for i in range(1024):
   offset=int.from_bytes(raw[i*4:i*4+3],'big')*4096
   if offset==0:continue
   size=int.from_bytes(raw[offset:offset+4],'big');kind=raw[offset+4];data=raw[offset+5:offset+4+size]
   if kind==1:data=gzip.decompress(data)
   elif kind==2:data=zlib.decompress(data)
   elif kind!=3:raise ValueError(f'Unsupported Anvil compression {kind}: {path}')
   for entity in read_nbt(data).get('Entities',[]):
    if entity.get('id') not in {'mca:male_villager','mca:female_villager'}:continue
    integers=entity['UUID'];identifier=str(uuid.UUID(bytes=b''.join((n&0xffffffff).to_bytes(4,'big') for n in integers)))
    pirates.append({'id':identifier,'entity_type':entity['id'],'nbt':entity})
 return {'save':str(Path(save)),'regions':regions,'pirates':pirates}

if __name__=='__main__':
 root=Path(__file__).resolve().parents[1];result=inventory(root/'run/visual-fix-before-on/saves/New World')
 path=root/'reports/visual-fix/baseline-saved-identities.json';path.write_text(json.dumps(result,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
 assert len(result['pirates'])==2,'Expected the two baseline MCA entities'
 print('Read preserved save:',[p['id'] for p in result['pirates']])
