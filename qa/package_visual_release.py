"""Package the local visual-fix candidate and editable sources; never invent approval.

No caches, Minecraft saves, personal profiles, stub builds or third-party optional
mods/resource packs are copied. Reference dependencies are the two supplied jars.
"""
from pathlib import Path
import hashlib,json,shutil,zipfile

ROOT=Path(__file__).resolve().parents[1]
VERSION='13.0.0-alpha.19'
OUT=ROOT/'delivery'/VERSION
OUT.mkdir(parents=True,exist_ok=True)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
expected=(ROOT/'reports/visual-fix/final-candidate.sha256').read_text().strip()
jar=ROOT/f'build/libs/piratesmca-{VERSION}.jar'
assert sha(jar)==expected,'Candidate changed; revalidation required'
for name in (f'piratesmca-{VERSION}.jar',f'piratesmca-{VERSION}-sources.jar'):
 source=ROOT/'build/libs'/name
 if not source.is_file():raise SystemExit('Missing build output: '+str(source))
 shutil.copy2(source,OUT/name)

report=ROOT/f'reports/visual-fix-{VERSION}.md'
shutil.copy2(report,OUT/report.name)
for source in (ROOT/'reports/visual-fix').glob('*'):
 if source.is_file() and source.suffix in {'.json','.csv','.txt','.log'}:
  dest=OUT/'technical-evidence'/source.name;dest.parent.mkdir(exist_ok=True);shutil.copy2(source,dest)
for source in ROOT.glob('reports/visual-fix-*.log'):
 dest=OUT/'technical-evidence'/source.name;dest.parent.mkdir(exist_ok=True);shutil.copy2(source,dest)

top_files=['README.md','CHANGELOG.md','build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat','.gitignore']
directories=['gradle','source/src','src/gametest','qa','scripts','models','definitions','textures','exports','art','docs','.agents/skills/pirates-mca-blockbench','imports/pirate_atelier_v5']
selected={ROOT/name for name in top_files if (ROOT/name).is_file()}
selected.update(p for directory in directories for p in (ROOT/directory).rglob('*') if p.is_file())
selected.update(p for p in (ROOT/'source').glob('*') if p.is_file() and (p.suffix in {'.java','.txt'}))
selected.update((ROOT/'reference-jars'/name) for name in ('mca-fabric-8.1.11+26.2.jar','fish_of_thieves-mc26.2-v26.2.2.0-fabric(3).jar'))
selected.add(report)
excluded_parts={'__pycache__','.pytest_cache','node_modules','fixtures','screenshots','.gradle','build','run'}
selected={p for p in selected if not excluded_parts.intersection(p.relative_to(ROOT).parts) and p.suffix not in {'.pyc','.class'}}
# The original import has resource-pack PNGs and editable provenance. Do not ship
# unrelated downloaded Minecraft jars, tool installs, or optional compat packs.
selected={p for p in selected if p.suffix!='.jar' or p.parent==ROOT/'reference-jars' or p.name=='gradle-wrapper.jar'}
manifest=[{'path':p.relative_to(ROOT).as_posix(),'bytes':p.stat().st_size,'sha256':sha(p)} for p in sorted(selected)]
archive=OUT/f'piratesmca-{VERSION}-source-editable.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 for p in sorted(selected):z.write(p,p.relative_to(ROOT))
 z.writestr('SOURCE_MANIFEST.json',json.dumps(manifest,indent=2,ensure_ascii=False)+'\n')

(OUT/'INSTALACAO.txt').write_text(f'''Pirates of MCA {VERSION} — candidato de correção visual

Instalação: faça backup do mundo. Substitua somente o JAR Pirates of MCA anterior
por piratesmca-{VERSION}.jar na pasta mods; não mantenha duas versões do addon.
Use Minecraft 26.2, Java 25, Fabric Loader 0.20.1, Fabric API 0.155.2+26.2,
MCA 8.1.11+26.2, Fish of Thieves 26.2.2.0 e YACL 3.9.7+26.2-fabric.
Os assets próprios já estão no JAR. Os packs Fresh e mods opcionais são externos;
este ZIP não os instala nem altera o perfil pessoal do launcher.

Build: extraia source-editable.zip; configure Java 25 (JAVA_HOME), Python/Pillow
e execute .\\gradlew.bat build ledgerRegressions ledgerSafetyRegressions.
O wrapper Gradle e os dois JARs de referência com hashes verificados estão incluídos.
O primeiro build precisa de rede para baixar Minecraft, Loom e bibliotecas.
Para reproduzir capturas, disponibilize os mods de compatibilidade fornecidos em
qa/fixtures/mca-skin-3d/mods e siga o relatório/build.gradle para os resource packs.
Não use stubs ou scripts de build históricos como validação.

Galeria: extraia todos os galeria-*.zip juntos e abra index.html. Extraia também os raw-*.zip na
mesma pasta para acessar os PNGs originais pelos links. Os hashes e o candidato
real de cada execução estão em manifest.csv/provenance.json. Recortes de navegação
não substituem os originais. Capturar um item não o aprova artisticamente.

Leia o relatório para PASS/FAIL/BLOCKED e limitações; não há aprovação integral
automática de todas as combinações. O mundo/instalação pessoal não foi substituído.
''',encoding='utf-8')
files=[p for p in OUT.rglob('*') if p.is_file() and p.name!='SHA256SUMS.txt' and 'visual-evidence' not in p.relative_to(OUT).parts]
(OUT/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.relative_to(OUT).as_posix()}\n' for p in sorted(files)),encoding='utf-8')
print(json.dumps({'jar_sha256':expected,'source_archive':str(archive),'source_files':len(manifest),'source_bytes':archive.stat().st_size,'output':str(OUT)},indent=2))
