"""Package only the screenshots created by the fresh in-game Atelier catalog run."""
from __future__ import annotations
from pathlib import Path
from datetime import datetime, timezone
import hashlib, json, re, zipfile

ROOT=Path(__file__).resolve().parents[1]
RUN=ROOT/'run/atelier-catalogue-test/screenshots'
LOG=ROOT/'qa/results/atelier-catalogue-capture.log'
OUT=ROOT/'delivery/11.0.0-alpha.15/piratesmca-catalogue-captures.zip'
VIEWS=('front','side','back')
items=json.loads((ROOT/'source/src/main/resources/data/piratesmca/atelier/items.json').read_text(encoding='utf-8'))
skins=json.loads((ROOT/'source/src/main/resources/data/piratesmca/atelier/skins.json').read_text(encoding='utf-8'))

assert 'QA_CATALOG_CAPTURE_COMPLETE screenshots=3333' in LOG.read_text(encoding='utf-8',errors='replace')
assert 'BUILD SUCCESSFUL' in LOG.read_text(encoding='utf-8',errors='replace')
armor={}
for item in items:
    if item['kind']!='armor': continue
    suffix='_'+item['slot']
    assert item['id'].endswith(suffix),item['id']
    armor.setdefault(item['id'][:-len(suffix)],{})[item['slot']]=item['id']
assert sum(len(v)==4 for v in armor.values())==1086
assert sum(len(v)==1 and 'helmet' in v for v in armor.values())==5

files={}
def add(source_label:str,dest:str):
    candidates=list(RUN.glob(f'*_{source_label}.png'))
    assert len(candidates)==1,(source_label,len(candidates))
    path=candidates[0]
    assert path.stat().st_mtime>=LOG.stat().st_ctime-2,(path,'image predates this capture run')
    files[dest]=path

for skin in skins:
    for view in VIEWS:
        add(f"CATALOG-SKIN-{skin['name']}-{view}",f"skins/{skin['name']}/{view}.png")
for base,pieces in sorted(armor.items()):
    namespace,path=base.split(':',1)
    key=f'{namespace}/{path}'
    kind='sets' if len(pieces)==4 else 'extra-helmets'
    for view in VIEWS:
        add(f"CATALOG-ARMOR-{base.replace(':','_')}-{view}",f'armor/{kind}/{key}/{view}.png')
assert len(files)==3333,len(files)

manifest={
    'title':'Pirates of MCA — capturas novas do catálogo',
    'version':'11.0.0-alpha.15',
    'source':'Minecraft 26.2 Fabric production client, isolated singleplayer GameTest',
    'capture_started_utc':datetime.fromtimestamp(LOG.stat().st_ctime,timezone.utc).isoformat(),
    'views':list(VIEWS),
    'skins':len(skins),
    'complete_armor_sets':sum(len(v)==4 for v in armor.values()),
    'extra_helmets':sum(len(v)==1 and 'helmet' in v for v in armor.values()),
    'screenshots':len(files),
    'images':{}
}
for dest,path in sorted(files.items()):
    manifest['images'][dest]={'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'bytes':path.stat().st_size}

OUT.parent.mkdir(parents=True,exist_ok=True)
tmp=OUT.with_suffix('.zip.tmp')
with zipfile.ZipFile(tmp,'w',compression=zipfile.ZIP_STORED,allowZip64=True) as z:
    for dest,path in sorted(files.items()): z.write(path,'piratesmca-catalogue-captures/'+dest)
    z.writestr('piratesmca-catalogue-captures/manifest.json',json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
    z.write(LOG,'piratesmca-catalogue-captures/execution.log')
    z.writestr('piratesmca-catalogue-captures/LEIA-ME.txt',
        'Capturas recém-geradas pelo cliente Minecraft 26.2 do projeto.\n'
        '20 skins e 1.091 configurações de armadura, cada uma em frente, lado e costas.\n'
        'Inclui 1.086 conjuntos completos e 5 capacetes avulsos.\n'
        'Estrutura: skins/<id>/<ângulo>.png e armor/<tipo>/<namespace>/<id>/<ângulo>.png.\n'
        'manifest.json lista SHA-256 e tamanho de cada imagem.\n')
tmp.replace(OUT)
print(f'PASS {OUT} screenshots={len(files)} bytes={OUT.stat().st_size} sha256={hashlib.sha256(OUT.read_bytes()).hexdigest()}')
