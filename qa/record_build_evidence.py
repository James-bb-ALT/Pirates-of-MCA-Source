from pathlib import Path
import hashlib, json, shutil
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
first=json.loads((ROOT/'qa/results/reproducibility-first.json').read_text(encoding='utf-8-sig'))
def sha(file):return hashlib.sha256(file.read_bytes()).hexdigest()
jar=ROOT/'build/libs/piratesmca-2.1.0-alpha.6.jar'
sources=ROOT/'build/libs/piratesmca-2.1.0-alpha.6-sources.jar'
log=(ROOT/'qa/results/reproducibility-clean-build.log').read_text(encoding='utf-8',errors='replace')
assert 'BUILD SUCCESSFUL' in log and 'All 22 required tests passed' in log
methods=0
destination=ROOT/'qa/results/junit-final';destination.mkdir(exist_ok=True)
for file in (ROOT/'build/test-results/test').glob('TEST-*.xml'):
    tree=ET.parse(file).getroot()
    assert int(tree.attrib['failures'])==0 and int(tree.attrib['errors'])==0
    methods+=int(tree.attrib['tests'])
    shutil.copy2(file,destination/file.name)
result={
    'jar_sha256':sha(jar),'sources_sha256':sha(sources),
    'first_build':first,
    'identical':first['jar_sha256']==sha(jar) and first['sources_sha256']==sha(sources),
    'junit_methods':methods,'required_gametests':22,
    'gametest_note':'21 addon tests plus Fabric built-in empty fixture test',
    'second_build_log':'qa/results/reproducibility-clean-build.log',
}
assert result['identical'] and methods==15,result
(ROOT/'qa/results/reproducibility.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result,indent=2))
