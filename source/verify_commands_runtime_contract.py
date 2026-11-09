#!/usr/bin/env python3
"""Bytecode regression guard, supplemental to real boot and command GameTests."""
from pathlib import Path
import json,subprocess,sys
root=Path(__file__).resolve().parents[1]
meta=json.loads((root/'source/src/main/resources/fabric.mod.json').read_text())
jar=Path(sys.argv[1]) if len(sys.argv)>1 else root/'build/libs'/('piratesmca-'+meta['version']+'.jar')
text=subprocess.check_output(['javap','-c','-p','-classpath',str(jar),'dev.arandu.piratesmca.PirateCommands'],text=True)
assert 'CommandRegistrationCallback.EVENT:Lnet/fabricmc/fabric/api/event/Event;' in text
assert 'CommandRegistrationCallback$Event;' not in text
assert 'Event.register:(Ljava/lang/Object;)V' in text
runtime=subprocess.check_output(['javap','-c','-p','-classpath',str(jar),'dev.arandu.piratesmca.CrewRuntime'],text=True)
assert 'ResourceKey.location:' not in runtime
print('PASS command descriptors (static only); real commands have separate GameTest logs')
