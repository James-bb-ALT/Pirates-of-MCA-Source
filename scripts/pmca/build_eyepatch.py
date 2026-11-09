"""Original editable face accessory; compose existing head models without copying MCA/Fresh."""
from pathlib import Path
import base64,json,copy
from build_tricorne import cube,write,png,uid
R=Path(__file__).resolve().parents[2]
SOURCE=R/'models/blockbench/face/eyepatch_leather.bbmodel'
TEXTURE=R/'textures/wearables/face/eyepatch_leather.png'
def create():
    if SOURCE.exists():return
    tex=png();write(TEXTURE,tex)
    specs=[('front_strap',[3.65,10.4,3.65],[12.35,10.85,3.9],0),
           ('back_strap',[3.65,10.4,12.1],[12.35,10.85,12.35],0),
           ('left_strap',[3.65,10.4,3.9],[3.9,10.85,12.1],0),
           ('right_strap',[12.1,10.4,3.9],[12.35,10.85,12.1],0),
           ('patch_border',[8.4,7.9,3.35],[11.5,10.55,3.65],1),
           ('patch_face',[8.65,8.1,3.2],[11.25,10.35,3.35],0),
           ('patch_bottom',[8.9,7.55,3.35],[11,7.9,3.65],0),
           ('side_buckle',[12.35,10.2,6],[12.5,11.05,7.2],2)]
    # MCA skin-library eyes sit lower than vanilla-player eyes. This offset was
    # measured in real client captures; the neutral head mesh alone was insufficient.
    for _,a,b,_ in specs:a[1]-=2.6;b[1]-=2.6
    elements=[cube(n,a,b,t) for n,a,b,t in specs];group=uid('eyepatch_head_attachment')
    display={'head':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1.6,1.6,1.6]},'gui':{'rotation':[15,20,0],'translation':[0,1,0],'scale':[1.4,1.4,1.4]}}
    write(SOURCE,{'meta':{'format_version':'5.0','model_format':'java_block','box_uv':False},'name':'eyepatch_leather','resolution':{'width':64,'height':64},'elements':elements,
        'groups':[{'name':'head_attachment','uuid':group,'origin':[8,8,8],'rotation':[0,0,0],'visibility':True,'export':True}],
        'outliner':[{'uuid':group,'isOpen':True,'children':[e['uuid'] for e in elements]}],
        'textures':[{'uuid':uid('eyepatch_texture'),'name':'eyepatch_leather.png','id':'0','width':64,'height':64,'uv_width':64,'uv_height':64,'relative_path':'../../../textures/wearables/face/eyepatch_leather.png','source':'data:image/png;base64,'+base64.b64encode(tex).decode()}],
        'display':display,'notes':'MCA real head equipment coordinates; head bounds item x/z 4..12, y4..12. No base skin exported.'})
def export():
    d=json.loads(SOURCE.read_text());elements=[]
    if any(g.get('rotation',[0,0,0])!=[0,0,0] for g in d['groups']):raise ValueError('Bake group rotation before export')
    for e in d['elements']:
        if e.get('rotation') not in (None,[0,0,0]):raise ValueError('Unsupported element rotation')
        if any(f.get('texture')!=0 for f in e['faces'].values()):raise ValueError('Unexpected texture')
        elements.append({'name':e['name'],'from':e['from'],'to':e['to'],'faces':{k:{'uv':[v*16/d['resolution']['width' if i%2==0 else 'height'] for i,v in enumerate(f['uv'])],'texture':'#patch'} for k,f in e['faces'].items()}})
    model={'credit':'Original Pirates of MCA leather eyepatch','textures':{'patch':'piratesmca:item/eyepatch_leather','particle':'piratesmca:item/eyepatch_leather'},'elements':elements,'display':d['display']}
    runtime=R/'source/src/main/resources/assets/piratesmca'
    for p in [R/'exports/fabric_renderer/eyepatch_leather.json',runtime/'models/item/eyepatch_leather.json']:write(p,model)
    player=copy.deepcopy(model)
    for e in player['elements']:e['from'][1]+=2;e['to'][1]+=2
    write(runtime/'models/item/eyepatch_leather_player.json',player)
    write(R/'exports/fabric_renderer/eyepatch_leather_player.json',player)
    write(runtime/'textures/item/eyepatch_leather.png',TEXTURE.read_bytes())
    ref=lambda name:{'type':'minecraft:model','model':'piratesmca:item/'+name}
    fit={'type':'minecraft:select','property':'minecraft:context_entity_type','cases':[{'when':['mca:male_villager','mca:female_villager'],'model':ref('eyepatch_leather')}],'fallback':ref('eyepatch_leather_player')}
    write(runtime/'items/eyepatch_leather.json',{'model':fit})
    composite={'model':{'type':'minecraft:composite','models':[ref('tricorne_classic'),fit]}}
    write(runtime/'items/tricorne_eyepatch.json',composite);write(R/'exports/fabric_renderer/tricorne_eyepatch.json',composite)
    # Pirate face materials retain their authored (higher) eye row. The client
    # chooses this fit only on a frame-local equipment copy for those profiles.
    write(runtime/'items/eyepatch_leather_pirate_face.json',{'model':ref('eyepatch_leather_player')})
    write(runtime/'items/tricorne_eyepatch_pirate_face.json',{'model':{'type':'minecraft:composite','models':[ref('tricorne_classic'),ref('eyepatch_leather_player')]}})
    print('Exported eyepatch:',len(elements),'cuboids, native composite with unchanged tricorne')
if __name__=='__main__':create();export()
