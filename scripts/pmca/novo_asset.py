#!/usr/bin/env python3
"""Add one planned asset to the catalog without generating fictitious models."""
from __future__ import annotations
import argparse
import json
from pathlib import Path
import re


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--id", required=True, help="Ex.: piratesmca:hat_tricorne_black")
    ap.add_argument("--category", required=True, choices=["civilian_outfit", "crew_outfit", "armor_set", "prosthetic", "accessory", "cursed_variant", "base_skin"])
    ap.add_argument("--slot", required=True, help="Ex.: head, hand_right")
    ap.add_argument("--concept", default="", help="Descricao concisa")
    args = ap.parse_args()
    if not re.fullmatch(r"piratesmca:[a-z0-9_./-]+", args.id):
        ap.error("ID precisa ser piratesmca:snake_case")
    root = Path(args.root).resolve()
    path = root / "definitions" / "asset_catalog.json"
    doc = json.loads(path.read_text(encoding="utf-8"))
    if any(a.get("id") == args.id for a in doc["assets"]):
        ap.error("asset ja existe: " + args.id)
    doc["assets"].append({
        "id": args.id,
        "category": args.category,
        "slot": args.slot,
        "concept": args.concept,
        "status": "planned",
        "export_target": "undecided",
        "model_source": None,
        "texture": None,
        "runtime_test_evidence": None,
    })
    doc["assets"] = sorted(doc["assets"], key=lambda a: a["id"])
    path.write_text(json.dumps(doc, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print("Incluido:", args.id, "(status=planned; nenhum modelo gerado)")

if __name__ == "__main__":
    main()
