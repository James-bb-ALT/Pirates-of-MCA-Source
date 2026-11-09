"""Crop fresh Minecraft screenshots and package upload-sized, independent ZIP parts."""
from __future__ import annotations
from pathlib import Path
import hashlib, json, math, re, zipfile
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
RUN=ROOT/'run/atelier-catalogue-test/screenshots'
LOG=ROOT/'qa/results/atelier-catalogue-capture.log'
OUT=ROOT/'delivery/11.0.0-alpha.15/chatgpt-capture-parts'
WORK=ROOT/'build/atelier-chatgpt-captures-20261105'
VIEWS=('front','side','back')
CROP=(315,100,539,395) # 224x295; excludes floating nameplate, keeps top/bottom margin.
QUALITY=90
MAX_PART=100*1024*1024

from PIL import ImageFile
ImageFile.LOAD_TRUNCATED_IMAGES=False
items=json.loads((ROOT/'source/src/main/resources/data/piratesmca/atelier/items.json').read_text(encoding='utf-8'))
skins=json.loads((ROOT/'source/src/main/resources/data/piratesmca/atelier/skins.json').read_text(encoding='utf-8'))
log=LOG.read_text(encoding='utf-8',errors='replace')
assert 'QA_CATALOG_CAPTURE_COMPLETE screenshots=3333' in log and 'BUILD SUCCESSFUL' in log
assert LOG.stat().st_ctime > 0

armor={}
for item in items:
    if item['kind']!='armor': continue
    suffix='_'+item['slot']
    assert item['id'].endswith(suffix)
    armor.setdefault(item['id'][:-len(suffix)],{})[item['slot']]=item['id']
armors=sorted(armor)
assert len(skins)==20 and len(armors)==1091

def source_for(label:str)->Path:
    found=list(RUN.glob(f'*_{label}.png'))
    assert len(found)==1,(label,len(found))
    p=found[0]
    assert p.stat().st_mtime>=LOG.stat().st_ctime-2,(p,'not from the current run')
    return p

def convert(label:str,dest:Path)->dict:
    src=source_for(label)
    dest.parent.mkdir(parents=True,exist_ok=True)
    with Image.open(src) as im:
        assert im.size==(854,480),(src,im.size)
        im.convert('RGB').crop(CROP).save(dest,'JPEG',quality=QUALITY,subsampling=0,optimize=True)
    with Image.open(dest) as out:
        assert out.size==(CROP[2]-CROP[0],CROP[3]-CROP[1]),(dest,out.size)
    return {'sha256':hashlib.sha256(dest.read_bytes()).hexdigest(),'bytes':dest.stat().st_size,
            'source_capture':src.name,'source_sha256':hashlib.sha256(src.read_bytes()).hexdigest()}

WORK.mkdir(parents=True,exist_ok=True)
records={}
for skin in skins:
    for view in VIEWS:
        rel=Path('skins')/skin['name']/f'{view}.jpg'
        records[rel.as_posix()]=convert(f"CATALOG-SKIN-{skin['name']}-{view}",WORK/rel)
for base in armors:
    ns,path=base.split(':',1)
    for view in VIEWS:
        rel=Path('armor')/ns/path/f'{view}.jpg'
        records[rel.as_posix()]=convert(f"CATALOG-ARMOR-{base.replace(':','_')}-{view}",WORK/rel)
assert len(records)==3333

OUT.mkdir(parents=True,exist_ok=True)
groups=[('skins', [p for p in sorted(records) if p.startswith('skins/')])]
chunk_size=math.ceil(len(armors)/4)
for i in range(4):
    names=armors[i*chunk_size:(i+1)*chunk_size]
    paths=[]
    for base in names:
        ns,path=base.split(':',1)
        paths.extend(f'armor/{ns}/{path}/{view}.jpg' for view in VIEWS)
    groups.append((f'armor-{i+1:02d}-of-04',paths))

index={'title':'Pirates of MCA — capturas compactas para upload','crop_px':list(CROP),
       'image_size_px':[CROP[2]-CROP[0],CROP[3]-CROP[1]],'format':'JPEG','quality':QUALITY,
       'screenshots':len(records),'skins':len(skins),'armor_configurations':len(armors),
       'parts':[]}
for name,paths in groups:
    outzip=OUT/f'piratesmca-{name}.zip'
    manifest={'part':name,'capture_source':'Fresh Minecraft 26.2 Fabric client screenshots',
              'transformation':{'crop_pixels':list(CROP),'output_pixels':index['image_size_px'],
                                'format':'JPEG','quality':QUALITY},
              'image_count':len(paths),'images':{p:records[p] for p in paths}}
    readme=(f"Pirates of MCA fresh in-game captures — {name}\n"
            f"{len(paths)} JPEG images. Each is a centered 224x295 crop with front/side/back angle.\n"
            "manifest.json includes SHA-256 for both the new JPEG and its original Minecraft PNG.\n")
    with zipfile.ZipFile(outzip,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6,allowZip64=True) as z:
        for rel in paths: z.write(WORK/rel,'piratesmca-captures/'+rel)
        z.writestr('piratesmca-captures/manifest.json',json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
        z.writestr('piratesmca-captures/README.txt',readme)
    size=outzip.stat().st_size
    assert size<MAX_PART,(outzip,size)
    index['parts'].append({'file':outzip.name,'bytes':size,'sha256':hashlib.sha256(outzip.read_bytes()).hexdigest(),
                           'images':len(paths),'configurations':(0 if name=='skins' else len(paths)//3)})

(OUT/'index.json').write_text(json.dumps(index,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'PASS':True,'images':len(records),'crop':index['image_size_px'],
                  'total_jpeg_bytes':sum(x['bytes'] for x in records.values()),'parts':index['parts']},ensure_ascii=False))
