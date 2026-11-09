#!/usr/bin/env python3
"""Non-destructive asset inventory validator for Pirates of MCA.

Validates own JSON manifest plus basic file existence/format. It is NOT an
in-game renderer validator and cannot certify EMF/Fresh Animations support.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import re
import struct
import sys

ID_RE = re.compile(r"^piratesmca:[a-z0-9_./-]+$")
STATUSES = {"planned", "modeling", "exported", "ready", "blocked"}
TARGETS = {"undecided", "mca_overlay", "mca_skin", "fabric_renderer", "emf_cem", "item_model"}
CATEGORIES = {"civilian_outfit", "crew_outfit", "armor_set", "prosthetic", "accessory", "cursed_variant", "base_skin"}


def png_size(path: Path) -> tuple[int, int]:
    with path.open("rb") as f:
        header = f.read(24)
    if len(header) < 24 or header[:8] != b"\x89PNG\r\n\x1a\n" or header[12:16] != b"IHDR":
        raise ValueError("PNG invalido ou header incompleto")
    return struct.unpack(">II", header[16:24])


def inside(root: Path, rel: str) -> Path:
    path = (root / rel).resolve()
    if path != root and root not in path.parents:
        raise ValueError(f"Caminho fora do projeto: {rel}")
    return path


def check_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def main() -> int:
    p = argparse.ArgumentParser(description="Valida o inventario e os arquivos de arte Pirates of MCA")
    p.add_argument("--root", default=".", help="Raiz do projeto configurado")
    p.add_argument("--strict", action="store_true", help="Planejados e bloqueados fazem a validacao retornar falha")
    args = p.parse_args()
    root = Path(args.root).resolve()
    catalog = root / "definitions" / "asset_catalog.json"
    if not catalog.exists():
        print(f"FAIL: catalogo nao encontrado: {catalog}")
        return 2
    try:
        doc = check_json(catalog)
    except (OSError, ValueError) as exc:
        print(f"FAIL: catalogo JSON invalido: {exc}")
        return 2
    assets = doc.get("assets")
    if not isinstance(assets, list):
        print("FAIL: 'assets' precisa ser uma lista")
        return 2

    fails = []
    warnings = []
    counts = {state: 0 for state in STATUSES}
    seen = set()
    for i, a in enumerate(assets, 1):
        key = f"entry #{i}"
        if not isinstance(a, dict):
            fails.append(f"{key}: objeto esperado")
            continue
        asset_id = a.get("id")
        if not isinstance(asset_id, str) or not ID_RE.fullmatch(asset_id):
            fails.append(f"{key}: id invalido: {asset_id!r}")
            continue
        key = asset_id
        if key in seen:
            fails.append(f"{key}: id duplicado")
            continue
        seen.add(key)
        if a.get("category") not in CATEGORIES:
            fails.append(f"{key}: category invalida: {a.get('category')!r}")
        if a.get("export_target", "undecided") not in TARGETS:
            fails.append(f"{key}: export_target invalido")
        state = a.get("status")
        if state not in STATUSES:
            fails.append(f"{key}: status invalido: {state!r}")
            continue
        counts[state] += 1
        if not a.get("slot"):
            warnings.append(f"{key}: sem slot definido")
        if state in ("planned", "blocked"):
            continue
        model = a.get("model_source")
        texture = a.get("texture")
        target = a.get("export_target", "undecided")
        if state == "ready" and target == "undecided":
            fails.append(f"{key}: ready mas export_target indefinido")
        requires_model = a.get("category") in {"armor_set", "prosthetic", "accessory", "cursed_variant"}
        if requires_model and state in ("exported", "ready") and not model:
            fails.append(f"{key}: model_source obrigatorio para {state}")
        if state in ("exported", "ready") and not texture:
            fails.append(f"{key}: texture obrigatoria para {state}")
        for field in ("model_source", "texture", "export_path"):
            rel = a.get(field)
            if not rel:
                continue
            if not isinstance(rel, str):
                fails.append(f"{key}: {field} nao e string")
                continue
            try:
                path = inside(root, rel)
            except ValueError as exc:
                fails.append(f"{key}: {exc}")
                continue
            if not path.is_file():
                if state in ("exported", "ready") or field != "export_path":
                    fails.append(f"{key}: {field} ausente: {rel}")
                else:
                    warnings.append(f"{key}: export ainda nao existe: {rel}")
                continue
            if path.suffix.lower() in (".bbmodel", ".jem", ".jpm", ".json"):
                try:
                    j = check_json(path)
                    if path.suffix.lower() == ".bbmodel" and not isinstance(j, dict):
                        fails.append(f"{key}: .bbmodel deve ser objeto JSON")
                    if path.suffix.lower() == ".bbmodel" and "meta" not in j:
                        warnings.append(f"{key}: .bbmodel sem meta; revisar no Blockbench")
                except (ValueError, OSError) as exc:
                    fails.append(f"{key}: JSON de {field} invalido: {exc}")
            elif path.suffix.lower() == ".png":
                try:
                    w, h = png_size(path)
                    if w > 2048 or h > 2048:
                        warnings.append(f"{key}: textura grande ({w}x{h}); revisar custo")
                except (OSError, ValueError) as exc:
                    fails.append(f"{key}: textura invalida: {exc}")
        if state == "ready" and not a.get("runtime_test_evidence"):
            fails.append(f"{key}: 'ready' exige runtime_test_evidence (arquivo de prova in-game)")
        proof = a.get("runtime_test_evidence")
        if proof:
            try:
                pr = inside(root, proof)
                if not pr.is_file():
                    fails.append(f"{key}: evidencia in-game ausente: {proof}")
            except ValueError as exc:
                fails.append(f"{key}: {exc}")

    for w in warnings:
        print("WARN:", w)
    for f in fails:
        print("FAIL:", f)
    print("\nResumo de catalogo:")
    print(f"  total={len(assets)} " + " ".join(f"{s}={counts[s]}" for s in sorted(STATUSES)))
    print(f"  warnings={len(warnings)} errors={len(fails)}")
    print("  OBS: esta verificacao nao atesta renderizacao no Minecraft, EMF ou Fresh Animations.")
    if fails or (args.strict and (counts["planned"] or counts["blocked"])):
        print("RESULTADO: FAIL")
        return 1
    print("RESULTADO: PASS (integridade do inventario; nao e teste in-game)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
