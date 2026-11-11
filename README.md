# Nightvision FPS

A single-player Minecraft Java mod with a semi-automatic pistol and wearable green night-vision goggles.

**Alpha 0.1.0-alpha.3.** Requires Minecraft Java **26.3**, Fabric Loader **0.20.1 or newer**, Fabric API **0.166.0+26.3**, and Java **25 or newer**.

Download the mod from [Releases](https://github.com/khanmahijyoti/nightvision-fps/releases). Report bugs through [Issues](https://github.com/khanmahijyoti/nightvision-fps/issues).

![Iron Pistol in first person](gallery/pistol.png)

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft **26.3**.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) **0.166.0+26.3** and `nightvision-fps-0.1.0-alpha.3.jar` from this repository's Releases page.
3. Put both JARs in your launcher's Minecraft `mods` folder. Keep one Nightvision FPS version installed at a time.
4. Launch your Fabric profile and open a single-player world. A pistol and a pair of goggles arrive automatically when inventory space is available.

The optional ZIP contains the same mod JAR, instructions, license, changelog, and screenshots. Extract it, then put the mod JAR in `mods`. Minecraft, Fabric Loader, and Fabric API are separate downloads.

## Pistol

Equip the **Iron Pistol** in your main hand. **Left-click** fires one shot per press. **Hold right-click** to aim with centered sights and a narrower first-person view. You can shoot while aiming. Release right-click to lower the pistol.

Each shot deals **8 base damage**, has a **64-block range**, and uses a **0.5-second cooldown**. This alpha has unlimited ammunition. Shots strike the nearest living target along the crosshair and stop at solid collision surfaces. Armor and projectile protection apply. Firing includes camera recoil, a muzzle flash, a brief tracer, impact particles, and a vanilla sound report.

![Aiming down the pistol sights](gallery/aiming.png)





![Night without the goggles](gallery/night-without-goggles.png)

![The same view with green night vision enabled](gallery/green-night-vision.png)

![Night-vision goggles equipped](gallery/goggles-equipped.png)

## Controls and equipment

| Control | Action |
| --- | --- |
| Left-click with pistol | Fire one shot |
| Hold right-click with pistol | Aim |
| Right-click while holding goggles | Equip goggles |




New and existing single-player worlds receive one pistol and one pair of goggles per player. Death respawns replace missing equipment. Keep Inventory retains existing items, including their custom names. A full inventory keeps delivery pending until a slot is available. Dropping a claimed item during ordinary play does not immediately grant a replacement.



## Compatibility

Nightvision FPS is a standalone mod.Single-player is the supported environment for this alpha. Multiplayer, LAN, dedicated servers, shader packs, and other mod combinations have not been validated.

When reporting bugs, include Minecraft and Fabric versions, reproduction steps, other installed mods, and relevant log excerpts with personal details removed.

## Rights

All rights reserved under [LICENSE](LICENSE). This repository contains release documentation, screenshots, and downloads. Development source files are maintained separately.

The mod references textures, sounds, and shaders supplied by the player's Minecraft installation. Its release contains original code, model definitions, language strings, and data files. All gallery images are gameplay captures.

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT. Minecraft belongs to its respective owners.
