"""Material/migration regressions. These tests do not award visual approval."""
import unittest,json,hashlib
from pathlib import Path
from PIL import Image,ImageChops

ROOT=Path(__file__).resolve().parents[3]
RES=ROOT/'source/src/main/resources'

class PirateSkinLayersTests(unittest.TestCase):
 def test_original_atlases_and_ids_survive(self):
  original=ROOT/'imports/pirate_atelier_v5/assets/pirateatelier/textures/skins'
  self.assertEqual(len(list(original.glob('*.png'))),20,'Original atlas evidence missing')
  catalog=json.loads((RES/'data/piratesmca/atelier/skins.json').read_text())
  registered={s['name'] for s in catalog}
  for png in original.glob('*.png'):
   with self.subTest(skin=png.stem):
    self.assertIn(png.stem,registered)
    self.assertEqual(png.read_bytes(),(RES/'assets/pirateatelier/textures/skins'/png.name).read_bytes())
 def test_layers_remove_baked_face_and_supply_blink(self):
  profiles=json.loads((RES/'data/piratesmca/atelier/skin_layers.json').read_text())
  self.assertEqual(len(profiles),38)
  for p in profiles:
   def image(key):
    ns,path=p[key].split(':');return Image.open(RES/'assets'/ns/path).convert('RGBA')
   with self.subTest(skin=p['skin']):
    body,face,blink=image('body'),image('face'),image('blink')
    self.assertEqual(body.size,(64,64))
    self.assertEqual(len(set(body.crop((0,0,32,16)).getdata())),1,'Baked eyes/hair remain in body')
    self.assertEqual(body.crop((32,0,64,16)).getbbox(),None,'Old cap duplicates headwear')
    self.assertNotEqual(face.tobytes(),blink.tobytes(),'No distinct closed-eye texture')
    self.assertEqual(face.crop((0,16,64,64)).getbbox(),None,'Face layer paints body')
    self.assertEqual(p['hair'],'persisted_mca_layer')
 def test_female_outfits_are_distinct_and_pirate_only(self):
  catalog=json.loads((RES/'data/piratesmca/atelier/skins.json').read_text())
  self.assertEqual(sum(s['gender']=='female' for s in catalog),19)
  self.assertEqual(sum(s['gender']=='male' for s in catalog),19)
  for s in catalog:
   if s['gender']!='female' or s['name']=='deckhand_f':continue
   prefix=RES/'assets/pirateatelier/textures/skins'
   female=Image.open(prefix/(s['name']+'.png')).convert('RGBA')
   male=Image.open(prefix/(s['name'][:-2]+'.png')).convert('RGBA')
   self.assertNotEqual(female.crop((16,16,40,32)).tobytes(),male.crop((16,16,40,32)).tobytes())
  pool=json.loads((RES/'data/piratesmca/skins/body/atelier.json').read_text())
  self.assertEqual({s['id'] for s in catalog},{s['id'] for s in pool})
  self.assertTrue(all(s['chance']==0 for s in pool))

if __name__=='__main__':unittest.main()
