#!/usr/bin/env python3
"""Original procedurally-authored pixel art and JSON models for pirate-themed equipment.
No pixel, geometry or bytecode is copied from the reference package.
"""
from __future__ import annotations
from PIL import Image, ImageDraw, ImageFont
import json, zipfile, pathlib, re, random, math, csv, io, os, hashlib
from collections import Counter

SOURCE=pathlib.Path('/mnt/data/LetsForgePirates_1.21.1_Extracao_Completa(1).zip')
OUT=pathlib.Path('/mnt/data/Pirate_Atelier_CleanRoom_26.2_v1.zip')
WORK=pathlib.Path('/mnt/data/_pirateatelier_build')
WORK.mkdir(parents=True,exist_ok=True)
SRC=zipfile.ZipFile(SOURCE)
SRC_PREFIX='src/main/resources/assets/lfpirates/'
NAMES=SRC.namelist()
ITEM_SRC=sorted(n for n in NAMES if n.startswith(SRC_PREFIX+'textures/item/') and n.endswith('.png'))
ARMOR_SRC=sorted(n for n in NAMES if n.startswith(SRC_PREFIX+'textures/models/armor/') and n.endswith('.png'))
MODEL_SRC=sorted(n for n in NAMES if n.startswith(SRC_PREFIX+'models/item/') and n.endswith('.json'))
SOURCE_ITEM_MODELS={pathlib.PurePosixPath(n).stem:json.loads(SRC.read(n)) for n in MODEL_SRC}
PALETTES={
 'crimson':'#a33b46','navy':'#293e69','sea_blue':'#277895','emerald':'#356a50',
 'forest':'#526a3a','plum':'#714e7d','ochre':'#b18443','ivory':'#bbb1a0',
 'charcoal':'#393b47','copper':'#9e5c40','teal':'#31817b','wine':'#772e4a',
 'cobalt':'#425aaa','saffron':'#b78d30','seafoam':'#5b9993','ash':'#69717a',
}
GOLD='#c49b48'; SILVER='#afbac2'; LEATHER='#65442f'; WOOD='#8a5b35'; BONE='#ddd1b5'
STYLES=('corsair','pirate','sailor','merchant','nordic','deckhand','fisherman','craftsman')
NEW_PALETTES=['crimson','navy','sea_blue','emerald','forest','plum','ochre','ivory','charcoal','copper','teal','wine']
WEAR=('pristine','weathered','tattered')
LEGS=('normal','peg_left','peg_right')
BASE_STYLES={
 'korsarz':('corsair','forest'), 'pirat':('pirate','charcoal'),
 'marynarkablue':('sailor','sea_blue'), 'marynarka':('sailor','crimson'),
 'neutralny':('sailor','ash'), 'nord':('nordic','navy'),
 'kupiec':('merchant','wine'), 'majtek':('deckhand','ochre'),
 'rybak':('fisherman','sea_blue'), 'rzemieslnik':('craftsman','copper'),
}


def rgb(h):
 h=h.lstrip('#');return tuple(int(h[i:i+2],16) for i in (0,2,4))
def shade(h,k,alpha=255):
 a=rgb(h) if isinstance(h,str) else h[:3]
 return tuple(max(0,min(255,int(v*k if k<=1 else v+(255-v)*(k-1)))) for v in a)+(alpha,)
def mix(a,b,t=0.5):
 a=rgb(a) if isinstance(a,str) else a[:3]
 b=rgb(b) if isinstance(b,str) else b[:3]
 return tuple(int(a[i]*(1-t)+b[i]*t) for i in range(3))+(255,)
def P(img):return ImageDraw.Draw(img)
def rect(d,xy,color):d.rectangle(xy,fill=color)
def line(d,pts,color,w=1):d.line(pts,fill=color,width=w)
def deterministic(s):return random.Random(int(hashlib.sha256(s.encode()).hexdigest()[:16],16))

def get_style(name):
 if 'nord_pirat' in name:return 'nordic','navy'
 for prefix,(style,palette) in BASE_STYLES.items():
  if prefix in name: return style,palette
 return 'pirate','charcoal'
def tier_of(name):
 if 'iiitier' in name or 'tier_3' in name or '_v3_' in name: return 3
 if 'iitier' in name or 'tier_2' in name or '_v2_' in name: return 2
 return 1

def wear_scratches(img,wear,part,seed,metal=False):
 if wear=='pristine':return
 d=P(img);r=deterministic(seed+'_'+wear)
 n={'weathered':4,'tattered':9}[wear]
 col=shade('#d9c9a4',.82,225) if metal else shade('#d4c5a1',.9,220)
 bbox={'chest':(4,8,28,27),'leggings':(6,9,27,28),'helmet':(3,9,29,23),'boots':(5,14,27,27)}.get(part,(4,7,28,28))
 for _ in range(n):
  x=r.randint(bbox[0],bbox[2]);y=r.randint(bbox[1],bbox[3]);
  if img.getpixel((x,y))[3]:line(d,[(x,y),(min(31,x+r.choice([1,2,3])),y+r.choice([0,1]))],col)
 if wear=='tattered' and part in ('chest','leggings'):
  for x,y in ([(5,24),(26,25),(13,26)] if part=='chest' else [(7,27),(22,26)]):
   rect(d,(x,y,x+2,y+2),(0,0,0,0));
   if x>0:line(d,[(x-1,y-1),(x-1,y+1)],shade('#b8a17a',.8))

def sprite(style, palette_name, part, tier=2, wear='pristine', peg='normal', seed=''):
 c=PALETTES.get(palette_name,palette_name);dark=shade(c,.52);low=shade(c,.78);light=shade(c,1.33)
 trim=shade(GOLD,1.08 if tier==3 else 1.0) if tier>1 else shade(SILVER,.85)
 seam=shade('#2c2325',.9); shadow=shade('#241f27',1);lining=shade('#e4d6b7',.92)
 d=P(im:=Image.new('RGBA',(32,32)))
 if part=='chest':
  # Silhouette with stepped shoulders and long ragged naval coat tails
  rect(d,(7,5,24,8),seam);rect(d,(5,9,26,25),shadow)
  d.polygon([(5,9),(2,13),(3,23),(8,25),(11,22),(20,22),(23,25),(29,23),(30,13),(26,9)],fill=dark)
  rect(d,(7,10,24,25),c);rect(d,(7,10,12,25),low);rect(d,(19,10,24,25),low)
  rect(d,(3,13,6,22),c);rect(d,(25,13,28,22),c)
  rect(d,(3,22,8,24),trim);rect(d,(24,22,28,24),trim)
  # Collar and double lapels, to signal a coat rather than a shirt
  d.polygon([(9,8),(15,10),(14,18),(9,16),(8,11)],fill=lining)
  d.polygon([(22,8),(16,10),(17,18),(22,16),(23,11)],fill=shade(lining,.76))
  rect(d,(14,11,17,22),shadow)
  if style in ('pirate','corsair','nordic'):
   d.polygon([(11,10),(13,10),(24,24),(21,24)],fill=shade(LEATHER,.85))
   line(d,[(12,11),(22,23)],trim)
   rect(d,(16,17,18,19),shade(SILVER,1.06))
  elif style=='sailor':
   for yy in (12,16,20):line(d,[(8,yy),(10,yy)],lining);line(d,[(21,yy),(24,yy)],lining)
   rect(d,(14,22,18,24),trim)
  elif style=='merchant':
   line(d,[(10,10),(10,25)],trim);line(d,[(22,10),(22,25)],trim)
   d.polygon([(12,10),(16,15),(20,10)],fill=shade('#efe2cb',.85))
  elif style=='fisherman':
   for xx,yy in ((9,19),(13,23),(23,18)):line(d,[(xx,yy),(xx+3,yy+2)],shade('#b4c4bb',.85))
  elif style=='craftsman':
   rect(d,(10,17,22,25),shade(LEATHER,1.12));rect(d,(13,19,19,24),shade(LEATHER,.7))
  elif style=='deckhand':
   rect(d,(10,11,21,20),shade('#e2d1b2',.88));line(d,[(10,14),(21,14)],dark)
  for bx,by in ((14,15),(18,19),(14,23)):
   rect(d,(bx,by,bx+1,by+1),trim)
  if tier>=2:
   line(d,[(8,26),(13,26)],trim);line(d,[(18,26),(24,26)],trim)
  if tier==3:
   rect(d,(3,18,5,20),trim);rect(d,(26,18,28,20),trim)
   line(d,[(6,10),(10,8)],trim);line(d,[(22,8),(26,10)],trim)
 if part=='leggings':
  rect(d,(6,7,25,14),shadow);rect(d,(7,8,24,13),low)
  rect(d,(6,13,14,28),dark);rect(d,(17,13,25,28),dark)
  rect(d,(8,14,13,25),c);rect(d,(19,14,24,25),c)
  line(d,[(6,15),(6,27)],shadow);line(d,[(17,15),(17,27)],shadow)
  rect(d,(8,9,23,10),shade(LEATHER,.9));rect(d,(14,8,18,12),trim);rect(d,(15,9,17,11),shadow)
  rect(d,(8,26,13,28),shade(LEATHER,.85));rect(d,(19,26,24,28),shade(LEATHER,.85))
  for xx in (9,21):line(d,[(xx,17),(xx+2,19)],shade(c,1.25))
  if style in ('corsair','pirate','nordic'):
   rect(d,(10,12,11,16),trim);rect(d,(22,12,23,16),trim)
  if tier>=2:
   line(d,[(7,16),(14,16)],trim);line(d,[(18,16),(25,16)],trim)
  if peg!='normal':
   px=8 if peg=='peg_left' else 19
   rect(d,(px-1,21,px+6,30),(0,0,0,0))
   rect(d,(px,20,px+5,22),shade(SILVER,.84))
   rect(d,(px+1,23,px+4,29),shade(WOOD,.97))
   line(d,[(px+2,23),(px+2,29)],shade(WOOD,1.35))
   rect(d,(px+1,26,px+4,27),shade('#30333a',1))
   rect(d,(px+2,30,px+3,30),shade('#2b2522',1))
 if part=='boots':
  for x in (4,18):
   rect(d,(x+2,9,x+9,23),shadow)
   rect(d,(x+3,10,x+8,20),shade(LEATHER,.75))
   rect(d,(x+2,11,x+9,14),low)
   rect(d,(x+2,20,x+10,24),shade(LEATHER,.57))
   rect(d,(x,24,x+11,26),shadow)
   rect(d,(x+1,25,x+11,26),shade('#54505a',.87))
   if tier>=2:
    rect(d,(x+2,16,x+9,17),trim);rect(d,(x+6,15,x+7,18),shadow)
   if style=='sailor':rect(d,(x+2,12,x+9,13),lining)
  if wear=='tattered':rect(d,(4,25,6,26),(0,0,0,0))
 if part=='helmet':
  if style in ('deckhand','fisherman','craftsman') or tier==1 and style in ('pirate','corsair','nordic'):
   # folded bandana or soft naval cap
   d.polygon([(6,11),(9,7),(23,7),(27,11),(27,17),(23,19),(7,19),(5,16)],fill=shadow)
   d.polygon([(7,11),(11,9),(22,9),(26,12),(25,16),(7,16)],fill=c)
   line(d,[(7,16),(26,16)],trim,2)
   if style=='deckhand':
    d.polygon([(24,17),(29,19),(27,24),(23,21)],fill=c)
    rect(d,(27,23,28,25),dark)
   if style=='fisherman':line(d,[(9,10),(21,10)],lining)
   if style=='craftsman':rect(d,(13,11,18,12),shade(LEATHER,1.3))
  else:
   # Original tricorn silhouette with raised corners, lower cutouts, front crown
   d.polygon([(2,18),(6,11),(9,8),(12,10),(20,10),(24,8),(27,11),(30,18),
              (26,22),(21,21),(11,21),(6,22)],fill=shadow)
   d.polygon([(4,18),(8,12),(13,14),(19,14),(24,12),(28,18),(24,19),(8,19)],fill=dark)
   d.polygon([(9,11),(12,7),(20,7),(23,11),(22,16),(10,16)],fill=c)
   line(d,[(6,18),(26,18)],trim,2)
   rect(d,(14,12,18,15),shade(LEATHER,.65))
   if tier>=2:
    rect(d,(15,13,17,15),trim)
   if tier==3:
    d.polygon([(21,11),(25,3),(23,11),(29,7),(25,14)],fill=shade('#f3e5c6',1))
    line(d,[(24,5),(24,12)],shade('#a59788',.7))
  if wear=='tattered':
   rect(d,(7,16,9,18),(0,0,0,0))
 if part=='eyepatch':
  line(d,[(2,11),(29,18)],shade(LEATHER,.8),2)
  line(d,[(3,12),(29,19)],shade(SILVER,.92))
  d.polygon([(8,12),(18,12),(22,15),(18,23),(12,24),(8,18)],fill=shadow)
  d.polygon([(10,13),(18,14),(20,16),(17,21),(12,21)],fill=c)
  rect(d,(13,15,17,16),trim)
 if part=='skull':
  # Helmet resembling a ceremonial death's-head, entirely newly drawn
  d.polygon([(7,6),(11,3),(21,3),(25,6),(27,15),(22,20),(22,25),(10,25),(10,20),(5,15)],fill=shadow)
  d.polygon([(9,7),(13,5),(20,5),(24,7),(25,14),(22,19),(20,22),(12,22),(8,17)],fill=c)
  d.polygon([(9,12),(14,12),(14,16),(11,18),(9,16)],fill=shadow)
  d.polygon([(18,12),(23,12),(23,16),(20,18),(18,16)],fill=shadow)
  d.polygon([(16,16),(14,20),(18,20)],fill=shadow)
  for xx in (12,15,18,21):rect(d,(xx,22,xx+1,25),shade('#26242a',.8))
  line(d,[(9,9),(13,7)],shade(c,1.35));line(d,[(18,7),(23,10)],shade(c,1.28))
 if part=='leather':
  d.polygon([(7,5),(24,5),(27,11),(23,25),(9,28),(4,24),(5,15)],fill=shade(LEATHER,.9))
  d.polygon([(8,8),(22,7),(23,19),(20,24),(10,25),(7,21)],fill=shade(LEATHER,1.26))
  for j in range(8,25,4):line(d,[(8,j),(11,j+2)],shade(LEATHER,.71))
 if wear!='pristine' and part not in ('leather','skull','eyepatch'):
  wear_scratches(im,wear,part,seed or style+palette_name+str(tier))
 return im

# UV atlas sides: head front (8..16,8..16), body front (20..28,20..32), arm front (44..48,20..32), leg front (4..8,20..32).
def armor_atlas(style,pal,tier,wear,peg,layer,seed):
 im=Image.new('RGBA',(64,32));d=P(im)
 c=PALETTES[pal]; outline=shade(c,.51); base=shade(c,.9);light=shade(c,1.26); dark=shade(c,.67)
 trim=shade(GOLD if tier>1 else SILVER,1.0)
 leather=shade(LEATHER,.93);r=deterministic(seed+'atlas')
 def surface(box, col, edge=None):
  rect(d,box,col)
  if edge and box[2]>box[0]+2:
   line(d,[(box[0],box[1]),(box[2],box[1])],edge)
 if layer==1:
  # all six UV segments for head (top/bottom/sides/front/back)
  surface((8,0,15,7),dark);surface((16,0,23,7),dark)
  for xx in (0,8,16,24):surface((xx,8,xx+7,15),dark)
  surface((8,8,15,15),base)
  if style in ('pirate','corsair','sailor','merchant','nordic'):
   line(d,[(8,13),(15,13)],trim)
   if tier==3:rect(d,(11,10,12,12),trim)
  else:line(d,[(8,10),(15,10)],light)
  # torso UV: front and back, sleeves all faces
  surface((20,16,27,19),dark);surface((28,16,35,19),dark)
  surface((16,20,19,31),outline);surface((20,20,27,31),base)
  surface((28,20,31,31),outline);surface((32,20,39,31),dark)
  # coat lapels, vest, buttons, sash
  d.polygon([(20,21),(23,20),(24,23),(23,29),(20,27)],fill=shade('#dac9a7',.95))
  d.polygon([(27,21),(25,20),(24,23),(25,29),(27,27)],fill=shade('#d4c2a0',.84))
  line(d,[(24,20),(24,31)],outline)
  for xx,yy in ((23,24),(25,26),(23,29)):rect(d,(xx,yy,xx,yy),trim)
  if style in ('pirate','corsair','nordic'):
   line(d,[(21,22),(26,30)],leather,2);rect(d,(24,26,25,27),trim)
  if style=='sailor':
   for yy in (23,26,29):line(d,[(20,yy),(21,yy)],light);line(d,[(26,yy),(27,yy)],light)
  if style=='merchant':
   line(d,[(20,21),(20,30)],trim);line(d,[(27,21),(27,30)],trim)
  if style=='craftsman':surface((21,27,26,31),leather)
  if style=='fisherman':line(d,[(21,25),(27,29)],shade('#a0c5b4',.9))
  surface((20,30,27,31),leather)
  if tier==3:line(d,[(20,30),(27,30)],trim)
  # arm layout: top/bottom at u44..52, side panels u40..56
  for box in ((44,16,47,19),(48,16,51,19),(40,20,43,31),(44,20,47,31),(48,20,51,31),(52,20,55,31)):
   surface(box,dark if box[0] in (40,52) else base)
  for xx in (40,44,48,52):
   rect(d,(xx,27,xx+3,28),trim)
   if tier>1:rect(d,(xx,29,xx+3,30),outline)
 else:
  # 3D pants UV 64x32: four repeated sides with different shading; pants/peg leg tex
  for box in ((4,16,7,19),(8,16,11,19),(0,20,3,31),(4,20,7,31),(8,20,11,31),(12,20,15,31)):
   surface(box, base if box[0] in (4,8) else dark)
  for xx in (0,4,8,12):
   line(d,[(xx,20),(xx+3,20)],trim)
   rect(d,(xx,24,xx+3,25),light)
   rect(d,(xx,29,xx+3,31),leather)
  # This UV is mirrored for both legs in vanilla humanoid equipment.
  # The single LEFT/RIGHT peg is instead authored in item sprites and
  # a client-side attachment geometry; painting it here would give TWO pegs.
  if style=='merchant':
   for xx in (0,4,8,12):rect(d,(xx+1,22,xx+2,24),trim)
 if wear in ('weathered','tattered'):
  n=4 if wear=='weathered' else 13
  for _ in range(n):
   x=r.randrange(64);y=r.randrange(32)
   if im.getpixel((x,y))[3]:
    d.point((x,y),fill=shade('#d6c6a4',.87,255))
 if wear=='tattered':
  # Transparent edge damage at armor UV seams
  for x,y in ([(19,31),(39,31),(47,31),(55,31)] if layer==1 else [(0,31),(15,31)]):
   d.point((x,y),fill=(0,0,0,0))
 return im

def skull_armor_atlas(group,layer):
 im=Image.new('RGBA',(64,32));d=P(im)
 if layer==2:return im  # old mod used empty pants layer for mask-only helmet
 pal=('ivory' if group.startswith('biala') else 'ochre' if group.startswith('brudna')
      else 'saffron' if group.startswith('zlota') else 'sea_blue' if group.startswith('diamentowa') else 'charcoal')
 c=PALETTES[pal]
 for x in (0,8,16,24):rect(d,(x,8,x+7,15),shade(c,.75 if x in (0,24) else 1))
 rect(d,(8,0,15,7),shade(c,1.2));rect(d,(16,0,23,7),shade(c,.6))
 rect(d,(9,10,10,11),shade('#1f1d23',1));rect(d,(13,10,14,11),shade('#1f1d23',1))
 d.polygon([(11,12),(10,14),(13,14)],fill=shade('#292729',1))
 for x in (9,11,13,15):d.point((x,15),fill=shade('#383037',1))
 d.line([(8,8),(15,8)],fill=shade(c,1.35))
 return im

def specialized_headwear(name,pal):
 # new original 64x64 hat layer texture for legacy custom hat renderer
 im=Image.new('RGBA',(64,64));d=P(im);c=PALETTES[pal]
 for face in ((0,16,15,31),(16,16,31,31),(32,16,47,31),(48,16,63,31)):
  rect(d,face,shade(c,.63))
  for yy in (22,25):line(d,[(face[0],yy),(face[2],yy)],shade(c,1.27))
  line(d,[(face[0],29),(face[2],29)],shade(GOLD,1.03))
 for box in ((16,0,31,15),(32,0,47,15)):
  rect(d,box,shade(c,.8));line(d,[(box[0],1),(box[2],1)],shade(c,1.25))
 return im


def save_image(rel,im):
 dest=WORK/rel;dest.parent.mkdir(parents=True,exist_ok=True);im.save(dest,'PNG',optimize=True)
def save_json(rel,data):
 dest=WORK/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def save_text(rel,s):
 dest=WORK/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(s,encoding='utf-8')

# === 3D original, block-aligned geometry ===
MAT_COLORS={'iron':('#abb5bb','#747e87','#e3e8e8'),'brass':('#b8974f','#715532','#f2d17e'),
 'copper':('#a97056','#6a4036','#e0a278'),'blacksteel':('#626c75','#292e34','#9aabb1'),
 'bone':('#ded5b7','#9f9175','#fff1d8'),'poison':('#92b786','#365e3c','#d7e8a7'),
 'silver':('#a6bac9','#526273','#e4edec')}
def atlas_material(metal):
 light,dark,shine=MAT_COLORS[metal]
 palette=[light,dark,shine,LEATHER,GOLD,WOOD,'#181d25','#e6dac7','#9c7b44','#4e5249',light,dark,shine,LEATHER,GOLD,SILVER]
 im=Image.new('RGBA',(64,64));d=P(im)
 for idx,h in enumerate(palette):
  x=(idx%4)*16;y=(idx//4)*16
  rect(d,(x,y,x+15,y+15),shade(h,.9))
  line(d,[(x,y),(x+15,y)],shade(h,1.24));line(d,[(x,y),(x,y+15)],shade(h,1.17))
  line(d,[(x+15,y),(x+15,y+15)],shade(h,.68));line(d,[(x,y+15),(x+15,y+15)],shade(h,.64))
  rect(d,(x+4,y+6,x+10,y+6),shade(h,1.11))
 return im

def cube(box,tile=0,rot=None):
 u=(tile%4)*4;v=(tile//4)*4
 faces={face:{'uv':[u,v,u+4,v+4],'texture':'#surface'} for face in ('north','south','east','west','up','down')}
 e={'from':list(box[:3]),'to':list(box[3:]),'faces':faces}
 if rot:e['rotation']={'origin':rot[0],'axis':rot[1],'angle':rot[2],'rescale':True}
 return e

def hook_elements():
 return [
 cube((6.0,1.0,6.5,10.0,2.2,9.5),1),
 cube((6.3,2.2,6.8,9.7,6.0,9.2),3), # leather socket
 cube((6.0,2.8,6.45,10.0,3.5,9.55),4),cube((6.0,5.0,6.45,10.0,5.8,9.55),4),
 cube((6.9,5.8,7.1,9.1,11.4,8.9),0),
 cube((6.8,10.9,7.0,9.0,13.0,9.0),2),
 cube((5.0,12.0,7.0,8.8,13.9,9.0),0),
 cube((3.5,11.0,7.0,5.5,13.5,9.0),0),
 cube((2.8,9.3,7.05,4.4,11.9,8.95),2),
 cube((3.3,8.6,7.25,4.15,9.7,8.75),0),
 cube((7.4,6.1,6.55,8.6,6.9,9.45),2),
 ]

def saber_elements(kind):
 # raised blade, slightly asymmetrical and angular to emulate cutlass curvature
 return [cube((6.5,0.6,6.8,9.5,1.8,9.3),1),cube((7.0,1.8,7.1,9.0,5.5,8.9),3),
 cube((6.2,2.6,6.7,9.8,3.3,9.3),4),
 cube((4.1,5.0,7.3,11.8,6.1,8.7),4),cube((4.3,4.8,7.45,5.3,7.8,8.55),4),
 cube((7.0,6.0,7.2,10.2,11.6,8.8),0),
 cube((7.0,11.1,7.2,10.8,14.1,8.8),2),
 cube((7.5,13.4,7.2,11.8,15.6,8.8),0),
 cube((7.2,7.0,7.21,7.7,13.8,7.5),2),
 cube((9.9,7.4,7.3,10.5,10.8,8.7),1),
 ]
DISPLAY={
 'gui':{'rotation':[16,-28,30],'translation':[0,1,0],'scale':[1.0,1.0,1.0]},
 'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.55,.55,.55]},
 'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[.85,.85,.85]},
 'thirdperson_righthand':{'rotation':[0,0,-10],'translation':[0,2,1],'scale':[.82,.82,.82]},
 'thirdperson_lefthand':{'rotation':[0,0,10],'translation':[0,2,1],'scale':[.82,.82,.82]},
 'firstperson_righthand':{'rotation':[0,-35,7],'translation':[1,3,1],'scale':[1,1,1]},
 'firstperson_lefthand':{'rotation':[0,35,-7],'translation':[1,3,1],'scale':[1,1,1]},
}
def three_d_model(namespace,tex,kind):
 return {'credit':'Pirate Atelier original voxel model','textures':{'surface':f'{namespace}:item/voxel/{tex}',
   'particle':f'{namespace}:item/voxel/{tex}'},'elements':hook_elements() if kind=='hook' else saber_elements(kind),
   'display':DISPLAY,'gui_light':'front'}

def overlay_item_model(namespace,name,tex=None):
 return {'parent':'minecraft:item/generated','textures':{'layer0':f'{namespace}:item/{tex or name}'}}
def modern_item(namespace,name):
 return {'model':{'type':'minecraft:model','model':f'{namespace}:item/{name}'}}
def modern_3d_item(namespace,name):
 return {'model':{'type':'minecraft:select','property':'minecraft:display_context',
  'cases':[{'when':['gui','fixed','ground'],
            'model':{'type':'minecraft:model','model':f'{namespace}:item/{name}_inventory'}}],
  'fallback':{'type':'minecraft:model','model':f'{namespace}:item/{name}'}}}

def equipment_json(namespace,name):
 return {'layers':{
  'humanoid':[{'texture':f'{namespace}:{name}'}],
  'humanoid_leggings':[{'texture':f'{namespace}:{name}'}]}}

def cutlass_icon(metal,theme='silver'):
 im=Image.new('RGBA',(32,32));d=P(im)
 light,dark,shine=MAT_COLORS[metal]
 # diagonal blade, black outline + bright edge
 d.polygon([(6,27),(10,24),(24,5),(28,2),(26,8),(13,25),(9,29)],fill=shade('#292a2d',1))
 d.polygon([(10,25),(24,7),(27,3),(25,10),(12,26)],fill=shade(light,1))
 line(d,[(11,24),(25,7)],shade(shine,1),2)
 line(d,[(9,24),(15,30)],shade(GOLD,1),2)
 d.polygon([(5,24),(8,22),(12,26),(10,29)],fill=shade(LEATHER,1))
 d.polygon([(4,26),(6,25),(9,28),(7,30)],fill=shade(GOLD,.93))
 return im

def hook_icon(metal):
 im=Image.new('RGBA',(32,32));d=P(im);li,da,hi=MAT_COLORS[metal]
 rect(d,(12,19,19,28),shade(LEATHER,.68));rect(d,(11,19,20,21),shade(GOLD,1.0))
 rect(d,(12,27,19,29),shade(GOLD,.83));
 d.polygon([(13,19),(12,9),(16,5),(24,4),(27,9),(26,15),(22,18),(20,17),(24,13),(24,9),(21,7),(17,9),(17,19)],fill=shade(da,1))
 d.polygon([(15,18),(14,10),(17,7),(23,6),(25,9),(24,14),(22,16),(23,11),(22,8),(18,9),(17,18)],fill=shade(li,1))
 line(d,[(15,15),(15,10),(18,7),(23,7)],shade(hi,1))
 return im

def material_from_original(name):
 if name=='bonesaber':return 'bone'
 if name=='poisonbonesaber':return 'poison'
 if name=='ironsaber':return 'iron'
 return 'iron'

# === Create base overlays, by original registry and texture IDs ===
source_to_new={}
base_counts=Counter()
for source in ITEM_SRC:
 name=pathlib.PurePosixPath(source).stem
 style,pal=get_style(name)
 tier=tier_of(name)
 if name in ('ironsaber','bonesaber','poisonbonesaber'): im=cutlass_icon(material_from_original(name));part='cutlass'
 elif name=='hook_icon':im=hook_icon('iron');part='hook'
 elif name=='eyepatch':im=sprite(style,'copper','eyepatch',tier);part='eyepatch'
 elif name=='sk3fra':im=sprite(style,'copper','leather',tier);part='leather'
 elif 'czacha' in name:
  skpal='ivory' if 'biala' in name else 'ochre' if 'brudna' in name else 'charcoal' if 'czarna' in name else 'saffron' if 'zlota' in name else 'sea_blue'
  im=sprite('pirate',skpal,'skull',tier);part='skull'
 elif name.startswith('buty_'):part='boots';im=sprite(style,pal,part,tier,seed=name)
 elif name.startswith('spodnie_'):part='leggings';im=sprite(style,pal,part,tier,seed=name)
 elif name.startswith('pancerz_'):part='chest';im=sprite(style,pal,part,tier,seed=name)
 elif name.startswith(('kapelusz_','chusta_','czapa_','czapka_')):part='helmet';im=sprite(style,pal,part,tier,seed=name)
 else:raise RuntimeError('Unsupported item texture:'+name)
 save_image('assets/lfpirates/textures/item/'+name+'.png',im)
 source_to_new[name]={'part':part,'style':style,'palette':pal,'tier':tier}
 base_counts[part]+=1

# Armor atlases, preserving every originally referenced name
armor_groups={}
for source in ARMOR_SRC:
 name=pathlib.PurePosixPath(source).stem
 group,layer=name.rsplit('__layer_',1)
 layer=int(layer)
 style,pal=get_style(group);tier=tier_of(group)
 im=skull_armor_atlas(group,layer) if 'czacha' in group else armor_atlas(style,pal,tier,'pristine','normal',layer,group)
 save_image(f'assets/lfpirates/textures/models/armor/{name}.png',im)
 save_image(f'assets/lfpirates/textures/entity/equipment/{"humanoid" if layer==1 else "humanoid_leggings"}/{group}.png',im)
 armor_groups.setdefault(group,set()).add(layer)
for group in armor_groups:save_json(f'assets/lfpirates/equipment/{group}.json',equipment_json('lfpirates',group))

# Hat overlays originally both root and entities paths, only hat textures, no mob textures
hat_paths=[n for n in NAMES if n.startswith(SRC_PREFIX+'textures/') and n.endswith('_layer_kapelusz.png')]
for name in hat_paths:
 rel=name[len(SRC_PREFIX):];_,pal=get_style(pathlib.PurePosixPath(name).stem)
 save_image('assets/lfpirates/'+rel,specialized_headwear(name,pal))

# Bridge original item IDs. 3D sabres/hooks custom-authored and texture manifests
orig_weapons={'hook':('hook','iron'),'iron_cutlass':('saber','iron'),
 'bone_cutlass':('saber','bone'),'poison_bone_cutlass':('saber','poison')}
for modelname,model in SOURCE_ITEM_MODELS.items():
 if modelname in ('barrel_bomb','ship_in_bottle'):continue
 if modelname in orig_weapons:
  kind,metal=orig_weapons[modelname]
  tex=f'{modelname}_surface'
  save_image(f'assets/lfpirates/textures/item/voxel/{tex}.png',atlas_material(metal))
  save_json(f'assets/lfpirates/models/item/{modelname}.json',three_d_model('lfpirates',tex,kind))
  icontex={'hook':'hook_icon','iron_cutlass':'ironsaber','bone_cutlass':'bonesaber','poison_bone_cutlass':'poisonbonesaber'}[modelname]
  save_json(f'assets/lfpirates/models/item/{modelname}_inventory.json',overlay_item_model('lfpirates',modelname,icontex))
 else:
  layer0=(model.get('textures') or {}).get('layer0')
  if not layer0:raise RuntimeError(f'No layer for {modelname}')
  save_json(f'assets/lfpirates/models/item/{modelname}.json',overlay_item_model('lfpirates',modelname,layer0.split(':item/')[-1]))
 save_json(f'assets/lfpirates/items/{modelname}.json',modern_3d_item('lfpirates',modelname) if modelname in orig_weapons else modern_item('lfpirates',modelname))

# Eyepatch was a loose texture in extracted file; attach as optional item model only (requires registration)
save_json('assets/lfpirates/models/item/eyepatch.json',overlay_item_model('lfpirates','eyepatch'))
save_json('assets/lfpirates/items/eyepatch.json',modern_item('lfpirates','eyepatch'))

# === Expansions: 8 garment families x 12 palettes x 3 wear levels x 3 prosthetic settings ===
records=[]
for style in STYLES:
 for pal in NEW_PALETTES:
  for wear in WEAR:
   for leg in LEGS:
    outfit=f'{style}_{pal}_{wear}_{leg}'
    tier={'corsair':3,'pirate':2,'sailor':2,'merchant':2,'nordic':3,'deckhand':1,'fisherman':1,'craftsman':2}[style]
    for piece in ('helmet','chestplate','leggings','boots'):
     part={'chestplate':'chest','leggings':'leggings','boots':'boots','helmet':'helmet'}[piece]
     fname=f'{outfit}_{piece}'
     img=sprite(style,pal,part,tier,wear,leg,seed=outfit)
     save_image(f'assets/pirateatelier/textures/item/{fname}.png',img)
     save_json(f'assets/pirateatelier/models/item/{fname}.json',overlay_item_model('pirateatelier',fname))
     save_json(f'assets/pirateatelier/items/{fname}.json',modern_item('pirateatelier',fname))
    for layer in (1,2):
     im=armor_atlas(style,pal,tier,wear,leg,layer,outfit)
     save_image(f'assets/pirateatelier/textures/models/armor/{outfit}__layer_{layer}.png',im)
     save_image(f'assets/pirateatelier/textures/entity/equipment/{"humanoid" if layer==1 else "humanoid_leggings"}/{outfit}.png',im)
    save_json(f'assets/pirateatelier/equipment/{outfit}.json',equipment_json('pirateatelier',outfit))
    records.append({'id':outfit,'style':style,'palette':pal,'wear':wear,'leg':leg,'tier':tier,
      'equipment_asset':f'pirateatelier:{outfit}',
      'item_ids':[f'pirateatelier:{outfit}_{p}' for p in ('helmet','chestplate','leggings','boots')]})

# === Accessories: original designs, inventory icon and 3D player-hand hook ===
for metal in ('iron','brass','copper','blacksteel','silver'):
 name=f'{metal}_hook'
 save_image(f'assets/pirateatelier/textures/item/{name}.png',hook_icon(metal))
 save_image(f'assets/pirateatelier/textures/item/voxel/{name}.png',atlas_material(metal))
 save_json(f'assets/pirateatelier/models/item/{name}.json',three_d_model('pirateatelier',name,'hook'))
 save_json(f'assets/pirateatelier/models/item/{name}_inventory.json',overlay_item_model('pirateatelier',name))
 save_json(f'assets/pirateatelier/items/{name}.json',modern_3d_item('pirateatelier',name))
for metal,name in [('iron','naval_cutlass'),('brass','gilded_saber'),('copper','corroded_cutlass'),('blacksteel','midnight_saber'),('bone','bone_falchion'),('poison','venom_saber'),('silver','officer_rapier')]:
 save_image(f'assets/pirateatelier/textures/item/{name}.png',cutlass_icon(metal))
 save_image(f'assets/pirateatelier/textures/item/voxel/{name}.png',atlas_material(metal))
 save_json(f'assets/pirateatelier/models/item/{name}.json',three_d_model('pirateatelier',name,'saber'))
 save_json(f'assets/pirateatelier/models/item/{name}_inventory.json',overlay_item_model('pirateatelier',name))
 save_json(f'assets/pirateatelier/items/{name}.json',modern_3d_item('pirateatelier',name))
for pat,mat in [('black','charcoal'),('brown','copper'),('emerald','emerald'),('officer','navy'),('crimson','crimson'),('lace','ivory'),('royal','plum'),('weathered','ochre')]:
 name=f'{pat}_eyepatch';save_image(f'assets/pirateatelier/textures/item/{name}.png',sprite('pirate',mat,'eyepatch',3))
 save_json(f'assets/pirateatelier/models/item/{name}.json',overlay_item_model('pirateatelier',name))
 save_json(f'assets/pirateatelier/items/{name}.json',modern_item('pirateatelier',name))

# Geometry templates to give to future Fabric player feature renderer (cube-only compatible with Blockbench)
ATTACHMENTS={
 'prosthetic_right_hand_hook':{'bones':[{'name':'rightArm','pivot':[-5,2,0], 'cubes':[
   {'origin':[-7,-12,-3],'size':[4,3,6],'uv':[12,0]},
   {'origin':[-6,-17,-2],'size':[2,6,4],'uv':[0,0]},
   {'origin':[-8,-20,-2],'size':[4,2,4],'uv':[0,8]},
   {'origin':[-10,-19,-2],'size':[2,4,4],'uv':[0,12]}]}]},
 'prosthetic_right_peg_leg':{'bones':[{'name':'rightLeg','pivot':[-1.9,12,0], 'cubes':[
   {'origin':[-3.2,-1,-2.1],'size':[4.4,4,4.2],'uv':[0,0]},
   {'origin':[-2.1,3,-1.0],'size':[2.2,8,2.2],'uv':[16,0]},
   {'origin':[-2.5,8,-1.3],'size':[3,1,3],'uv':[24,0]}]}]},
 'prosthetic_left_peg_leg':{'bones':[{'name':'leftLeg','pivot':[1.9,12,0], 'cubes':[
   {'origin':[-1.2,-1,-2.1],'size':[4.4,4,4.2],'uv':[0,0]},
   {'origin':[0.1,3,-1.0],'size':[2.2,8,2.2],'uv':[16,0]},
   {'origin':[-0.3,8,-1.3],'size':[3,1,3],'uv':[24,0]}]}]},
 'tricorn_hat':{'bones':[{'name':'head','pivot':[0,0,0], 'cubes':[
   {'origin':[-5,-9,-5],'size':[10,3,10],'uv':[0,0]},
   {'origin':[-7,-8,-6],'size':[14,1,12],'uv':[0,16]},
   {'origin':[-6,-11,-2],'size':[2,3,7],'uv':[16,0]},
   {'origin':[4,-11,-2],'size':[2,3,7],'uv':[16,0]}]}]},
 'eyepatch':{'bones':[{'name':'head','pivot':[0,0,0], 'cubes':[
   {'origin':[-4.3,-5,-4.35],'size':[3,3,.6],'uv':[0,0]},
   {'origin':[-4.2,-5, -3.8],'size':[8,.8,.7],'uv':[0,8]}]}]},
}
# Modular, original 3D apparel meshes for client feature renderer / Blockbench import.
# Base family meshes can be materialized using the original 64x32 atlas; variants
# reuse their family mesh and their new atlas. These models are not baked into
# vanilla player equipment automatically.
for style in STYLES:
 basebones=[
  {'name':'head','pivot':[0,0,0],'cubes':[]},
  {'name':'body','pivot':[0,0,0],'cubes':[
    {'origin':[-4.25,0,-2.25],'size':[8.5,12.0,4.5],'uv':[16,16]},
    {'origin':[-4.35,11,-2.5],'size':[8.7,1.4,5.0],'uv':[20,16]}]},
  {'name':'rightArm','pivot':[-5,2,0],'cubes':[
    {'origin':[-8.6,1.8,-2.5],'size':[4.6,11,5],'uv':[40,16]}]},
  {'name':'leftArm','pivot':[5,2,0],'cubes':[
    {'origin':[4,1.8,-2.5],'size':[4.6,11,5],'uv':[40,16]}]},
  {'name':'rightLeg','pivot':[-1.9,12,0],'cubes':[]},
  {'name':'leftLeg','pivot':[1.9,12,0],'cubes':[]},
 ]
 if style in ('corsair','pirate','nordic','merchant','sailor'):
  basebones[0]['cubes'] += [
   {'origin':[-5.4,-8.9,-5.0],'size':[10.8,2.7,10.0],'uv':[0,0]},
   {'origin':[-6.7,-7.6,-5.5],'size':[13.4,0.8,11.0],'uv':[0,8]},
   {'origin':[-5.9,-10,-2],'size':[1.6,2.9,5.0],'uv':[0,0]},
   {'origin':[4.3,-10,-2],'size':[1.6,2.9,5.0],'uv':[0,0]}]
 else:
  basebones[0]['cubes'] += [
   {'origin':[-4.7,-8,-4.7],'size':[9.4,1.3,9.4],'uv':[0,0]},
   {'origin':[-4.1,-9.2,-4.2],'size':[8.2,1.5,8.4],'uv':[8,0]}]
 if style in ('corsair','pirate','merchant','nordic'):
  # shoulder guards, long coat tails and front belt clasp
  basebones[1]['cubes'] += [
   {'origin':[-4.8,8.8,-2.7],'size':[9.6,3.0,5.4],'uv':[20,20]},
   {'origin':[-4.5,11,-2.7],'size':[3.3,5.5,1.0],'uv':[20,20]},
   {'origin':[1.2,11,-2.7],'size':[3.3,5.5,1.0],'uv':[20,20]}]
  basebones[2]['cubes'].append({'origin':[-8.9,1.5,-3.2],'size':[5.5,2.4,6.3],'uv':[40,16]})
  basebones[3]['cubes'].append({'origin':[3.4,1.5,-3.2],'size':[5.5,2.4,6.3],'uv':[40,16]})
 if style=='craftsman':
  basebones[1]['cubes'].append({'origin':[-3.2,5.8,-2.9],'size':[6.4,6.8,0.8],'uv':[20,20]})
 if style=='fisherman':
  basebones[1]['cubes'].append({'origin':[-4.3,0.5,-2.8],'size':[8.6,4,0.7],'uv':[20,20]})
 if style=='deckhand':
  basebones[1]['cubes'].append({'origin':[-3.3,1,-2.7],'size':[6.6,8,0.7],'uv':[20,20]})
 data={'format_version':'1.12.0','minecraft:geometry':[{'description':{
  'identifier':f'geometry.pirateatelier.clothing_{style}',
  'texture_width':64,'texture_height':32,
  'visible_bounds_width':4,'visible_bounds_height':4,'visible_bounds_offset':[0,0,0]},
  'bones':basebones}]}
 save_json(f'development_assets/geometries/clothing_{style}.geo.json',data)

for name,data in ATTACHMENTS.items():
 data['format_version']='1.12.0'
 data['minecraft:geometry']=[{'description':{'identifier':'geometry.pirateatelier.'+name,'texture_width':64,'texture_height':64,
  'visible_bounds_width':3,'visible_bounds_height':3,'visible_bounds_offset':[0,0,0]},'bones':data.pop('bones')}]
 save_json(f'development_assets/geometries/{name}.geo.json',data)
# Separate custom geometry isn't loaded by vanilla player renderer, kept in developer directory.

# Optional sprite icons for each style/palette/wear for people planning plugin models; don't create unsupported vanilla item registries.
save_json('manifests/variants.json',records)
save_json('manifests/reference_inventory.json',{
 'source_zip':SOURCE.name,'source_item_texture_paths':[n[len(SRC_PREFIX):] for n in ITEM_SRC],
 'source_armor_texture_paths':[n[len(SRC_PREFIX):] for n in ARMOR_SRC],
 'source_item_model_ids':[pathlib.PurePosixPath(n).stem for n in MODEL_SRC],
 'authored_item_profiles':source_to_new,
 'counts':{'item_textures':len(ITEM_SRC),'armor_textures':len(ARMOR_SRC),'armor_sets':len(armor_groups),'outfit_variants':len(records)},
 'source_pixels_copied':0,'source_models_copied':0,
})

save_json('pack.mcmeta',{'pack':{'min_format':88,'max_format':88,'description':'Pirate Atelier | arte original de vestimentas, armaduras, espadas e ganchos | MC 26.2'}})

# Previews, generated exclusively from our own sprites
FONT=ImageFont.load_default()
def render_sheet(styles,colors,wears,legs,title,pieces=('helmet','chest','leggings','boots')):
 scale=3;cw=116;ch=154
 im=Image.new('RGB',(len(colors)*cw+24,(len(styles)*len(wears)*len(legs))*ch+65),(21,25,32));d=P(im)
 d.text((12,11),title,font=FONT,fill=(244,238,218))
 d.text((12,27),'Original procedural pixel art | 32x32 inventory sprites | zoom nearest-neighbor',font=FONT,fill=(151,169,174))
 y0=65
 for ii,(style,wear,leg) in enumerate((s,w,l) for s in styles for w in wears for l in legs):
  y=y0+ii*ch;d.text((7,y+1),f'{style} | {wear} | {leg}',font=FONT,fill=(211,206,182))
  for j,pal in enumerate(colors):
   x=16+j*cw
   if ii==0:d.text((x,y-12),pal,font=FONT,fill=(214,214,214))
   for k,part in enumerate(pieces):
    sprite_im=sprite(style,pal,part,2,wear,leg)
    t=sprite_im.resize((32*scale,32*scale),resample=Image.Resampling.NEAREST)
    im.paste(t,(x,y+22+k*31),t)
    d.text((x+35,y+35+k*31),part[:5],font=FONT,fill=(166,181,186))
 return im
# Better contact sheet with icons in row and captions rather than giant repetitive columns

def gallery(title, selections, target):
 (WORK/target).parent.mkdir(parents=True,exist_ok=True)
 cols=8;w=160;h=178;rows=math.ceil(len(selections)/cols)
 im=Image.new('RGB',(cols*w,56+rows*h),(23,27,34));d=P(im)
 d.text((17,13),title,fill=(246,230,207),font=FONT)
 d.text((17,30),'New designs, no pixels reused from reference',fill=(169,180,182),font=FONT)
 for idx,(style,pal,wear,peg) in enumerate(selections):
  x=(idx%cols)*w;y=55+(idx//cols)*h
  d.rounded_rectangle((x+4,y+4,x+w-5,y+h-4),radius=6,fill=(36,43,50),outline=(71,77,79))
  for k,part in enumerate(('helmet','chest','leggings','boots')):
   art=sprite(style,pal,part,3 if style in ('corsair','nordic') else 2,wear,peg)
   a=art.resize((64,64),Image.Resampling.NEAREST)
   xx=x+10+(k%2)*71; yy=y+7+(k//2)*65
   im.paste(a,(xx,yy),a)
  d.text((x+12,y+141),f'{style} / {pal}',fill=(228,225,211),font=FONT)
  d.text((x+12,y+156),f'{wear} / {peg}',fill=(166,181,190),font=FONT)
 im.save(WORK/target,optimize=True)

gallery('PIRATE ATELIER | outfit families, colorways, wear, peg-leg',[
 ('sailor','crimson','pristine','normal'),('sailor','sea_blue','pristine','peg_right'),
 ('pirate','charcoal','tattered','peg_left'),('pirate','wine','weathered','normal'),
 ('corsair','emerald','pristine','peg_right'),('corsair','forest','tattered','normal'),
 ('merchant','plum','pristine','normal'),('merchant','ivory','tattered','peg_left'),
 ('nordic','navy','pristine','normal'),('nordic','copper','tattered','peg_right'),
 ('deckhand','ochre','weathered','normal'),('deckhand','teal','tattered','peg_left'),
 ('fisherman','sea_blue','pristine','normal'),('fisherman','forest','weathered','peg_right'),
 ('craftsman','copper','pristine','normal'),('craftsman','wine','tattered','peg_left'),
 ('sailor','ivory','weathered','normal'),('corsair','ochre','pristine','normal'),
 ('merchant','teal','tattered','peg_left'),('pirate','plum','pristine','normal'),
 ('sailor','forest','pristine','peg_left'),('nordic','sea_blue','weathered','normal'),
 ('deckhand','crimson','tattered','peg_right'),('fisherman','plum','pristine','normal'),
], 'previews/01_colecoes.png')
# Catalog weapons zoom
wpn=[('hook '+m,hook_icon(m)) for m in MAT_COLORS if m in ('iron','brass','copper','blacksteel','silver')]
wpn += [('saber '+m,cutlass_icon(m)) for m in MAT_COLORS]
wpn += [('eyepatch '+p,sprite('pirate',p,'eyepatch',3)) for p in ('charcoal','copper','emerald','plum')]
im=Image.new('RGB',(900,math.ceil(len(wpn)/7)*152+65),(24,28,35));d=P(im)
d.text((12,13),'PIRATE ATELIER | cutlasses / hand-hooks / eyepatches',font=FONT,fill=(236,228,214))
for i,(name,art) in enumerate(wpn):
 x=(i%7)*128+12;y=(i//7)*152+49
 scale=3;a=art.resize((96,96),Image.Resampling.NEAREST);im.paste(a,(x,y),a)
 d.text((x,y+105),name[:20],font=FONT,fill=(207,216,217))
im.save(WORK/'previews/02_armas_acessorios.png',optimize=True)

# texture validation
files=list(WORK.rglob('*'))
PNG=[p for p in files if p.suffix.lower()=='.png']
JSON=[p for p in files if p.suffix.lower()=='.json']+[WORK/'pack.mcmeta']
for f in PNG:
 with Image.open(f) as im: im.verify()
for f in JSON: json.loads(f.read_text(encoding='utf-8'))
assert len(source_to_new)==len(ITEM_SRC)
assert all(v=={1,2} for v in armor_groups.values())
for p in PNG:
 assert (WORK/p.relative_to(WORK)).exists()

DOC=f'''# Pirate Atelier – Clean-room asset set (Minecraft 26.2)

Original pixel art and voxel models, rebuilt independently using **only asset names,
category, texture dimensions, resource paths and conventions** from the supplied
Let's Forge Pirates archive as functional specifications. **No source image pixels,
JSON geometries, original PNGs, bytecode or proprietary models have been copied.**
The reference mod declares **All Rights Reserved**; this release contains only
original, newly authored artwork.

## What is included
- {len(ITEM_SRC)} new replacement sprites (all reference item PNG names, including eyepatch, hook and sabers).
- {len(ARMOR_SRC)} new classic 64x32 armor layer atlases, corresponding to {len(armor_groups)} original outfits/skulls.
- {len(hat_paths)} new legacy 3D headwear overlay textures (hat textures only).
- Replacement item model JSONs for original wearable items plus **new 3D block-cuboid models** for hook and three original cutlasses. Modern display-context selectors show pixel-art icons in GUI, true voxel models in hands.
- {len(records)} complete extended outfits: 8 themes x 12 colorways x 3 wear conditions x 3 leg configurations, each with 4 item icons, modern item JSONs, a 64x32 head/chest/arms atlas and a leggings atlas, plus 26.2-style equipment JSON.
- 5 additional sculpted 3D metal hooks, 7 additional 3D sabers, 8 eyepatches, 8 original modular 3D clothing/hat meshes, plus standalone geometry templates for pegs, hook hand, eyepatch and tricorn.
- Catalogue previews, variant metadata and the fully reproducible pixel-art source generator.

## Full outfit combinations
Styles: {', '.join(STYLES)}.
Palettes: {', '.join(NEW_PALETTES)}.
Wear: {', '.join(WEAR)}.
Leg variants: {', '.join(LEGS)}.
An example: `pirateatelier:sailor_plum_tattered_peg_right_leggings`.

## Directory conventions
`assets/lfpirates/` = drop-in resource *names* matching the old mod, where relevant.
`assets/pirateatelier/` = independent newly invented expansions. The code of the
running mod must register the latter as items and set its equipment asset ID.
`assets/*/models/item/` = legacy-style item geometry models.
`assets/*/items/` = 26.2 item model references.
`assets/*/textures/models/armor/` = legacy 64x32 armor sprite sheets.
`assets/*/textures/entity/equipment/humanoid/` and `humanoid_leggings/` = modern equipment textures.
`assets/*/equipment/<outfit>.json` = 26.2 equipment definitions.
`development_assets/geometries/` = Minecraft geometry templates intended for a future
player-feature attachment renderer, **not directly loaded by the resource pack**.

## HOW TO USE
### Reference mod (1.21.1 NeoForge)
This is a resource pack with `lfpirates` overrides, but `pack_format=88`
is targeted to Minecraft 26.2 (`min_format=max_format=88`). For 1.21.1,
remove those two fields and set `pack_format=34` first.
**Visual compatibility with the original NeoForge custom headwear renderer is not verified.**
Enable this pack ABOVE the mod's built-in resources. The original 3D hook/item
models are replaced with independently generated geometric alternatives.

### Minecraft 26.2 Fabric
The ZIP is a resource pack. It **does not port** the 1.21.1 NeoForge mod,
register new armor items or attach geometry to players by itself. The original
mod cannot be assumed compatible with 26.2 Fabric. In a new Fabric mod:
  1. Register `pirateatelier:` item IDs found in `manifests/variants.json`.
  2. For each piece set its `minecraft:equippable` component / equipment asset
     to the outfit `pirateatelier:<outfit>` and appropriate slot.
  3. Add a client-side player render layer for truly thin peg leg, arm mounted
     hook, 3D tricorn, and eyepatch; use the geometry templates as starting points.
  4. For held hook items the 3D JSON works with item rendering; this does NOT
     remove the player's real hand or permanently replace its geometry.
  5. Test in game and adjust display pose as needed (has not been runtime tested).

### Limits / deliberate exclusions
- No mobs, mob textures, cannons, muskets, decorative blocks, or special-world items.
- Peg variants have dedicated item sprites and geometry templates, but the
  legacy 64x32 leggings atlas uses standard pants: vanilla mirrors the same
  UV on both legs. A **one-sided peg leg** requires client renderer/model code
  to avoid accidentally giving both legs a wooden texture.
- Dynamic changes (torn variants, cosmetic swaps) and new inventory items require
  mod code or data-driven item registration; new assets alone cannot spawn items.
- All artwork is procedurally designed by shape/category and uses custom palette
  variations; hand-polishing selected styles in Blockbench is still worthwhile.
'''
save_text('LEIA-ME_PRIMEIRO.md',DOC)
save_text('LICENSE_NEW_ASSETS.txt','New procedural artwork and model definitions in this bundle authored independently for this task. No license to original Let\'s Forge Pirates assets, bytecode, or trademarks is conveyed.\n')
# create a portable copy of this script for reproducibility, without hardcoding local absolute files as functional use
script=pathlib.Path(__file__).read_text(encoding='utf-8')
save_text('development_assets/generate_assets.py',script)

# final zip, avoid adding files from user's original archive
with zipfile.ZipFile(OUT,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=7) as z:
 for f in sorted(WORK.rglob('*')):
  if f.is_file():z.write(f,f.relative_to(WORK).as_posix())
with zipfile.ZipFile(OUT) as z:
 broken=z.testzip();assert broken is None,broken
 names=z.namelist()
 print('DONE',OUT,'size bytes',OUT.stat().st_size,'files',len(names))
 print('counts:',dict(base_counts),'orig_group',len(armor_groups),'variants',len(records),'PNGs',len(PNG),'JSONs',len(JSON))
 print('archive ext counter',Counter(pathlib.PurePosixPath(n).suffix for n in names).most_common())
