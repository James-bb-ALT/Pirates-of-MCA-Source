"""Package only completed, counted client runs. Execution PASS is not artistic approval.
Original screenshots are read unchanged and split into ordinary ZIPs below 95 MiB.
"""
from pathlib import Path
import argparse,csv,hashlib,json,re,zipfile
from PIL import Image,ImageDraw

ROOT=Path(__file__).resolve().parents[1]
VERSION='13.0.0-alpha.19'
parser=argparse.ArgumentParser();parser.add_argument('stages',nargs='+');parser.add_argument('--raw',action='store_true');args=parser.parse_args()
OUT=ROOT/'delivery'/VERSION/'visual-evidence';OUT.mkdir(parents=True,exist_ok=True)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=ROOT/f'build/libs/piratesmca-{VERSION}.jar'
artifact_hash=sha(jar)
reuse_path=ROOT/'reports/visual-fix/on-matrix-reuse.json'
reuse=json.loads(reuse_path.read_text()) if reuse_path.exists() else None
rows=[];runs=[]
for stage in args.stages:
 log=ROOT/f'reports/visual-fix-{stage}.log';text=log.read_text(encoding='utf-8',errors='replace')
 if 'BUILD SUCCESSFUL' not in text:raise SystemExit('Incomplete/failed run: '+stage)
 match=re.findall(r'QA_ARTIFACT_SHA256 ([0-9a-f]{64}) ',text)
 if not match:raise SystemExit('No tested JAR identity: '+stage)
 reused=reuse and reuse.get('reusable_gates',{}).get(stage)==match[-1] and reuse['final_jar_sha256']==artifact_hash
 baseline=stage=='before-on' and match[-1]==sha(ROOT/'reports/visual-fix/before-alpha18.jar')
 if match[-1]!=artifact_hash and not reused and not baseline:raise SystemExit('Run used another candidate JAR without preservation proof: '+stage)
 if 'QA_VISUAL_AUDIT_COMPLETE' in text:
  total=int(re.findall(r'QA_VISUAL_AUDIT_COMPLETE[^\n]*shots=(\d+)',text)[-1])
  if 'RECON_AVATAR_PASS' in text:total+=27
 elif 'QA_VISUAL_VARIANTS_COMPLETE' in text:total=int(re.findall(r'QA_VISUAL_VARIANTS_COMPLETE[^\n]*shots=(\d+)',text)[-1])
 elif 'QA_FACE_CAPTURE_COMPLETE' in text:total=10
 elif 'QA_HELD_WEAPONS_COMPLETE' in text:total=int(re.findall(r'QA_HELD_WEAPONS_COMPLETE[^\n]*shots=(\d+)',text)[-1])
 elif 'QA_PROTOTYPES_COMPLETE' in text:total=int(re.findall(r'QA_PROTOTYPES_COMPLETE[^\n]*shots=(\d+)',text)[-1])
 elif 'RECON_AVATAR_PASS' in text:total=27
 else:raise SystemExit('No completed capture marker: '+stage)
 candidates={}
 for p in (ROOT/f'run/visual-fix-{stage}/screenshots').glob('*.png'):
  m=re.fullmatch(r'(\d+)_((?:AUDIT|HEAD|RECON)-.+)\.png',p.name)
  if m and (m[2] not in candidates or p.stat().st_mtime_ns>candidates[m[2]][0]):candidates[m[2]]=(p.stat().st_mtime_ns,p)
 if len(candidates)!=total:raise SystemExit(f'Coverage mismatch {stage}: {len(candidates)} unique PNGs, expected {total}')
 epoch=re.findall(r'QA_VISUAL_CAPTURE_BEGIN epoch_ms=(\d+)',text)
 if epoch and any(p.stat().st_mtime_ns<int(epoch[-1])*1_000_000 for _,p in candidates.values()):
  raise SystemExit('Stale capture selected for '+stage)
 for label,(_,p) in sorted(candidates.items(),key=lambda pair:pair[1][0]):
  gallery=OUT/'gallery'/stage/(label+'.webp');gallery.parent.mkdir(parents=True,exist_ok=True)
  with Image.open(p) as image:
   # Crop only. Keep raw camera frames, lighting and source PNGs unchanged.
   cropped=image.convert('RGB') if label.startswith('HEAD-') else image.convert('RGB').crop((247,115,607,452))
   cropped.save(gallery,quality=90,method=4)
  kind=label.removeprefix('AUDIT-').split('-',1)[0]
  row={'stage':stage,'label':label,'kind':kind,'gallery':gallery.relative_to(OUT).as_posix(),
       'raw':f'raw/{stage}/{p.name}','workspace_original':p.relative_to(ROOT).as_posix(),'sha256_png':sha(p)}
  rows.append(row)
 runs.append({'stage':stage,'screenshots':total,'jar_sha256':match[-1],'reused_unchanged_subsystems':bool(reused),'baseline_before_correction':bool(baseline),
              'log_sha256':sha(log),'fresh_pack_line':next((line for line in text.splitlines() if 'QA_VISUAL_ACTIVE_PACKS' in line),'')})
 logs=OUT/'logs';logs.mkdir(exist_ok=True);(logs/log.name).write_bytes(log.read_bytes())

with (OUT/'manifest.csv').open('w',encoding='utf-8-sig',newline='') as f:
 writer=csv.DictWriter(f,fieldnames=list(rows[0]));writer.writeheader();writer.writerows(rows)
(OUT/'provenance.json').write_text(json.dumps({'version':VERSION,'jar_sha256':artifact_hash,'runs':runs,
 'screenshots':len(rows),'original_pngs_unmodified':True,'universal_visual_approval':False},indent=2,ensure_ascii=False)+'\n',encoding='utf-8')

SHEETS=OUT/'contact-sheets';SHEETS.mkdir(exist_ok=True)
for stage in args.stages:
 for kind in sorted({r['kind'] for r in rows if r['stage']==stage}):
  selected=[r for r in rows if r['stage']==stage and r['kind']==kind and (r['label'].endswith('-FRONT') or kind=='HEAD')]
  for page in range(0,len(selected),60):
   chunk=selected[page:page+60];board=Image.new('RGB',(750,175*((len(chunk)+4)//5)),(235,235,235));draw=ImageDraw.Draw(board)
   for i,r in enumerate(chunk):
    with Image.open(OUT/r['gallery']) as im:
     im.thumbnail((150,145));x,y=(i%5)*150,(i//5)*175;board.paste(im,(x+(150-im.width)//2,y));draw.text((x+2,y+148),r['label'].removeprefix('AUDIT-')[:23],fill='black')
   board.save(SHEETS/f'{stage}-{kind.lower()}-{page//60+1:02}.jpg',quality=92)

html='''<!doctype html><html lang="pt-BR"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Pirates of MCA — capturas reais</title><style>body{font:16px system-ui;margin:24px;background:#141820;color:#eee}h1{font-size:24px}input,select,button{font:inherit;padding:9px;margin:4px;background:#242c37;color:#fff;border:1px solid #66717d;border-radius:5px}.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:14px}.card{padding:8px;background:#222a34;overflow-wrap:anywhere}.card img{width:100%;height:240px;object-fit:contain}.card small{display:block}a{color:#a8d9ff}dialog{background:#18212b;color:white;max-width:95vw;border:1px solid #8191a0}dialog img{max-width:80vw;max-height:72vh;object-fit:contain}</style>
<h1>Pirates of MCA — capturas reais</h1><p>Galeria de revisão. A captura de um modelo não equivale à sua aprovação artística. PNGs originais e SHA256 no manifesto.</p>
<select id="stage" aria-label="Execução"></select><select id="kind" aria-label="Categoria"></select><input id="q" placeholder="Ex.: first_mate RUN FRONT" aria-label="Buscar capturas"><button id="prev">Anterior</button><button id="next">Próxima</button><p id="count"></p><div class="grid" id="grid"></div>
<dialog id="viewer"><button id="close">Fechar</button><button id="back">Quadro anterior</button><button id="forward">Quadro seguinte</button><p id="label"></p><img id="large"><p><a id="raw">Abrir PNG original</a></p><small id="hash"></small></dialog>
<script>const entries=__DATA__;let page=0,filtered=[],current=0;const $=id=>document.getElementById(id);
function options(id,values,label){const first=document.createElement('option');first.value='';first.textContent=label;$(id).append(first);for(const value of values){const option=document.createElement('option');option.value=value;option.textContent=value;$(id).append(option)}}
options('stage',[...new Set(entries.map(e=>e.stage))],'Todas as execuções');options('kind',[...new Set(entries.map(e=>e.kind))],'Todas as categorias');
function render(){const words=$('q').value.toLowerCase().trim().split(/\\s+/).filter(Boolean);filtered=entries.filter(e=>(!$('stage').value||e.stage===$('stage').value)&&(!$('kind').value||e.kind===$('kind').value)&&words.every(w=>e.label.toLowerCase().includes(w)));const max=Math.max(0,Math.ceil(filtered.length/50)-1);page=Math.max(0,Math.min(page,max));$('count').textContent=filtered.length+' capturas · página '+(page+1)+' de '+(max+1);$('grid').replaceChildren();for(const [offset,e] of filtered.slice(page*50,page*50+50).entries()){const card=document.createElement('article');card.className='card';const button=document.createElement('button');button.style.width='100%';const img=document.createElement('img');img.loading='lazy';img.src=e.gallery;img.alt=e.label;button.append(img);button.onclick=()=>show(page*50+offset);const title=document.createElement('small');title.textContent=e.label;const stage=document.createElement('small');stage.textContent=e.stage;card.append(button,title,stage);$('grid').append(card)}}
function show(index){current=(index+filtered.length)%filtered.length;const e=filtered[current];$('large').src=e.gallery;$('large').alt=e.label;$('label').textContent=e.stage+' / '+e.label;$('raw').href=e.raw;$('hash').textContent=e.sha256_png;if(!$('viewer').open)$('viewer').showModal()}
for(const id of ['stage','kind','q'])$(id).oninput=()=>{page=0;render()};$('prev').onclick=()=>{page--;render()};$('next').onclick=()=>{page++;render()};$('close').onclick=()=>$('viewer').close();$('back').onclick=()=>show(current-1);$('forward').onclick=()=>show(current+1);document.addEventListener('keydown',e=>{if($('viewer').open&&e.key==='ArrowRight')show(current+1);if($('viewer').open&&e.key==='ArrowLeft')show(current-1)});render();</script></html>'''
(OUT/'index.html').write_text(html.replace('__DATA__',json.dumps(rows,ensure_ascii=False)),encoding='utf-8')
(OUT/'LEIA-ME.txt').write_text('Extraia todas as partes galeria-*.zip juntas e abra index.html para filtrar por execução, modelo, vista e ação.\nOs recortes WebP servem para navegação. Não substituem os PNGs originais.\nExtraia todos os raw-*.zip nesta pasta para ativar os links Abrir PNG original.\nmanifest.csv vincula cada PNG ao hash e à captura original do workspace.\nResultados técnicos/artísticos ficam no relatório; a galeria não concede PASS.\n',encoding='utf-8')
archive=OUT.parent/f'piratesmca-{VERSION}-galeria.zip'
gallery_files=[p for p in sorted(OUT.rglob('*')) if p.is_file()]
if sum(p.stat().st_size for p in gallery_files)<=95*1024*1024:
 with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
  for p in gallery_files:z.write(p,p.relative_to(OUT))
 gallery_archives=[archive]
else:
 # Ordinary independent ZIPs: extract every part into the same directory.
 gallery_archives=[];part=0;size=0;z=None
 try:
  for p in gallery_files:
   if z is None or size+p.stat().st_size>95*1024*1024:
    if z:z.close()
    part+=1;size=0;target=OUT.parent/f'piratesmca-{VERSION}-galeria-{part:02}.zip';gallery_archives.append(target)
    z=zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=6)
   z.write(p,p.relative_to(OUT));size+=p.stat().st_size
 finally:
  if z:z.close()
if args.raw:
 part=0;size=0;z=None
 try:
  for row in rows:
   p=ROOT/row['workspace_original']
   if z is None or size+p.stat().st_size>95*1024*1024:
    if z:z.close()
    part+=1;size=0;z=zipfile.ZipFile(OUT.parent/f'piratesmca-{VERSION}-raw-{part:02}.zip','w',zipfile.ZIP_DEFLATED,compresslevel=6)
   z.write(p,row['raw']);size+=p.stat().st_size
 finally:
  if z:z.close()
print('Packaged',len(rows),'counted original captures; gallery parts=',[(p.name,sha(p)) for p in gallery_archives])
