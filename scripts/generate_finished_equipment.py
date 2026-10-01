"""Build finished-tool models from the original head and rod pixel art.

Run from any directory with: python scripts/generate_finished_equipment.py
The order below must match HeadBlueprintType and MonsterMaterial in Java.
"""

import json
import shutil
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REFERENCE = ROOT / "mcreator-reference/src/main/resources/assets/smeltingandforging/textures/item"
ITEMS = ROOT / "src/main/resources/assets/examplemod/textures/item"
MODELS = ROOT / "src/main/resources/assets/examplemod/models/item"

TOOLS = ("sword", "axe", "pickaxe", "shovel", "hoe")
MATERIALS = (
    "flesh", "bone", "string", "gunpowder", "slime", "pearl", "blaze_rode",
    "ghast", "wither", "phantom", "dragon_breath", "shulker", "nether_star",
)
EXTRA = {"dragon_breath", "shulker", "nether_star"}


def original(tool, material):
    if material in EXTRA or (material == "phantom" and tool in {"sword", "pickaxe", "shovel"}) or (tool == "sword" and material == "ghast"):
        return ITEMS / f"{tool}_{material}.png"
    if tool == "pickaxe" and material == "blaze_rode":
        return REFERENCE / "pickaxe_header_blaze_rods.png"
    return REFERENCE / f"{tool}_header_{material}.png"


def original_rod(material):
    return (ITEMS if material in EXTRA or material == "phantom" else REFERENCE) / f"rode_{material}.png"


def copy_art(source, target):
    if not source.is_file():
        raise FileNotFoundError(source)
    data = source.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n" or struct.unpack(">II", data[16:24]) != (16, 16):
        raise ValueError(f"Expected a 16x16 PNG: {source}")
    shutil.copyfile(source, target)


def write_model(name, layers):
    (MODELS / f"{name}.json").write_text(
        json.dumps({"parent": "minecraft:item/handheld", "textures": layers}, indent=2) + "\n",
        encoding="utf-8",
    )


def main():
    for material in MATERIALS:
        copy_art(original_rod(material), ITEMS / f"finished_rod_{material}.png")
    for tool in TOOLS:
        for material in MATERIALS:
            copy_art(original(tool, material), ITEMS / f"finished_{tool}_head_{material}.png")

    overrides = []
    variant = 0
    for tool in TOOLS:
        for head in MATERIALS:
            for rod in MATERIALS:
                variant += 1
                name = f"finished_{tool}_{head}_{rod}"
                # The rod is behind the head wherever their source pixels overlap.
                write_model(name, {
                    "layer0": f"examplemod:item/finished_rod_{rod}",
                    "layer1": f"examplemod:item/finished_{tool}_head_{head}",
                })
                overrides.append({"predicate": {"examplemod:equipment_variant": variant}, "model": f"examplemod:item/{name}"})

    base = {
        "parent": "minecraft:item/handheld",
        "textures": {"layer0": "minecraft:item/iron_sword"},
        "overrides": overrides,
    }
    (MODELS / "forged_equipment.json").write_text(json.dumps(base, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(TOOLS) * len(MATERIALS)} heads, {len(MATERIALS)} rods, {variant} models")


if __name__ == "__main__":
    main()
