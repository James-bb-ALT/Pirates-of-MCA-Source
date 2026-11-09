"""Gate and package reconstruction evidence. Never turns capture coverage into visual approval."""
from pathlib import Path
import csv, hashlib, io, json, shutil, zipfile
import xml.etree.ElementTree as ET
from PIL import Image

R=Path(__file__).resolve().parents[2]
V='11.1.0-alpha.16'
O=R/'delivery'/V
REPORT=R/'reports/reconstruction'
sha=lambda p: hashlib.sha256(p.read_bytes()).hexdigest()

def write_json(p,data):
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')

def archive(dest,files):
    with zipfile.ZipFile(dest,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for p in sorted(set(files)):
            info=zipfile.ZipInfo(p.relative_to(R).as_posix(),(2026,11,5,0,0,0))
            info.compress_type=zipfile.ZIP_DEFLATED
            info.external_attr=(0o100755 if p.name=='gradlew' else 0o100644)<<16
            z.writestr(info,p.read_bytes())

def collect(names):
    return [p for n in names for p in (R/n).rglob('*') if p.is_file()
            and not any(x in p.parts for x in ['__pycache__','stubs','fixtures','.git'])
            and p.suffix not in {'.pyc','.class'}]

def main():
    jar=R/f'build/libs/piratesmca-{V}.jar'; sources=jar.with_name(f'piratesmca-{V}-sources.jar')
    backend=json.loads((R/'qa/results/reconstruction-tested-backend.json').read_text())
    with zipfile.ZipFile(jar) as z,zipfile.ZipFile(R/'delivery/11.0.0-alpha.15/piratesmca-11.0.0-alpha.15.jar') as old:
        assert all(hashlib.sha256(z.read(n)).hexdigest()==v for n,v in backend['entries'].items())
        assert not set(old.namelist())-set(z.namelist())
        assert all(old.read(n)==z.read(n) for n in old.namelist() if n.endswith('.png'))
        assert not any(n.startswith(('net/','org/','com/')) or n.endswith('.jem') or 'GameTest' in n or '/stubs/' in n for n in z.namelist())
    results=[]
    for mode in ['fa-wide-on','fa-wide-off','fa-slim-on','fa-slim-off']:
        name='reconstruction-ab-wide-on-retry.log' if mode=='fa-wide-on' else f'reconstruction-ab-{mode}.log'
        log=(R/'qa/results'/name).read_text(encoding='utf-8',errors='replace')
        for marker in ['BUILD SUCCESSFUL','RECON_TEMPORAL_PASS poses=16 frames=48','RECON_AVATAR_PASS poses=9 frames=27','RECON_NATIVE_ARMOR_FALLBACK_PASS',f'QA_ARTIFACT_SHA256 {sha(jar)}']:
            assert marker in log,(mode,marker)
        assert f'RECON_PACK_ASSERT_PASS FAPlayer={str(mode.endswith("on")).lower()}' in log
        expected_root='root=traben.entity_model_features.models.parts.EMFModelPartRoot' if mode.endswith('on') else 'root=net.minecraft.client.model.geom.ModelPart'
        assert expected_root in log and 'RECON_POST_ANIMATION kind=AVATAR' in log
        results.append({'mode':mode,'status':'PASS_EXECUTED_FIXTURES','log':name,'jar_sha256':sha(jar),'npc_poses':16,'player_poses':9})
    log=(R/'qa/results/reconstruction-catalogue.log').read_text(encoding='utf-8',errors='replace')
    assert 'BUILD SUCCESSFUL' in log and 'QA_CATALOG_CAPTURE_COMPLETE screenshots=3413' in log
    assert f'QA_ARTIFACT_SHA256 {sha(jar)}' in log
    assert log.count('QA_CATALOG_ARMOR_RESOLVED item=')==4349
    assert log.count('QA_CATALOG_SKIN_RESOLVED id=')==20
    for name in ['captain','fisherman']:
        e=json.loads((REPORT/f'blockbench-{name}/execution.json').read_text())
        assert not e['page_errors'] and e['results'][0]['status']=='PASS'
    base=(R/'qa/results/reconstruction-final-base.log').read_text(encoding='utf-8',errors='replace')
    assert 'All 78 required tests passed' in base and 'BUILD SUCCESSFUL' in base
    suites=[ET.parse(p).getroot() for p in (R/'build/test-results/test').glob('TEST-*.xml')]
    assert sum(int(s.get('tests',0)) for s in suites)==35
    assert not sum(int(s.get('failures',0))+int(s.get('errors',0)) for s in suites)
    assert 'Ran 9 tests' in base and 'RESULTADO: PASS' in base
    write_json(REPORT/'validation.json',{'version':V,'jar_sha256':sha(jar),'ab':results,'junit_tests':35,'existing_python_tests':9,'catalogue_captures':3413,'resolved_armor_pieces':4349,'resolved_skins':20,'backend_reuse':{'unchanged_entries':len(backend['entries']),'tested_jar_sha256':backend['jar_sha256'],'note':'78 GameTests ran on this earlier candidate. Final client-only delta verified byte-for-byte against its backend.'},'universal_visual_approval':False})

    final=REPORT/'final.md'
    text=final.read_text(encoding='utf-8')
    text=text.replace('STATUS: EM EXECUÇÃO. O fechamento desta versão depende de validation.json e do empacotador com gates; não distribuir este relatório como aprovação enquanto esse status permanecer.',
        'STATUS: PASS nos cenários executados; validação visual universal e cenários não executados permanecem abertos. Os gates de empacotamento confirmaram os quatro A/B, catálogo de 3413 novas capturas, 35 testes JVM sem falhas e preservação do backend que passou os 78 GameTests. validation.json registra o JAR exato. Não é uma certificação integral do modpack ou dos 62 casos originais.')
    final.write_text(text,encoding='utf-8')
    for name,statement in [('phase2.md','Fechamento: catálogo completo capturado, binding das 4349 peças confirmado e quatro cenários A/B aprovados. Glint/trim nativos exercitados nas fixtures de jogador. A pendência de revisão artística universal permanece; os parágrafos anteriores registram o estado histórico desta fase.'),('phase4.md','Fechamento: as vinte skins também foram capturadas vestidas em três vistas. O catálogo completo e os hashes estão em validation.json/capture-manifest.json; a indicação de execução pendente acima era o estado anterior ao gate final.')]:
        p=REPORT/name;s=p.read_text(encoding='utf-8')
        if statement not in s:p.write_text(s+'\n'+statement+'\n',encoding='utf-8')
    O.mkdir(parents=True,exist_ok=True)
    for p in [jar,sources,R/'CHANGELOG.md',REPORT/'final.md']:
        shutil.copy2(p,O/p.name)
    # All final captures are newly produced by Minecraft, never taken from the audit ZIP.
    records={}; run=R/'run/reconstruction-catalogue/screenshots'
    captures=sorted(run.glob('*_CATALOG-*.png'))
    assert len(captures)==3413,len(captures)
    capture_parts=O/'captures';capture_parts.mkdir(exist_ok=True)
    groups={}
    for p in captures:
        label=p.stem.split('_CATALOG-',1)[1]
        if label.startswith('ARMOR-'):
            group='armor';kind='armor'
        else:group='skins';kind='skins'
        groups.setdefault(group,[]).append((label,p,kind))
    part_index=[]
    for group,items in groups.items():
        # Keep each archive small enough for convenient uploads; no multipart ZIP dependency.
        for start in range(0,len(items),750):
            part=f'{group}-{start//750+1:02d}'
            path=capture_parts/f'piratesmca-{V}-{part}.zip';manifest={}
            with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
                for label,p,kind in items[start:start+750]:
                    with Image.open(p) as im:
                        assert im.size==(854,480)
                        # Fixed camera; retain ground, plume and margin. Head closeups need wider crop.
                        crop=(290,105,564,330) if label.endswith('-head') else (290,105,564,360)
                        im=im.convert('RGB').crop(crop);b=io.BytesIO();im.save(b,'JPEG',quality=90,subsampling=0,optimize=True)
                    data=b.getvalue();name=label+'.jpg';z.writestr(name,data)
                    priority=label.startswith(('ARMOR-pirateatelier_captain_armor_pristine_normal-','ARMOR-pirateatelier_fisherman_teal_pristine_peg_left-','ARMOR-lfpirates_corsair_tier_1-')) or (label.startswith('SKIN-') and not label.startswith('SKIN-EQUIPPED-') and label.endswith('-front'))
                    record={'source':p.relative_to(R).as_posix(),'source_sha256':sha(p),'jpeg_sha256':hashlib.sha256(data).hexdigest(),'crop':crop,'archive':path.name,'visual_review':'PRIORITY_COMPARISON_SCREENED' if priority else 'NOT_EXECUTED_individual_visual_review','capture_binding':'PASS'}
                    manifest[name]=record;records[label]=record
                z.writestr('manifest.json',json.dumps(manifest,indent=2))
            assert path.stat().st_size<100*1024*1024
            part_index.append({'file':path.name,'images':len(manifest),'bytes':path.stat().st_size,'sha256':sha(path)})
    write_json(capture_parts/'index.json',{'captures':3413,'parts':part_index,'originals':'run/reconstruction-catalogue/screenshots','note':'JPEG crops of fresh final Minecraft PNGs; source hashes retained. Coverage is not universal visual approval.'})
    write_json(REPORT/'capture-manifest.json',records)
    # One row for every final capture, with explicit limits instead of invented per-outfit approval.
    with (REPORT/'outfit-skin-results.csv').open('w',newline='',encoding='utf-8-sig') as f:
        w=csv.writer(f);w.writerow(['capture','capture_binding','visual_review','archive','source_sha256'])
        for label,d in sorted(records.items()):w.writerow([label,d['capture_binding'],d['visual_review'],d['archive'],d['source_sha256']])
    before=R/'build/atelier-chatgpt-captures-20261105'
    audit=list(csv.DictReader((REPORT/'inventario_diagnostico_3333_imagens.csv').open(encoding='utf-8-sig'),delimiter=';'))
    assert len(audit)==3333
    with (REPORT/'diagnostic-followup.csv').open('w',newline='',encoding='utf-8-sig') as f:
        w=csv.writer(f);w.writerow(['original_image','original_sha256','diagnostic_ids','final_capture','final_source_sha256','capture_binding','full_visual_review'])
        for row in audit:
            parts=row['imagem'].split('/');view=Path(parts[-1]).stem
            label=f'ARMOR-{parts[1]}_{parts[2]}-{view}' if parts[0]=='armor' else f'SKIN-{parts[1]}-{view}'
            d=records[label]
            if before.exists():assert sha(before/row['imagem'])==row['sha256']
            w.writerow([row['imagem'],row['sha256'],row['ids_de_falha'],label,d['source_sha256'],'PASS',d['visual_review']])
    archive(O/f'piratesmca-{V}-real-assets.zip',collect(['source/src/main/resources/assets','source/src/main/resources/data/piratesmca/atelier','models/blockbench','definitions','scripts/pmca']))
    # Preserve editable sources and original conversion references for a real offline source handoff.
    roots=['source/src','src','models','textures','exports','art','definitions','scripts/pmca','gradle','reference-jars','baseline','docs','reports/reconstruction','imports/pirate_atelier_v5/development_assets','qa','.agents/skills/pirates-mca-blockbench']
    files=collect(roots)+[R/n for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','README.md','CHANGELOG.md','.gitignore','source/tests_ledger.java','source/LICENCE.txt','imports/pirate_atelier_v5/LICENSE_NEW_ASSETS.txt']]
    archive(O/f'piratesmca-{V}-workspace.zip',files)
    evidence=collect(['reports/reconstruction'])+[p for p in (R/'qa/results').glob('reconstruction*') if p.is_file()]
    archive(O/f'piratesmca-{V}-reports.zip',evidence)
    # Reference before captures remain separate from newly produced final captures.
    if before.exists():archive(O/f'piratesmca-{V}-before-reference.zip',[p for p in before.rglob('*.jpg')])
    for mode in ['fa-wide-on','fa-wide-off','fa-slim-on','fa-slim-off']:
        archive(capture_parts/f'animation-{mode}.zip',list((R/f'run/reconstruction-{mode}/screenshots').glob('*.png')))
    (O/'LEIA-ME.md').write_text(f'Pirates of MCA {V}\n\nInstale somente piratesmca-{V}.jar, substituindo a versão antiga. Não instale sources/gametest nem duas versões.\nMinecraft26.2 / Java25 / Fabric Loader0.20.1 / MCA8.1.11+26.2 / Fish of Thieves26.2.2.0. Dependências mantidas; assets embutidos.\n\nLeia final.md para testes executados, falhas de preparação, limites e cenários não executados. Os ZIPs de capturas são independentes, com manifesto e hashes dos PNGs originais. Aprovação de captura/binding não significa inspeção artística universal.\n',encoding='utf-8')
    all_files=sorted(p for p in O.rglob('*') if p.is_file() and p.name!='SHA256SUMS.txt')
    (O/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.relative_to(O).as_posix()}\n' for p in all_files))
    print(json.dumps({'version':V,'jar_sha256':sha(jar),'captures':len(records),'output':str(O)},ensure_ascii=False))

if __name__=='__main__':main()
