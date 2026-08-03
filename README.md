<p align="center">
    <img src="./src/main/resources/icon.png" alt="logo" width="200" title="None">
</p>
<hr>
<p align="center">Auto Roll Trade.</p>
<p align="center">
    <a href="https://modrinth.com/mod/athirdhand">
        <img src="https://img.shields.io/modrinth/dt/7coPSolx?label=Modrinth&logo=Modrinth&style=flat-square" alt="Modrinth Downloads">
    </a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/athirdhand">
        <img src="https://img.shields.io/curseforge/dt/1597584?style=flat-square&logo=curseforge&label=CurseForge" alt="CurseForge Downloads">
    </a>
</p>
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ticsea/librarianRoller)
## Features

- 🧩 **Pure Client** - Runs entirely on the client side, does not modify server data, safe and reliable
- 🔄 **Automated Loop** - Automatically breaks and re-places lecterns to refresh villager trades
- 🎯 **Enchantment Filtering** - Specify desired enchantments and levels for precise targeting
- 👥 **Multi-Villager Support** - Supports 1, 4, or 6 villagers simultaneously for faster farming
- 🛡️ **Durability Protection** - Automatically stops when axe durability drops below threshold to prevent breakage
- ⚙️ **Flexible Configuration** - All parameters adjustable via config file

---

## Usage

### V1 Mode (Recommended)

1. Prepare a librarian villager and a lectern
2. Configure the target enchantment list
3. Face the lectern and villager (Player → Lectern → Villager)
4. Press `J` (default) to start/stop the automation loop

### V4 Mode

1. Place librarian villagers and lecterns in the **specific arrangement** shown below
2. Configure the target enchantment list
3. Stand in the designated position
4. Press `J` (default) to start/stop the automation loop

### V6 Mode

1. Place librarian villagers and lecterns in the **specific arrangement** shown below
2. Configure the target enchantment list
3. 👁 Player's line of sight must be **parallel** to the two rows of lecternsy
4. Stand in the designated position
5. Press `J` (default) to start/stop the automation loop

---

## V4 & V6 Layout

1. See images in the image gallery section
2. Color Legend:
    - 🟧 Orange: Player
    - 🟩 Green: Villager
    - 🟥 Magma Block: Lectern

---

## Configuration Options

| Option | Description |
|--------|-------------|
| **Auto-Buy** | Automatically purchases and locks the enchantment when found (Not yet implemented) |
| **Prevent Axe Breaking** | Stops automation when axe durability falls below 10 to protect the tool |
| **Legit Mode** | Makes player eyes follow the target lectern/villager (Not yet implemented, considering removal) |
| **Villager Count** | Selects V1/V4/V6 mode as described above |
| **Enchantment Entries** | List of enchantments to target (format: `enchantment_name level`, one per line) |

> **Example:**
> ```
> Smite 5
> Sharpness 5
> Silk Touch
> ```
> *Enchantments without a specified level are excluded from the filter list.*

## AND
* This mod modify from CameraOverhaul Mod and inspire by Librarian Trade Finder.
* https://github.com/Mirsario/Minecraft-CameraOverhaul
* https://github.com/Greeenman999/LibrarianTradeFinder

## NOTE
- replace "Please write something"
