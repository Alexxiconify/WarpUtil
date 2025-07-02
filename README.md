# NestedWarps - Advanced Nested Warps & Homes Plugin

A high-performance, feature-rich Minecraft plugin for managing nested warps and homes with extensive customization options, safety features, and economy integration.

## ✨ Features

### 🎯 Core Functionality

- **Nested Warps**: Organize warps in folders (e.g., `creative/build1`, `survival/spawn`)
- **Personal Homes**: Players can set multiple homes with nested organization
- **Shared Homes**: Share homes with other players
- **Permission-based Access**: Granular permissions for specific warps and homes

### 🛡️ Safety & Security

- **Teleport Delays**: Configurable delay before teleportation
- **Cooldowns**: Prevent spam teleporting
- **Movement Cancellation**: Cancel teleport if player moves too much
- **Damage Cancellation**: Cancel teleport if player takes damage
- **Safe Location Checks**: Prevent teleporting into dangerous areas
- **Cross-world Protection**: Configurable cross-world teleport permissions

### 💰 Economy Integration

- **Vault Support**: Full integration with Vault economy plugins
- **Configurable Costs**: Set costs for setting warps/homes and teleporting
- **Refund System**: Automatic refunds when deleting warps/homes
- **Cost Customization**: Different costs for different actions

### 🎨 Visual & Audio Effects

- **Particle Effects**: Customizable particle effects for teleportation
- **Sound Effects**: Configurable sound effects
- **Modern API**: Uses latest Bukkit particle and sound systems

### 🔧 Advanced Configuration

- **Comprehensive Settings**: 50+ configuration options
- **Message Customization**: Fully customizable messages with color codes
- **Permission Granularity**: Fine-grained permission control
- **Integration Support**: WorldGuard, WorldEdit, PlaceholderAPI support

## 📋 Commands

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

### Admin Commands

- `/warputil reload` - Reload configuration
- `/warputil info` - Show plugin information

## 🔐 Permissions

### Warp Permissions

- `nestedwarps.warp` - Use warp commands
- `nestedwarps.warp.*` - Access all warps
- `nestedwarps.warp.<warp_name>` - Access specific warp
- `nestedwarps.setwarp` - Set warps
- `nestedwarps.delwarp` - Delete warps
- `nestedwarps.list` - List warps
- `nestedwarps.crossworld` - Cross-world warp teleportation
- `nestedwarps.bypass.regions` - Bypass region protection

### Home Permissions

- `nestedhomes.home` - Use home commands
- `nestedhomes.sethome` - Set homes
- `nestedhomes.delhome` - Delete homes
- `nestedhomes.list` - List homes
- `nestedhomes.share` - Share homes
- `nestedhomes.crossworld` - Cross-world home teleportation

### Admin Permissions

- `nestedwarps.admin` - Access admin commands

## ⚙️ Configuration

The plugin includes extensive configuration options in `config.yml`:

### General Settings

```yaml
general:
  debug: false
  language: "en"
  auto-save: true
```

### Teleport Settings

```yaml
teleport:
  delay: 3                    # Teleport delay in seconds
  cooldown: 5                 # Cooldown between teleports
  cancel-on-damage: true      # Cancel on damage
  cancel-on-movement: true    # Cancel on movement
  movement-threshold: 0.5     # Movement threshold
  allow-cross-world: true     # Allow cross-world teleports
```

### Economy Settings

```yaml
economy:
  enabled: false              # Enable economy integration
  warp-cost: 100.0           # Cost to set a warp
  home-cost: 50.0            # Cost to set a home
  warp-teleport-cost: 10.0   # Cost to teleport to warp
  home-teleport-cost: 5.0    # Cost to teleport to home
  refund-on-delete: true     # Refund on deletion
  refund-percentage: 0.5     # Refund percentage
```

### Effects Settings

```yaml
effects:
  enabled: true              # Enable effects
  start-effect: "SMOKE_NORMAL"      # Start teleport effect
  end-effect: "PORTAL"       # End teleport effect
  start-sound: "ENTITY_ENDERMAN_TELEPORT"
  end-sound: "ENTITY_ENDERMAN_TELEPORT"
  sound-volume: 0.5
  sound-pitch: 1.0
```

### Safety Settings

```yaml
safety:
  check-safe-location: true   # Check for safe locations
  max-fall-distance: 10       # Maximum fall distance
  prevent-block-teleport: true # Prevent teleporting into blocks
  prevent-lava-teleport: true  # Prevent teleporting into lava
  prevent-water-teleport: false # Prevent teleporting into water
  prevent-void-teleport: true   # Prevent teleporting into void
```

## 🎨 Message Customization

All messages can be customized in `messages.yml` with color codes and placeholders:

```yaml
messages:
  teleport-success: "&aSuccessfully teleported to %type%: &6%name%"
  warp-set: "&aWarp &6%name% &aset successfully!"
  home-shared: "&aSuccessfully shared your home &6%name% &awith &6%player%&a."
```

## 🔌 Integrations

### Supported Plugins

- **Vault**: Economy integration
- **WorldGuard**: Region protection
- **WorldEdit**: Selection-based teleportation
- **PlaceholderAPI**: Placeholder support

### Integration Configuration

```yaml
integrations:
  worldguard:
    enabled: false
    check-regions: true
    allow-in-protected: false
  worldedit:
    enabled: false
    allow-selection: true
  placeholderapi:
    enabled: false
```

## 🚀 Performance Optimizations

- **Efficient Data Structures**: Uses ConcurrentHashMap for thread-safe operations
- **Caching**: Configuration and message caching for better performance
- **Async Operations**: Cooldown cleanup runs asynchronously
- **Memory Management**: Automatic cleanup of expired cooldowns
- **Optimized Algorithms**: Efficient path finding and location validation
- **Modern APIs**: Uses latest Bukkit APIs for optimal performance

## 🛠️ Installation

1. **Download** the latest JAR file
2. **Place** it in your server's `plugins` folder
3. **Install Dependencies** (optional):
   - Vault for economy integration
   - WorldGuard for region protection
   - WorldEdit for selection features
4. **Restart** your server
5. **Configure** permissions and settings as needed

## 📊 Requirements

- **Java**: 21+
- **Server**: Paper/Spigot 1.21+
- **API**: Bukkit API
- **Optional**: Vault, WorldGuard, WorldEdit, PlaceholderAPI

## 🔧 Development

### Building from Source

```bash
mvn clean package
```

### Project Structure

```src/main/java/net/Alexxiconify/warputil/
├── NestedWarpsPlugin.java      # Main plugin class
├── ConfigurationManager.java    # Configuration management
├── MessageManager.java         # Message handling (Adventure API)
├── EconomyManager.java         # Economy integration
├── SafetyManager.java          # Safety checks
└── EffectsManager.java         # Visual/audio effects (Particle API)
```

## 🐛 Troubleshooting

### Common Issues

1. **Economy not working**: Ensure Vault is installed and economy plugin is loaded
2. **Permissions not working**: Check permission nodes and group configurations
3. **Cross-world teleports failing**: Verify cross-world permissions are set
4. **Effects not showing**: Check if effects are enabled in config

### Debug Mode

Enable debug mode in config to get detailed logging:

```yaml
general:
  debug: true
```

## 📈 Changelog

### v1.0.0 (Current)

- ✨ Complete rewrite with modular architecture
- 🛡️ Added comprehensive safety features
- 💰 Economy integration with Vault
- 🎨 Visual and audio effects system (Particle API)
- ⚙️ Extensive configuration options
- 🔧 Message customization system (Adventure API)
- 🚀 Performance optimizations
- 🔌 Plugin integration support
- 🔧 Modern API usage (no deprecated methods)

### v0.1.0 (Previous)

- Basic warp and home functionality
- Nested path support
- Home sharing system

## 🤝 Support

- **Issues**: Create an issue on the project repository
- **Documentation**: Check the wiki for detailed guides

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Bukkit/Spigot team for the excellent API
- Vault team for economy integration
- WorldGuard team for region protection
- Adventure API team for modern text handling
- All contributors and testers
