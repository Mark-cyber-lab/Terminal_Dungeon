# ⚔️ Terminal Knight Progression Guide

Embark on your journey through the Kingdom of the Command Line, where each level grants you new powers, skills, and abilities. Master the path from a humble squire to an arcane knight.

---

## Level 1 — Squire
**Skills:** Navigation  
**Description:**  
A beginner explorer of the realm. The Squire can traverse directories and unveil the structure of unfamiliar lands.

**Commands:**  
`ls`, `cd`, `pwd`, `tree`

---

## Level 2 — Apprentice Knight
**Skills:** Messenger  
**Description:**  
A keeper of stories and lore. The Apprentice Knight can read scrolls and ancient texts.

**Commands:**  
`cat`

---

## Level 3 — Scout Knight
**Skills:** Combat  
**Description:**  
Trained in the art of destruction. The Scout Knight can eliminate enemies and clear obstacles—sometimes entire battalions.

**Commands:**  
`rm`, `rm -rf`

---

## Level 4 — Warrior Knight
**Skills:** Key Keeper  
**Description:**  
Guardian of access and passage. The Warrior Knight controls who may enter doors—opened or sealed.

**Commands:**  
`chmod`

---

## Level 5 — Guardian Knight
**Skills:** The Saviour  
**Description:**  
A protector who brings order to chaos. The Guardian Knight can relocate precious items to safer grounds.

**Commands:**  
`cp`, `mv`

---

## Level 6 — Paladin
**Skills:** Divine Restoration  
**Description:**  
A holy warrior capable of invoking higher authority. The Paladin can perform actions beyond mortal permission, restoring or altering the world with elevated power.

**Commands:**  
`sudo`

---

## Level 7 — Arcane Knight
**Skills:** The Master  
**Description:**  
A wielder of mystical forces. The Arcane Knight can weave commands together, forming powerful chains of magic.

**Commands:**  
`&`, `|`

---

# 🧩 Game Elements & How They Work

Below are the interactive elements found in the dungeon world and how they behave inside the game.

---

## 🗝️ Treasure Chest
**Description:**  
A container holding valuable loot or scrolls.

**Mechanics:**  
- Appears as a directory or a file.  
- Requires navigation (`cd`) to open.  
- Sometimes contains hidden items readable via `ls -a`.  

---

## 🔐 Hidden Key
**Description:**  
A small but essential file needed to open blocked doors.

**Mechanics:**  
- Found inside obscure folders, often only visible using `ls -a`.  
- The key must be moved to a door directory to unlock it.  
- Sometimes requires `chmod` to "activate" the key.

---

## 👹 Goblin
**Description:**  
A hostile creature blocking your path.

**Mechanics:**  
- Represented as a file or directory.  
- Must be defeated using `rm` or `rm -rf`.  
- Some goblins drop keys or scrolls when removed.

---

## 🐲 Kobold
**Description:**  
A tougher enemy than goblins, sometimes guarding treasure.

**Mechanics:**  
- Appears as protected files (permission-locked).  
- Requires `sudo rm` to defeat.  
- Can trigger traps if deleted without sudo.

---

## 🔮 Orbs
**Description:**  
Mystical artifacts that aid the knight.

**Mechanics:**  
- Found as files named `orb_of_*`.  
- Collecting orbs grants passive buffs like:  
  - revealing hidden files (auto-use `ls -a`)  
  - unlocking new commands  
  - shortening paths  
- Orbs cannot be destroyed.

---

## 🚪 Blocked Doors
**Description:**  
Doors preventing access to new areas.

**Mechanics:**  
- Appears as a directory with restricted permissions.  
- Can be opened using:  
  - a Hidden Key  
  - `chmod`  
  - or `sudo` for very strong doors.  
- Some doors require moving a key into the door folder:  
  - `mv key ./door/`

---

## 📜 Hidden Scrolls (Lost Scrolls)
**Description:**  
Ancient lore fragments hidden deep within the world.

**Mechanics:**  
- Appears as files like `lost_scroll.txt`.  
- Must be found via deep navigation.  
- Contents can be read using `cat`.  
- Some are encrypted and require a wizard.

---

## 🧙 Wizard
**Description:**  
A wise entity that deciphers Lost Scrolls.

**Mechanics:**  
- The wizard exists in a directory like `/wizard_tower/`.  
- To have a scroll deciphered, it must be delivered via:  
  - `mv lost_scroll.txt /wizard_tower/`  
- Once the scroll is brought, the wizard can “read” it and unlock new areas or commands.

---

# 🗺️ Suggested Interactions & Flow

- Find **Hidden Keys** → unlock **Blocked Doors**.  
- Defeat **Goblins** and **Kobolds** → clear paths or gain items.  
- Collect **Orbs** → gain passive abilities.  
- Discover **Lost Scrolls** → bring them to the **Wizard** via `mv`.  
- Use **Knight Levels** (command skills) to traverse, fight, unlock, and progress.
