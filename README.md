# Terminal Dungeon

Welcome to the **Terminal Dungeon**, a gamified Linux CLI game simulated using Java where every directory is a chamber, every file is an
entity, and every command is a skill.  
Progress from **Squire to Grandmaster Knight** while learning Linux commands safely in a controlled sandbox.

---

## Prerequesites

- Only fully runnable on WSL or any linux environment
- Java JDK25 must be insalled on the target linux environment

## Level & Stage Structure

- **Total Levels:** 6
- **Stages per Level:** 2
- **Stage Types:**
  - **Exploration** — navigation, discovering files
  - **Combat** — goblins, kobolds, and simulated dangerous commands
  - **Puzzle** — scroll reading, key movement, unlocking doors

---

## Stage-by-Stage Layout (Updated)

| Level                                 | Stage | Goal / Interaction            | Objects                                 | Commands / Mechanics                                                                         |
| ------------------------------------- | ----- | ----------------------------- | --------------------------------------- | -------------------------------------------------------------------------------------------- |
| **1 — Squire (Tutorial)**             | 1     | Learn directory navigation    | Basic directories                       | `cd`, `pwd`, `ls`, `tree`                                                                    |
|                                       | 2     | Retrieve a hidden message     | Simple files                            | `ls`, `cd`, `tree`, `cat`                                                                    |
| **2 — Apprentice Knight (Messenger)** | 3     | Complete the Navigation Trial | Multi-level directories                 | `cd`, `ls`, `pwd`, `cat` to explore areas, follow clues, and complete navigation tasks       |
|                                       | 4     | Explore the Great Archive     | Archive-like directories                | `cd`, `ls`, `cat` to read documents and synthesize knowledge to progress                     |
| **3 — Scout Knight**                  | 5     | Learn combat basics           | Training area with single enemies       | `rm` to defeat single or multiple targets; complete training to advance                      |
|                                       | 6     | Advanced combat training      | Areas with multiple enemy groups        | Use `rm` strategically to defeat groups and stronger opponents; complete training to advance |
| **4 — Warrior Knight**                | 7     | Unlock restricted areas       | Locked directories                      | `mv` to access new areas                                                                     |
|                                       | 8     | Organize key items            | Scattered collectible items             | `mv`, `mkdir`, `ls` to organize and manage items                                             |
| **5 — Arcane Knight**                 | 9     | Reconstruct the dungeon map   | Map pieces scattered across directories | `mv`, `mkdir`, `ls` to rebuild map                                                           |
|                                       | 10    | Clean up dungeon debris       | Temporary/junk files                    | `rm`, `rm -rf` safely to remove clutter                                                      |
| **6 — Grandmaster Knight**            | 11    | Restore the Grand Archive     | Core archive directories                | `mv`, `mkdir`, `ls` to restore structure                                                     |
|                                       | 12    | Defeat the final adversary    | Boss and decoy files                    | `rm`, `rm -rf` to complete final challenge                                                   |

## Game Elements

### **Enemies**

| Enemy      | HP Damage | Interaction                | Notes                                      | Special Skill / Ability                               |
| ---------- | --------- | -------------------------- | ------------------------------------------ | ----------------------------------------------------- |
| Goblin     | 5         | `rm <mob>`                 | Basic enemy                                | None                                                  |
| Kobold     | 10        | `rm <mob>`                 | Stronger variant                           | None                                                  |
| Zombie     | 15        | `rm <mob>`                 | Medium-difficulty enemy                    | None                                                  |
| Ghoul      | 20        | `rm <mob>`                 | Advanced enemy, part of groups             | None                                                  |
| Ogre       | 25        | `rm <mob>`                 | Powerful enemy, often in squads            | None                                                  |
| Vampire    | 30        | `rm <mob>`                 | Boss-level mob in combat stages            | Life Drain                                            |
| Demon Lord | 40        | `rm <mob>` (not spawnable) | Ultimate boss; appears only in final stage | Shadow Strike (deals extra damage and spawns enemies) |

**Combat Rules:**

- Wrong commands → damage
- Deleting one mob triggers the remaining mobs' damage
- Multiple mobs act independently

---

### **Obstacles**

| Obstacle    | Interaction   |
| ----------- | ------------- |
| Hidden Door | Requires keys |

---

## Development

Run via WSL:

```wsl
./run.sh
```

> Make sure that the EOL (End of Line Sequence) of the shell file is set up to LF.

## Build

### A. Generate and Distribute the Application

Run the following command to export the distributable package:

```wsl
./export.sh
```

Ensure the shell script uses `LF` (Unix) line endings. `CRLF` line endings may cause execution issues.

After execution, the distributable ZIP file will be available in the `./app` directory and is ready for distribution.

### B. Test the Distributable Application

You may test the application in one of the following ways:

#### Option 1: Run Directly from the Directory

> Note: Just do this if you are in testing mode, you just want to test if the distributed app is truly working.

```wsl
cd dist
```

If you obrained the zip file, just go to the option 2.

#### Option 2: Install the Distributed Package

If you have the zip file, go to the exported directory, then run wsl. Go to the exported zip, and then, unzip it.

After doing those options, install the exported application:

```wsl
./install.sh
```

Follow the on-screen instructions to complete the installation.

## Running the Game

Once installed, you can launch the game from any terminal:

```wsl
terminal-dungeon
```

or

```wsl
tdungeon start
```
