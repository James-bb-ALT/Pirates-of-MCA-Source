"""Original pixel-art MCA overlays. One named asset per invocation, no skin/MCA texture copied."""
from pathlib import Path
import argparse,base64,json,random,struct,zlib
from build_tricorne import write,uid
R=Path(__file__).resolve().parents[2]
PALETTES={'deckhand':((222,211,175),(43,65,87),(65,53,45)),
          'smuggler':((57,49,44),(112,69,43),(47,42,40)),
          'captain_civilian':((105,32,38),(191,145,55),(38,48,66))}
def encode(pixels,w=64,h=64):
    raw=b''.join(b'\0'+bytes(sum(pixels[y*w:(y+1)*w],())) for y in range(h))
    def chunk(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d)&0xffffffff)
    return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b'')
def make_texture(name):
    pixels=[(0,0,0,0)]*4096;main,accent,pants=PALETTES[name];rng=random.Random(1200+list(PALETTES).index(name))
    def rect(x0,y0,x1,y1,color):
        for y in range(y0,y1):
            for x in range(x0,x1):
                n=rng.choice([-3,-1,0,1,3]);pixels[y*64+x]=tuple(max(0,min(255,v+n)) for v in color)+(255,)
    # Exact standard PlayerModel UV used by MCA bodyData(false). Breasts share
    # texOffs(18,21): front lies within the solid torso shirt, never head UV.
    rect(16,20,40,32,main);rect(20,16,36,20,main)
    if name=='deckhand':
        for y in [23,27]:rect(16,y,40,y+2,accent)
    else:
        rect(23,21,25,29,(220,208,174));rect(21,20,22,31,accent);rect(26,20,27,31,accent)
        if name=='smuggler':
            for y in range(21,29):rect(20+(y-21)//2,y,22+(y-21)//2,y+1,accent)
        else:
            for y in [23,26,28]:rect(22,y,23,y+1,accent);rect(25,y,26,y+1,accent)
    # Small neck opening, head/face completely transparent.
    for y in range(20,22):
        for x in range(23,25):pixels[y*64+x]=(0,0,0,0)
    rect(16,30,40,32,(101,37,35));rect(23,30,25,31,(183,137,52))
    for x,y in [(40,16),(32,48)]:
        rect(x+4,y,x+8,y+4,main)
        length=5 if name=='deckhand' else 8
        rect(x,y+4,x+16,y+4+length,main)
        rect(x,y+3+length,x+16,y+4+length,accent)
        # Remaining forearm/hand/bottom-cap pixels stay transparent.
    for x,y in [(0,16),(16,48)]:
        rect(x+4,y,x+12,y+4,pants);rect(x,y+4,x+16,y+16,pants)
        rect(x,y+12,x+16,y+16,(45,32,26));rect(x+8,y,x+12,y+4,(45,32,26))
        if name=='captain_civilian':rect(x,y+4,x+3,y+9,main);rect(x+13,y+4,x+16,y+9,main)
        elif name=='deckhand':rect(x+5,y+9,x+7,y+11,(135,119,93))
    return encode(pixels)
def editor(name,tex):
    # From audited reference_adult_mca: Java Y converted as Blockbench 24-Y.
    specs=[('head',[-4,24,-4],[4,32,4],[0,24,0],(0,0),(8,8,8)),
           ('body',[-4,12,-2],[4,24,2],[0,24,0],(16,16),(8,12,4)),
           ('right_arm',[-8,12,-2],[-4,24,2],[-5,22,0],(40,16),(4,12,4)),
           ('left_arm',[4,12,-2],[8,24,2],[5,22,0],(32,48),(4,12,4)),
           ('right_leg',[-3.9,0,-2],[.1,12,2],[-1.9,12,0],(0,16),(4,12,4)),
           ('left_leg',[-.1,0,-2],[3.9,12,2],[1.9,12,0],(16,48),(4,12,4))]
    elements=[];groups=[];out=[]
    for part,a,b,pivot,(u,v),(w,h,d) in specs:
        faces={'west':[u,v+d,u+d,v+d+h],'north':[u+d,v+d,u+d+w,v+d+h],
               'east':[u+d+w,v+d,u+2*d+w,v+d+h],'south':[u+2*d+w,v+d,u+2*d+2*w,v+d+h],
               'up':[u+d,v,u+d+w,v+d],'down':[u+d+w,v,u+d+2*w,v+d]}
        e={'name':part,'type':'cube','uuid':uid(name+'_'+part),'from':a,'to':b,'origin':pivot,'faces':{f:{'uv':uv,'texture':0} for f,uv in faces.items()}}
        g=uid(name+'_group_'+part);elements.append(e);groups.append({'name':part,'uuid':g,'origin':pivot,'export':False});out.append({'uuid':g,'children':[e['uuid']],'isOpen':True})
    return {'meta':{'format_version':'5.0','model_format':'free','box_uv':False},'name':name+'_MCA_overlay_reference','resolution':{'width':64,'height':64},'elements':elements,'groups':groups,'outliner':out,
            'textures':[{'id':'0','uuid':uid(name+'_texture'),'name':name+'.png','width':64,'height':64,'source':'data:image/png;base64,'+base64.b64encode(tex).decode(),'relative_path':'../../../textures/outfits/'+name+'.png'}],
            'notes':'EDITOR REFERENCE ONLY. Runtime uses MCA ClothingLayer and its genetic model, including breast UV(18,21). No body/skin/model replacement; one neutral-gender texture. Transparent head/hands are intentional.'}
def main(name):
    texture=R/f'textures/outfits/{name}.png';model=R/f'models/blockbench/outfits/{name}.bbmodel'
    if not texture.exists():write(texture,make_texture(name))
    if not model.exists():write(model,editor(name,texture.read_bytes()))
    runtime=R/'source/src/main/resources/assets/piratesmca';write(runtime/f'outfits/{name}.png',texture.read_bytes())
    # Distinct, original reusable pattern icons; not armor equipment items.
    pixels=[(0,0,0,0)]*256;main,accent,_=PALETTES[name]
    for y in range(2,14):
        for x in range(2,14):
            if (x<5 or x>10) and y>6:continue
            if y<4 and 6<=x<=9:continue
            c=accent if x in [5,10] or y==12 else main;pixels[y*16+x]=c+(255,)
    write(runtime/f'textures/item/{name}_outfit.png',encode(pixels,16,16))
    write(runtime/f'models/item/{name}_outfit.json',{'parent':'minecraft:item/generated','textures':{'layer0':'piratesmca:item/'+name+'_outfit'}})
    write(runtime/f'items/{name}_outfit.json',{'model':{'type':'minecraft:model','model':'piratesmca:item/'+name+'_outfit'}})
    built=[n for n in PALETTES if (runtime/f'outfits/{n}.png').exists()]
    entries=[{'id':f'piratesmca:outfits/{n}.png','profession':'piratesmca:pirate','gender':3,'exclude':True,'chance':1.0} for n in built]
    write(R/'source/src/main/resources/data/piratesmca/skins/clothing/outfits.json',entries)
    write(R/f'exports/mca_overlay/{name}.json',{'id':'piratesmca:'+name,'clothing_id':f'piratesmca:outfits/{name}.png','texture':f'textures/outfits/{name}.png','runtime_texture':f'assets/piratesmca/outfits/{name}.png','renderer':'MCA ClothingLayer','gender':'neutral','random_pool':False})
    print('Exported original outfit',name,'64x64, neutral gender, preset only')
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('name',choices=list(PALETTES));main(p.parse_args().name)
