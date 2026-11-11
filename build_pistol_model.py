"""Generate native cuboid models referencing textures in the player's Minecraft installation."""
import copy
import json
from pathlib import Path

root = Path(__file__).parent / "resources/assets/iron_sidearm/models/item"
root.mkdir(parents=True, exist_ok=True)
elements = []


def box(start, end, texture):
    elements.append({
        "from": start, "to": end,
        "faces": {face: {"uv": [4, 4, 12, 12], "texture": "#" + texture}
                  for face in ("north", "south", "east", "west", "up", "down")},
    })


box([4.5, 1, 6.5], [7.5, 7.5, 9.5], "grip")
box([4.3, .7, 6.3], [7.7, 1.5, 9.7], "steel")
box([4, 7, 6], [16, 9.8, 10], "steel")
box([7.5, 6.3, 6.4], [15.7, 7.3, 9.6], "dark")
box([15.8, 7.4, 6.7], [16.2, 9.3, 9.3], "dark")
box([16.21, 7.9, 7.4], [16.3, 8.8, 8.6], "bore")
box([14.2, 9.8, 7.75], [15, 10.3, 8.25], "dark")
box([4.4, 9.8, 6.6], [5.2, 10.3, 7.2], "dark")
box([4.4, 9.8, 8.8], [5.2, 10.3, 9.4], "dark")
box([8, 3.6, 7.2], [10.6, 4.1, 8.8], "steel")
box([10.1, 4, 7.2], [10.6, 6.6, 8.8], "steel")
box([8.4, 5, 7.6], [8.9, 6.6, 8.4], "dark")
for x in (4.6, 5.4, 6.2):
    box([x, 7.2, 5.92], [x + .28, 9.4, 6.04], "dark")
    box([x, 7.2, 9.96], [x + .28, 9.4, 10.08], "dark")

model = {
    "parent": "minecraft:block/block", "ambientocclusion": False, "gui_light": "front",
    "textures": {
        "particle": "minecraft:block/iron_block", "steel": "minecraft:block/iron_block",
        "dark": "minecraft:block/polished_deepslate", "grip": "minecraft:block/stripped_dark_oak_log",
        "bore": "minecraft:block/black_concrete",
    },
    "elements": elements,
    "display": {
        "gui": {"rotation": [15, -25, -25], "translation": [-1, 1, 0], "scale": [.9, .9, .9]},
        "firstperson_righthand": {"rotation": [0, 90, 0], "translation": [1, 1, 0], "scale": [.85, .85, .85]},
        "firstperson_lefthand": {"rotation": [0, -90, 0], "translation": [1, 1, 0], "scale": [.85, .85, .85]},
        "thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [0, 2, 1], "scale": [.75, .75, .75]},
        "thirdperson_lefthand": {"rotation": [0, -90, 0], "translation": [0, 2, 1], "scale": [.75, .75, .75]},
        "ground": {"translation": [0, 2, 0], "scale": [.5, .5, .5]},
        "fixed": {"rotation": [0, 180, 0], "scale": [.8, .8, .8]},
    },
}
aim = copy.deepcopy(model)
aim.pop("elements")
aim.pop("textures")
aim["parent"] = "iron_sidearm:item/iron_pistol"
for hand in ("firstperson_righthand", "firstperson_lefthand"):
    aim["display"][hand]["translation"] = [-8.96, 6.45, 1.5]

for name, data in (("iron_pistol", model), ("iron_pistol_aim", aim)):
    (root / (name + ".json")).write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
print("Generated pistol and aiming models")

elements = []
box([0, 5, 0], [16, 12, 3], "frame")
box([1, 6, -1], [6.8, 11, .1], "lens")
box([9.2, 6, -1], [15, 11, .1], "lens")
box([2, 7, -1.1], [3, 10, -1], "glint")
box([10.2, 7, -1.1], [11.2, 10, -1], "glint")
box([6.5, 7.5, -1.3], [9.5, 9.5, 1], "frame")
box([0, 7, 3], [1, 10, 16], "strap")
box([15, 7, 3], [16, 10, 16], "strap")
box([0, 7, 15], [16, 10, 16], "strap")
goggles = {
    "parent": "minecraft:block/block", "ambientocclusion": False, "gui_light": "front",
    "textures": {"particle": "minecraft:block/black_concrete", "frame": "minecraft:block/polished_blackstone",
                 "lens": "minecraft:block/lime_concrete", "glint": "minecraft:block/emerald_block",
                 "strap": "minecraft:block/black_wool"},
    "elements": elements,
    "display": {
        "gui": {"rotation": [20, 155, 0], "translation": [0, 0, 0], "scale": [.85, .85, .85]},
        "head": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [.85, .85, .85]},
        "firstperson_righthand": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [.5, .5, .5]},
        "firstperson_lefthand": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [.5, .5, .5]},
        "thirdperson_righthand": {"rotation": [75, 0, 0], "translation": [0, 2, 0], "scale": [.4, .4, .4]},
        "thirdperson_lefthand": {"rotation": [75, 0, 0], "translation": [0, 2, 0], "scale": [.4, .4, .4]},
        "ground": {"translation": [0, 2, 0], "scale": [.45, .45, .45]},
    },
}
(root / "night_vision_goggles.json").write_text(json.dumps(goggles, indent=2) + "\n", encoding="utf-8")
print("Generated wearable night-vision goggles model")
