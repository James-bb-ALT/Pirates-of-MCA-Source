"""Prepare a private client-test fixture using the supplied jars and prior EMF stack."""
from pathlib import Path
import hashlib,json,shutil,zipfile

ROOT=Path(__file__).resolve().parents[1]
DEST=ROOT/'qa/fixtures/mca-skin-3d'
MODS=DEST/'mods'
SOURCE=ROOT/'qa/fixtures/tricorne-emf-extensions'
MODS.mkdir(parents=True,exist_ok=True)

def copy_checked(source,dest,expected):
    if not source.is_file():raise FileNotFoundError(f'Missing test dependency: {source}')
    digest=hashlib.sha256(source.read_bytes()).hexdigest()
    if expected and digest!=expected:raise ValueError(f'Unexpected SHA-256 for {source.name}: {digest}')
    shutil.copy2(source,dest)
    with zipfile.ZipFile(dest) as archive:
        metadata=json.loads(archive.read('fabric.mod.json'),strict=False)
    return {'file':dest.name,'id':metadata['id'],'version':metadata['version'],'sha256':digest}

entries=[]
for jar in (SOURCE/'mods').glob('*.jar'):
    entries.append(copy_checked(jar,MODS/jar.name,None))
for name,modid,version in [
    ('3d-armor-0.9.4.2-mod.jar','armor_3d','0.9.4.2'),
    ('skinlayers3d-fabric-1.11.4-mc26.2.jar','skinlayers3d','1.11.4')]:
    entry=copy_checked(ROOT/name,MODS/name,None)
    if (entry['id'],entry['version'])!=(modid,version):raise ValueError(f'Wrong mod: {entry}')
    entries.append(entry)
overrides=DEST/'overrides';overrides.mkdir(parents=True,exist_ok=True)
for item in (SOURCE/'overrides').iterdir():
    dest=overrides/item.name
    if item.is_dir():shutil.copytree(item,dest,dirs_exist_ok=True)
    else:shutil.copy2(item,dest)
packs=['FreshAnimations_v1.11.2_26.1+.zip','FA+All_Extensions-v1.10.1.zip','FA+Player-v1.1.zip']
assert all((overrides/'resourcepacks'/p).is_file() for p in packs)
(DEST/'MOD_MANIFEST.json').write_text(json.dumps({'mods':entries,'resource_packs':packs,'redistribute_mod_jars':False},indent=2)+'\n',encoding='utf-8')
print(f'PASS fixture prepared: {len(entries)} mods, {len(packs)} resource packs; user jars stay local')
