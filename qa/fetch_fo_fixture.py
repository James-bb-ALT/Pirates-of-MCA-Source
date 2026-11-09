"""Fetch the exact official FO 14.1.1 fixture into this workspace; never writes an installed profile."""
from pathlib import Path
from zipfile import ZipFile
from concurrent.futures import ThreadPoolExecutor
import urllib.request,json,hashlib,io

ROOT=Path(__file__).resolve().parents[1]
DEST=ROOT/'qa/fixtures/fo-14.1.1'
DEST.mkdir(parents=True,exist_ok=True)
DEST=DEST.resolve()
URL='https://cdn.modrinth.com/data/1KVo5zza/versions/uQxD0ENB/Fabulously.Optimized-v14.1.1.mrpack'
SHA512='97bcd5c52362fd4d5add5a476eeb2565f46f685785cc062c51b2bb58f30a91c2dfd44ceea1546f3aed46785ce421ef4a33dd36f99c783b0f3af9ddbe40d1624e'

def get(url):
    return urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'PiratesMCA-QA/2.1.0'}),timeout=60).read()
def target(name):
    p=(DEST/name).resolve()
    if not p.is_relative_to(DEST):raise ValueError('Unsafe fixture path: '+repr((name,str(p),str(DEST))))
    p.parent.mkdir(parents=True,exist_ok=True)
    return p
def fetch(entry):
    p=target(entry['path'])
    algorithm='sha512' if 'sha512' in entry['hashes'] else 'sha1'
    expected=entry['hashes'][algorithm]
    if p.exists() and hashlib.new(algorithm,p.read_bytes()).hexdigest()==expected:return
    data=get(entry['downloads'][0])
    assert hashlib.new(algorithm,data).hexdigest()==expected,entry['path']
    p.write_bytes(data)

if __name__=='__main__':
    data=get(URL);assert hashlib.sha512(data).hexdigest()==SHA512,'FO pack hash mismatch'
    with ZipFile(io.BytesIO(data)) as z:
        index=json.loads(z.read('modrinth.index.json'))
        target('modrinth.index.json').write_text(json.dumps(index,indent=2))
        for name in z.namelist():
            if name.startswith('overrides/') and not name.endswith('/'):
                target(name).write_bytes(z.read(name))
    with ThreadPoolExecutor(max_workers=6) as pool:list(pool.map(fetch,index['files']))
    inventory=[]
    for p in sorted((DEST/'mods').glob('*.jar')):
        with ZipFile(p) as z:
            if 'fabric.mod.json' in z.namelist():
                meta=json.loads(z.read('fabric.mod.json'),strict=False)
                inventory.append({'path':p.name,'id':meta['id'],'version':meta['version'],'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
    target('verified-mods.json').write_text(json.dumps(inventory,indent=2))
    print('Verified FO 14.1.1:',index['dependencies'],len(inventory),'root mod JARs')
    print('Fabric API:',next(m['version'] for m in inventory if m['id']=='fabric-api'))
