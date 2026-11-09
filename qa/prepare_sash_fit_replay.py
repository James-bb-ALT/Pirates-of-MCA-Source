"""Copy a QA save into the runner's actual saves directory; never overwrite a world."""
from pathlib import Path
import argparse,shutil
R=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--save',type=Path,required=True);a=p.parse_args()
source=a.save.resolve();dest=(R/'run/sash-fit-test/saves/Fit Original').resolve()
assert source.is_relative_to((R/'run').resolve()),'Only project QA worlds are accepted'
assert dest.is_relative_to((R/'run').resolve())
assert (source/'level.dat').is_file(),'Source is not a saved QA world'
if dest.exists():raise SystemExit('Destination already exists; kept intact. Use the existing replay fixture.')
shutil.copytree(source,dest)
print('PASS copied QA world to isolated replay directory; source kept intact')
