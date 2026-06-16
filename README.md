<p align="center"><big><strong>Elevator</strong></big></p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.13%2B-brightgreen" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/Java-8%2B-orange" alt="Java Version">
  <img src="https://img.shields.io/badge/License-GPL%203.0-blue" alt="License">
</p>

A comprehensive Minecraft plugin that adds functional elevators and teleporter swap systems to your server.

## Features

- **Elevator System**: Jump to go up, sneak to go down
- **Teleporter System**: Right-click to swap positions with another player
- **Customizable Messages**: Full support for HEX colors (&#RRGGBB)
- **Particle Effects**: Configurable particle types and counts
- **Sound Effects**: Customizable sounds for actions
- **Cooldown System**: Prevent spam with configurable cooldowns
- **World Blacklist**: Disable in specific worlds
- **Permission Support**: Granular permission control
- **Cross-World Support**: Configurable cross-world teleportation

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
4. Reload the config with `/elevator reload`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `elevator.use` | Allows using elevators (jump/sneak) | true |
| `elevator.teleport` | Allows using teleporters | true |
| `elevator.bypass` | Bypasses cooldown system | op |
| `elevator.reload` | Allows reloading config | op |

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/elevator reload` | Reloads the configuration file | `elevator.reload` |

## Configuration

### Elevator Settings
```yaml
# ============================================
# ELEVATOR SETTINGS
# ============================================
Elevator:
  # Blocks that act as elevators (all carpet types)
  BlockTypes:
    - CARPET
    - BLACK_CARPET
    - BLUE_CARPET
    - BROWN_CARPET
    - CYAN_CARPET
    - GRAY_CARPET
    - GREEN_CARPET
    - LIGHT_BLUE_CARPET
    - LIGHT_GRAY_CARPET
    - LIME_CARPET
    - MAGENTA_CARPET
    - ORANGE_CARPET
    - PINK_CARPET
    - PURPLE_CARPET
    - RED_CARPET
    - WHITE_CARPET
    - YELLOW_CARPET

  # Maximum vertical search distance
  BlockDistance: 50

  # Visual effects
  EnableParticle: true
  ParticleType: SPELL_WITCH
  ParticleCount: 20

  # Sounds
  UsageSound: entity.enderman.teleport
  ActivateSound: entity.player.levelup

  # Allow teleporting into unsafe locations (lava, fire, etc.)
  AllowUnsafe: true

# ============================================
# TELEPORTER SETTINGS
# ============================================
Teleporter:
  # Blocks that act as teleporters (right-click to swap with another player)
  BlockTypes:
    - SEA_LANTERN
    - CRYING_OBSIDIAN
    - LIGHT_BLUE_GLAZED_TERRACOTTA

  # Visual effects
  EnableParticle: true

  # Sound when using teleporter
  UsageSound: entity.enderman.teleport

  # How many seconds to wait for another player
  WarmupTime: 5

  # Allow teleporting into unsafe locations
  AllowUnsafe: true

  # Allow swapping players across different worlds
  AllowCrossWorlds: true

# ============================================
# COOLDOWN SETTINGS
# ============================================
Cooldown:
  # Enable cooldown for elevator usage
  EnableCooldown: false

  # Cooldown time in seconds
  Time: 30

  # Message shown when on cooldown (%time% = seconds remaining)
  Locale: "&#FF5555Elevator is on cooldown. Please wait for another %time% seconds!"

  # Message type: CHAT, TITLE, or SUBTITLE
  MessageType: SUBTITLE

# ============================================
# ELEVATOR MESSAGES
# ============================================
ElevatorLocale:
  # Message type: CHAT, TITLE, or SUBTITLE
  MessageType: SUBTITLE

  # Title settings (only used for TITLE/SUBTITLE)
  Title:
    FadeIn: 10
    Stay: 40
    FadeOut: 10

  # Message when going up
  ElevatorUp: "&#55FF55⬆ Going up"

  # Message when going down
  ElevatorDown: "&#FFAA00⬇ Going down"

  # Message when destination is unsafe
  ElevatorDanger: "&#FF5555⚠ Danger! Unsafe location!"

# ============================================
# TELEPORTER MESSAGES
# ============================================
TeleporterLocale:
  # Message type: CHAT, TITLE, or SUBTITLE
  MessageType: SUBTITLE

  # Title settings (only used for TITLE/SUBTITLE)
  Title:
    FadeIn: 10
    Stay: 60
    FadeOut: 10

  # Message when waiting for another player (%time% = seconds)
  TeleporterWaiting: "&#FFFF55⏳ Waiting for another player... (%time%s) ⏳"

  # Message when another player is found
  TeleporterMatched: "&#55FF55✓ Player found! Swapping places..."

  # Message after successful swap
  TeleporterSwapped: "&#55FFFF✨ Swapped places! ✨"

  # Message when waiting time expires
  TeleporterTimeout: "&#FF5555❌ Teleport request timed out!"

  # Message when player cancels their own request
  TeleporterCancelled: "&#FF5555❌ Teleport cancelled!"

  # Message when player tries to swap with themselves
  TeleporterSamePlayer: "&#FF5555❌ You cannot swap with yourself!"

# ============================================
# DISABLED WORLDS
# ============================================
# Worlds where elevator and teleporter won't work
DisabledWorlds:
  - "world_nether"
  - "world_the_end"

# ============================================
# PERMISSIONS SETTINGS
# ============================================
Permissions:
  # Enable permission checking
  CheckPermission: false

  # Permission to use elevators
  Use: "elevator.use"

  # Permission to use teleporters
  Teleport: "elevator.teleport"

  # Permission to bypass cooldown
  BypassCooldown: "elevator.bypass"
```

## How It Works

### Elevator System
- **Jump**: Stand on an elevator block and jump to teleport to the nearest elevator above
- **Sneak**: Sneak on an elevator block to teleport to the nearest elevator below
- The elevator searches in a straight vertical line within the configured distance

### Teleporter System
- **Right-click** a teleporter block to start searching for another player
- Another player must right-click the same type of teleporter within the warmup time
- Both players will swap positions instantly
- Double-click the same teleporter to cancel the request

## Support

For issues or suggestions, please create an issue on the [GitHub repository](https://github.com/MootComb/Elevator/issues).

## License

This project is licensed under the GNU General Public License v3.0 - see the LICENSE file for details.
