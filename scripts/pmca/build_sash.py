"""Original torso-anchored sash. Runtime FIXED item coordinates -> Java torso (x,-y,-z)."""
from pathlib import Path
import base64,hashlib,json,random,struct,zlib
from build_tricorne import cube,write,uid
R=Path(__file__).resolve().parents[2]
SOURCE=R/'models/blockbench/torso/sash_red.bbmodel'
TEXTURE=R/'textures/wearables/waist/sash_red.png'
def texture():
    rng=random.Random(914);raw=bytearray();palettes=[(152,33,35),(87,20,27),(185,134,50),(198,55,45)]
    for y in range(64):
        raw.append(0)
        for x in range(64):
            if y>=32:raw.extend((0,0,0,0));continue
            n=rng.choice([-4,-2,0,2,4]);raw.extend((*[v+n for v in palettes[x//16]],255))
    def chunk(k,d):return struct.pack('>I',len(d))+k+d+struct.pack('>I',zlib.crc32(k+d)&0xffffffff)
    return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',64,64,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(bytes(raw),9))+chunk(b'IEND',b'')
def create():
    if SOURCE.exists():return
    tex=texture();write(TEXTURE,tex)
    specs=[('front_band',[3.35,-3.6,10.25],[12.65,-.6,10.65],0),
           ('back_band',[3.35,-3.6,5.35],[12.65,-.6,5.75],0),
           ('left_band',[3.35,-3.6,5.75],[3.75,-.6,10.25],0),
           ('right_band',[12.25,-3.6,5.75],[12.65,-.6,10.25],0),
           ('knot_base',[11.1,-3.15,10.65],[13.5,-1.1,11.4],1),
           ('knot_fold',[11.6,-2.9,11.4],[13.1,-1.35,11.8],3),
           ('front_upper_seam',[3.4,-1,10.65],[12.6,-.8,10.75],1),
           ('back_lower_seam',[3.4,-3.4,5.25],[12.6,-3.2,5.35],1)]
    elements=[cube(n,a,b,t) for n,a,b,t in specs];g=uid('sash_torso_anchor')
    write(SOURCE,{'meta':{'format_version':'5.0','model_format':'java_block','box_uv':False},'name':'sash_red','resolution':{'width':64,'height':64},'elements':elements,
        'groups':[{'name':'torso_anchor','uuid':g,'origin':[8,8,8],'rotation':[0,0,0],'visibility':True,'export':True}],
        'outliner':[{'uuid':g,'isOpen':True,'children':[e['uuid'] for e in elements]}],
        'textures':[{'uuid':uid('sash_red_texture'),'name':'sash_red.png','id':'0','width':64,'height':64,'uv_width':64,'uv_height':64,'relative_path':'../../../textures/wearables/waist/sash_red.png','source':'data:image/png;base64,'+base64.b64encode(tex).decode()}],
        'display':{'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]},'gui':{'rotation':[25,35,0],'translation':[0,10,0],'scale':[1,1,1]}},
        'notes':'Actual HumanoidModel body pivot Java (0,0,0), torso x+-4,z+-2,y0..12. Item x=X+8,y=8-Y,z=8-Z. Layer follows root and actual hip-center delta from neutral y12, retaining torso yaw; scales(1,-1,-1). Adult only.'})
def export():
    d=json.loads(SOURCE.read_text());elements=[]
    if any(g.get('rotation',[0,0,0])!=[0,0,0] for g in d['groups']):raise ValueError('Bake group rotation before export')
    for e in d['elements']:
        if e.get('rotation') not in (None,[0,0,0]):raise ValueError('Unsupported element rotation')
        if any(f.get('texture')!=0 for f in e['faces'].values()):raise ValueError('Unexpected texture')
        elements.append({'name':e['name'],'from':e['from'],'to':e['to'],'faces':{k:{'uv':[v*16/d['resolution']['width' if i%2==0 else 'height'] for i,v in enumerate(f['uv'])],'texture':'#sash'} for k,f in e['faces'].items()}})
    model={'credit':'Original Pirates of MCA sash','textures':{'sash':'piratesmca:item/sash_red','particle':'piratesmca:item/sash_red'},'elements':elements,'display':d['display']}
    runtime=R/'source/src/main/resources/assets/piratesmca'
    for p in [R/'exports/fabric_renderer/sash_red.json',runtime/'models/item/sash_red.json']:write(p,model)
    write(runtime/'textures/item/sash_red.png',TEXTURE.read_bytes())
    write(runtime/'items/sash_red.json',{'model':{'type':'minecraft:model','model':'piratesmca:item/sash_red'}})
    print('Exported sash:',len(elements),'cuboids, PNG SHA256',hashlib.sha256(TEXTURE.read_bytes()).hexdigest())
if __name__=='__main__':create();export()
