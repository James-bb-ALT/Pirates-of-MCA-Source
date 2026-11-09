"""Regressions for the real UV/atlas failures, not a graphical certification."""
from pathlib import Path
import json,struct,unittest,zlib
R=Path(__file__).resolve().parents[3]
def pixels(path):
    data=path.read_bytes();offset=8;compressed=b''
    while offset<len(data):
        size=struct.unpack('>I',data[offset:offset+4])[0];kind=data[offset+4:offset+8];payload=data[offset+8:offset+8+size];offset+=size+12
        if kind==b'IHDR':w,h,bits,color,_,_,interlace=struct.unpack('>IIBBBBB',payload)
        if kind==b'IDAT':compressed+=payload
    assert (bits,color,interlace)==(8,6,0)
    raw=zlib.decompress(compressed);rows=[];prev=bytearray(w*4)
    for y in range(h):
        at=y*(w*4+1);mode=raw[at];row=bytearray(raw[at+1:at+1+w*4])
        for x in range(w*4):
            a=row[x-4] if x>=4 else 0;b=prev[x];c=prev[x-4] if x>=4 else 0
            p=a+b-c;pa,pb,pc=abs(p-a),abs(p-b),abs(p-c)
            predictor=[0,a,b,(a+b)//2,a if pa<=pb and pa<=pc else b if pb<=pc else c][mode]
            row[x]=(row[x]+predictor)&255
        rows.append(row);prev=row
    return w,h,rows
class TricorneExportTests(unittest.TestCase):
    def test_uv_samples_opaque_palette_not_transparent_half(self):
        model=json.loads((R/'exports/fabric_renderer/tricorne_classic.json').read_text())
        w,h,rows=pixels(R/'textures/wearables/head/tricorne_classic.png')
        for element in model['elements']:
            for face in element['faces'].values():
                uv=face['uv'];x=int((uv[0]+uv[2])/2*w/16);y=int((uv[1]+uv[3])/2*h/16)
                self.assertEqual(rows[y][x*4+3],255,element['name'])
                if 'gold' in element['name']:self.assertGreater(rows[y][x*4],rows[y][x*4+2]+50)
    def test_runtime_texture_is_in_real_item_atlas_directory(self):
        model=json.loads((R/'exports/fabric_renderer/tricorne_classic.json').read_text())
        texture=model['textures']['hat'];self.assertEqual(texture,'piratesmca:item/tricorne_classic')
        runtime=R/'source/src/main/resources/assets/piratesmca/textures/item/tricorne_classic.png'
        self.assertEqual(runtime.read_bytes(),(R/'textures/wearables/head/tricorne_classic.png').read_bytes())
        self.assertEqual((R/'exports/fabric_renderer/tricorne_classic.json').read_bytes(),(R/'source/src/main/resources/assets/piratesmca/models/item/tricorne_classic.json').read_bytes())
    def test_editable_model_has_complete_group_hierarchy(self):
        model=json.loads((R/'models/blockbench/head/tricorne_classic.bbmodel').read_text())
        self.assertEqual(model['meta']['format_version'],'5.0')
        ids=[e['uuid'] for e in model['elements']];self.assertEqual(len(ids),len(set(ids)))
        self.assertEqual(set(ids),set(model['outliner'][0]['children']))
        self.assertEqual(model['groups'][0]['uuid'],model['outliner'][0]['uuid'])
        for element in model['elements']:
            self.assertTrue(all(a<b for a,b in zip(element['from'],element['to'])))
            self.assertEqual(set(element['faces']),{'north','east','south','west','up','down'})
if __name__=='__main__':unittest.main()
