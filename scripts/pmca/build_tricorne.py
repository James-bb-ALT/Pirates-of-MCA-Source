"""Create the initial editable source once; subsequent runs export that source.
No MCA/Fresh textures are copied. Pixel art is original and deterministic.
"""
from pathlib import Path
import base64,hashlib,json,random,struct,uuid,zlib
R=Path(__file__).resolve().parents[2]
SOURCE=R/'models/blockbench/head/tricorne_classic.bbmodel'
TEXTURE=R/'textures/wearables/head/tricorne_classic.png'
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(data if isinstance(data,bytes) else (json.dumps(data,indent=2,ensure_ascii=False)+'\n').encode())
def uid(name):return str(uuid.uuid5(uuid.NAMESPACE_URL,'piratesmca:'+name))
def png():
    rng=random.Random(732);raw=bytearray()
    palettes=[(47,32,27),(79,48,32),(191,138,52),(108,42,34)]
    for y in range(64):
        raw.append(0)
        for x in range(64):
            if y>=32:raw.extend((0,0,0,0));continue
            c=palettes[x//16];noise=rng.choice([-5,-2,0,2,5])
            raw.extend((*[max(0,min(255,v+noise)) for v in c],255))
    def chunk(kind,data):return struct.pack('>I',len(data))+kind+data+struct.pack('>I',zlib.crc32(kind+data)&0xffffffff)
    return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',64,64,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(bytes(raw),9))+chunk(b'IEND',b'')
def cube(name,a,b,tile):
    uv=[tile*16,0,tile*16+16,16]
    return {'name':name,'type':'cube','uuid':uid(name),'from':a,'to':b,'origin':[8,8,8],
            'faces':{f:{'uv':uv,'texture':0} for f in ['north','east','south','west','up','down']}}
def create():
    if SOURCE.exists():return
    tex=png();write(TEXTURE,tex)
    # Coordinates for the proven CustomHeadLayer: head display scale 1.6
    # cancels native 0.625. y=12 maps to Java head y=-8 (top of the MCA head).
    elements=[cube('crown',[3.6,12.5,4.6],[12.4,16,11.4],0),cube('burgundy_band',[3.5,12.5,4.5],[12.5,13.15,11.5],3)]
    rows=[(6,10,1.4,3.2),(4,12,3.2,5),(2,14,5,7),(1,15,7,11.8)]
    for i,(x1,x2,z1,z2) in enumerate(rows):elements.append(cube('brim_'+str(i),[x1,12,z1],[x2,12.6,z2],1))
    for name,a,b in [
        ('front_fold',[6,12.6,1.4],[10,14.1,2.4]),
        ('left_fold',[1,12.6,7],[2.2,14.6,11.8]),
        ('right_fold',[13.8,12.6,7],[15,14.6,11.8])]:elements.append(cube(name,a,b,0))
    for name,a,b in [
        ('front_gold',[6,14.1,1.35],[10,14.45,2.45]),
        ('left_gold',[.95,14.6,6.95],[2.25,14.95,11.85]),
        ('right_gold',[13.75,14.6,6.95],[15.05,14.95,11.85]),
        ('left_slope_gold',[3.95,12.6,3.15],[6.05,12.95,3.65]),
        ('right_slope_gold',[9.95,12.6,3.15],[12.05,12.95,3.65]),
        ('back_gold',[2.2,12.6,11.35],[13.8,12.95,11.85])]:elements.append(cube(name,a,b,2))
    group=uid('head_attachment')
    doc={'meta':{'format_version':'5.0','model_format':'java_block','box_uv':False},'name':'tricorne_classic',
         'resolution':{'width':64,'height':64},'elements':elements,
         'groups':[{'name':'head_attachment','uuid':group,'origin':[8,8,8],'rotation':[0,0,0],'visibility':True,'export':True}],
         'outliner':[{'uuid':group,'isOpen':True,'children':[e['uuid'] for e in elements]}],
         'textures':[{'uuid':uid('tricorne_texture'),'name':'tricorne_classic.png','id':'0','width':64,'height':64,'uv_width':64,'uv_height':64,
                      'relative_path':'../../../textures/wearables/head/tricorne_classic.png','source':'data:image/png;base64,'+base64.b64encode(tex).decode()}],
         'display':{'head':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1.6,1.6,1.6]},
                    'gui':{'rotation':[28,35,0],'translation':[0,-2,0],'scale':[.9,.9,.9]}}}
    write(SOURCE,doc)
def export():
    d=json.loads(SOURCE.read_text(encoding='utf-8'));elements=[]
    if any(g.get('rotation',[0,0,0])!=[0,0,0] for g in d.get('groups',[])):raise ValueError('Group rotation must be baked before Java item export')
    for e in d['elements']:
        if e.get('export',True) is False:continue
        if e.get('rotation') not in (None,[0,0,0]):raise ValueError('MVP exporter does not silently discard rotations')
        if any(v.get('texture') not in (0,d['textures'][0]['uuid']) for v in e['faces'].values()):raise ValueError('MVP exporter supports the declared hat texture only')
        uv_scale=[16/d['resolution']['width'],16/d['resolution']['height']]*2
        elements.append({'name':e['name'],'from':e['from'],'to':e['to'],'faces':{f:{'uv':[n*uv_scale[i] for i,n in enumerate(v['uv'])],'texture':'#hat'} for f,v in e['faces'].items()}})
    model={'credit':'Original Pirates of MCA tricorne; editable source in models/blockbench/head','textures':{'hat':'piratesmca:item/tricorne_classic','particle':'piratesmca:item/tricorne_classic'},'elements':elements,'display':d['display']}
    for p in [R/'exports/fabric_renderer/tricorne_classic.json',R/'source/src/main/resources/assets/piratesmca/models/item/tricorne_classic.json']:write(p,model)
    write(R/'source/src/main/resources/assets/piratesmca/textures/item/tricorne_classic.png',TEXTURE.read_bytes())
    write(R/'source/src/main/resources/assets/piratesmca/items/tricorne_classic.json',{'model':{'type':'minecraft:model','model':'piratesmca:item/tricorne_classic'}})
    print('Exported',len(elements),'cuboids; PNG SHA256',hashlib.sha256(TEXTURE.read_bytes()).hexdigest())
def reference():
    path=R/'models/blockbench/reference_adult_mca.bbmodel'
    if path.exists():return
    # Neutral adult PlayerModel mesh used by MCA bodyData(createMesh(false)).
    parts=[('head',[-4,24,-4],[4,32,4],[0,24,0]),('body',[-4,12,-2],[4,24,2],[0,24,0]),
           ('right_arm',[-8,12,-2],[-4,24,2],[-5,22,0]),('left_arm',[4,12,-2],[8,24,2],[5,22,0]),
           ('right_leg',[-3.9,0,-2],[.1,12,2],[-1.9,12,0]),('left_leg',[-.1,0,-2],[3.9,12,2],[1.9,12,0])]
    elements=[];groups=[];out=[]
    for name,a,b,pivot in parts:
        e=cube('reference_'+name,a,b,0);e['origin']=pivot;elements.append(e);g=uid('reference_group_'+name)
        groups.append({'name':name,'uuid':g,'origin':pivot,'export':False});out.append({'uuid':g,'isOpen':True,'children':[e['uuid']]})
    write(path,{'meta':{'format_version':'5.0','model_format':'free','box_uv':False},'name':'MCA_neutral_adult_reference_only',
                'resolution':{'width':64,'height':64},'elements':elements,'groups':groups,'outliner':out,
                'textures':[{'name':'reference_only.png','id':'0','uuid':uid('reference_texture'),'source':'data:image/png;base64,'+base64.b64encode(TEXTURE.read_bytes()).decode()}],
                'notes':'EDITOR ONLY. Neutral real PlayerModel/MCA baseline. Blockbench Y=24-JavaY. No MCA skin included; no runtime body export. Head top is Java -8. Gender/genetic scaling is runtime, not a fixed mesh.'})
if __name__=='__main__':create();export();reference()
