"""Read-only static biome/tag inventory of the user's selected profile."""
from pathlib import Path
import argparse,collections,json,re,zipfile
p=argparse.ArgumentParser();p.add_argument('profile',type=Path);a=p.parse_args()
out=Path('qa/results/pack-biomes');out.mkdir(parents=True,exist_ok=True)
biomes={};tags=collections.defaultdict(list);mods=[];structures=[]
def consume(name,raw,source):
    m=re.fullmatch(r'data/([^/]+)/worldgen/biome/(.+)\.json',name)
    t=re.fullmatch(r'data/([^/]+)/tags/worldgen/biome/(.+)\.json',name)
    s=re.fullmatch(r'data/([^/]+)/worldgen/structure/(.+)\.json',name)
    if not(m or t or s):return
    try:d=json.loads(raw)
    except (ValueError,UnicodeError):return
    if m:biomes[m[1]+':'+m[2]]=source
    if t:
        key=t[1]+':'+t[2]
        if d.get('replace'):tags[key]=[]
        tags[key].extend(d.get('values',[]))
    if s:structures.append({'id':s[1]+':'+s[2],'biomes':d.get('biomes'),'source':source})
def scan(file):
    try:
        with zipfile.ZipFile(file) as z:
            if 'fabric.mod.json' in z.namelist():
                try:
                    m=json.loads(z.read('fabric.mod.json'));mods.append({'id':m.get('id'),'version':m.get('version'),'file':file.name})
                except ValueError:pass
            for name in z.namelist():
                if name.startswith('data/') and name.endswith('.json'):consume(name,z.read(name),file.name)
    except zipfile.BadZipFile:pass
scan(Path.home()/'.gradle/caches/fabric-loom/26.2/minecraft-merged.jar')
for f in sorted((a.profile/'mods').glob('*.jar')):scan(f)
for root in [a.profile/'datapacks',*sorted((a.profile/'saves').glob('*/datapacks'))]:
    for f in sorted(root.glob('*.zip')):scan(f)
    for f in sorted(root.glob('*/data/**/*.json')):consume('data/'+str(f).split('data\\',1)[-1].replace('\\','/'),f.read_bytes(),str(f.parent))
def resolve(key,seen=()):
    if key in seen:return set()
    result=set()
    for value in tags.get(key,[]):
        value=value.get('id','') if isinstance(value,dict) else value
        if value.startswith('#'):result.update(resolve(value[1:],seen+(key,)))
        elif value:result.add(value)
    return result
coastal={k:sorted(resolve(k)) for k in sorted(tags) if any(w in k for w in ['ocean','beach','shore','coast'])}
summary={'profile':str(a.profile),'active_jar_count':len(list((a.profile/'mods').glob('*.jar'))),'mods':mods,'biome_definitions':biomes,'coastal_tags':coastal,'structures':structures,'limitations':['Static resources only; Java-registered biomes may be absent.','World datapacks scanned as candidates; enabled order not established.','No personal profile or save was modified.']}
(out/'inventory.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'active_jars':summary['active_jar_count'],'biome_definitions':len(biomes),'by_namespace':dict(collections.Counter(b.split(':')[0] for b in biomes)),'coastal_tags':coastal,'pirates':[m for m in mods if m['id']=='piratesmca']},ensure_ascii=False,indent=2))
