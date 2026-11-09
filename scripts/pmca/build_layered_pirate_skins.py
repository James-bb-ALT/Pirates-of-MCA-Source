"""Build scoped MCA body/face materials; retain supplied atlases and every legacy ID.

The body material loses baked facial features. The original face is rendered once by
the MCA FaceLayer, with the MCA blink state. Hair remains the NPC's persisted MCA hair.
Female outfits retain each family's palette, with a tailored neckline and waist rather
than relabeling male textures. Re-running this generator is deterministic.
"""
from pathlib import Path
from collections import Counter
import json
from statistics import median
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / 'source/src/main/resources'
TEX = RES / 'assets/pirateatelier/textures/skins'
OUT = TEX / 'layered'

def skin_tone(image):
    cheeks=[image.getpixel(p) for p in ((9,12),(14,12),(11,10),(12,10))]
    return tuple(int(median(p[channel] for p in cheeks)) for channel in range(3))+(255,)

def build():
    OUT.mkdir(parents=True, exist_ok=True)
    original = json.loads((ROOT/'imports/pirate_atelier_v5/manifests/v4_skins.json').read_text())['skins']
    skins = [{'name':s['name'], 'title':s['title'],
              'id':f"pirateatelier:textures/skins/{s['name']}.png",
              'gender':'female' if s['name'].endswith('_f') else 'male'} for s in original]
    female_head = Image.open(TEX/'deckhand_f.png').convert('RGBA')
    female_tone = skin_tone(female_head)
    additions=[]
    for male in list(skins):
        if male['gender'] != 'male' or male['name'] == 'deckhand':continue
        name=male['name']+'_f'
        image=Image.open(TEX/(male['name']+'.png')).convert('RGBA')
        tone=skin_tone(image)
        head=female_head.crop((0,0,64,16))
        head.putdata([tuple(max(0,min(255,tone[i]+p[i]-female_tone[i])) for i in range(3))+(p[3],)
                      if max(abs(p[i]-female_tone[i]) for i in range(3))<35 else p for p in head.getdata()])
        image.paste(head,(0,0))
        draw=ImageDraw.Draw(image)
        shirt=image.getpixel((23,22));trim=image.getpixel((21,25))
        # Short V-neck, fitted side panels and a two-pixel sash; all UV regions
        # are the vanilla/MCA female torso, never a second face on the body.
        draw.line([(23,20),(23,22),(24,23),(25,22),(25,20)],fill=tone,width=1)
        draw.rectangle((20,25,21,29),fill=trim)
        draw.rectangle((26,25,27,29),fill=trim)
        draw.rectangle((20,30,27,31),fill=image.getpixel((20,31)))
        for y in (24,26,28):draw.point((24,y),fill=(209,167,87,255))
        image.save(TEX/(name+'.png'))
        additions.append({'name':name,'title':male['title']+' (Female)',
                          'id':f'pirateatelier:textures/skins/{name}.png','gender':'female'})
    skins.extend(additions)
    profiles=[]
    for s in skins:
        image=Image.open(TEX/(s['name']+'.png')).convert('RGBA')
        tone=skin_tone(image)
        body=image.copy();draw=ImageDraw.Draw(body)
        draw.rectangle((0,0,31,15),fill=tone)
        draw.rectangle((32,0,63,15),fill=(0,0,0,0))
        body.save(OUT/(s['name']+'_body.png'))
        face=Image.new('RGBA',(64,64))
        # Leave the forehead to native HairLayer and keep original eyes/brows/mouth.
        face.paste(image.crop((8,10,16,16)),(8,10))
        face.save(OUT/(s['name']+'_face.png'))
        blink=face.copy();d=ImageDraw.Draw(blink)
        # Each supplied face has two 1x2 eyes at columns 10 and 13, rows 11/12.
        for x in (10,13):
            d.line((x,10,x,13),fill=tone)
            d.point((x,12),fill=tuple(max(0,c-48) for c in tone[:3])+(255,))
        blink.save(OUT/(s['name']+'_blink.png'))
        profiles.append({'skin':s['id'],'gender':s['gender'], 'body':f"pirateatelier:textures/skins/layered/{s['name']}_body.png",
                         'face':f"pirateatelier:textures/skins/layered/{s['name']}_face.png",
                         'blink':f"pirateatelier:textures/skins/layered/{s['name']}_blink.png",'hair':'persisted_mca_layer'})
    def write(path,data):path.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    write(RES/'data/piratesmca/atelier/skins.json',skins)
    write(RES/'data/piratesmca/atelier/skin_layers.json',profiles)
    write(RES/'data/piratesmca/skins/body/atelier.json',[{'id':s['id'],'gender':s['gender'],'chance':0.0} for s in skins])
    print('Built layered pirate skins:',len(skins),dict(Counter(s['gender'] for s in skins)))

if __name__=='__main__':build()
