"""Prove the final delta is restricted to held Atelier items and prototype hats.
The completed ON body/wardrobe matrix contained empty NPC hands and no prototype hats.
This does not claim that its screenshots came from the final JAR. New paths need new runs.
"""
from pathlib import Path
import json,hashlib,zipfile
ROOT=Path(__file__).resolve().parents[1]
old=ROOT/'reports/visual-fix/pre-hand-candidate.jar'
new=ROOT/'build/libs/piratesmca-13.0.0-alpha.19.jar'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
allowed_changed={'dev/arandu/piratesmca/mixin/client/TricorneHeadLayerMixin.class','dev/arandu/piratesmca/mixin/client/TricorneExtractMixin.class','piratesmca.mixins.json'}
face_models={'assets/piratesmca/items/eyepatch_leather_pirate_face.json','assets/piratesmca/items/tricorne_eyepatch_pirate_face.json'}
allowed_added={'dev/arandu/piratesmca/mixin/client/PirateHeldAttachmentMixin.class'}|face_models
with zipfile.ZipFile(old) as a,zipfile.ZipFile(new) as b:
 before={n:a.read(n) for n in a.namelist() if not n.endswith('/')}
 after={n:b.read(n) for n in b.namelist() if not n.endswith('/')}
 changed={n for n in before.keys()&after.keys() if before[n]!=after[n]}
 added=after.keys()-before.keys();removed=before.keys()-after.keys()
 assert changed<=allowed_changed,(changed-allowed_changed)
 assert added<=allowed_added,(added-allowed_added)
 assert not removed,removed
 assert 'dev/arandu/piratesmca/mixin/client/PirateHeldAttachmentMixin.class' in added
 unchanged={n:hashlib.sha256(before[n]).hexdigest() for n in before.keys()&after.keys() if before[n]==after[n]}
 log=(ROOT/'reports/visual-fix-full-final-on.log').read_text(errors='replace')
 assert 'BUILD SUCCESSFUL' in log and 'RECON_AVATAR_PASS' in log and sha(old) in log
 proof={'baseline_jar_sha256':sha(old),'final_jar_sha256':sha(new),'result':'PASS_BYTE_PRESERVATION_FOR_REUSE',
        'scope':'ON body, skins, wardrobe and peg matrix only; NPC hands were empty and armor helmets were not the prototype cosmetic hats. Vanilla player model is outside the new MCA-only held-item branch.',
        'not_a_final_jar_full_on_execution':True,'changed':sorted(changed),'added':sorted(added),'removed':sorted(removed),
        'unchanged_entry_count':len(unchanged),'unchanged_sha256':dict(sorted(unchanged.items())),
        'required_new_execution':['accessories','prototype hats and sash ON/OFF','persistence and regressions']}
 previous=ROOT/'reports/visual-fix/pre-eyepatch-candidate.jar'
 with zipfile.ZipFile(previous) as b:
  previous_entries={n:b.read(n) for n in b.namelist() if not n.endswith('/')}
 previous_changed={n for n in previous_entries.keys()&after.keys() if previous_entries[n]!=after[n]}
 assert previous_changed<={'dev/arandu/piratesmca/mixin/client/TricorneExtractMixin.class'},previous_changed
 assert after.keys()-previous_entries.keys()<=face_models
 assert not previous_entries.keys()-after.keys()
 proof['pre_eyepatch_jar_sha256']=sha(previous)
 proof['pre_eyepatch_changed']=sorted(previous_changed)
 proof['pre_eyepatch_unchanged_entry_count']=sum(previous_entries[n]==after[n] for n in previous_entries.keys()&after.keys())
 proof['reusable_gates']={'full-final-on':sha(old),'full-final-off':sha(previous),'weapons-on':sha(previous),'weapons-off':sha(previous)}
 proof['eyepatch_reuse_scope']='The only delta from the pre-eyepatch candidate changes cosmetic eyepatches on authored pirate faces. Full OFF wardrobe, empty NPC head slots in weapon tests, and player armor helmets do not enter that branch. Prototype captures from that candidate are NOT reusable as corrected evidence.'
 (ROOT/'reports/visual-fix/on-matrix-reuse.json').write_text(json.dumps(proof,indent=2)+'\n')
 print('PASS scoped byte preservation:',len(unchanged),'unchanged entries; completed ON screenshots retain their original candidate SHA256')
