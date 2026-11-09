#!/usr/bin/env python3
"""Structural audit of the real Loom artifact; does not claim runtime correctness."""
import runpy
from pathlib import Path
runpy.run_path(str(Path(__file__).resolve().parents[1]/'qa/audit_release.py'),run_name='__main__')
