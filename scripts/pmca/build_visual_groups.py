"""Choose one runtime representative per distinct geometry+UV, retaining alias coverage.
Texture aliases are inventoried, not awarded an artistic PASS by this grouping.
"""
from pathlib import Path
import json,hashlib,collections
ROOT=Path(__file__).resolve().parents[2]
RES=ROOT/'source/src/main/resources'
groups=collections.defaultdict(list)
for p in sorted((RES/'assets/piratesmca/wardrobe').rglob('*.json')):
 data=json.loads(p.read_text())
 canonical=json.dumps({k:data[k] for k in ('textureSize','slots')},sort_keys=True,separators=(',',':'))
 group=hashlib.sha256(canonical.encode()).hexdigest()
 asset=p.parent.name+':'+p.stem
 groups[group].append(asset)
out=[{'asset':aliases[0],'sha256_geometry_uv':key,'aliases':aliases} for key,aliases in groups.items()]
target=ROOT/'src/gametest/resources/data/piratesmca-tests/visual_groups.json'
target.parent.mkdir(parents=True,exist_ok=True)
target.write_text(json.dumps(out,indent=2)+'\n')
print(f'Visual QA representatives={len(out)}; inventory={sum(len(g["aliases"]) for g in out)}; no visual approval inferred')
