"""Regenerate the 16x16 pixel art for the forging block and anvil.

Requires Pillow: python -m pip install pillow
Run from the repository root: python scripts/generate_forging_block_assets.py
"""
from pathlib import Path
from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/examplemod/textures/block'
OUT.mkdir(parents=True, exist_ok=True)
DARK=(24,27,30); IRON=(107,115,118); BRIGHT=(153,158,154)
SIDE=(68,77,83); LIGHT=(190,178,131); EMBER=(233,100,22); HOT=(255,177,45)

def canvas():
    im=Image.new('RGBA',(16,16),(0,0,0,255))
    return im,ImageDraw.Draw(im)

def grain(draw, color, coords):
    for x,y in coords: draw.point((x,y),fill=color)

def save(im,name): im.save(OUT/name)

# Furnace front: keep the two orange chambers of the reference forger.png.
im,d=canvas();d.rectangle((0,0,15,15),fill=(65,71,74));d.rectangle((1,1,14,14),fill=IRON)
d.rectangle((2,2,13,7),fill=(54,59,62));d.rectangle((3,3,12,6),fill=DARK)
d.rectangle((4,6,11,6),fill=EMBER);d.rectangle((6,6,9,6),fill=HOT)
d.rectangle((2,9,13,14),fill=(53,58,61));d.rectangle((3,10,12,13),fill=DARK)
d.rectangle((4,13,11,13),fill=EMBER);d.rectangle((6,13,9,13),fill=HOT)
for x in (1,14):
    for y in (2,13): d.point((x,y),fill=LIGHT)
grain(d,BRIGHT,[(3,1),(11,1),(2,8),(8,8),(13,8),(3,15),(12,15)])
save(im,'forge_front.png')

im,d=canvas();d.rectangle((0,0,15,15),fill=SIDE);d.rectangle((1,1,14,14),outline=IRON)
for x in (3,6,9,12):
    d.rectangle((x,5,x+1,11),fill=(30,36,42));d.point((x,5),fill=(49,56,60))
for y in (2,13):
    for x in (2,13):d.point((x,y),fill=LIGHT)
grain(d,(93,102,104),[(4,2),(10,3),(5,13),(11,13)])
save(im,'forge_side.png')

im,d=canvas();d.rectangle((0,0,15,15),fill=IRON)
d.rectangle((1,1,14,14),outline=BRIGHT)
d.rectangle((3,3,12,12),fill=(50,55,58));d.rectangle((4,4,11,11),fill=DARK)
d.rectangle((5,5,10,10),fill=EMBER);d.rectangle((6,6,9,9),fill=HOT)
d.line((2,2,13,2),fill=(206,204,187));d.line((2,13,13,13),fill=(70,76,78))
save(im,'forge_top.png')

im,d=canvas();d.rectangle((0,0,15,15),fill=(57,63,67));d.rectangle((2,2,13,13),outline=(95,102,105))
save(im,'forge_bottom.png')

# Shared anvil materials; faces sample the correct UV area from each atlas.
im,d=canvas();d.rectangle((0,0,15,15),fill=(106,114,118))
d.rectangle((1,1,14,14),outline=(156,160,156))
d.line((3,5,12,5),fill=(187,188,176));d.line((3,11,12,11),fill=(63,73,79))
for x in (3,12):
    for y in (3,12): d.point((x,y),fill=LIGHT)
save(im,'anvil_base.png')

im,d=canvas();d.rectangle((0,0,15,15),fill=(72,81,88))
d.line((0,3,15,3),fill=(119,127,130));d.line((0,13,15,13),fill=(45,53,61))
for x in (3,12):
    for y in (3,12):d.rectangle((x,y,x+1,y+1),fill=(164,150,109))
save(im,'anvil_side.png')

im,d=canvas();d.rectangle((0,0,15,15),fill=(137,147,150))
d.rectangle((2,4,13,11),outline=(180,183,174))
d.rectangle((5,6,10,9),fill=(42,48,52))
d.rectangle((6,7,9,8),fill=EMBER);d.line((7,7,8,7),fill=HOT)
grain(d,(190,190,178),[(3,5),(12,5),(3,10),(12,10)])
save(im,'anvil_top.png')
