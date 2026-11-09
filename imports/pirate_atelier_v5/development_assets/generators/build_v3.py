#!/usr/bin/env python3
"""Pirate Atelier v3: original painted outfit atlas/voxel CEM from supplied concept sheets.
Keeps v2 unchanged and adds new inventory-equipment mockup + player geometry library.
Does NOT contain mod runtime or promise automatic EMF per-item model switching.
"""
import io,json,hashlib,random,zipfile,pathlib,math,collections
from PIL import Image,ImageDraw,ImageFont
ROOT=pathlib.Path('/mnt/data')
V2=ROOT/'Pirate_Atelier_CleanRoom_Player_3D_EMF_26.2_v2.zip'
OUT=ROOT/'Pirate_Atelier_CleanRoom_Player_3D_EMF_26.2_v3.zip'
# Colors: base fabric, coat-lining, stitching, hardware, trousers, straps, special effects
OUTFITS=[
 ('captain_armor','Armadura do capitao','armor','#792c36','#e3c49b','#dbad51','#292c42','#512d34','#7f4b2f','tricorn','longcoat','epaulet','skull','plume'),
 ('first_mate_armor','Primeiro imediato','armor','#2d4c70','#c9bca5','#c2cbd7','#272e44','#3a3b49','#815938','bandana','longcoat','guard','stripes','strap'),
 ('quartermaster_armor','Contramestre de suprimentos','armor','#b5a183','#ddd0a8','#ab7235','#6c4b38','#624c40','#684e2d','headwrap','vest','light','pouches','satchel'),
 ('boarding_raider_armor','Saqueador de abordagem','armor','#49464d','#b1a7a1','#b26a4f','#262936','#443333','#554334','bandana','ragged','spiked','crossbones','cape'),
 ('cutlass_duelist_armor','Duelista de sabre','armor','#373638','#e9e0cb','#c59649','#1e2125','#49392e','#6f4a2d','tricorn','duelist','guard','fleur','scabbard'),
 ('gunner_armor','Artilheiro','armor','#74604e','#d4bd95','#a87742','#423931','#574337','#604934','goggles','vest','metal','ammo','flask'),
 ('heavy_bruiser_armor','Brutamontes naval','armor','#493a31','#c4ae88','#a4a6a0','#2e2f2d','#413b32','#5f4632','bandana','heavy','spiked','iron','bandolier'),
 ('naval_officer_armor','Oficial naval','armor','#253e72','#f2dcb7','#e3bc55','#18304e','#253451','#704c37','tricorn','officer','epaulet','anchor','sash'),
 ('cursed_ghost_armor','Corsario fantasma','armor','#426e69','#a1c7ad','#7dcdc5','#263c40','#36584d','#4a4838','cursed','ragged','coral','glow','chain'),
 ('treasure_keeper_armor','Guardiao de tesouros','armor','#7f303b','#edd3a1','#e3b75b','#483449','#4e2934','#6a3d32','tricorn','longcoat','epaulet','jewel','coins'),
 ('harbor_merchant','Comerciante do porto','civilian','#763e4b','#e6d1b0','#af9368','#462b2c','#6b4140','#7a573a','hairwrap','dress','none','pouches','satchel'),
 ('smuggler','Contrabandista','civilian','#3a343a','#c0ae91','#8c7258','#29252c','#4b403d','#68442d','headwrap','longcoat','none','pouches','satchel'),
 ('rogue_navigator','Navegador renegado','civilian','#223d56','#ddcda8','#9d6e3c','#2b2b32','#423b3e','#715436','goggles','longcoat','light','compass','mapcase'),
 ('tavern_raider','Corsario de taverna','civilian','#84453e','#e4d1ad','#c3a06d','#4b3539','#6b3836','#71503d','hair','vest','none','patch','sash'),
 ('weathered_sailor','Marinheiro veterano','civilian','#474752','#c6beab','#8b8b76','#322f39','#423a3e','#665039','headwrap','ragged','none','stripes','satchel'),
 ('fisherman_pirate','Pescador pirata','civilian','#607a91','#e4d7b8','#b89c68','#454b47','#81725a','#6b5535','headwrap','striped','none','fish','rope'),
 ('civilian_captain','Capitao civil','crew','#752d39','#e6d5b1','#cda54c','#252d40','#493c3b','#70462f','tricorn','longcoat','light','anchor','plume'),
 ('deckhand','Marujo do conves','crew','#944c46','#e5d3ad','#9d805c','#574c3b','#4b3a33','#715033','bandana','vest','none','patch','rope'),
 ('deckhand_f','Maruja do conves','crew','#a2675d','#e3d3bd','#b18d66','#51483f','#593b39','#69503b','headwrap','vest','none','patch','sash'),
 ('ships_cook','Cozinheiro do navio','crew','#d8ceb3','#f5eddd','#b48b56','#7b5945','#50443d','#715a49','chef','apron','none','stains','apron'),
 ('crew_gunner','Artilheiro da tripulacao','crew','#494247','#c1a78e','#a68d56','#2e3339','#433b33','#624a33','goggles','vest','light','ammo','bandolier'),
 ('boatswain','Mestre de embarcacao','crew','#304e75','#dbcea9','#d3b15f','#29313b','#3b3434','#7b5739','headwrap','longcoat','light','anchor','sash'),
]
WEARS=('pristine','weathered','tattered'); LEGS=('normal','peg_left','peg_right')
SLOTS=('helmet','chestplate','leggings','boots')
NAMES=('head','body','right_arm','left_arm','right_leg','left_leg')

# Materials are an 8x8 atlas of original pixels, in contrast to v2's flat 16-tile palette.
MATS=[
 'coat','coat_shadow','coat_light','lining','trim','trim_dark','leather','leather_dark',
 'pants','pants_dark','linen','linen_shadow','wood','metal','metal_dark','steel_highlight',
 'stripes','brocade','patch','rips','stitched','buttons','gold_braid','skull',
 'anchor','crossbones','jewel','emerald','glow','coral','scales','barnacles',
 'belt','belt_holes','buckles','sash','sash_shade','epaulet','fringe','rope',
 'ammunition','chain','studs','plume','feather','eyepatch','bandana','hat_crown',
 'hat_brim','bandolier','pouch','scroll','fish','compass','coin','cuff',
 'boot','boot_sole','boot_buckle','apron','apron_stain','fur','spikes','ribbon'
]
assert len(MATS)==64
MAT_INDEX={v:k for k,v in enumerate(MATS)}
def rgb(h):
 h=h.lstrip('#');return tuple(int(h[i:i+2],16) for i in (0,2,4))
def mix(a,b,t):return tuple(int(a[i]*(1-t)+b[i]*t) for i in range(3))
def shade(c,f):return tuple(max(0,min(255,int(round(x*f)))) for x in c)
def alpha(c):return tuple(c)+(255,)
def colored(c):return rgb(c) if isinstance(c,str) else c
def rng_for(s):return random.Random(int(hashlib.sha256(s.encode()).hexdigest()[:12],16))
def outdef(row):
 keys=('id','title','group','coat','lining','trim','dark','pants','leather','hat','silhouette','shoulder','motif','prop')
 d=dict(zip(keys,row));[d.__setitem__(k,rgb(d[k])) for k in ('coat','lining','trim','dark','pants','leather')]
 return d
PROFILES=[outdef(x) for x in OUTFITS]
BYID={d['id']:d for d in PROFILES}

def texture_atlas(e,wear,leg):
 base=e['coat'];dark=e['dark']; lining=e['lining']; gold=e['trim'];pants=e['pants']; leather=e['leather'];
 metal=mix((173,183,180),gold,.15);amethyst=(155,72,189)
 cols={
 'coat':base,'coat_shadow':shade(base,.62),'coat_light':mix(base,lining,.28),
 'lining':lining,'trim':gold,'trim_dark':shade(gold,.68),'leather':leather,'leather_dark':shade(leather,.58),
 'pants':pants,'pants_dark':shade(pants,.65),'linen':mix(lining,(244,234,205),.2),'linen_shadow':shade(lining,.76),
 'wood':(125,78,46),'metal':metal,'metal_dark':shade(metal,.57),'steel_highlight':(219,218,194),
 'stripes':mix(base,lining,.33),'brocade':mix(base,gold,.27),'patch':shade(base,.73),'rips':shade(lining,.7),
 'stitched':lining,'buttons':gold,'gold_braid':gold,'skull':lining,'anchor':gold,
 'crossbones':lining,'jewel':(196,52,68),'emerald':(74,191,160),'glow':(107,250,234) if e['motif']=='glow' else gold,
 'coral':(131,173,151),'scales':(74,132,115),'barnacles':(177,174,138),
 'belt':leather,'belt_holes':shade(leather,.48),'buckles':gold,'sash':mix(base,(175,45,52),.52) if e['prop']=='sash' else gold,
 'sash_shade':shade(gold,.56),'epaulet':gold,'fringe':shade(gold,.82),'rope':(173,141,92),
 'ammunition':(144,108,64),'chain':metal,'studs':metal,'plume':(178,45,62),'feather':lining,
 'eyepatch':(30,30,37),'bandana':base,'hat_crown':dark,'hat_brim':shade(dark,.8),'bandolier':leather,'pouch':leather,
 'scroll':lining,'fish':(114,157,160),'compass':gold,'coin':gold,'cuff':lining,
 'boot':leather,'boot_sole':shade(leather,.42),'boot_buckle':gold,
 'apron':(220,215,199),'apron_stain':(158,112,99),'fur':(145,120,94),'spikes':metal,'ribbon':base,
 }
 im=Image.new('RGBA',(128,128));p=ImageDraw.Draw(im)
 rnd=rng_for(e['id']+wear+leg)
 for mat,index in MAT_INDEX.items():
  x=index%8*16;y=index//8*16;c=cols[mat];
  p.rectangle((x,y,x+15,y+15), fill=alpha(c))
  p.line((x,y+1,x+15,y+1), fill=alpha(shade(c,1.15)))
  p.line((x,y+14,x+15,y+14), fill=alpha(shade(c,.68)))
  if mat in ('coat','coat_light','coat_shadow','pants','pants_dark','lining','linen','bandana','ribbon','apron'):
   for k in range(2,15,4):p.line((x+k,y+2,x+k,y+12), fill=alpha(shade(c,1.06)))
  if mat in ('wood','leather','leather_dark','belt','pouch','boot'):
   for k in (4,8,12):p.line((x+2,y+k,x+13,y+k+1),fill=alpha(shade(c,.85)))
  if mat in ('metal','metal_dark','steel_highlight','studs','chain'):
   for k in (3,8,12):p.line((x+k,y+2,x+k+1,y+13), fill=alpha(shade(c,1.13)))
  if mat=='stripes':
   for yy in (3,7,11):p.rectangle((x+1,y+yy,x+14,y+yy+1),fill=alpha(lining))
  if mat=='brocade':
   for xx in range(3,16,5):
    p.line((x+xx,y+1,x+xx+2,y+6),fill=alpha(gold));p.line((x+xx+2,y+6,x+xx,y+12),fill=alpha(gold))
  if mat=='patch':
   p.rectangle((x+4,y+3,x+12,y+12),fill=alpha(shade(base,.48)));p.line((x+4,y+3,x+12,y+3),fill=alpha(lining))
   for a in range(5,12,3):p.point((x+a,y+4),fill=alpha(gold))
  if mat=='rips':
   for dx,dy in ((3,4),(8,8),(6,12)):
    p.line((x+dx,y+dy,x+dx+3,y+dy+1),fill=alpha(shade(lining,.6)))
  if mat=='stitched':
   for yy in range(3,14,3):p.line((x+4,y+yy,x+6,y+yy+1),fill=alpha(shade(leather,.7)))
  if mat=='buttons':
   for yy in (4,10):
    p.ellipse((x+5,y+yy,x+8,y+yy+3),fill=alpha(gold),outline=alpha(shade(gold,.65)))
  if mat in ('gold_braid','epaulet','fringe'):
   for a in range(3,16,3):p.line((x+a,y+2,x+a-2,y+14),fill=alpha(shade(gold,.67)))
  if mat=='skull':
   p.ellipse((x+4,y+3,x+11,y+10),fill=alpha(lining));p.rectangle((x+6,y+10,x+9,y+12),fill=alpha(lining))
   for xx in (6,9):p.rectangle((x+xx,y+6,x+xx+1,y+7),fill=alpha(dark))
  if mat=='anchor':
   p.ellipse((x+6,y+1,x+10,y+5),outline=alpha(gold));p.line((x+8,y+4,x+8,y+13),fill=alpha(gold),width=2)
   p.arc((x+3,y+8,x+13,y+14),0,180,fill=alpha(gold),width=2)
  if mat=='crossbones':
   p.line((x+3,y+5,x+12,y+12),fill=alpha(lining),width=2);p.line((x+12,y+5,x+3,y+12),fill=alpha(lining),width=2)
  if mat in ('jewel','emerald','glow'):
   center=cols[mat];p.polygon([(x+8,y+2),(x+13,y+8),(x+8,y+14),(x+3,y+8)],fill=alpha(center),outline=alpha(shade(center,.54)))
   p.line((x+5,y+6,x+8,y+3),fill=alpha(shade(center,1.4)))
  if mat in ('coral','barnacles'):
   for a in (4,9,12):p.ellipse((x+a-2,y+4+a%4,x+a+1,y+7+a%4),fill=alpha(shade(c,1.16)))
  if mat=='scales':
   for yy in (3,8,13):
    for xx in (2,7,12):p.arc((x+xx,y+yy,x+xx+5,y+yy+3),0,180,fill=alpha(shade(c,.7)))
  if mat in ('buckles','boot_buckle'):
   p.rectangle((x+4,y+4,x+11,y+11),outline=alpha(gold),width=2);p.line((x+7,y+7,x+11,y+7),fill=alpha(gold))
  if mat=='belt_holes':
   for yy in (3,7,11):p.rectangle((x+5,y+yy,x+6,y+yy+1),fill=alpha(dark))
  if mat in ('sash','sash_shade'):
   for a in range(-7,16,5):p.line((x+max(a,0),y+max(0,-a),x+min(a+11,15),y+min(15,11-a)),fill=alpha(shade(c,1.2)))
  if mat=='rope':
   for a in range(3,14,3):p.line((x+5,y+a,x+10,y+a+2),fill=alpha(shade(c,.72)),width=2)
  if mat=='ammunition':
   for a in (3,7,11):p.rectangle((x+a,y+3,x+a+2,y+12),fill=alpha((145,100,59)));p.rectangle((x+a,y+2,x+a+2,y+4),fill=alpha(metal))
  if mat=='chain':
   for a in (3,8,12):p.ellipse((x+4,y+a-2,x+10,y+a+3),outline=alpha(metal),width=2)
  if mat=='plume':
   for k in range(4):p.polygon([(x+1+k*3,y+12-k),(x+3+k*3,y+2),(x+6+k*3,y+11)],fill=alpha((177+k*10,53+k*7,71)))
  if mat=='feather':
   p.polygon([(x+2,y+13),(x+7,y+2),(x+12,y+4),(x+5,y+13)], fill=alpha(lining));p.line((x+3,y+14,x+9,y+3),fill=alpha(gold))
  if mat=='eyepatch':p.line((x+1,y+3,x+14,y+12),fill=alpha(leather),width=2)
  if mat=='hat_brim':p.line((x+1,y+12,x+14,y+12),fill=alpha(gold),width=3)
  if mat=='bandolier':p.line((x+1,y+14,x+14,y+1),fill=alpha(leather),width=5);p.line((x+4,y+10,x+10,y+4),fill=alpha(gold),width=1)
  if mat=='scroll':p.rectangle((x+4,y+3,x+12,y+13),fill=alpha(lining));p.line((x+5,y+6,x+11,y+6),fill=alpha(leather))
  if mat=='fish':p.ellipse((x+3,y+5,x+11,y+11),fill=alpha(cols['fish']));p.polygon(((x+10,y+8),(x+15,y+4),(x+15,y+12)),fill=alpha(cols['fish']))
  if mat=='compass':p.ellipse((x+2,y+2,x+14,y+14),outline=alpha(gold),width=2);p.line((x+8,y+4,x+10,y+10),fill=alpha(lining))
  if mat=='coin':
   for cx in (5,11):p.ellipse((x+cx-3,y+4,x+cx+2,y+11),outline=alpha(gold),width=2)
  if mat=='apron_stain':
   for cx,cy in ((5,5),(11,8),(8,13)):p.ellipse((x+cx-2,y+cy-1,x+cx+2,y+cy+1),fill=alpha(cols['apron_stain']))
  if mat=='fur':
   for k in range(3,15,3):p.line((x+k,y+2,x+k-2,y+14),fill=alpha(shade(c,.66)))
  if mat=='spikes':
   for k in (1,6,11):p.polygon(((x+k,y+13),(x+k+3,y+2),(x+k+6,y+13)),fill=alpha(metal))
  if wear in ('weathered','tattered') and mat in ('coat','coat_shadow','pants','leather','boot','hat_crown','lining','sash'):
   for _ in range(5 if wear=='weathered' else 13):
    px=x+rnd.randrange(2,15);py=y+rnd.randrange(2,14)
    p.point((px,py),fill=alpha(shade(c,rnd.choice((.61,1.30)))))
 return im

# CEM cuboid attached to animation bones; the shapes themselves are custom for each archetype.
def bx(x,y,z,w,h,d,mat):
 assert min(w,h,d)>0, (w,h,d)
 i=MAT_INDEX[mat];u=i%8*16;v=i//8*16
 faces={k:[u+1,v+1,u+15,v+15] for k in ('uvDown','uvUp','uvNorth','uvSouth','uvWest','uvEast')}
 return {'coordinates':[round(float(t),3) for t in (x,y,z,w,h,d)],**faces}

def cem(e,wear,leg,slot,access=None):
 sh={x:[] for x in NAMES}
 def a(p,*args):sh[p].append(bx(*args))
 def arms(w=4.0,armmat='coat'):
  for part in ('right_arm','left_arm'):
   a(part,-w/2-.3,-1.8,-2.35,w+.6,10.9,4.7,armmat)
   a(part,-w/2-.4,8.2,-2.55,w+.8,2.4,5.1,'cuff' if e['silhouette'] in ('duelist','officer') else 'coat_shadow')
   a(part,-w/2-.42,9.1,-2.62,w+.84,.45,5.2,'gold_braid' if e['group']=='armor' else 'stitched')
 def hat():
  typ=e['hat'];
  if typ in ('tricorn','cursed'):
   a('head',-4.7,-8,-4.7,9.4,1.9,9.4,'hat_crown')
   a('head',-5.6,-8.1,-5.3,11.2,.9,10.6,'hat_brim')
   a('head',-6.7,-9.4,-2.75,2.1,3.2,6.5,'hat_crown');a('head',4.6,-9.4,-2.75,2.1,3.2,6.5,'hat_crown')
   a('head',-4.5,-11.1,-3.0,9,3.2,6.0,'hat_crown');a('head',-4.4,-8,-5.6,8.8,.75,1,'gold_braid')
   a('head',-.9,-9.1,-5.75,1.8,1.5,.7, 'skull' if e['motif'] in ('skull','crossbones') else 'anchor')
   if e['prop']=='plume' or e['id'] in ('captain_armor','civilian_captain','treasure_keeper_armor'):
    for j in range(3):a('head',2.8+j*.6,-14+j*.5,.3+j*.8,.7,6-j*.5,1,'plume')
   if typ=='cursed':
    for j in range(3):a('head',-5+j*3,-11.7,0,1.1,2.5,1.4,'coral')
  elif typ in ('bandana','headwrap','hairwrap'):
   a('head',-4.65,-8,-4.65,9.3,2.45,9.3,'bandana');a('head',-4.7,-6.6,-4.75,9.4,.9,9.5,'ribbon')
   a('head',3.8,-7.1,2.4,2.5,4.5,1.6,'ribbon');a('head',4.0,-3.2,2.3,1.8,1.0,1.9,'stitched')
   if typ=='hairwrap':a('head',-4.4,-7.6,2.8,8.8,2.1,1.2,'linen')
  elif typ=='goggles':
   a('head',-4.55,-8.4,-4.55,9.1,2.0,9.1,'hat_crown')
   for x in (-3.1,1.1):a('head',x,-7.7,-5.1,2.1,1.7,1.0,'metal')
   a('head',-4.4,-7.3,-4.8,8.8,.6,.6,'leather')
  elif typ=='chef':
   a('head',-4.2,-8.2,-4.2,8.4,2.5,8.4,'apron');a('head',-4.5,-11.7,-4.5,9,3.9,9,'apron')
   a('head',-4.55,-8.3,-4.55,9.1,.9,9.1,'linen_shadow')
  else: # visible hair and simple kerchief rather than making a face/skin
   a('head',-4.45,-8.3,-4.45,8.9,1.3,8.9,'coat_shadow')
 if slot=='helmet':
  hat()
  if wear=='tattered':a('head',-4.0,-7.8,-4.98,1.5,.65,.55,'rips')
  if access in ('eyepatch_left','eyepatch_right'):
   x=-3.3 if access.endswith('left') else 1.1
   a('head',x,-4.9,-4.7,2.4,2.6,.75,'eyepatch');a('head',-4.2,-5.15,-4.5,8.4,.6,.4,'leather')
 if slot=='chestplate':
  shape=e['silhouette']
  arms()
  mat='apron' if shape=='apron' else ('stripes' if shape=='striped' else 'coat')
  a('body',-4.45,-.1,-2.5,8.9,12.25,5.0,mat)
  # vertical detail: lined collars/vest & lapels follow the reference concept
  if shape in ('longcoat','officer','duelist','ragged','heavy'):
   for x in (-4.1,2.5):a('body',x,.8,-2.92,1.6,9.8,.7,'lining')
   a('body',-.55,1.9,-2.96,1.1,9.2,.68,'coat_shadow')
   for y in (3,6,9):
    for x in (-1.05,1.4) if shape=='officer' else (-.6,):
     a('body',x,y,-3.25,.72,.75,.7,'buttons')
   for x in (-4.25,1.45):a('body',x,11.0,1.75,2.8,5.15,1.0,'coat_shadow')
   for x in (-4.15,2.1):a('body',x,11.05,-2.88,2.05,5.4,.9,'coat')
   for x in (-4.22,2.1):a('body',x,16.2,-2.92,2.12,.55,.95,'gold_braid' if e['group']=='armor' else 'stitched')
  elif shape in ('vest','dress','apron','striped'):
   for x in (-3.8,2.7):a('body',x,1.0,-3.0,1.15,8.8,.75,'linen')
   a('body',-2.8,1.7,-3.08,5.6,8.4,.8,'apron' if shape=='apron' else ('stripes' if shape=='striped' else 'coat_shadow'))
   for y in (4,8):a('body',-.5,y,-3.24,.95,.95,.55,'buttons')
   if shape=='apron':a('body',-3.1,10.1,-3.35,6.2,5,.9,'apron_stain')
   if shape=='dress':a('body',-4,11,-2.6,8,5.5,5.2,'sash')
  # identity-defining diagonal harness / bandolier / fitted sash
  if e['motif'] in ('ammo','compass','pouches','patch','fish') or e['prop'] in ('satchel','mapcase','bandolier'):
   for j in range(5):a('body',-3.3+j*.68,j*1.7+.3,-3.34,1.15,2.05,.55,'bandolier')
  if e['motif']=='ammo':
   for j in range(4):a('body',-2.7+j*1.0,1+j*1.45,-3.98,1.0,1.65,.7,'ammunition')
  if e['motif']=='glow':
   for j in range(5):a('body',-3.2+j*1.5,8+(j%2)*1.7,-3.23,.9,1.6,.5,'glow')
  if e['motif'] in ('skull','anchor','crossbones','jewel','compass','fish','patch'):
   a('body',-1.5,5.5,-3.36,3.0,3.4,.8,{'skull':'skull','anchor':'anchor','crossbones':'crossbones','jewel':'jewel','compass':'compass','fish':'fish','patch':'patch'}[e['motif']])
  if e['motif']=='stains':a('body',-2.0,6.5,-3.5,4.0,4.0,.52,'apron_stain')
  if shape in ('officer','longcoat') or e['prop'] in ('plume','coins','sash'):
   a('body',-4.45,10,-2.72,8.9,1.35,5.5,'sash');a('body',-1.2,10,-3.48,2.4,1.45,.85,'buckles')
  else:
   a('body',-4.47,9.8,-2.64,8.95,1.15,5.35,'belt');a('body',-.95,9.66,-3.18,1.9,1.52,.75,'buckles')
  # signature shoulder shapes attach to arms so Fresh movement is inherited
  s=e['shoulder']
  if s!='none':
   for part in ('right_arm','left_arm'):
    a(part,-2.55,-2.75,-2.73,5.1,3.2,5.5,'metal' if s in ('guard','metal','spiked') else ('coral' if s=='coral' else 'epaulet'))
    if s=='epaulet':
     for x in (-1.75,-.35,.95):a(part,x,-.55,-2.85,.55,2.4,.8,'fringe')
    if s=='spiked':
     for x in (-2.0,0,1.45):a(part,x,-4.0,-2.3,.9,1.5,1.0,'spikes')
    if s=='coral':
     for x in (-1.8,.5):a(part,x,-3.7,1.5,.85,2,1.1,'coral')
  if shape=='heavy':
   for part in ('right_arm','left_arm'):
    a(part,-2.7,4.0,-2.7,5.4,2.3,5.5,'metal');a(part,-2.4,7.9,-2.76,4.8,1.2,5.6,'metal_dark')
   a('body',-4.4,4.1,-3.02,8.8,3.1,.75,'metal_dark');a('body',-4.4,7.3,-3.02,8.8,2.6,.76,'studs')
  if shape=='ragged' and wear!='pristine':
   for x in (-3.6,2.6):a('body',x,12.2,-3.18,1.0,5.2,.9,'rips')
  if e['prop'] in ('scabbard','satchel','mapcase','flask','coins','sash','cape','bandolier','chain','rope','apron'):
   if e['prop'] in ('scabbard','mapcase'):a('body',3.7,7.4,1.9,1.6,9.3,1.7,'leather');a('body',3.9,7.6,1.7,1.3,1.0,2.1,'metal')
   if e['prop']=='satchel':a('body',3.4,6.6,-2.8,2.3,5.0,2.1,'pouch');a('body',3.5,7,-3.0,2.2,.75,2.4,'buckles')
   if e['prop']=='coins':
    for j in range(4):a('body',-3.7+j*2.2,10.7,-3.02,.8,1.0,.5,'coin')
   if e['prop']=='cape':a('body',-4.15,2.1,2.45,8.3,15,.8,'rips')
   if e['prop']=='chain':
    for j in range(3):a('body',3.1,6.0+j*2.8,-2.9,1.1,2.6,.6,'chain')
   if e['prop']=='sash':a('body',2.6,10.6,-2.85,1.7,7.0,.85,'sash')
   if e['prop']=='flask':a('body',3.85,8.2,-3.55,1.7,3.2,1.4,'pouch')
   if e['prop']=='bandolier':
    for j in range(4):a('body',-3.2+j*1.15,1+j*1.7,-3.9,.9,1.25,.7,'ammunition')
   if e['prop']=='rope':a('body',3.45,8.5,-3.15,2.0,2.2,1.8,'rope')
  # Second QA pass: model-specific details from concept arts.
  # These are independent voxel components, not generic recolors.
  eid=e['id']
  if eid in ('captain_armor','civilian_captain'):
   for y in (3.0,5.3,7.6):
    a('body',-3.4,y,-3.45,1.0,.7,.6,'gold_braid')
    a('body',2.4,y,-3.45,1.0,.7,.6,'gold_braid')
   a('body',-4.55,11.5,1.5,9.1,5.2,.75,'brocade')
   a('body',-1.1,9.45,-3.5,2.2,2.0,.8,'skull')
  elif eid=='first_mate_armor':
   # Cross-chest naval harness and a pair of brass-silver fasteners.
   for k in range(6):
    a('body',-3.8+k*1.15,1.2+k*1.4,-3.7,1.35,2.0,.5,'bandolier')
   for y in (4.5,8.2):a('body',2.3,y,-3.6,1.0,.9,.7,'metal')
   a('body',-4.55,11.6,1.5,9.1,4.5,.7,'coat_shadow')
  elif eid=='quartermaster_armor':
   for x in (-3.4,-.85,1.75):
    a('body',x,7.6,-3.75,2.0,3.4,1.55,'pouch')
    a('body',x+.55,8.1,-3.97,.85,.6,.55,'buckles')
   a('body',-4.0,11.8,-2.7,8.0,1.4,5.4,'belt')
   a('body',-1.6,1.3,-3.2,3.2,4.1,.9,'linen')
  elif eid=='boarding_raider_armor':
   a('body',-4.8,10.8,2.5,9.6,6.5,.85,'rips')
   for part in ('right_arm','left_arm'):
    for x in (-2,0,1.2):a(part,x,-4.1,0,.95,2.0,1.3,'spikes')
   a('body',-1.9,7.2,-3.7,3.8,2.1,.6,'crossbones')
  elif eid=='cutlass_duelist_armor':
   for x in (-2.8,-1.1,.6,2.3):a('body',x,1.2,-3.52,.65,4.5,.8,'linen')
   a('body',-1.6,9.6,-3.55,3.2,1.6,.9,'gold_braid')
   a('body',4.1,9.2,0.7,1.4,8.2,1.45,'leather')
   a('body',4.1,9.0,.7,1.5,1.2,1.5,'metal')
  elif eid in ('gunner_armor','crew_gunner'):
   for k in range(5):
    a('body',-3.0+k*.95,1.5+k*1.25,-3.85,.85,1.5,.7,'ammunition')
   for k in range(3):a('body',-3.4+k*2.6,9.0,-3.8,1.7,3.3,1.3,'pouch')
   if eid=='gunner_armor':a('body',4,8,-3.7,1.4,3.2,1.2,'metal')
  elif eid=='heavy_bruiser_armor':
   for x in (-4.7,1.8):a('body',x,5.2,-3.45,2.8,3.5,1.1,'metal')
   for y in (2.2,7.4):
    for x in (-3.2,0,3.0):a('body',x,y,-3.6,.8,.9,.7,'studs')
   a('body',-4.2,11.4,1.5,8.4,3.3,2.0,'metal_dark')
  elif eid=='naval_officer_armor':
   for y in (3,5.4,7.8):
    for x in (-2.6,1.6):a('body',x,y,-3.35,.8,.9,.6,'buttons')
   a('body',1.7,9.0,-3.8,2.7,7.0,.7,'sash')
   a('body',-1.4,3.5,-3.7,2.8,2.7,.75,'anchor')
   for part in ('right_arm','left_arm'):
    for x in (-1.2,0,1.2):a(part,x,-.4,-3.0,.5,2.9,.6,'fringe')
  elif eid=='cursed_ghost_armor':
   for x in (-3.8,-1.7,1.2,3.0):
    a('body',x,11.5,2.0,1.0,6.2,.75,'rips')
   for k in range(4):a('body',-3.1+k*2.2,7.3,-3.5,.95,2.1,.65,'glow')
   for part in ('right_arm','left_arm'):
    a(part,-1.3,-4.5,1.1,1.2,3,1.2,'coral')
    a(part,.7,-5.2,1.4,1.1,3.5,1.0,'barnacles')
  elif eid=='treasure_keeper_armor':
   a('body',-2.1,4.1,-3.85,4.2,4.0,.95,'jewel')
   for x in (-3.5,-1.5,.8,2.9):a('body',x,10.7,-3.7,1.0,1.5,.7,'coin')
   for part in ('right_arm','left_arm'):
    a(part,-2.2,-1.9,-3.2,4.4,2.0,.9,'brocade')
  elif eid=='harbor_merchant':
   a('body',-4.8,11.6,-2.6,9.6,5.1,5.2,'pants')
   for x in (-3.6,2.4):a('body',x,8.1,-3.65,2.3,3.3,1.2,'pouch')
   a('body',-1.8,1.6,-3.3,3.6,5.0,.75,'lining')
  elif eid=='smuggler':
   for k in range(4):a('body',-3.2+k*1.7,1.1+k*2,-3.6,1.2,2.3,.65,'bandolier')
   a('body',3.65,7.8,-3.1,2.2,4.8,2.0,'pouch')
   a('body',-4.5,11.1,1.5,9.0,6.6,.7,'coat_shadow')
  elif eid=='rogue_navigator':
   a('body',-1.5,4.5,-3.5,3.0,3.0,.9,'compass')
   a('body',3.9,6.2,1.5,1.6,9.8,2,'scroll')
   a('body',-4.3,11.7,1.6,8.6,5.0,.8,'coat_shadow')
  elif eid=='tavern_raider':
   a('body',-4.5,10.6,-2.7,9.0,1.6,5.3,'sash')
   for x in (-3.4,2.1):a('body',x,8.8,-3.5,1.3,2.1,.9,'patch')
  elif eid=='weathered_sailor':
   a('body',-4.5,11.3,1.6,9.0,4.2,.65,'rips')
   for x,y in ((-3.2,6.3),(1.8,8.7),(-1.4,11.2)):
    a('body',x,y,-3.55,1.8,1.4,.6,'patch')
  elif eid=='fisherman_pirate':
   for yy in (2.0,5.4,8.7):a('body',-4.0,yy,-3.1,8.0,.65,.9,'linen')
   a('body',2.6,9.1,-3.4,1.9,4.4,.9,'rope')
  elif eid in ('deckhand','deckhand_f'):
   a('body',-4.3,10.6,-2.8,8.6,1.5,5.3,'sash')
   for x in (-3.2,2.2):a('body',x,7.8,-3.7,1.25,2.8,1,'pouch')
  elif eid=='ships_cook':
   for x in (-2.5,1.0):a('body',x,2.5,-3.55,1.6,7.8,.7,'apron')
   a('body',-2.8,9.1,-3.8,5.6,3.2,.85,'apron_stain')
   a('body',-3.4,10.0,-3.85,1.2,1.4,.7,'pouch')
  elif eid=='boatswain':
   a('body',-4.7,11.4,1.7,9.4,4.4,.8,'coat_shadow')
   a('body',2.1,9.3,-3.25,2.0,6.8,.75,'sash')
   a('body',-1.7,5.4,-3.65,3.4,2.9,.75,'anchor')
  if access and access.startswith('hook_'):
   limb='right_arm' if access.endswith('right') else 'left_arm'
   a(limb,-1.8,9.6,-2.35,3.6,2.0,4.7,'leather')
   a(limb,-.65,11.1,-1.0,1.3,4.0,1.5,'metal');a(limb,-2.4,14.3,-1.0,2.0,.9,1.5,'metal')
   a(limb,-3.0,12.7,-1.0,1.0,2.2,1.5,'metal')
 if slot=='leggings':
  a('body',-4.27,9.8,-2.4,8.54,2.5,4.8,'belt');a('body',-1.0,9.6,-3.1,2,1.6,1.0,'buckles')
  for part in ('right_leg','left_leg'):
   a(part,-2.30,0,-2.25,4.6,9.0,4.5,'pants')
   a(part,-2.4,.5,-2.36,4.8,.9,4.72,'pants_dark')
   if e['silhouette']=='striped':
    for z in (-2.54,2.27):a(part,-2.25,1,z,4.5,.55,.35,'stripes')
   if e['silhouette']=='heavy':a(part,-2.45,4.0,-2.4,4.9,3.0,4.9,'metal')
   if leg!='normal' and part==('left_leg' if leg=='peg_left' else 'right_leg'):
    a(part,-2.35,8.0,-2.4,4.7,1.6,4.8,'leather');a(part,-1.25,9.5,-1.1,2.5,1.1,2.2,'metal')
    a(part,-.95,10.5,-.8,1.9,3.1,1.6,'wood');a(part,-.9,12.8,-.8,1.8,.9,1.6,'metal_dark')
   else:a(part,-2.33,8.6,-2.3,4.66,.9,4.6,'coat_shadow')
 if slot=='boots':
  for part in ('right_leg','left_leg'):
   if leg!='normal' and part==('left_leg' if leg=='peg_left' else 'right_leg'):
    a(part,-1.,10.8,-1.0,2.0,1.0,2.0,'metal_dark');a(part,-1.15,11.8,-1.1,2.3,.6,2.3,'boot_sole')
   else:
    a(part,-2.42,6.8,-2.42,4.84,4.95,4.84,'boot')
    a(part,-2.5,7.1,-2.49,5.0,1.45,4.98,'cuff')
    a(part,-2.54,10.6,-3.2,5.08,1.35,6.05,'boot_sole')
    a(part,-.6,7.9,-2.77,1.2,1.15,.65,'boot_buckle')
    if e['silhouette']=='heavy':a(part,-2.55,9.3,-2.65,5.1,1.05,5.3,'metal')
 # attach-to-bone data, not player replacement
 return {'texture':f'pirateatelier:textures/entity/cem/{e["id"]}_{wear}_{leg}', 'textureSize':[128,128], 'shadowSize':.5,
         'models':[{'part':p,'id':p,'attach':True,'boxes':lst} for p,lst in sh.items() if lst]}

def paint_equipment(e,wear,leg,layer):
 """New original high-readability 64x32 equipment sprite, not a recolor of the v2 layer."""
 im=Image.new('RGBA',(64,32));d=ImageDraw.Draw(im)
 base=e['coat']; dark=e['dark'];lining=e['lining'];trim=e['trim'];pants=e['pants'];leather=e['leather']
 def fill(rect,c):d.rectangle(rect,fill=alpha(c))
 def ln(coords,c,width=1):d.line(coords,fill=alpha(c),width=width)
 if layer==1:
  for rect in ((8,0,15,7),(16,0,23,7),(0,8,7,15),(8,8,15,15),(16,8,23,15),(24,8,31,15)):
   fill(rect,base if rect[0]==8 else dark)
  if e['hat']=='chef':fill((8,8,15,13),lining);ln(((9,10),(15,10)),dark)
  elif e['hat'] in ('tricorn','cursed'):ln(((8,12),(15,12)),trim,2);fill((11,10,12,11),trim)
  elif e['hat'] in ('headwrap','hairwrap','bandana'):ln(((8,10),(15,10)),lining)
  else:ln(((8,9),(15,9)),trim)
  # chest (20-27,20-31) and rear (32-39,20-31)
  fill((20,16,27,19),dark);fill((28,16,35,19),dark)
  for rect in ((16,20,19,31),(20,20,27,31),(28,20,31,31),(32,20,39,31)):
   fill(rect,base if rect[0] in (20,32) else dark)
  if e['silhouette'] in ('longcoat','officer','duelist','ragged','heavy'):
   ln(((20,20),(23,23),(22,30)),lining);ln(((27,20),(24,23),(25,30)),lining)
   ln(((21,20),(21,31)),trim);ln(((26,20),(26,31)),trim)
   for yy in (23,26,29):
    fill((23,yy,23,yy),trim)
    if e['silhouette']=='officer':fill((25,yy,25,yy),trim)
  else:
   fill((22,21,25,29),lining if e['silhouette']!='apron' else (220,215,199))
   for yy in (23,27):fill((23,yy,23,yy),trim)
  if e['motif']=='ammo':ln(((20,21),(27,30)),leather,2);ln(((22,22),(26,29)),trim)
  if e['motif']=='compass':fill((23,25,25,27),trim)
  if e['motif'] in ('anchor','skull','crossbones','jewel','glow'):
   if e['motif']=='anchor':ln(((24,22),(24,28)),trim);ln(((22,28),(26,28)),trim)
   elif e['motif']=='crossbones':ln(((22,23),(26,27)),lining);ln(((26,23),(22,27)),lining)
   elif e['motif']=='skull':fill((23,23,25,25),lining);fill((24,24,24,24),dark)
   elif e['motif']=='jewel':fill((24,23,25,25),(193,63,77));fill((24,23,24,23),(242,157,159))
   else:fill((23,26,25,27),(91,234,218))
  if e['silhouette']=='striped':
   for y in (23,26,29):ln(((20,y),(27,y)),lining)
  if e['silhouette']=='apron':fill((20,26,27,31),(212,205,183));fill((24,28,25,29),(164,110,92))
  # front sash and rear sash
  ln(((20,30),(27,30)),trim)
  ln(((32,30),(39,30)),trim)
  if e['prop'] in ('satchel','bandolier','mapcase'):ln(((20,20),(27,31)),leather,2)
  # Sleeve maps: u40..55
  for x in (40,44,48,52):
   fill((x,16,x+3,19),dark);fill((x,20,x+3,31),base)
   if e['shoulder'] in ('spiked','metal','guard','epaulet','coral'):
    fill((x,20,x+3,23),trim if e['shoulder']=='epaulet' else (127,140,133) if e['shoulder']!='coral' else (135,175,154))
   if e['shoulder']=='epaulet':ln(((x,24),(x+3,24)),trim)
   if e['silhouette']=='striped':
    for yy in (25,29):ln(((x,yy),(x+3,yy)),lining)
   ln(((x,29),(x+3,29)),trim)
 else:
  for x in (0,4,8,12):
   fill((x+4 if x<8 else x,16,x+7 if x<8 else x+3,19),pants)
   fill((x,20,x+3,31),pants if x in (4,8) else dark)
   ln(((x,20),(x+3,20)),trim)
   if e['silhouette']=='striped':
    for yy in (24,27):ln(((x,yy),(x+3,yy)),lining)
   fill((x,30,x+3,31),leather)
 if wear!='pristine':
  r=rng_for(e['id']+wear+leg+str(layer)); n=18 if wear=='weathered' else 45
  for i in range(n):
   x=r.randrange(64);y=r.randrange(32)
   if im.getpixel((x,y))[3]:d.point((x,y),fill=alpha(shade(im.getpixel((x,y))[:3],r.choice((.64,1.28)))))
 if wear=='tattered':
  if layer==1:
   for pt in ((20,31),(22,31),(34,31),(40,31),(55,31)):im.putpixel(pt,(0,0,0,0))
  else:im.putpixel((3,31),(0,0,0,0))
 return im

def icon(e,slot,wear,leg):
 im=Image.new('RGBA',(32,32));p=ImageDraw.Draw(im)
 b=e['coat'];l=e['lining'];t=e['trim'];d=e['dark'];pants=e['pants'];leather=e['leather']
 R=lambda q,c:p.rectangle(q,fill=alpha(c))
 L=lambda q,c,w=1:p.line(q,fill=alpha(c),width=w)
 if slot=='helmet':
  if e['hat'] in ('tricorn','cursed'):
   p.polygon(((2,18),(7,10),(12,11),(15,7),(21,11),(26,8),(30,18),(25,22),(7,22)),fill=alpha(d))
   p.polygon(((6,17),(12,14),(21,14),(26,17),(24,19),(8,19)),fill=alpha(b))
   L(((5,19),(27,19)),t,2)
   if e['prop']=='plume':p.polygon(((23,14),(25,3),(26,12),(30,6),(28,16)),fill=alpha((177,58,70)))
   if e['motif']=='glow':R((15,16,17,18),(91,234,218))
  elif e['hat']=='chef':
   R((9,6,24,21),l);R((7,8,27,15),l);L(((8,17),(26,17)),d,2)
  elif e['hat']=='goggles':
   p.polygon(((6,10),(10,7),(23,7),(27,12),(25,19),(7,19)),fill=alpha(b))
   for xx in (11,19):p.ellipse((xx,10,xx+5,14),fill=alpha(d),outline=alpha(t),width=1)
  else:
   p.polygon(((5,12),(11,7),(24,7),(28,13),(25,20),(7,20)),fill=alpha(b));L(((7,17),(27,17)),t,2)
   R((24,20,27,26),d)
 if slot=='chestplate':
  p.polygon(((7,6),(11,4),(20,4),(25,7),(29,13),(26,28),(21,27),(19,30),(13,30),(10,27),(5,28),(3,13)),fill=alpha(d))
  R((9,7,23,26),b);R((4,12,9,24),b);R((23,12,28,24),b)
  p.polygon(((10,8),(15,9),(15,20),(12,16)),fill=alpha(l));p.polygon(((21,8),(16,9),(16,20),(19,16)),fill=alpha(l))
  L(((15,12),(16,25)),d)
  for y in (14,18,22):R((16,y,17,y),t)
  if e['motif']=='ammo':L(((10,9),(21,25)),leather,3)
  if e['silhouette']=='striped':
   for yy in (11,16,21):L(((9,yy),(23,yy)),l)
  if e['silhouette']=='apron':R((12,16,21,27),l);R((16,22,17,23),(168,121,95))
  if e['shoulder']=='epaulet':R((4,10,9,13),t);R((23,10,28,13),t)
  if e['shoulder'] in ('spiked','metal','guard'):R((3,10,9,15),(118,129,133));R((23,10,29,15),(118,129,133))
  if e['motif']=='glow':R((14,24,17,27),(92,243,222))
  L(((10,26),(23,26)),t,2)
  if e['silhouette'] in ('longcoat','officer','duelist','ragged'):R((9,26,12,31),b);R((20,26,23,31),b)
 if slot=='leggings':
  R((7,7,25,14),d);R((8,8,24,13),b);R((7,13,14,29),pants);R((18,13,25,29),pants)
  R((7,14,14,15),t);R((18,14,25,15),t)
  if leg!='normal':
   x=7 if leg=='peg_left' else 18;R((x,22,x+7,30),(0,0,0));R((x+2,23,x+5,30),(125,78,46));R((x+1,23,x+6,25),(146,159,161))
 if slot=='boots':
  for x in (4,18):R((x+2,9,x+9,23),d);R((x+3,10,x+8,21),leather);R((x,22,x+12,27),d);R((x+3,18,x+9,19),t)
 if wear=='tattered' and slot in ('chestplate','helmet'):
  for pt in ((6,22),(24,25),(8,21)):im.putpixel(pt,(0,0,0,0))
 return im

def pngbytes(im):
 f=io.BytesIO();im.save(f,'PNG',optimize=True);return f.getvalue()
def dump(d):return json.dumps(d,ensure_ascii=False,separators=(',',':')).encode()
def outfit_id(e,wear,leg):return e['id']+'_'+wear+'_'+leg

# Expanded first-class accessory concept library, generated as separate original voxel assets.
ACCESSORIES={
 'hook':['brass','iron','naval','ornate','bone','cursed'],
 'peg_leg':['wood','iron','reinforced','bone','cursed'],
 'eyepatch':['simple','skull','stitched','ornate','cloth_wrap','cursed'],
 'hat':['tricorn','captain_plume','admiral','worn','feathered','cursed'],
 'bandana':['plain','striped','wrap','tied','headscarf'],
 'parrot_shoulder':['macaw','blue','cockatoo'],
 'belt_sash':['sash_plain','sash_knot','sash_striped','belt','double_belt','coin_chain'],
 'lantern_hook':['ship','storm'],
 'scabbard':['single','crossed'],
 'shoulder_guard':['barnacle','heavy_reef'],
 'medal':['star','anchor','laurel','captain'],
 'jewelry':['hoop','anchor','gem','skull','signet','ruby','sapphire','cursed_ring'],
 'scarf':['scarf','cravat','neckerchief'],
 'backpack':['backpack','sea_pack','treasure_satchel'],
 'spyglass':['standard','naval','captain'],
 'cursed_accessory':['amulet','tentacle_charm','coin','barnacle_band','shackles'],
}
# Pixel icon and mesh generator for all accessories. Each is distinct in shape and colors.
def accessory_color(group,v):
 if 'cursed' in v:return (123,61,171),(91,238,212),(40,39,57)
 if v in ('bone','cockatoo'):return (201,184,142),(109,84,65),(97,75,58)
 if v in ('iron','naval','reinforced','storm'):return (128,146,165),(193,168,97),(46,61,77)
 if v in ('wood','brown','backpack','sea_pack','treasure_satchel','worn'):return (138,89,52),(200,169,114),(75,51,35)
 if v in ('brass','ornate','captain','admiral','star','coin_chain','captain_plume','ruby','signet'):return (185,140,57),(226,194,102),(102,58,38)
 if v in ('macaw','feathered'):return (191,51,49),(241,182,66),(49,89,135)
 if v=='blue':return (48,112,178),(79,165,214),(41,61,95)
 if v in ('plain','sash_plain','scarf','tied'):return (143,48,59),(190,115,104),(78,40,40)
 return (101,71,64),(204,176,129),(48,47,52)

def accessory_icon(group,v):
 c,t,d=accessory_color(group,v);im=Image.new('RGBA',(32,32));dr=ImageDraw.Draw(im)
 P=lambda a,col:dr.polygon(a,fill=alpha(col));R=lambda a,col:dr.rectangle(a,fill=alpha(col));L=lambda pts,col,w=1:dr.line(pts,fill=alpha(col),width=w)
 if group=='hook':
  R((11,22,21,29),d);R((12,22,20,26),c)
  L(((16,23),(16,13),(10,8),(11,3),(20,3),(24,9),(20,14)),t,4)
  if v in ('ornate','cursed'):R((14,21,19,23),t)
 if group=='peg_leg':
  R((9,3,23,9),c);R((12,10,20,14),d);P(((14,14),(20,14),(18,30),(15,30)),c);R((14,24,19,26),t)
 if group=='eyepatch':
  L(((4,11),(27,17)),d,2);P(((9,12),(19,12),(23,17),(18,24),(11,22)),c)
  if v in ('skull','ornate','cursed'):R((14,15,17,18),t)
 if group=='hat':
  P(((1,18),(6,12),(10,12),(15,7),(23,10),(26,12),(31,18),(27,24),(5,24)),d)
  P(((4,17),(10,15),(23,15),(28,17),(25,20),(7,20)),c);L(((5,20),(27,20)),t,2)
  if v in ('captain_plume','feathered','cursed'):P(((22,13),(25,2),(27,14),(31,7),(28,18)),t)
 if group=='bandana':P(((5,10),(12,6),(22,7),(27,12),(25,19),(7,19)),c);R((24,17,28,26),d);L(((6,17),(26,17)),t,2)
 if group=='parrot_shoulder':
  P(((11,8),(19,8),(24,14),(19,23),(14,25),(10,20)),c);R((17,6,23,12),t);R((22,11,27,13),d);R((19,12,20,13),d)
  L(((13,22),(8,31)),c,3)
 if group=='belt_sash':
  R((3,13,29,20),c)
  if 'sash' in v:L(((10,13),(21,31)),t,5)
  else:R((13,11,20,23),t);R((15,14,18,20),d)
 if group=='lantern_hook':
  R((12,9,22,26),d);R((14,12,20,24),(242,162,69));L(((9,6),(19,6),(19,9)),c,3);L(((14,27),(23,27)),t,2)
 if group=='scabbard':
  L(((7,24),(25,5)),d,7);L(((7,24),(25,5)),c,4);R((20,4,28,8),t)
  if v=='crossed':L(((5,7),(26,26)),d,5)
 if group=='shoulder_guard':
  P(((4,18),(9,9),(20,5),(28,11),(29,20),(13,26)),c)
  for p in ((9,12),(18,7),(24,14)):R((p[0],p[1],p[0]+2,p[1]+2),t)
 if group=='medal':
  R((12,3,21,12),c);P(((16,11),(26,18),(22,28),(10,28),(7,18)),t);R((14,18,19,21),d)
 if group=='jewelry':
  dr.ellipse((8,7,25,24),outline=alpha(t),width=4);R((14,4,20,10),c)
  if v in ('gem','ruby','sapphire','cursed_ring'):P(((16,13),(20,18),(16,23),(12,18)),(60,130,205) if v=='sapphire' else (187,52,82) if v=='ruby' else (135,71,180))
 if group=='scarf':P(((4,9),(26,8),(28,19),(19,24),(11,20)),c);P(((21,20),(26,18),(22,31),(16,31)),t)
 if group=='backpack':
  R((6,6,26,28),d);R((8,8,24,26),c);R((10,17,22,28),c);R((14,17,19,20),t)
 if group=='spyglass':
  L(((4,25),(25,7)),d,8);L(((6,23),(24,9)),c,5);R((21,5,27,11),t)
 if group=='cursed_accessory':
  dr.ellipse((5,5,27,27),outline=alpha(c),width=4);P(((16,10),(23,17),(16,25),(9,17)),t);L(((11,10),(7,3)),d,3)
 return im

def accessory_cem(group,v):
 c,t,d=accessory_color(group,v)
 sh={p:[] for p in NAMES};
 def a(p,*arg):sh[p].append(bx(*arg))
 # Uses generic atlas created for each accessory that maps c/t/d swatches to common materials.
 if group=='hook':
  part='right_arm';a(part,-1.9,10,-2.5,3.8,2.1,5.0,'leather');a(part,-.65,12.1,-.9,1.3,3.5,1.8,'metal');a(part,-2.4,15,-.9,2.0,.8,1.8,'metal');a(part,-2.9,13.4,-.9,.9,2.2,1.8,'metal')
 if group=='peg_leg':
  part='right_leg';a(part,-2.2,8,-2.25,4.4,2.3,4.5,'leather');a(part,-1,10,-.95,2.0,3.4,1.8,'wood');a(part,-1.3,12.9,-1.1,2.6,.8,2.2,'metal')
 if group=='eyepatch':a('head',-3.3,-5,-4.75,2.8,2.75,.8,'eyepatch');a('head',-4.2,-5.6,-4.5,8.4,.65,.35,'leather')
 if group=='hat':
  a('head',-5.3,-8,-5,10.6,1.2,10,'hat_brim');a('head',-4.6,-11,-3.4,9.2,4,6.8,'hat_crown');a('head',-6.6,-9.4,-2.2,2.3,2.5,5.8,'hat_crown');a('head',4.3,-9.4,-2.2,2.3,2.5,5.8,'hat_crown')
  if v in ('captain_plume','feathered','cursed'):a('head',3.1,-14.2,1.0,1.2,6.7,1.1,'plume')
 if group=='bandana':a('head',-4.7,-8,-4.7,9.4,2.3,9.4,'bandana');a('head',4.0,-7.0,2.2,2.6,5.0,1.1,'ribbon')
 if group=='parrot_shoulder':
  a('left_arm',-1.7,-7.8,0.2,3.3,4.3,3.2,'coat');a('left_arm',-1.9,-11.1,-1,3.8,3.4,3.6,'feather');a('left_arm',-1.5,-9.2,-2.9,3,1.2,2.2,'trim')
 if group=='belt_sash':a('body',-4.6,9.5,-2.8,9.2,2.1,5.6,'sash' if v.startswith('sash') else 'belt')
 if group=='lantern_hook':a('right_arm',-1.5,11.4,-.6,3.0,1.1,1.3,'metal');a('right_arm',-1.9,13,-1.6,3.8,5.0,3.4,'glow');a('right_arm',-2.15,13,-1.8,4.3,.8,3.8,'metal')
 if group=='scabbard':a('body',4.5,6,1.5,1.6,10.5,1.8,'leather')
 if group=='shoulder_guard':a('left_arm',-2.65,-2.8,-2.8,5.3,3.6,5.6,'metal');a('left_arm',-.9,-4.0,-2.6,1.8,1.5,1.5,'coral' if v=='barnacle' else 'spikes')
 if group=='medal':a('body',2.9,2.4,-3.4,1.2,2.1,.5,'trim');a('body',2.7,4,-3.6,1.7,1.7,.6,'anchor')
 if group=='jewelry':a('head',-4.4,-4.2,-1.1,.9,2.2,1.3,'metal')
 if group=='scarf':a('body',-4.3,-.2,-3.1,8.6,2.1,5.9,'linen');a('body',2,1.5,-3.3,1.8,6.0,.7,'sash')
 if group=='backpack':a('body',-3.7,2.7,2.3,7.4,8.5,3.6,'leather');a('body',-3,8.2,5.9,6,3.2,1.0,'pouch')
 if group=='spyglass':a('right_arm',-1.5,11.1,-2.5,3.0,6.0,3.2,'metal');a('right_arm',-1.75,15.0,-2.75,3.5,1.3,3.6,'trim')
 if group=='cursed_accessory':a('body',-1.1,3.0,-3.6,2.2,3.7,1,'glow');a('body',-1.5,2.1,-3.25,3.0,1,.5,'chain')
 return {'texture':f'pirateatelier:textures/entity/cem/accessories/{group}_{v}', 'textureSize':[128,128], 'models':[{'part':p,'id':p,'attach':True,'boxes':lst} for p,lst in sh.items() if lst]}

def generate():
 with zipfile.ZipFile(V2) as src: oldnames=set(src.namelist())
 extra={}; records=[]; previews=[]
 for i,e in enumerate(PROFILES):
  for wear in WEARS:
   for leg in LEGS:
    ident=outfit_id(e,wear,leg);profile={'id':ident,'style':e['id'],'palette':e['id'],'wear':wear,'leg':leg,'tier':3 if e['group']=='armor' else 1,
      'equipment_asset':'pirateatelier:'+ident,'item_ids':['pirateatelier:'+ident+'_'+s for s in SLOTS],'concept_group':e['group'],'concept_title':e['title']}
    records.append(profile)
    tex=texture_atlas(e,wear,leg)
    extra[f'assets/pirateatelier/textures/entity/cem/{ident}.png']=pngbytes(tex)
    root=f'development_assets/cem_wardrobe/{ident}/'
    for slot in SLOTS:
     extra[root+slot+'.jem']=dump(cem(e,wear,leg,slot))
    for side in ('left','right'):
     extra[root+f'helmet_eyepatch_{side}.jem']=dump(cem(e,wear,leg,'helmet','eyepatch_'+side))
     extra[root+f'chestplate_hook_{side}.jem']=dump(cem(e,wear,leg,'chestplate','hook_'+side))
    for layer,dirn in ((1,'humanoid'),(2,'humanoid_leggings')):
     img=paint_equipment(e,wear,leg,layer)
     extra[f'assets/pirateatelier/textures/entity/equipment/{dirn}/{ident}.png']=pngbytes(img)
     extra[f'assets/pirateatelier/textures/models/armor/{ident}__layer_{layer}.png']=pngbytes(img)
    extra[f'assets/pirateatelier/equipment/{ident}.json']=dump({'layers':{'humanoid':[{'texture':f'pirateatelier:{ident}'}],'humanoid_leggings':[{'texture':f'pirateatelier:{ident}'}]}})
    for slot in SLOTS:
     itemname=ident+'_'+slot
     extra[f'assets/pirateatelier/textures/item/{itemname}.png']=pngbytes(icon(e,slot,wear,leg))
     extra[f'assets/pirateatelier/models/item/{itemname}.json']=dump({'parent':'minecraft:item/generated','textures':{'layer0':f'pirateatelier:item/{itemname}'}})
     extra[f'assets/pirateatelier/items/{itemname}.json']=dump({'model':{'type':'minecraft:model','model':f'pirateatelier:item/{itemname}'}})
  previews.append(e)
 print('22 new outfit archetypes, 198 profiles, generated assets',len(extra),flush=True)
 # Accessory atlas and modular 3D pieces. This code does not pretend these auto-equip to player.
 mods=[]
 for group,variants in ACCESSORIES.items():
  for v in variants:
   name=f'{group}_{v}';e=PROFILES[0].copy();e['id']='accessory_'+name
   c,t,d=accessory_color(group,v);e['coat']=c;e['lining']=t;e['trim']=t;e['dark']=d;e['pants']=d;e['leather']=mix(c,d,.2)
   e['motif']='glow' if 'cursed' in v else 'anchor'
   extra[f'assets/pirateatelier/textures/entity/cem/accessories/{name}.png']=pngbytes(texture_atlas(e,'pristine','normal'))
   extra[f'development_assets/cem_accessories/{name}.jem']=dump(accessory_cem(group,v))
   extra[f'assets/pirateatelier/textures/item/atelier_{name}.png']=pngbytes(accessory_icon(group,v))
   extra[f'assets/pirateatelier/models/item/atelier_{name}.json']=dump({'parent':'minecraft:item/generated','textures':{'layer0':f'pirateatelier:item/atelier_{name}'}})
   extra[f'assets/pirateatelier/items/atelier_{name}.json']=dump({'model':{'type':'minecraft:model','model':f'pirateatelier:item/atelier_{name}'}})
   mods.append({'id':'pirateatelier:atelier_'+name,'group':group,'variant':v,'cem':f'development_assets/cem_accessories/{name}.jem'})
 print('accessory module variants',len(mods),flush=True)
 extra['manifests/v3_concept_outfits.json']=dump(records)
 extra['manifests/v3_accessories.json']=dump(mods)
 extra['manifests/v3_summary.json']=dump({'additional_outfit_archetypes':len(PROFILES),'new_outfit_profiles':len(records),'new_cem_player_models':len(records)*8,'accessory_modules':len(mods),'accessory_groups':len(ACCESSORIES),'base_v2_retained':True,'automatic_player_model_selector':False,'validated_in_game':False,'fresh_player_animations_untouched':True,'art_direction':'three user-provided original pirate concept sheets'})
 extra['LEIA-ME_V3_CONCEPT_ARTS.md'] = '''# Pirate Atelier v3 — novos trajes derivados das concept arts fornecidas pelo usuário

## 1. Conteúdo novo

- **10 conjuntos de armadura**: capitão, imediato, contramestre, saqueador, duelista, artilheiro, brutamontes, oficial naval, fantasma amaldiçoado e guardião de tesouros.
- **6 roupas civis**: comerciante do porto, contrabandista, navegador renegado, corsário de taverna, marinheiro veterano e pescador pirata.
- **6 trajes de tripulação**: capitão civil, dois marujos, cozinheiro, artilheiro e mestre de embarcação.
- Cada conjunto tem **3 estados** (intacto, gasto e rasgado) e **3 opções de pernas** (normal, prótese esquerda e direita), num total de **198 novos perfis** além dos 864 da v2.
- Cada perfil novo tem 4 sprites de inventário, 2 atlas de armadura em UV vanilla, 1 atlas CEM original de 128x128, 8 modelos CEM (capacete, peito, calças, botas, gancho esquerdo/direito e tapa-olho esquerdo/direito).
- Acessórios modulares 3D com textura e sprite separados (gancho, perna de pau, tapa-olho, chapéu, bandana, papagaio, cinto, lanterna, bainha, ombreira, medalha, joalheria, cachecol, mochila, luneta e objetos amaldiçoados).

## 2. Onde estão os assets

- `assets/pirateatelier/textures/entity/equipment/humanoid/` e `humanoid_leggings/`: atlas para armaduras do Minecraft 26.2.
- `assets/pirateatelier/textures/item/`: ícones para itens (precisam ser registrados em um mod).
- `assets/pirateatelier/textures/entity/cem/`: material atlas para modelos de roupa 3D.
- `development_assets/cem_wardrobe/<id>/`: modelos CEM `.jem` por peça e suas alternativas.
- `development_assets/cem_accessories/`: módulos 3D CEM individuais.
- `manifests/v3_concept_outfits.json` e `manifests/v3_accessories.json`: índices para integração de itens/equipamentos em Fabric.

## 3. Limites de integração

**Este ZIP é uma biblioteca de assets, NÃO um mod funcional com itens novos equipáveis.** A v2 continua preservada. EMF e Fresh Player Animations NÃO selecionam automaticamente esses `.jem` por ID de um item `pirateatelier:`. Para colocar cada modelo no jogador em runtime, um mod Fabric 26.2 deve registrar os itens e associar renderer de camada à peça equipada. Não sobrescrevemos o `player.jem` do Fresh Player Animations.

Há geometria 3D completa, inclusive de ganchos e próteses; **ocultar a mão e a perna originais do modelo vanilla** continua dependendo do renderer. Os modelos foram gerados e testados sintaticamente (JSON, UV e PNG), mas **não foram testados no Minecraft/EMF**. Alguns módulos exigem ajustes finais de pivot ao serem conectados a um renderer real.

## 4. Autoria

Desenhos de textura e modelos foram implementados proceduralmente para este pacote, com base **nas concept arts enviadas pelo usuário**, sem inserir PNGs dessas concept arts dentro das texturas do Minecraft, e sem reutilizar pixels do mod de referência.
'''.encode()
 # QA: no collisions, all references resolved and no skin player.jem.
 assert not set(extra)&oldnames
 errs=[]
 for path,data in extra.items():
  if path.endswith(('.jem','.json')):
   try:d=json.loads(data)
   except Exception as ex:errs.append((path,str(ex)));continue
   if path.endswith('.jem'):
    assert d['models'],path
    for b in (b for m in d['models'] for b in m['boxes']):
     if min(b['coordinates'][3:])<=0:errs.append((path,'invalid cube size'))
     if not all(0<=q<=128 for k in ('uvUp','uvDown','uvNorth','uvSouth','uvEast','uvWest') for q in b[k]):errs.append((path,'UV out of bounds'))
     assert d['texture'].startswith('pirateatelier:')
  if path.endswith('.png'):
   try: Image.open(io.BytesIO(data)).verify()
   except Exception as ex:errs.append((path,str(ex)))
 assert not errs, errs[:10]
 for e in PROFILES:
  for wear in WEARS:
   for leg in LEGS:
    n=outfit_id(e,wear,leg)
    for slot in SLOTS:
     assert f'assets/pirateatelier/textures/item/{n}_{slot}.png' in extra
     assert f'development_assets/cem_wardrobe/{n}/{slot}.jem' in extra
 print('QC JSON/PNG/UV passed',len(extra),'additional paths',flush=True)
 # ZIP is a superset of v2, original bytes same. No path shadowing.
 with zipfile.ZipFile(V2) as src,zipfile.ZipFile(OUT,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=5,allowZip64=True) as dst:
  for inf in src.infolist():dst.writestr(inf,src.read(inf.filename))
  for p,data in extra.items():dst.writestr(p,data)
 with zipfile.ZipFile(OUT) as z:
  assert z.testzip() is None
  assert len(z.namelist())==len(oldnames)+len(extra)
  print('FINISHED v3',str(OUT),OUT.stat().st_size,len(z.namelist()),'files',flush=True)
 # create easy-to-scan contact sheet with *actual newly painted* inventory sprites, not reference image.
 f=ImageFont.load_default()
 w=1200; cellw=200;cellh=166;h=math.ceil(len(PROFILES)/6)*cellh+70
 can=Image.new('RGB',(w,h),(234,228,213));d=ImageDraw.Draw(can)
 d.text((18,10),'PIRATE ATELIER V3 | 22 CONJUNTOS NOVOS | sprites reais do ZIP',font=f,fill=(55,40,37))
 for i,e in enumerate(PROFILES):
  x=i%6*cellw; y=i//6*cellh+40
  d.rectangle((x+5,y+5,x+195,y+155), fill=(251,248,238),outline=(151,132,104))
  d.text((x+12,y+12),e['id'][:23].replace('_',' '),font=f,fill=(53,47,43))
  for j,slot in enumerate(SLOTS):
   raw=icon(e,slot,'pristine','normal').resize((76,76),Image.Resampling.NEAREST)
   px=x+19+(j%2)*83;py=y+34+(j//2)*56
   raw=raw.resize((54,54),Image.Resampling.NEAREST)
   can.paste(raw,(px,py),raw)
 can.save(ROOT/'Pirate_Atelier_V3_preview_colecoes.png',optimize=True)
 # Accessory contact sheet
 allmods=[(g,v) for g,vs in ACCESSORIES.items() for v in vs]
 cw=1100;cs=90;cn=11;rh=116
 acan=Image.new('RGB',(cw,math.ceil(len(allmods)/cn)*rh+55),(235,226,208));d=ImageDraw.Draw(acan)
 d.text((16,12),f'PIRATE ATELIER V3 | {len(allmods)} ACESSORIOS MODULARES NOVOS',font=f,fill=(48,42,41))
 for i,(g,v) in enumerate(allmods):
  x=(i%cn)*100;y=(i//cn)*rh+40
  d.rectangle((x+3,y,x+97,y+107),fill=(251,247,233),outline=(144,129,115))
  img=accessory_icon(g,v).resize((74,74),Image.Resampling.NEAREST)
  acan.paste(img,(x+13,y+5),img);d.text((x+5,y+85),v[:13],font=f,fill=(49,46,44))
 acan.save(ROOT/'Pirate_Atelier_V3_preview_acessorios.png',optimize=True)
 print('PREVIEWS saved',flush=True)
if __name__=='__main__':generate()
