"""Audit built JAR against source metadata; runtime tests remain separate evidence."""
import hashlib,json,struct,sys
from pathlib import Path
from zipfile import ZipFile

ROOT=Path(__file__).resolve().parents[1]
meta=json.loads((ROOT/'source/src/main/resources/fabric.mod.json').read_text())
jar=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'build/libs'/f'piratesmca-{meta["version"]}.jar'
with ZipFile(jar) as z:
    assert z.testzip() is None
    names=z.namelist()
    assert json.loads(z.read('fabric.mod.json'))==meta,'metadata mismatch'
    assert json.loads(z.read('piratesmca.mixins.json'))['compatibilityLevel']=='JAVA_25'
    assert not any(n.startswith(('net/','com/mojang/','org/spongepowered/','org/junit/')) for n in names),'bundled foreign classes'
    assert not any('GameTest' in n or 'Regressions' in n or 'stubs/' in n for n in names),'bundled test/stub'
    assert len([n for n in names if n.endswith('.png') and '/skins/clothing/' in n])==48
    assert len([n for n in names if n.endswith('.json') and '/villager_trade/pirate/' in n and '/tags/' not in n])==20
    assert len([n for n in names if n.endswith('.json') and '/trade_set/pirate/' in n])==5
    assert 'LICENSE' in names
    for n in names:
        if n.endswith('.json'):
            json.loads(z.read(n))
            assert b'minecraft:chain"' not in z.read(n),n
        if n.endswith('.class'):
            assert struct.unpack('>H',z.read(n)[6:8])[0]==69,(n,'not Java 25')
    for gender in ('male','female'):
        for entry in json.loads(z.read(f'data/piratesmca/skins/clothing/{gender}.json')):
            ns,path=entry['id'].split(':',1)
            assert 'assets/'+ns+'/'+path in names,entry
    for level in range(1,6):
        trade_set=json.loads(z.read(f'data/piratesmca/trade_set/pirate/level_{level}.json'))
        assert trade_set['trades']==f'#piratesmca:pirate/level_{level}'
        for entry in json.loads(z.read(f'data/piratesmca/tags/villager_trade/pirate/level_{level}.json'))['values']:
            assert 'data/piratesmca/villager_trade/'+entry.split(':',1)[1]+'.json' in names,entry
print(f'PASS release structure {jar.name}; SHA256 {hashlib.sha256(jar.read_bytes()).hexdigest()}')
print('No startup, gameplay or visual claim is implied by this structural audit.')
