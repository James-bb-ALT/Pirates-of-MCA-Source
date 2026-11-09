from pathlib import Path
import json,unittest
from test_tricorne_export import pixels
R=Path(__file__).resolve().parents[3]
class OutfitTests(unittest.TestCase):
 def test_head_hands_remain_transparent_and_breast_front_covered(self):
  w,h,rows=pixels(R/'textures/outfits/deckhand.png');self.assertEqual((w,h),(64,64))
  for y in range(16):
   for x in range(64):self.assertEqual(rows[y][x*4+3],0)
  for x0,y0 in [(48,16),(40,48)]:
   for y in range(y0,y0+4):
    for x in range(x0,x0+4):self.assertEqual(rows[y][x*4+3],0)
  for y in range(24,27):
   for x in range(21,27):self.assertEqual(rows[y][x*4+3],255)
 def test_native_overlay_is_neutral_explicit_only_and_matches_source(self):
  a=R/'source/src/main/resources';entries=json.loads((a/'data/piratesmca/skins/clothing/outfits.json').read_text())
  self.assertEqual(entries,[{'id':'piratesmca:outfits/deckhand.png','profession':'piratesmca:pirate','gender':3,'exclude':True,'chance':1.0}])
  self.assertEqual((R/'textures/outfits/deckhand.png').read_bytes(),(a/'assets/piratesmca/outfits/deckhand.png').read_bytes())
  m=json.loads((R/'models/blockbench/outfits/deckhand.bbmodel').read_text());self.assertEqual(len(m['elements']),6)
