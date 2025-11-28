# ⚔️ Terminal Dungeon: Full Gameplay & Implementation Guide

Embark on your journey through the **Terminal Dungeon**, a mystical realm of directories and files. Each level grants new powers, skills, and abilities, progressing from Squire to Arcane Knight. Players learn Linux commands safely in a sandbox while sensitive commands are simulated.

---

## 🎮 Level & Stage Overview

- **Levels:** 1–7
- **Stages per Level:** up to 3
- **Stage types:**
    - **Exploration** — learn navigation, find items
    - **Combat** — defeat enemies (goblins, kobolds)
    - **Puzzle** — move keys, open doors, deliver scrolls

---

## 🏁 Level Progression

### Level 1 — Squire (Tutorial)
- **Skills:** Navigation
- **Description:** Fully guided introduction. The game teaches safe commands.
- **Commands (real/sandboxed):** `ls`, `cd`, `pwd`, `tree`

**Stage Example:**
1. Navigate directories (`cd`)
2. List files (`ls`)
3. Show current path (`pwd`)

**Goal:** Learn core commands safely. All actions are real, executed in a sandboxed directory.

---

### Level 2 — Apprentice Knight (Messenger)
- **Skills:** Scroll Reading (`cat`)
- **Description:** Players read scrolls containing lore, guidance, and combat instructions.
- **Commands:** `cat` (real/sandboxed)

**Stage Example:**
1. `cat scroll.txt` → learns about goblins (`rm`)
2. `cat scroll.txt` → learns about hidden keys (`mv`)
3. `cat scroll.txt` → learns about doors and orbs (`chmod`)

**Goal:** Read scrolls to gain knowledge and unlock abilities. Scrolls teach both lore and mechanics.

---

### Level 3 — Scout Knight
- **Skills:** Combat (`rm`)
- **Description:** Trained to eliminate enemies.
- **Commands:** `rm`, `rm -rf`

**Execution:**
- Safe deletions can run in sandbox directories.
- Sensitive paths (like system root) are simulated, showing effects without harming files.

**Stage Example:**
1. Defeat goblins blocking paths
2. Clear temporary enemy directories
3. Remove multiple enemies at once (`rm -rf` in simulation)

---

### Level 4 — Warrior Knight
- **Skills:** Key Keeper (`chmod`)
- **Description:** Guardian of doors and access.
- **Commands:** `chmod`

**Execution:**
- Simulated for sensitive permissions.
- Safe changes allowed in sandbox directories.

---

### Level 5 — Guardian Knight
- **Skills:** The Saviour (`cp`, `mv`)
- **Description:** Protects treasures and items.
- **Commands:** `cp`, `mv`

**Execution:**
- Can move and copy files inside the sandbox.
- Some locked doors require moving hidden keys.

---

### Level 6 — Paladin
- **Skills:** Divine Restoration (`sudo`)
- **Description:** Invokes higher authority for powerful effects.
- **Commands:** `sudo`

**Execution:**
- Fully simulated.
- Provides feedback for actions like `sudo rm` without executing dangerous commands.

---

### Level 7 — Arcane Knight
- **Skills:** The Master (`&`, `|`)
- **Description:** Wielder of complex command chains.
- **Commands:** `&`, `|`

**Execution:**
- Sandbox-safe execution for combining commands.
- Simulated effects for sensitive operations.

---

## 🧩 Game Elements & How They Work

| Element | Description | Mechanics |
|---------|------------|-----------|
| 🗝️ Treasure Chest | Holds loot or scrolls | Navigate (`cd`) and list (`ls -a`) to find contents |
| 🔐 Hidden Key | Opens blocked doors | Move key to door (`mv key ./door/`) and sometimes activate via `chmod` |
| 👹 Goblin | Hostile enemy | Defeat using `rm` or `rm -rf` (simulated for sensitive paths) |
| 🐲 Kobold | Tough enemy | Requires `sudo rm` simulation |
| 🔮 Orbs | Mystical buffs | Grant passive abilities (hidden file detection, unlock commands) |
| 🚪 Blocked Doors | Restrict access | Unlock using keys, `chmod`, or `sudo` |
| 📜 Hidden Scrolls | Lore & guidance | Read using `cat`, deliver to wizard to unlock areas |
| 🧙 Wizard | Deciphers scrolls | Scrolls delivered via `mv`, unlocks commands or levels |

---

## 🏰 Stage-by-Stage Layout

| Level | Stage | Goal / Interaction | Objects | Commands / Mechanics |
|-------|-------|-----------------|---------|--------------------|
| 1 — Squire (Tutorial) | 1 | Learn directory navigation | None or simple directories | `cd`, `pwd` |
|  | 2 | List files and directories | Basic files/folders | `ls`, `tree` |
|  | 3 | Explore a mini-map / exit tutorial | None | Sandbox commands only |
| 2 — Apprentice Knight (Messenger) | 1 | Read scroll about Goblins | `scroll.txt` | `cat scroll.txt` → learn `rm` basics |
|  | 2 | Read scroll about Hidden Keys | `scroll.txt`, `key.txt` | `cat scroll.txt`, `mv key ./door/` |
|  | 3 | Read scroll about Doors & Orbs | `scroll.txt`, `orb_of_vision` | `cat scroll.txt`, passive buffs activate |
| 3 — Scout Knight | 1 | Defeat Goblins blocking path | Goblins as files | `rm filename` (simulated if sensitive) |
|  | 2 | Clear enemy mini-directory | Multiple goblins | `rm -rf` (simulated) |
|  | 3 | Defeat Kobold guarding treasure | Kobold | `sudo rm` simulation |
| 4 — Warrior Knight | 1 | Unlock Blocked Door | Door directory + Hidden Key | `mv key ./door/`, `chmod` (simulated) |
|  | 2 | Explore deeper dungeon | Orbs, loot | Collect orbs, buffs auto-trigger |
|  | 3 | Solve permission puzzle | Multi-layer door | `chmod` + keys to progress |
| 5 — Guardian Knight | 1 | Move treasure to safe location | Treasure Chest | `mv chest ./safe_room` |
|  | 2 | Copy critical scroll | `lost_scroll.txt` | `cp scroll.txt ./backup` |
|  | 3 | Combine movement & combat | Enemy + treasure | Use `mv` + `rm` together (simulated for dangerous commands) |
| 6 — Paladin | 1 | Restore corrupted dungeon files | Protected directories | `sudo cp /sandbox/...` (simulation) |
|  | 2 | Unlock sealed boss room | Strong door + Key | `sudo chmod` (simulated) |
|  | 3 | Clear large enemy block | Enemy cluster | `sudo rm -rf ./enemies` (simulated) |
| 7 — Arcane Knight | 1 | Chain commands for complex puzzle | Orbs, doors | `cat scroll.txt | grep "key"` |
|  | 2 | Combine combat & puzzles | Enemies + Doors | `rm -rf enemies & mv key ./door/` (simulated) |
|  | 3 | Master terminal dungeon | All mechanics combined | `&`, `|` |

---

## ✅ Notes on Gameplay Flow

- **Level 1 Tutorial:** Fully guided; players learn navigation commands safely.
- **Level 2 (Messenger):** Scrolls introduce lore and teach `cat` usage, gradually unlocking gameplay mechanics.
- **Level 3+:** Combat and puzzles rely on a mix of **real sandbox commands** (safe) and **simulated commands** (sensitive).
- **Scrolls:** Each stage may contain scrolls. Reading them is essential to learn enemy weaknesses, key usage, or dungeon lore.
- **Orbs:** Grant passive buffs automatically when collected.
- **Keys & Doors:** Introduce puzzles requiring multiple commands (`mv`, `chmod`, `sudo`).
- **Boss / Arcane Level:** Commands are combined using `&` or `|` for chaining actions — the ultimate test of Terminal Dungeon mastery.

