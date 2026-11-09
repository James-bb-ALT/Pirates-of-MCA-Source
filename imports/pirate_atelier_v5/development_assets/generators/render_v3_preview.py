import io,json,math,zipfile
from PIL import Image,ImageDraw,ImageFont
from matplotlib import pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
from pathlib import Path
BASE=Path('/mnt/data')
Z=zipfile.ZipFile(BASE/'Pirate_Atelier_CleanRoom_Player_3D_EMF_26.2_v3.zip')
SETS=['captain_armor','first_mate_armor','quartermaster_armor','boarding_raider_armor','naval_officer_armor','cursed_ghost_armor','harbor_merchant','ships_cook']
offsets={'head':(0,0,0),'body':(0,0,0),'right_arm':(-5,2,0),'left_arm':(5,2,0),'right_leg':(-1.9,12,0),'left_leg':(1.9,12,0)}

# Collate using bounding boxes and actual atlas pixels from .jem; this is only a geometric preview, not an EMF runtime screenshot.
def drawprofile(profile):
 name=profile+'_pristine_normal'
 atlas=Image.open(io.BytesIO(Z.read(f'assets/pirateatelier/textures/entity/cem/{name}.png'))).convert('RGB')
 fig=plt.figure(figsize=(3.6,5.2),facecolor='#f5efe4');ax=fig.add_subplot(projection='3d');ax.set_facecolor('#f5efe4')
 polygons=[];colors=[]
 def cube(coords,col,offset):
  x,y,z,w,h,d=coords;ox,oy,oz=offset;x+=ox;y+=oy;z+=oz
  a=[(x,z,-y),(x+w,z,-y),(x+w,z,-(y+h)),(x,z,-(y+h))]
  b=[(x,z+d,-y),(x+w,z+d,-y),(x+w,z+d,-(y+h)),(x,z+d,-(y+h))]
  for face,sh in (([a[0],a[1],a[2],a[3]],.95),([b[0],b[1],b[2],b[3]],.75),([a[0],b[0],b[3],a[3]],.8),([a[1],b[1],b[2],a[2]],.85),([a[0],a[1],b[1],b[0]],1.09),([a[3],a[2],b[2],b[3]],.62)):
   polygons.append(face);colors.append(tuple(min(1,n*sh) for n in col)+(1,))
 for bone,coords in {'head':(-4,-8,-4,8,8,8),'body':(-4,0,-2,8,12,4),'right_arm':(-2,-2,-2,4,12,4),'left_arm':(-2,-2,-2,4,12,4),'right_leg':(-2,0,-2,4,12,4),'left_leg':(-2,0,-2,4,12,4)}.items():cube(coords,(.71,.61,.52),offsets[bone])
 prefix='development_assets/cem_wardrobe/'+name+'/'
 for slot in ('helmet','chestplate','leggings','boots'):
  model=json.loads(Z.read(prefix+slot+'.jem'))
  for part in model['models']:
   for b in part['boxes']:
    u,v,U,V=b['uvNorth'];col=atlas.getpixel(((u+U)//2,(v+V)//2));cube(b['coordinates'],tuple(k/255 for k in col),offsets[part['part']])
 ax.add_collection3d(Poly3DCollection(polygons,facecolors=colors,linewidths=0,edgecolors='none',zsort='average'))
 ax.set_box_aspect((1,1,2.3));ax.set_xlim(-10.5,10.5);ax.set_ylim(-10.5,10.5);ax.set_zlim(-28,18)
 ax.view_init(elev=12,azim=-64);ax.set_axis_off();ax.dist=8
 ax.set_title(profile.replace('_',' ').title(),fontsize=10,pad=8)
 out=BASE/f'_v3preview_{profile}.png'
 fig.savefig(out,dpi=110,bbox_inches='tight',pad_inches=.08,facecolor='#f5efe4');plt.close(fig)
 return out
imgs=[]
for profile in SETS:
 f=drawprofile(profile);print(profile,flush=True);imgs.append(Image.open(f).convert('RGB'))
w=320;h=430
canvas=Image.new('RGB',(4*w,2*h),(244,236,223))
for i,im in enumerate(imgs):
 im.thumbnail((w-12,h-12))
 canvas.paste(im,((i%4)*w+(w-im.width)//2,(i//4)*h+(h-im.height)//2))
canvas.save(BASE/'Pirate_Atelier_V3_preview_player_3D.png',optimize=True)
print('saved 3d preview',flush=True)
