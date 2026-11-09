"""Use the original sword sprites through vanilla's thin, extruded handheld pipeline."""
from pathlib import Path
import json

R=Path(__file__).resolve().parents[2]
RES=R/'source/src/main/resources'

def export():
    entries=json.loads((RES/'data/piratesmca/atelier/items.json').read_text())
    weapons=[e for e in entries if e['kind']=='weapon']
    for entry in weapons:
        ns,name=entry['id'].split(':')
        root=RES/'assets'/ns
        # The inventory model holds the original full weapon silhouette, not its voxel atlas.
        original=json.loads((root/'models/item'/f'{name}_inventory.json').read_text())
        texture=original['textures']['layer0']
        model={'parent':'minecraft:item/handheld','textures':{'layer0':texture}}
        definition={'model':{'type':'minecraft:model','model':f'{ns}:item/{name}'}}
        for path,data in [(root/'models/item'/f'{name}.json',model),(root/'items'/f'{name}.json',definition)]:
            text=json.dumps(data,indent=2)+'\n'
            if not path.exists() or path.read_text()!=text:path.write_text(text,encoding='utf-8')
    print(f'Exported {len(weapons)} vanilla handheld weapon sprites; hooks/armor untouched')

if __name__=='__main__':export()
