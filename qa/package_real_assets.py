"""Build a compact archive of the game's real Atelier runtime assets and editable models."""
from pathlib import Path
import hashlib,json,zipfile

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'source/src/main/resources'
OUT=ROOT/'delivery/11.0.0-alpha.15/piratesmca-real-assets-v11.0.0-alpha.15.zip'
items={}
for source,dest in [
    (RES/'assets','assets'),
    (RES/'data/piratesmca/atelier','data/piratesmca/atelier'),
    (ROOT/'models/blockbench/atelier','models/blockbench/atelier'),
]:
    assert source.is_dir(),source
    for path in source.rglob('*'):
        if path.is_file(): items[(Path(dest)/path.relative_to(source)).as_posix()]=path
for source,dest in [
    (ROOT/'definitions/atelier_catalog.json','definitions/atelier_catalog.json'),
    (ROOT/'delivery/11.0.0-alpha.15/LICENSE_NEW_ASSETS.txt','LICENSE_NEW_ASSETS.txt')
]:
    assert source.is_file(),source
    items[dest]=source
assert len([p for p in items if p.startswith('assets/')])==20133
assert len([p for p in items if p.startswith('models/blockbench/atelier/')])==17

manifest={'name':'Pirates of MCA — runtime assets and editable Atelier models',
          'version':'11.0.0-alpha.15','source':'source/src/main/resources/assets',
          'runtime_asset_files':20133,'editable_bbmodels':17,'files':{}}
for name,path in sorted(items.items()):
    manifest['files'][name]={'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'bytes':path.stat().st_size}

OUT.parent.mkdir(parents=True,exist_ok=True)
tmp=OUT.with_suffix('.zip.tmp')
readme=('Pirates of MCA 11.0.0-alpha.15 — assets reais do jogo.\n'
        'Inclui os assets runtime completos (modelos, texturas, idiomas e definições),\n'
        'o catálogo de armaduras/skins, 17 fontes editáveis .bbmodel e a licença dos novos assets.\n'
        'A pasta assets/ corresponde ao conteúdo entregue em source/src/main/resources/assets.\n'
        'manifest.json contém SHA-256 e tamanho de cada arquivo.\n')
with zipfile.ZipFile(tmp,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=6,allowZip64=True) as z:
    for name,path in sorted(items.items()): z.write(path,name)
    z.writestr('README.txt',readme)
    z.writestr('manifest.json',json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
tmp.replace(OUT)
print(json.dumps({'PASS':True,'path':str(OUT),'files':len(items),'bytes':OUT.stat().st_size,
                  'sha256':hashlib.sha256(OUT.read_bytes()).hexdigest()},ensure_ascii=False))
