"""Copy the user's named EMF stack into an isolated, non-redistributed fixture."""
from pathlib import Path
import argparse,hashlib,json,shutil,zipfile
R=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--profile',type=Path,required=True);a=p.parse_args()
out=R/'qa/fixtures/tricorne-emf-extensions';mods=out/'mods';overrides=out/'overrides'
mods.mkdir(parents=True,exist_ok=True);(overrides/'resourcepacks').mkdir(parents=True,exist_ok=True)
needed={'entity_model_features','entity_texture_features','emf_compat_core','emf_compat_better_combat','bettercombat','player_animation_library','cloth-config'}
entries=[];found=set()
for f in (a.profile/'mods').glob('*.jar'):
    try:
        with zipfile.ZipFile(f) as z:meta=json.loads(z.read('fabric.mod.json'),strict=False)
    except (KeyError,zipfile.BadZipFile,json.JSONDecodeError):continue
    if meta['id'] not in needed:continue
    shutil.copy2(f,mods/f.name);found.add(meta['id'])
    entries.append({'file':f.name,'id':meta['id'],'version':meta['version'],'depends':meta.get('depends',{}),'sha256':hashlib.sha256(f.read_bytes()).hexdigest()})
assert found==needed,(needed-found)
packs=['FreshAnimations_v1.11.2_26.1+.zip','FA+All_Extensions-v1.10.1.zip','FA+Player-v1.1.zip']
active=next(x for x in (a.profile/'options.txt').read_text(encoding='utf-8').splitlines() if x.startswith('resourcePacks:'))
selected=json.loads(active.split(':',1)[1]);order=[x for x in selected if x in ['file/'+n for n in packs]]
assert order==['file/'+n for n in packs],order
for n in packs:
    f=a.profile/'resourcepacks'/n;shutil.copy2(f,overrides/'resourcepacks'/n)
    entries.append({'file':n,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()})
base=(R/'qa/fixtures/tricorne-emf/overrides/options.txt').read_text(encoding='utf-8')
options='\n'.join('resourcePacks:'+json.dumps(['vanilla']+order) if line.startswith('resourcePacks:') else line for line in base.splitlines())+'\n'
assert all(n in options for n in packs)
(overrides/'options.txt').write_text(options,encoding='utf-8')
configs=[]
for f in (a.profile/'config').glob('*.json'):
    if f.name.startswith(('entity_model_features','entity_texture_features','emf_compat')):
        (overrides/'config').mkdir(exist_ok=True);shutil.copy2(f,overrides/'config'/f.name)
        configs.append({'file':f.name,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()})
report={'entries':entries,'pack_order_low_to_high':order,'configs':configs,'profile_untouched':True,'redistributed':False}
(R/'reports/hair-review/emf-extensions-fixture.json').write_text(json.dumps(report,indent=2)+'\n')
print(f'PASS isolated fixture: {len(found)} mods, 3 packs; source profile unchanged')
