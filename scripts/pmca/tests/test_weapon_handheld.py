"""Regressions for the inventory/held-model split that bypassed vanilla weapon sprites."""
from pathlib import Path
import json,unittest
R=Path(__file__).resolve().parents[3]
RES=R/'source/src/main/resources'

class WeaponHandheldTests(unittest.TestCase):
    def weapons(self):
        return [e for e in json.loads((RES/'data/piratesmca/atelier/items.json').read_text()) if e['kind']=='weapon']

    def test_every_context_uses_vanilla_handheld_sprite(self):
        self.assertEqual(len(self.weapons()),10)
        for e in self.weapons():
            with self.subTest(item=e['id']):
                ns,name=e['id'].split(':');root=RES/'assets'/ns
                definition=json.loads((root/'items'/f'{name}.json').read_text())
                self.assertEqual(definition,{'model':{'type':'minecraft:model','model':f'{ns}:item/{name}'}})
                model=json.loads((root/'models/item'/f'{name}.json').read_text())
                self.assertEqual(model['parent'],'minecraft:item/handheld')
                self.assertNotIn('elements',model)
                self.assertNotIn('display',model)
                original=json.loads((root/'models/item'/f'{name}_inventory.json').read_text())
                self.assertEqual(model['textures']['layer0'],original['textures']['layer0'])
                tn,tp=model['textures']['layer0'].split(':')
                self.assertTrue((RES/f'assets/{tn}/textures/{tp}.png').is_file())

    def test_combat_classification_preserved(self):
        swords=json.loads((RES/'data/minecraft/tags/item/swords.json').read_text())['values']
        for e in self.weapons():
            with self.subTest(item=e['id']):
                self.assertIn(e['id'],swords)
                ns,name=e['id'].split(':')
                profile=json.loads((RES/f'data/{ns}/weapon_attributes/{name}.json').read_text())
                self.assertEqual(profile['parent'],'bettercombat:rapier' if 'rapier' in name else 'bettercombat:cutlass')

if __name__=='__main__':unittest.main()
