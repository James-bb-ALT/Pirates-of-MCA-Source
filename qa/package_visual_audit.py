from __future__ import annotations

import csv
import hashlib
import re
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
FULL = ROOT / "run/visual-asset-audit/screenshots"
PEG = ROOT / "run/visual-peg-audit/screenshots"
OUT = ROOT / "reports/visual-audit-12.0.0-alpha.18/final-corrected"
RAW = OUT / "raw"
CROPS = OUT / "gallery"
SHEETS = OUT / "contact-sheets"
for directory in (RAW, CROPS, SHEETS, OUT / "logs"):
    directory.mkdir(parents=True, exist_ok=True)

def identity(path: Path) -> tuple[int, str]:
    match = re.match(r"(\d+)_AUDIT-(.+)\.png$", path.name)
    if not match:
        raise ValueError(path)
    return int(match.group(1)), match.group(2)

# Repeated runs leave old screenshot files in the game's screenshots folder.
# Keep the newest frame for each exact label and discard superseded duplicates.
latest: dict[str, tuple[int, Path]] = {}
for path in FULL.glob("*_AUDIT-*.png"):
    index, label = identity(path)
    if label.startswith("PEG-"):
        continue
    if label.startswith("MCA-SKIN-") and not re.match(r"MCA-SKIN-(?:male|female)-", label):
        continue  # Superseded captures from before the gender-label correction.
    if label not in latest or index > latest[label][0]:
        latest[label] = (index, path)
selected = [path for _, path in sorted(latest.values(), key=lambda item: item[0])]
peg_selected = sorted(PEG.glob("*_AUDIT-PEG-*.png"), key=lambda p: identity(p)[0])
if len(selected) != 350 or len(peg_selected) != 14:
    raise SystemExit(f"Unexpected evidence coverage: core={len(selected)} peg={len(peg_selected)}")
for category, expected in (("MCA-SKIN-", 140), ("SKIN-", 140), ("ARMOR-", 70)):
    found = sum(identity(p)[1].startswith(category) for p in selected)
    if found != expected:
        raise SystemExit(f"Expected {expected} {category} frames, found {found}")

entries: list[tuple[str, Path, Path, str]] = []
for path in selected + peg_selected:
    _, label = identity(path)
    category = label.split("-", 1)[0].lower()
    raw = RAW / category / f"{label}.png"
    raw.parent.mkdir(parents=True, exist_ok=True)
    raw.write_bytes(path.read_bytes())
    with Image.open(path) as im:
        image = im.convert("RGB")
        crop = image.crop((247, 115, 607, 452))
        gallery = CROPS / category / f"{label}.jpg"
        gallery.parent.mkdir(parents=True, exist_ok=True)
        crop.save(gallery, quality=94, optimize=True)
    digest = hashlib.sha256(raw.read_bytes()).hexdigest()
    entries.append((label, raw, gallery, digest))

def sheet(name: str, predicate, columns: int = 5) -> None:
    chosen = [(label, gallery) for label, _, gallery, _ in entries if predicate(label)]
    tile_w, tile_h = 150, 175
    rows = (len(chosen) + columns - 1) // columns
    board = Image.new("RGB", (columns * tile_w, rows * tile_h), (232, 232, 232))
    draw = ImageDraw.Draw(board)
    for i, (label, path) in enumerate(chosen):
        im = Image.open(path).convert("RGB")
        im.thumbnail((tile_w, 150))
        x, y = (i % columns) * tile_w, (i // columns) * tile_h
        board.paste(im, (x + (tile_w - im.width) // 2, y))
        draw.text((x + 3, y + 152), label[:23], fill=(0, 0, 0))
    board.save(SHEETS / f"{name}.jpg", quality=90, optimize=True)

sheet("mca-skin-front", lambda s: s.startswith("MCA-SKIN-") and s.endswith("FRONT"), 5)
sheet("pirate-skin-front", lambda s: s.startswith("SKIN-") and s.endswith("FRONT"), 5)
sheet("armor-front", lambda s: s.startswith("ARMOR-") and s.endswith("FRONT"), 5)
sheet("walk-front", lambda s: s.endswith("WALK"), 5)
sheet("run-front", lambda s: s.endswith("RUN"), 5)
sheet("attack-front", lambda s: s.endswith("ATTACK"), 5)
sheet("peg-all-views-and-poses", lambda s: s.startswith("PEG-"), 4)

with (OUT / "manifest.csv").open("w", encoding="utf-8-sig", newline="") as f:
    writer = csv.writer(f)
    writer.writerow(("asset_label", "raw_png", "cropped_jpg", "sha256"))
    for label, raw, gallery, digest in entries:
        writer.writerow((label, raw.relative_to(OUT).as_posix(), gallery.relative_to(OUT).as_posix(), digest))

log_lines = []
for log in (ROOT / "run/visual-asset-audit/logs/latest.log", ROOT / "run/visual-peg-audit/logs/latest.log"):
    log_lines.append(f"\n## {log.parent.parent.name}\n")
    log_lines.extend(line.rstrip() for line in log.read_text(errors="replace").splitlines()
                     if "QA_VISUAL_" in line or "Loading Minecraft 26.2" in line or "fabricloader 0.20.1" in line)
(OUT / "logs/runtime-evidence.txt").write_text("\n".join(log_lines) + "\n", encoding="utf-8")

archive = ROOT / "reports/visual-audit-12.0.0-alpha.18/piratesmca-visual-audit-current-assets-corrected.zip"
with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as zf:
    for path in sorted(OUT.rglob("*")):
        if path.is_file():
            zf.write(path, path.relative_to(OUT.parent).as_posix())
print(f"PASS packaged {len(entries)} screenshots; raw={sum(p.stat().st_size for _,p,_,_ in entries)} bytes; archive={archive}")
