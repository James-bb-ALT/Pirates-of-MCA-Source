#!/usr/bin/env python3
"""Current Loom release audit. Original Alpha 5 results/code are archived in qa/results/baseline/."""
import runpy
from pathlib import Path
runpy.run_path(str(Path(__file__).resolve().with_name('audit_release.py')),run_name='__main__')
