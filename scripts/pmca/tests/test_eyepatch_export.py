from pathlib import Path
import json,unittest
from test_tricorne_export import pixels
R=Path(__file__).resolve().parents[3]
class EyepatchExportTests(unittest.TestCase):
    def test_authored_pirate_face_uses_higher_fit_without_moving_native_hat(self):
        runtime=R/'source/src/main/resources/assets/piratesmca/items'
        patch=json.loads((runtime/'eyepatch_leather_pirate_face.json').read_text())['model']
        self.assertEqual(patch,{'type':'minecraft:model','model':'piratesmca:item/eyepatch_leather_player'})
        combo=json.loads((runtime/'tricorne_eyepatch_pirate_face.json').read_text())['model']['models']
        self.assertEqual(combo[0]['model'],'piratesmca:item/tricorne_classic')
        self.assertEqual(combo[1],patch)
    def test_face_is_outside_real_head_and_uv_is_opaque(self):
        model=json.loads((R/'exports/fabric_renderer/eyepatch_leather.json').read_text())
        _,_,rows=pixels(R/'textures/wearables/face/eyepatch_leather.png')
        for e in model['elements']:
            if e['name'].startswith('patch_'):self.assertLessEqual(e['to'][2],4,'Eye geometry intersects base head front')
            for f in e['faces'].values():
                uv=f['uv'];x=int((uv[0]+uv[2])*2);y=int((uv[1]+uv[3])*2)
                self.assertEqual(rows[y][x*4+3],255,e['name'])
        self.assertEqual((R/'textures/wearables/face/eyepatch_leather.png').read_bytes(),(R/'source/src/main/resources/assets/piratesmca/textures/item/eyepatch_leather.png').read_bytes())
    def test_composition_references_two_sources_without_duplicate_head_geometry(self):
        model=json.loads((R/'exports/fabric_renderer/tricorne_eyepatch.json').read_text())['model']
        self.assertEqual(model['type'],'minecraft:composite')
        self.assertEqual(model['models'][0]['model'],'piratesmca:item/tricorne_classic')
        fit=model['models'][1];self.assertEqual(fit['property'],'minecraft:context_entity_type')
        self.assertEqual(fit['cases'][0]['when'],['mca:male_villager','mca:female_villager'])
        self.assertEqual(fit['cases'][0]['model']['model'],'piratesmca:item/eyepatch_leather')
        self.assertEqual(fit['fallback']['model'],'piratesmca:item/eyepatch_leather_player')
        mca=json.loads((R/'exports/fabric_renderer/eyepatch_leather.json').read_text())
        player=json.loads((R/'exports/fabric_renderer/eyepatch_leather_player.json').read_text())
        for a,b in zip(mca['elements'],player['elements']):
            self.assertAlmostEqual(b['from'][1]-a['from'][1],2)
            self.assertAlmostEqual(b['to'][1]-a['to'][1],2)
            if a['name']=='patch_face':self.assertAlmostEqual(sum([a['from'][1],a['to'][1]])/2,6.625)
        source=json.loads((R/'models/blockbench/face/eyepatch_leather.bbmodel').read_text())
        self.assertEqual(set(source['outliner'][0]['children']),{e['uuid'] for e in source['elements']})
        runtime=R/'source/src/main/resources/assets/piratesmca'
        self.assertEqual((R/'exports/fabric_renderer/eyepatch_leather.json').read_bytes(),(runtime/'models/item/eyepatch_leather.json').read_bytes())
if __name__=='__main__':unittest.main()
