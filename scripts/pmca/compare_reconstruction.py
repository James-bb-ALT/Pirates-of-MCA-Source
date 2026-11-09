"""Make labelled comparison sheets from actual before/after client captures, never synthesize renders."""
from pathlib import Path
import json
from PIL import Image,ImageDraw,ImageOps
R=Path(__file__).resolve().parents[2]
RUN=R/'run/reconstruction-catalogue/screenshots'
OUT=R/'reports/reconstruction'
OLD=R/'build/atelier-chatgpt-captures-20261105'

def find(label):
    matches=list(RUN.glob('*_CATALOG-'+label+'.png'))
    assert len(matches)==1,(label,len(matches))
    return matches[0]

def draw_pair(sheet,position,before,after,label):
    x,y=position;d=ImageDraw.Draw(sheet)
    d.text((x+5,y+5),label,fill='white')
    for i,(path,crop,title) in enumerate([(before,None,'ANTES'),(after,(290,105,564,360),'DEPOIS')]):
        with Image.open(path) as im:
            im=im.convert('RGB');im=im.crop(crop) if crop else im.copy()
            im=ImageOps.contain(im,(212,235),Image.Resampling.NEAREST)
        left=x+i*224;sheet.paste(im,(left+(224-im.width)//2,y+36))
        d.text((left+6,y+20),title,fill='#d6d6d6')

def main():
    skins=json.loads((R/'source/src/main/resources/data/piratesmca/atelier/skins.json').read_text())
    sheet=Image.new('RGB',(448*4,280*5),'#22252b')
    for i,s in enumerate(skins):
        draw_pair(sheet,((i%4)*448,(i//4)*280),OLD/f'skins/{s["name"]}/front.jpg',find(f'SKIN-{s["name"]}-front'),s['name'])
    OUT.mkdir(parents=True,exist_ok=True);sheet.save(OUT/'before-after-20-skins.jpg',quality=92,subsampling=0)
    ids=['pirateatelier:captain_armor_pristine_normal','pirateatelier:fisherman_teal_pristine_peg_left','lfpirates:corsair_tier_1']
    sheet=Image.new('RGB',(448*3,280*3),'#22252b')
    for row,asset in enumerate(ids):
        ns,name=asset.split(':')
        for col,view in enumerate(['front','side','back']):
            draw_pair(sheet,(col*448,row*280),OLD/f'armor/{ns}/{name}/{view}.jpg',find(f'ARMOR-{ns}_{name}-{view}'),name+' '+view)
    sheet.save(OUT/'before-after-priority-armor.jpg',quality=92,subsampling=0)
    print('PASS: comparison sheets use original before JPEGs and fresh final client PNGs; no generated render.')

if __name__=='__main__':main()
