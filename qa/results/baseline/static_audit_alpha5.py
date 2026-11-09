#!/usr/bin/env python3
"""Source and JAR structural audit; no Minecraft launch or runtime-compatibility claims."""
from pathlib import Path
from zipfile import ZipFile
import json,hashlib,re,subprocess,sys
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'source'
JAR=ROOT/'baseline/PiratesMCA_Alpha5_CrewRuntime_FIX.jar'
results=[]
def check(category, condition, detail):
    results.append({'category':category,'result':'PASS' if condition else 'FAIL','detail':detail})
def issue(category, detail):
    results.append({'category':category,'result':'WARN','detail':detail})
with ZipFile(JAR) as z:
    check('JAR integrity',z.testzip() is None,'ZIP members CRC')
    paths=set(z.namelist())
    artifact_meta=json.loads(z.read('fabric.mod.json'))
    src_meta=json.loads((SRC/'src/main/resources/fabric.mod.json').read_text())
    check('Version match', artifact_meta['version']==src_meta['version']=='2.0.0-alpha.5',str((artifact_meta['version'],src_meta['version'])))
    check('No bundled game libs', not any(p.startswith(('net/minecraft/','net/fabricmc/','org/spongepowered/','com/mojang/')) for p in paths),'Java packages')
    png=[p for p in paths if p.startswith('assets/piratesmca/skins/clothing/') and p.endswith('.png')]
    check('Clothing count',len(png)==48,f'{len(png)} PNGs')
    trades=[p for p in paths if p.startswith('data/piratesmca/villager_trade/') and p.endswith('.json')]
    trade_sets=[p for p in paths if p.startswith('data/piratesmca/trade_set/') and p.endswith('.json')]
    check('Trade counts',len(trades)==20 and len(trade_sets)==5,f'{len(trades)} offers; {len(trade_sets)} trade sets')
    invalid=[]
    for name in paths:
        if name.endswith('.json'):
            try: json.loads(z.read(name))
            except Exception as e:invalid.append((name,str(e)))
            if name.startswith('data/piratesmca/') and b'minecraft:chain' in z.read(name):invalid.append((name,'legacy minecraft:chain'))
    check('JSON syntax / chain id',not invalid,str(invalid))
    check('CrewRuntime class', 'dev/arandu/piratesmca/CrewRuntime.class' in paths,'expected runtime class')
    jhash=hashlib.sha256(JAR.read_bytes()).hexdigest()
    issue('Runtime not tested',f'JAR SHA256 {jhash}; static audit cannot prove startup or gameplay')
script=(SRC/'build_offline.sh').read_text()
check('Reproducible build script version','alpha5' in script.lower() and 'alpha4' not in script.lower(), 'build_offline.sh must produce Alpha5, not Alpha4')
check('No stub-based release', 'generate_stubs.py' not in script and 'stubs/classes' not in script, 'release build must use real Fabric Loom dependencies')
check('Java target matches manifest','-source 25' in script or '--release 25' in script,'manifest requires Java>=25; old script uses -source 21 -target 21')
if shutil_available:=__import__('shutil').which('javap'):
    out=subprocess.run(['javap','-c','-p','-classpath',str(JAR),'dev.arandu.piratesmca.CrewRuntime'],capture_output=True,text=True)
    if out.returncode==0:
        check('ResourceKey.location crash regression','ResourceKey.location:' not in out.stdout,'CrewRuntime must not call stale descriptor ResourceKey.location')
    else:issue('javap',f'javap failed: {out.stderr[:180]}')
else:issue('javap','not installed; skipped bytecode inspection')
for res in results:print(f"{res['result']:4} [{res['category']}] {res['detail']}")
report={'baseline':'2.0.0-alpha.5','results':results,'counts':{r:sum(i['result']==r for i in results) for r in ('PASS','FAIL','WARN')}}
out=ROOT/'docs/RESULTADO_AUDITORIA_ESTATICA.json'
out.write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
print(f'Wrote {out}')
sys.exit(1 if report['counts']['FAIL'] else 0)
