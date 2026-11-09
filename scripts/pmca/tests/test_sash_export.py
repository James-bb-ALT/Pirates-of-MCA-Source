from pathlib import Path
import json,unittest
from test_tricorne_export import pixels
R=Path(__file__).resolve().parents[3]
class SashExportTests(unittest.TestCase):
    def test_waist_stays_above_leg_pivot_and_outside_torso(self):
        model=json.loads((R/'exports/fabric_renderer/sash_red.json').read_text())
        for e in model['elements']:
            self.assertGreater(8-e['to'][1],8,'Sash is above the intended neutral waist interval')
            self.assertLess(8-e['from'][1],12,'Sash crosses the leg pivot')
        front=next(e for e in model['elements'] if e['name']=='front_band');self.assertGreater(front['from'][2],10)
        self.assertEqual(model['display']['fixed']['scale'],[1,1,1])
    def test_runtime_atlas_uv_and_editable_hierarchy(self):
        model=json.loads((R/'exports/fabric_renderer/sash_red.json').read_text());_,_,rows=pixels(R/'textures/wearables/waist/sash_red.png')
        for e in model['elements']:
            for f in e['faces'].values():
                u=f['uv'];x=int((u[0]+u[2])*2);y=int((u[1]+u[3])*2);rgba=rows[y][x*4:x*4+4]
                self.assertEqual(rgba[3],255);self.assertGreater(rgba[0],rgba[1]*2)
        runtime=R/'source/src/main/resources/assets/piratesmca'
        self.assertEqual((R/'textures/wearables/waist/sash_red.png').read_bytes(),(runtime/'textures/item/sash_red.png').read_bytes())
        self.assertEqual((R/'exports/fabric_renderer/sash_red.json').read_bytes(),(runtime/'models/item/sash_red.json').read_bytes())
        source=json.loads((R/'models/blockbench/torso/sash_red.bbmodel').read_text());self.assertEqual(set(source['outliner'][0]['children']),{e['uuid'] for e in source['elements']})
if __name__=='__main__':unittest.main()
