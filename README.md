<h1 align="center">Elevator</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.13%2B-brightgreen" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Java-8%2B-orange" alt="Java Version">
  <img src="https://img.shields.io/badge/License-GPL%203.0-blue" alt="License">
</p>

A comprehensive Minecraft plugin that adds functional elevators and paired teleporter blocks with a full in-game GUI, ID linking system, per-block access control, and holographic previews.

## Features

### Elevator System
- **Jump** to go up, **sneak** to go down
- Configurable block types (carpets by default)
- Adjustable vertical search distance
- Per-world blacklist

### Teleporter Block System
- Bind **any allowed block** using an **Ender Pearl** (Shift + Right-Click)
- Set a numeric **ID** to link two blocks into a pair
- Full **GUI menu** on Shift + Right-Click
- Per-block settings:
  - Click type (LEFT / RIGHT)
  - Require sneak (yes / no)
  - Require item (custom display name)
  - Teleport location (TOP / CURRENT position)
  - Access levels: Teleport / Manage / Break
  - Local and global member/owner lists
- **Holographic label** above the block (name + ID)
- **Distance check** between paired blocks
- **Cross-world** teleport toggle

### View Mode
- `/elevator view all` — highlight every block on the server
- `/elevator view owner` — highlight only your blocks
- `/elevator view member` — highlight blocks where you are a member or owner
- Particle outline rendered client-side only (no server lag)

### General
- Full HEX color support (`&#RRGGBB`)
- Configurable particles, sounds, titles, messages
- Cooldown system with bypass permission
- Granular permission control per feature
- Separate `blocks.yml` data file (production-safe keys)
- Debug mode for troubleshooting

## How to Build

### Prerequisites
- Java 8 or higher
- Maven 3.6+

### Build Instructions
```bash
git clone https://github.com/MootComb/Elevator.git
cd Elevator
mvn clean package
```

The compiled JAR will be located in the `target/` directory as `Elevator-<version>.jar`.

### Installation
1. Copy the JAR file to your server's `plugins/` folder
2. Restart your server or use a plugin manager
3. Configure the `config.yml` file to your liking
4. Reload with `/elevator reload`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `elevator.use` | Allows using elevators (jump/sneak) | true |
| `elevator.teleport` | Allows binding and using teleporter blocks | true |
| `elevator.manage` | Allows opening the GUI and editing block settings | true |
| `elevator.break` | Allows breaking teleporter blocks | true |
| `elevator.break.bypass` | Bypasses break access checks | op |
| `elevator.bypass` | Bypasses cooldown system | op |
| `elevator.bypass.access` | Bypasses teleport access checks | op |
| `elevator.bypass.distance` | Bypasses distance check | op |
| `elevator.bypass.item` | Bypasses required item check | op |
| `elevator.bypass.sneak` | Bypasses sneak requirement | op |
| `elevator.reload` | Allows reloading config | op |
| `elevator.view.all` | Allows `/elevator view all` | op |
| `elevator.view.owner` | Allows `/elevator view owner` | true |
| `elevator.view.member` | Allows `/elevator view member` | true |

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/elevator reload` | Reloads the configuration | `elevator.reload` |
| `/elevator info` | Shows info about the block you're looking at | `elevator.use` |
| `/elevator list` | Lists your teleporter blocks | `elevator.use` |
| `/elevator remove <id>` | Removes your blocks with the given ID | `elevator.use` |
| `/elevator view <all\|owner\|member>` | Highlights teleporter blocks | `elevator.view.*` |

## How It Works

### Binding a Block
1. Hold an **Ender Pearl** in your main hand
2. **Shift + Right-Click** an allowed block
3. The pearl is consumed and the block becomes a teleporter
4. The GUI opens automatically — set an ID

### Linking Two Blocks
1. Bind a second block
2. Set the **same ID** on both blocks (max 2 blocks per ID)
3. Right-click either block to teleport to the other one

### Opening the GUI
- **Shift + Right-Click** a bound block
- Requires `elevator.manage` (or you must be owner/member/global owner)

### Teleporting
- Depends on the block settings:
  - **Click type**: LEFT or RIGHT
  - **Sneak**: required or not
  - **Item**: custom-named item required or not
- Access is controlled by `TeleportAccess` (OWNER / MEMBERS / OWNERS / ALL)

## Configuration

### Elevator Settings
```yaml
Elevator:
  BlockTypes:
    - CARPET
    - BLACK_CARPET
    # ... etc
  BlockDistance: 50
  EnableParticle: true
  ParticleType: SPELL_WITCH
  ParticleCount: 20
  UsageSound: entity.enderman.teleport
  ActivateSound: entity.player.levelup
  AllowUnsafe: true
```

### Teleporter Settings
```yaml
Teleporter:
  EnableParticle: true
  UsageSound: entity.enderman.teleport
  AllowUnsafe: true
  AllowCrossWorlds: true
  AllowAllBlocks: false
  BlockTypesPermission: ""
  BlockTypes:
    - SEA_LANTERN
    - CRYING_OBSIDIAN
    - LIGHT_BLUE_GLAZED_TERRACOTTA
  DistanceCheck:
    Enabled: true
    MaxDistance: 10000.0
  Bind:
    ConsumePearl: true
```

### Per-Feature Permission Toggle
```yaml
TeleporterBlock:
  Teleport:
    Enabled: true
    CheckPermission: false
    Permission: "elevator.teleport"
    DefaultAccess: "ALL"
  Manage:
    Enabled: true
    CheckPermission: false
    Permission: "elevator.manage"
    DefaultAccess: "OWNER"
  Break:
    Enabled: true
    CheckPermission: false
    Permission: "elevator.break"
    DefaultAccess: "ALL"
```

### View Mode
```yaml
View:
  DurationTicks: 200
  UpdateEveryTicks: 5
  MaxDistance: 64
  ParticleCount: 3
  ParticleType: END_ROD
```

### Holograms
```yaml
Holograms:
  DefaultEnabled: false
  DefaultColor: "&#FF5300"
  ViewDistance: 16
  UpdateIntervalTicks: 20
  ShowId: true
  ShowName: true
  Format: "%name% &7| &fID: %id%"
  FormatNoName: "&7ID: %id%"
  LineHeight: 0.3
```

### Block Naming
```yaml
BlockNaming:
  Enabled: true
  MaxLength: 32
  DefaultName: ""
```

## Data Storage

- `config.yml` — plugin configuration
- `blocks.yml` — block data (auto-generated)
  - `Blocks.<world>_<x>_<y>_<z>` — block entries
  - `Players.<uuid>` — global member/owner lists

## Support

For issues or suggestions, please open an issue on the [GitHub repository](https://github.com/MootComb/Elevator/issues).

## License

This project is licensed under the GNU General Public License v3.0 — see the LICENSE file for details.

---

## 🗺️ Roadmap

Planned and proposed features, roughly ordered by priority.

### 🎨 Cosmetic / UX
- [x] **View mode** — `/elevator view <all|owner|member>` with particle highlights
- [x] **Holographic labels** above blocks (name + ID, configurable color & view distance)
- [x] **Custom block names** — assign a display name to each teleporter block
- [ ] **Per-block sound & particle override** — choose sound/particle per block from a list in the GUI
- [ ] **Custom GUI sounds** — click/pickup/open sounds configurable in `config.yml`
- [ ] **GUI animations** — blinking buttons, progress bars during long operations
- [ ] **Dark theme GUI** — black glass background, colored borders for active/inactive buttons
- [ ] **CustomModelData support** — texture packs for GUI items

### 🛡️ Access & Security
- [x] **Bypass permissions** — `elevator.bypass.access`, `elevator.bypass.distance`, `elevator.bypass.item`, `elevator.bypass.sneak`
- [ ] **Password/key system** — require a chat-entered password to teleport
- [ ] **Region protection integration** — WorldGuard, GriefPrevention, Towny
- [ ] **Auto-cleanup of dead blocks** — periodic scan of `blocks.yml` for missing blocks
- [ ] **Per-player block limit** — `elevator.limit.10`, `elevator.limit.50`, etc.

### ⚙️ Gameplay
- [x] **Per-block cooldown** — configurable per block
- [ ] **Warmup timer** before teleport — cancel on move/damage
- [ ] **Group teleport** — `/elevator tp <id>`, `/elevator tp <id> <player>`, `/elevator tphere <player>`
- [ ] **Block categories** — tags like `public`, `private`, `shop`, `pvp` for filtering
- [ ] **ID browser** — `/elevator ids` to list all IDs and their owners

### 💰 Economy (Vault)
- [ ] **Teleport cost** — charge per use
- [ ] **Bind cost** — charge per block binding
- [ ] **Pair cost** — charge for linking two blocks
- [ ] **Vault integration** — soft-depend

### 📊 Logging & Admin
- [x] **Debug mode** — detailed console logging
- [ ] **Action logs** — `logs.yml` / `logs.txt` with timestamp, player, action, ID, location
- [ ] **Discord webhook** — log block creation, deletion, teleports, errors
- [ ] **Admin commands** — `/elevator admin list`, `remove <id>`, `transfer <id> <player>`, `reload`
- [ ] **Statistics** — `/elevator stats`, top-10 players by teleports

### 🔌 Integrations
- [ ] **PlaceholderAPI** — `%elevator_blocks%`, `%elevator_teleports%`, `%elevator_id%`
- [ ] **WorldGuard** — region-based restrictions
- [ ] **Vault** — economy
- [ ] **Citizens** — NPC teleporters
- [ ] **Dynmap / BlueMap** — display blocks on web map

### 🔧 Utilities
- [ ] **Import/export** — `/elevator export <file>`, `/elevator import <file>`
- [ ] **Auto-backup** of `blocks.yml` — periodic backups to `backups/`
- [ ] **GUI pagination** for block lists — sorting by ID / name / date

### 🌍 Localization
- [ ] **Multi-language support** — `lang/en.yml`, `lang/ru.yml`, etc.
- [ ] **Per-player language selection** — `/elevator lang <code>`
