"""Import the supplied bilingual data; never execute included code or activate global MCA nodes."""
from pathlib import Path
import argparse,hashlib,json,zipfile
R=Path(__file__).resolve().parents[2]
RES=R/'source/src/main/resources'

def main():
    p=argparse.ArgumentParser();p.add_argument('--zip',type=Path,required=True);a=p.parse_args()
    with zipfile.ZipFile(a.zip) as z:
        assert z.testzip() is None
        pool=json.loads(z.read('spec/pool.json'));scenes=json.loads(z.read('spec/scenes.json'))
        assert len(pool['lines'])==216 and len(scenes['scenes'])==9
        assert all(x['pirateOnly'] for x in pool['lines']+scenes['scenes'])
        translations={lang:json.loads(z.read(f'assets/piratesmca/lang/{lang}.json')) for lang in ['en_us','pt_br']}
        assert set(translations['en_us'])==set(translations['pt_br']) and len(translations['en_us'])==279
        assert all(k.startswith(('piratesmca.dialogue.','dialogue.piratesmca_')) for k in translations['en_us'])
        source=R/'imports/pirate_dialogues_v1';source.mkdir(parents=True,exist_ok=True)
        for info in z.infolist():
            if info.is_dir() or info.filename.startswith('__pycache__/'):continue
            if not (info.filename in ['README.md','dialogues_bilingual.csv'] or info.filename.endswith('.json')):continue
            dest=(source/info.filename).resolve();assert dest.is_relative_to(source.resolve())
            dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(info))
        for name,data in [('pool',pool),('scenes',scenes)]:
            dest=RES/f'data/piratesmca/pirate_dialogues/{name}.json';dest.parent.mkdir(parents=True,exist_ok=True)
            dest.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        for lang,added in translations.items():
            dest=RES/f'assets/piratesmca/lang/{lang}.json';current=json.loads(dest.read_text(encoding='utf-8'));current.update(added)
            current.update(json.loads((R/f'definitions/dialogue_ui_{lang}.json').read_text(encoding='utf-8')))
            dest.write_text(json.dumps(current,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        (source/'INPUT_SHA256.txt').write_text(hashlib.sha256(a.zip.read_bytes()).hexdigest()+'  '+a.zip.name+'\n')
    print('PASS: 216 lines, 9 scenes, 279 translations/locale. Original MCA nodes archived only; no global main injection.')

if __name__=='__main__':main()
