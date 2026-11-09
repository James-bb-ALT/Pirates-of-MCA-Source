"""Repo-native block model; uses existing Minecraft textures, no external art."""
from pathlib import Path
import json
R=Path(__file__).resolve().parents[1]/'source/src/main/resources'
def write(path,value):
    file=R/path;file.parent.mkdir(parents=True,exist_ok=True);file.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def box(start,end,texture,rotation=None):
    value={'from':start,'to':end,'faces':{face:{'texture':'#'+texture} for face in ['north','south','east','west','up','down']}}
    if rotation:value['rotation']=rotation
    return value
elements=[box([7,0,7],[9,16,9],'pole'),box([.5,7,7.6],[7,15,8.4],'cloth'),box([7,15,7],[9,16,9],'tip'),
    box([3,11.5,7.4],[6,14,8.6],'bone'),box([3.4,12.5,7.35],[4.2,13.3,8.65],'cloth'),box([4.8,12.5,7.35],[5.6,13.3,8.65],'cloth'),box([4,10.8,7.4],[5,11.5,8.6],'bone')]
for angle in [-45,45]:elements.append(box([2,9,7.4],[6.5,9.6,8.6],'bone',{'origin':[4.25,9.3,8],'axis':'z','angle':angle}))
write(Path('assets/piratesmca/models/block/pirate_flag.json'),{'parent':'minecraft:block/block','textures':{'particle':'minecraft:block/black_wool','pole':'minecraft:block/oak_log','cloth':'minecraft:block/black_wool','bone':'minecraft:block/white_wool','tip':'minecraft:block/gold_block'},'elements':elements})
write(Path('assets/piratesmca/blockstates/pirate_flag.json'),{'variants':{f'facing={direction}':{'model':'piratesmca:block/pirate_flag','y':angle} for direction,angle in [('north',0),('east',90),('south',180),('west',270)]}})
write(Path('assets/piratesmca/items/pirate_flag.json'),{'model':{'type':'minecraft:model','model':'piratesmca:block/pirate_flag'}})
write(Path('data/piratesmca/recipe/pirate_flag.json'),{'type':'minecraft:crafting_shaped','category':'building','pattern':['BBW','BBS','  S'],'key':{'B':'minecraft:black_wool','W':'minecraft:bone','S':'minecraft:stick'},'result':{'id':'piratesmca:pirate_flag','count':1}})
write(Path('data/piratesmca/loot_table/blocks/pirate_flag.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'piratesmca:pirate_flag'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
en={'title':'Crew flag','requirements':'Nearby volunteers: %s. You need 2 free adult pirates within 14 blocks, at least 0 MCA hearts, morale 35+ and fear below 80. Meet them first; pirates in a brawl will not join.',
    'found':'Found crew','link':'Link my crew','link_help':'Link your crew to this flag. It becomes the rally point.', 'members':'Crew members: %s',
    'orders_help':'Rally and guard use this flag, within 64 blocks. Sailors may refuse orders.', 'other_crew':'This flag belongs to another crew. Only its captain can command here.',
    'refresh':'Refresh','close':'Close','follow':'Follow captain','hold':'Hold position','rally':'Rally at flag','guard':'Guard flag','retreat':'Retreat','cease_fight':'Stop brawls','dismiss':'Dismiss order','roster':'View roster',
    'too_far':'Approach a visible crew flag (within 6 blocks).','occupied':'This flag is claimed, or you do not have authority to link it.','not_enough':'Two volunteers are required. Befriend the pirates and try again.',
    'founded':'Crew founded! You are the captain; two volunteers joined your flag.','linked':'Crew flag linked. Rally and guard now use this point.'}
pt={'title':'Bandeira da tripulação','requirements':'Voluntários próximos: %s. Precisa de 2 piratas adultos livres em até 14 blocos, ao menos 0 corações MCA, moral 35+ e medo abaixo de 80. Conheça-os primeiro; quem está em briga não aceita.',
    'found':'Fundar tripulação','link':'Vincular minha tripulação','link_help':'Vincule sua tripulação a esta bandeira. Ela será o ponto de reunião.','members':'Tripulantes: %s',
    'orders_help':'Reunir e defender usam esta bandeira, até 64 blocos. Piratas podem recusar ordens.','other_crew':'Esta bandeira pertence a outra tripulação. Só o capitão dela pode comandar aqui.',
    'refresh':'Atualizar','close':'Fechar','follow':'Seguir capitão','hold':'Manter posição','rally':'Reunir na bandeira','guard':'Defender bandeira','retreat':'Recuar','cease_fight':'Apartar brigas','dismiss':'Encerrar ordem','roster':'Ver tripulantes',
    'too_far':'Aproxime-se de uma bandeira visível (até 6 blocos).','occupied':'Esta bandeira já tem dono, ou você não tem autoridade para vinculá-la.','not_enough':'São necessários dois voluntários. Faça amizade com os piratas e tente novamente.',
    'founded':'Tripulação fundada! Você é o capitão; dois voluntários se juntaram à bandeira.','linked':'Bandeira vinculada. Reunir e defender agora usam este ponto.'}
for lang,values,name in [('en_us',en,'Pirate flag'),('pt_br',pt,'Bandeira pirata')]:
    file=Path(f'assets/piratesmca/lang/{lang}.json');current=json.loads((R/file).read_text(encoding='utf-8'))
    current['block.piratesmca.pirate_flag']=name;current.update({'piratesmca.flag.'+key:value for key,value in values.items()});write(file,current)
print('Flag model, item, recipe, loot and translations generated.')
