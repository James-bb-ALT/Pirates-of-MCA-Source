"""Create a verifiable partial-audit handoff, never a full-release certification."""
from pathlib import Path
import csv, hashlib, json, re, shutil, zipfile

ROOT=Path(__file__).resolve().parents[1]
version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M)[1]
OUT=ROOT/'delivery'/version
OUT.mkdir(parents=True,exist_ok=True)
def sha(file):return hashlib.sha256(file.read_bytes()).hexdigest()
jar=ROOT/f'build/libs/piratesmca-{version}.jar'
for log in ['isolated-48-final.log','fo-48-final.log']:
    text=(ROOT/'qa/results'/log).read_text(encoding='utf-8',errors='replace')
    shutdown_blocked=(log=='fo-48-final.log' and (ROOT/'qa/results/fo-shutdown-observation.json').is_file())
    if ('BUILD SUCCESSFUL' not in text and not shutdown_blocked) or 'after-server-restart count=48' not in text or 'AssertionError' in text:
        raise SystemExit(f'Final client run incomplete: {log}')
    if f'QA_ARTIFACT_SHA256 {sha(jar)} ' not in text:
        raise SystemExit(f'Final JAR differs from tested artifact: {log}')
restart=(ROOT/'qa/results/fo-full-process-restart.log').read_text(encoding='utf-8',errors='replace')
if 'QA_FULL_PROCESS_RESTART_CONFIRMED count=48' not in restart or 'BUILD SUCCESSFUL' not in restart or f'QA_ARTIFACT_SHA256 {sha(jar)} ' not in restart:
    raise SystemExit('Full-process restart not verified for current artifact')
repro=json.loads((ROOT/'qa/results/reproducibility.json').read_text())
if not repro['identical'] or repro['jar_sha256']!=sha(jar):
    raise SystemExit('Reproducibility not verified for current JAR')
rows=list(csv.DictReader((ROOT/'qa/TEST_CASES.csv').open(encoding='utf-8')))
if len(rows)!=62 or any(not row['notes'] for row in rows):
    raise SystemExit('Contract matrix incomplete')
for file in [jar,ROOT/f'build/libs/piratesmca-{version}-sources.jar',
             ROOT/'docs/RELATORIO_EXECUCAO_ALPHA6.md',ROOT/'qa/TEST_CASES.csv',
             ROOT/'CHANGELOG.md',ROOT/f'docs/RELEASE_NOTES_{version}.md']:
    shutil.copy2(file,OUT/file.name)

roots=['source','src','qa','docs','gradle','reference-jars','baseline']
root_files=['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore','README.md','CHANGELOG.md','SHA256SUMS']
files=[ROOT/file for file in root_files if (ROOT/file).is_file()]
for root in roots:
    for file in (ROOT/root).rglob('*'):
        if not file.is_file():continue
        rel=file.relative_to(ROOT)
        if 'fixtures' in rel.parts or '__pycache__' in rel.parts or file.suffix in {'.pyc','.class'}:continue
        files.append(file)
archive=OUT/f'piratesmca-{version}-workspace.zip'
with zipfile.ZipFile(archive,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
    for file in sorted(set(files)):
        name=file.relative_to(ROOT).as_posix()
        info=zipfile.ZipInfo(name,(2026,11,3,0,0,0))
        info.compress_type=zipfile.ZIP_DEFLATED
        info.external_attr=(0o100755 if name=='gradlew' else 0o100644)<<16
        z.writestr(info,file.read_bytes())
checksums={file.name:sha(file) for file in sorted(OUT.iterdir()) if file.is_file() and file.name!='SHA256SUMS.txt'}
(OUT/'SHA256SUMS.txt').write_text(''.join(f'{value}  {name}\n' for name,value in checksums.items()),encoding='utf-8')
print(json.dumps({'directory':str(OUT),'jar_sha256':sha(jar),'artifacts':len(checksums),'compatibility_status':'BLOCKED (FO shutdown hang)','contract_counts':{status:sum(row['status']==status for row in rows) for status in sorted({r['status'] for r in rows})}},indent=2))
