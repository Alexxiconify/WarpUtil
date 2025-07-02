# WarpUtil - Nested Warps & Homes Plugin

A high-performance Minecraft plugin for managing nested warps and homes with sharing capabilities.

## Features

### Warp System
- **Nested Warps**: Organize warps in folders (e.g., `creative/build1`, `survival/spawn`)
- **Permission-based Access**: Granular permissions for specific warps
- **Commands**: `/warp`, `/setwarp`, `/delwarp`, `/warps`

### Home System
- **Personal Homes**: Players can set multiple homes with nested organization
- **Shared Homes**: Share homes with other players
- **Commands**: `/home`, `/sethome`, `/delhome`, `/homes`, `/sharehome`

## Commands

### Warp Commands
- `/warp <name>` - Teleport to a warp
- `/setwarp <name>` - Set a warp at current location
- `/delwarp <name>` - Delete a warp
- `/warps` - List all available warps

### Home Commands
- `/home <name>` - Teleport to a home (personal or shared)
- `/sethome <name>` - Set a home at current location
- `/delhome <name>` - Delete a personal home
- `/homes` - List all homes (personal and shared)
- `/sharehome <add|remove> <player> <home>` - Manage home sharing

## Permissions

### Warp Permissions
- `nestedwarps.warp` - Use warp commands
- `nestedwarps.warp.*` - Access all warps
- `nestedwarps.warp.<warp_name>` - Access specific warp
- `nestedwarps.setwarp` - Set warps
- `nestedwarps.delwarp` - Delete warps
- `nestedwarps.list` - List warps

### Home Permissions
- `nestedhomes.home` - Use home commands
- `nestedhomes.sethome` - Set homes
- `nestedhomes.delhome` - Delete homes
- `nestedhomes.list` - List homes
- `nestedhomes.share` - Share homes

## Configuration

The plugin automatically generates a `config.yml` file with example warps and homes. The structure supports nested organization:

```yaml
warps:
  hub:
    world: world
    x: 0.0
    y: 64.0
    z: 0.0
    yaw: 0.0
    pitch: 0.0
  creative:
    build1:
      world: world
      x: 100.5
      y: 70.0
      z: -50.5
      yaw: 90.0
      pitch: 45.0

homes:
  "player-uuid":
    mybase:
      world: world_survival
      x: 123.45
      y: 64.0
      z: 67.89
      yaw: 180.0
      pitch: 0.0
```

## Optimizations

This plugin has been optimized for:
- **Performance**: Reduced code duplication and improved efficiency
- **Memory Usage**: Streamlined data structures and caching
- **Maintainability**: Clean, modular code structure
- **Error Handling**: Robust error checking and logging
- **Tab Completion**: Intelligent command completion for nested paths

## Requirements

- Java 21+
- Paper/Spigot 1.20.1+
- Bukkit API

## Installation

1. Download the latest JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure permissions as needed

## Support

For issues or feature requests, please create an issue on the project repository.