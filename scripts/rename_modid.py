#!/usr/bin/env python3
"""One-time namespace migration; keeps Java package names unchanged."""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
old, new = "examplemod", "smeltingandforging"
extensions = {".json", ".java", ".toml", ".properties", ".mcmeta", ".gradle"}
skip = {".git", ".gradle", "build", "run", "mcreator-reference", ".idea", "scripts"}

def migrate(path, value):
    if path.suffix == ".java":
        return re.sub(r'"(?:\\.|[^"\\])*"', lambda m: m.group().replace(old, new), value)
    if path.name == "gradle.properties":
        return value.replace("mod_id="+old, "mod_id="+new).replace("mod_name=Example Mod", "mod_name=Smelting & Forging")
    return value.replace(old, new)

def main():
    updated = 0
    for path in sorted(root.rglob("*")):
        if not path.is_file() or any(p in skip for p in path.relative_to(root).parts) or path.suffix not in extensions:
            continue
        value = path.read_text(encoding="utf-8")
        replacement = migrate(path, value)
        if replacement != value:
            path.write_text(replacement, encoding="utf-8")
            updated += 1
    renamed = 0
    for base in [root/"src/main/resources", root/"src/generated/resources"]:
        for category in ("assets", "data"):
            source, destination = base/category/old, base/category/new
            if source.exists():
                if destination.exists():
                    raise RuntimeError("Refusing to overwrite " + str(destination))
                source.rename(destination)
                renamed += 1
    assert "mod_id="+new in (root/"gradle.properties").read_text(encoding="utf-8")
    assert 'MODID = "'+new+'"' in (root/"src/main/java/com/example/examplemod/ExampleMod.java").read_text(encoding="utf-8")
    print(f"Migrated {updated} text files and {renamed} resource directories")

if __name__ == "__main__":
    main()
