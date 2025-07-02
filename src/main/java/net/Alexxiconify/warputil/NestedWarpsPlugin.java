package net.Alexxiconify.warputil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class NestedWarpsPlugin extends org.bukkit.plugin.java.JavaPlugin {

 private static final String WARPS_SECTION = "warps";
 private static final String HOMES_SECTION = "homes";
 private static final String SHARED_HOMES_SECTION = "shared_homes";
 private static final String[] LOCATION_KEYS = {"world", "x", "y", "z", "yaw", "pitch"};

 // Data storage
 private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
 private final Map<UUID, TeleportRequest> pendingTeleports = new ConcurrentHashMap<>();
 private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();

 // Configuration cache
 private ConfigurationManager configManager;
 private MessageManager messageManager;
 private EconomyManager economyManager;
 private SafetyManager safetyManager;
 private EffectsManager effectsManager;

 @Override
 public void onEnable() {
  // Save default config first
  saveDefaultConfig();
  
  // Initialize managers
  configManager = new ConfigurationManager(this);
  messageManager = new MessageManager(this);
  economyManager = new EconomyManager(this);
  safetyManager = new SafetyManager(this);
  effectsManager = new EffectsManager(this);

  // Load settings
  configManager.reloadConfig();
  messageManager.reloadMessages();

  // Register commands
  registerCommands();

  // Start cooldown cleanup task
  startCooldownCleanupTask();

  getLogger().info("NestedWarps plugin enabled!");
 }

 @Override
 public void onDisable() {
  // Cancel all pending teleports
  pendingTeleports.clear();
  cooldowns.clear();
  lastLocations.clear();

  getLogger().info("NestedWarps plugin disabled!");
 }

 /**
  * Programmatically registers all commands for this plugin using Bukkit's CommandMap.
  * This method is called during onEnable().
  */
 private void registerCommands() {
  CommandMap commandMap = Bukkit.getCommandMap();

  // Warp commands
  registerCommand(commandMap, "warp", "Teleports to a warp", "/warp <warp_name>", 
                 "nestedwarps.warp", new WarpCommandExecutor(), new WarpTabCompleter());
  registerCommand(commandMap, "setwarp", "Sets a new warp", "/setwarp <warp_name>", 
                 "nestedwarps.setwarp", new SetWarpCommandExecutor(), new SetWarpTabCompleter());
  registerCommand(commandMap, "delwarp", "Deletes a warp", "/delwarp <warp_name>", 
                 "nestedwarps.delwarp", new DelWarpCommandExecutor(), new DelWarpTabCompleter());
  registerCommand(commandMap, "warps", "Lists all warps", "/warps", 
                 "nestedwarps.list", new WarpsCommandExecutor(), null);
  
  // Home commands
  registerCommand(commandMap, "home", "Teleports to home", "/home <home_name>", 
                 "nestedhomes.home", new HomeCommandExecutor(), new HomeTabCompleter());
  registerCommand(commandMap, "sethome", "Sets a new home", "/sethome <home_name>", 
                 "nestedhomes.sethome", new SetHomeCommandExecutor(), new SetHomeTabCompleter());
  registerCommand(commandMap, "delhome", "Deletes a home", "/delhome <home_name>", 
                 "nestedhomes.delhome", new DelHomeCommandExecutor(), new DelHomeTabCompleter());
  registerCommand(commandMap, "homes", "Lists all homes", "/homes", 
                 "nestedhomes.list", new HomesCommandExecutor(), null);
  registerCommand(commandMap, "sharehome", "Shares a home", "/sharehome <add|remove> <player> <home>", 
                 "nestedhomes.share", new ShareHomeCommandExecutor(), new ShareHomeTabCompleter());

  // Admin commands
  registerCommand(commandMap, "warputil", "Admin commands", "/warputil <reload|info>", 
                 "nestedwarps.admin", new AdminCommandExecutor(), null);
 }

   private void registerCommand(CommandMap commandMap, String name, String description, String usage, 
                             String permission, CommandExecutor executor, TabCompleter completer) {
   Command cmd = new Command(name, description, usage, new ArrayList<>()) {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
     return executor.onCommand(sender, this, commandLabel, args);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
     return completer != null ? completer.onTabComplete(sender, this, alias, args) : Collections.emptyList();
    }
   };
   cmd.setPermission(permission);
   commandMap.register("nestedwarps", cmd);
  }

  private void startCooldownCleanupTask() {
   new BukkitRunnable() {
    @Override
    public void run() {
     long currentTime = System.currentTimeMillis();
     cooldowns.entrySet().removeIf(entry -> 
        currentTime - entry.getValue() > configManager.getCooldown() * 1000L);
    }
   }.runTaskTimerAsynchronously(this, 20L * 60, 20L * 60); // Run every minute
  }

  // Location management methods
  private @Nullable Location getLocation(String section, String path) {
   ConfigurationSection configSection = getConfig().getConfigurationSection(section);
   if (configSection == null) return null;

   ConfigurationSection locationData = configSection.getConfigurationSection(path.replace("/", "."));
   if (locationData == null) return null;

   return loadLocationFromSection(locationData, path);
  }

  private void saveLocation(String section, String path, @NotNull Location location) {
   String configPath = section + "." + path.replace("/", ".");
   getConfig().set(configPath + ".world", location.getWorld().getName());
   getConfig().set(configPath + ".x", location.getX());
   getConfig().set(configPath + ".y", location.getY());
   getConfig().set(configPath + ".z", location.getZ());
   getConfig().set(configPath + ".yaw", location.getYaw());
   getConfig().set(configPath + ".pitch", location.getPitch());
   saveConfig();
  }

  private boolean deleteLocation(String section, String path) {
   String configPath = section + "." + path.replace("/", ".");
   if (getConfig().contains(configPath)) {
    getConfig().set(configPath, null);
    saveConfig();
    return true;
   }
   return false;
  }

  private @Nullable Location loadLocationFromSection(@NotNull ConfigurationSection section, String pathForLogging) {
   try {
    String worldName = section.getString("world");
    if (worldName == null) return null;
    
    World world = Bukkit.getWorld(worldName);
    if (world == null) {
     getLogger().warning("World '" + worldName + "' for path '" + pathForLogging + "' not found!");
     return null;
    }

    return new Location(world, 
     section.getDouble("x"), 
     section.getDouble("y"), 
     section.getDouble("z"),
     (float) section.getDouble("yaw"), 
     (float) section.getDouble("pitch"));
   } catch (Exception e) {
    getLogger().warning("Error loading location for path '" + pathForLogging + "': " + e.getMessage());
    return null;
   }
  }

 /**
  * Retrieves a warp location from the config.
  * Supports nested paths like "creative/build1".
  *
  * @param warpPath The path to the warp (e.g., "hub" or "creative/build1").
  * @return The Location object if found, null otherwise.
  */
 private @Nullable Location getWarpLocation(String warpPath) {
  return getLocation(WARPS_SECTION, warpPath);
 }

 /**
  * Saves a warp location to the config.
  * Supports nested paths like "creative/build1".
  *
  * @param warpPath The path to the warp.
  * @param location The Location object to save.
  */
 private void saveWarpLocation(String warpPath, @NotNull Location location) {
  saveLocation(WARPS_SECTION, warpPath, location);
 }

 /**
  * Deletes a warp location from the config.
  * Supports nested paths like "creative/build1".
  *
  * @param warpPath The path to the warp to delete.
  * @return true if the warp was found and deleted, false otherwise.
  */
 private boolean deleteWarpLocation(String warpPath) {
  return deleteLocation(WARPS_SECTION, warpPath);
 }

 /**
  * Retrieves a home location for a specific player from the config.
  * It checks personal homes first, then homes shared by others.
  *
  * @param playerUUID The UUID of the player.
  * @param homePath The path to the home (e.g., "mybase/farm").
  * @return The Location object if found, null otherwise.
  */
 private @Nullable Location getHomeLocation(@NotNull UUID playerUUID, String homePath) {
  // Check personal homes first
  String personalPath = HOMES_SECTION + "." + playerUUID.toString();
  Location personalHome = getLocation(personalPath, homePath);
  if (personalHome != null) return personalHome;

  // Check shared homes
  ConfigurationSection sharedSection = getConfig().getConfigurationSection(SHARED_HOMES_SECTION);
  if (sharedSection != null) {
   for (String sharerUUIDStr : sharedSection.getKeys(false)) {
    String sharedPath = SHARED_HOMES_SECTION + "." + sharerUUIDStr + "." + playerUUID.toString();
    if (getConfig().contains(sharedPath + "." + homePath.replace("/", "."))) {
     UUID sharerUUID = UUID.fromString(sharerUUIDStr);
     return getLocation(HOMES_SECTION + "." + sharerUUID.toString(), homePath);
    }
   }
  }
  return null;
 }

 /**
  * Saves a home location for a specific player to the config.
  * Supports nested paths like "mybase/farm".
  * Homes are stored under `homes.<player_uuid>.<home_path>`.
  *
  * @param playerUUID The UUID of the player.
  * @param homePath The path to the home.
  * @param location The Location object to save.
  */
 private void saveHomeLocation(@NotNull UUID playerUUID, String homePath, @NotNull Location location) {
  saveLocation(HOMES_SECTION + "." + playerUUID.toString(), homePath, location);
 }

 /**
  * Deletes a home location for a specific player from the config.
  * Supports nested paths like "mybase/farm".
  * Homes are stored under `homes.<player_uuid>.<home_path>`.
  * Also removes any sharing entries for this home.
  *
  * @param playerUUID The UUID of the player.
  * @param homePath The path to the home to delete.
  * @return true if the home was found and deleted, false otherwise.
  */
 private boolean deleteHomeLocation(@NotNull UUID playerUUID, String homePath) {
  if (deleteLocation(HOMES_SECTION + "." + playerUUID.toString(), homePath)) {
   removeSharingEntriesForHome(playerUUID, homePath);
   return true;
  }
  return false;
 }

 /**
  * Gets all homes shared *with* a specific player by other players.
  * Returns a map where key is the full home path (e.g., "sharer_name/home_path")
  * and value is the sharer's UUID. This allows for clear identification.
  *
  * @param targetUUID The UUID of the player receiving shared homes.
  * @return A map of shared home paths and their sharer UUIDs.
  */
 private @NotNull Map<String, UUID> getSharedHomesWithPlayer(@NotNull UUID targetUUID) {
  Map<String, UUID> sharedHomes = new HashMap<>();
  ConfigurationSection sharedSection = getConfig().getConfigurationSection(SHARED_HOMES_SECTION);
  if (sharedSection == null) return sharedHomes;

  for (String sharerUUIDStr : sharedSection.getKeys(false)) {
   ConfigurationSection targetSection = sharedSection.getConfigurationSection(sharerUUIDStr + "." + targetUUID.toString());
   if (targetSection != null) {
    UUID sharerUUID = UUID.fromString(sharerUUIDStr);
    for (String homeKey : targetSection.getKeys(false)) {
     String homePath = homeKey.replace(".", "/");
     if (getLocation(HOMES_SECTION + "." + sharerUUID.toString(), homePath) != null) {
      OfflinePlayer sharer = Bukkit.getOfflinePlayer(sharerUUID);
      String sharerName = sharer.getName() != null ? sharer.getName() : sharerUUIDStr.substring(0, 8);
      sharedHomes.put(sharerName + "/" + homePath, sharerUUID);
     }
    }
   }
  }
  return sharedHomes;
 }

 /**
  * Inner class for /warp command logic.
  */
 private class WarpCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    messageManager.sendMessage(sender, "player-only");
    return true;
   }

   if (args.length == 0) {
    messageManager.sendMessage(player, "warp-usage");
    return true;
   }

   String warpPath = String.join("/", args);
   if (!player.hasPermission("nestedwarps.warp." + warpPath.replace("/", ".")) && !player.hasPermission("nestedwarps.warp")) {
    messageManager.sendMessage(player, "no-permission");
    return true;
   }

   Location warpLocation = getWarpLocation(warpPath);
   if (warpLocation != null) {
    initiateTeleport(player, warpLocation, "warp", warpPath);
   } else {
    messageManager.sendMessage(player, "warp-not-found", Map.of("name", warpPath));
   }
   return true;
  }
 }

 /**
  * Inner class for /warp tab completion logic.
  */
 private class WarpTabCompleter implements TabCompleter {
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
   if (!sender.hasPermission("nestedwarps.warp")) {
    return Collections.emptyList();
   }
   return getTabCompletions(getConfig().getConfigurationSection(WARPS_SECTION), args, args.length > 0 ? args[args.length - 1] : "");
  }
 }

 /**
  * Inner class for /setwarp command logic.
  */
   private class SetWarpCommandExecutor implements CommandExecutor {
   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
     messageManager.sendMessage(sender, "player-only");
     return true;
    }

    if (args.length == 0) {
     messageManager.sendMessage(player, "setwarp-usage");
     return true;
    }

    String warpPath = String.join("/", args);
    
    // Check limits
    if (getAllWarpPaths().size() >= configManager.getMaxWarps()) {
     messageManager.sendMessage(player, "warp-limit-reached");
     return true;
    }
    
    // Check economy
    if (!economyManager.canAffordWarp(player)) {
     messageManager.sendMessage(player, "insufficient-funds");
     return true;
    }
    
    // Charge economy
    if (!economyManager.chargeWarp(player)) {
     messageManager.sendMessage(player, "economy-error");
     return true;
    }
    
    saveWarpLocation(warpPath, player.getLocation());
    messageManager.sendMessage(player, "warp-set", Map.of("name", warpPath));
    return true;
   }
  }

  private class SetWarpTabCompleter implements TabCompleter {
   @Override
   public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    if (!sender.hasPermission("nestedwarps.setwarp")) return Collections.emptyList();
    if (args.length == 0) return Collections.emptyList();
    
    List<String> completions = new ArrayList<>();
    List<String> existingWarps = getAllWarpPaths();
    String currentInput = args[args.length - 1].toLowerCase();
    
    existingWarps.stream()
     .filter(warp -> warp.toLowerCase().startsWith(currentInput))
     .forEach(completions::add);
    
    return completions.stream().distinct().collect(Collectors.toList());
   }
  }

 /**
  * Inner class for /delwarp command logic.
  */
   private class DelWarpCommandExecutor implements CommandExecutor {
   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 0) {
     messageManager.sendMessage(sender, "delwarp-usage");
     return true;
    }

    String warpPath = String.join("/", args);
    if (deleteWarpLocation(warpPath)) {
     // Refund if enabled
     if (sender instanceof Player player && configManager.isRefundOnDelete()) {
      economyManager.refundWarp(player);
     }
     messageManager.sendMessage(sender, "warp-deleted", Map.of("name", warpPath));
    } else {
     messageManager.sendMessage(sender, "warp-not-found", Map.of("name", warpPath));
    }
    return true;
   }
  }

  private class DelWarpTabCompleter implements TabCompleter {
   @Override
   public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    if (!sender.hasPermission("nestedwarps.delwarp")) return Collections.emptyList();
    return getTabCompletions(getConfig().getConfigurationSection(WARPS_SECTION), args, args.length > 0 ? args[args.length - 1] : "");
   }
  }

 /**
  * Inner class for /warps command logic.
  */
 private class WarpsCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   List<String> allWarps = getAllWarpPaths();

   if (allWarps.isEmpty()) {
    messageManager.sendMessage(sender, "no-warps");
   } else {
    messageManager.sendMessage(sender, "warps-header");
    Collections.sort(allWarps);
    allWarps.forEach(warp -> messageManager.sendMessage(sender, "warp-list-item", Map.of("name", warp)));
    messageManager.sendMessage(sender, "warps-footer");
   }
   return true;
  }
 }

 // --- Inner Classes for Home Commands ---

 /**
  * Inner class for /home command logic.
  */
 private class HomeCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    messageManager.sendMessage(sender, "player-only");
    return true;
   }

   if (args.length == 0) {
    messageManager.sendMessage(player, "home-usage");
    return true;
   }

   String homePath = String.join("/", args);
   Location homeLocation = getHomeLocation(player.getUniqueId(), homePath);
   if (homeLocation != null) {
    initiateTeleport(player, homeLocation, "home", homePath);
   } else {
    messageManager.sendMessage(player, "home-not-found", Map.of("name", homePath));
   }
   return true;
  }
 }

 /**
  * Inner class for /home tab completion logic.
  */
 private class HomeTabCompleter implements TabCompleter {
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    return Collections.emptyList();
   }
   if (!player.hasPermission("nestedhomes.home")) {
    return Collections.emptyList();
   }

   List<String> completions = new ArrayList<>();
   String currentInput = args.length > 0 ? args[args.length - 1] : "";

   // Personal homes
   ConfigurationSection playerHomes = getConfig().getConfigurationSection(HOMES_SECTION + "." + player.getUniqueId().toString());
   completions.addAll(getTabCompletions(playerHomes, args, currentInput));

   // Shared homes
   Map<String, UUID> sharedHomes = getSharedHomesWithPlayer(player.getUniqueId());
   for (String sharedHome : sharedHomes.keySet()) {
    String[] pathParts = sharedHome.split("/");
    if (args.length <= pathParts.length && pathParts[args.length - 1].toLowerCase().startsWith(currentInput.toLowerCase())) {
     completions.add(pathParts[args.length - 1]);
    }
   }

   return completions.stream().distinct().collect(Collectors.toList());
  }
 }

 /**
  * Inner class for /sethome command logic.
  */
 private class SetHomeCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    messageManager.sendMessage(sender, "player-only");
    return true;
   }

   if (args.length == 0) {
    messageManager.sendMessage(player, "sethome-usage");
    return true;
   }

   String homePath = String.join("/", args);
   
   // Check limits
   if (getAllHomePaths(player.getUniqueId()).size() >= configManager.getMaxHomes()) {
    messageManager.sendMessage(player, "home-limit-reached");
    return true;
   }
   
   // Check economy
   if (!economyManager.canAffordHome(player)) {
    messageManager.sendMessage(player, "insufficient-funds");
    return true;
   }
   
   // Charge economy
   if (!economyManager.chargeHome(player)) {
    messageManager.sendMessage(player, "economy-error");
    return true;
   }
   
   saveHomeLocation(player.getUniqueId(), homePath, player.getLocation());
   messageManager.sendMessage(player, "home-set", Map.of("name", homePath));
   return true;
  }
 }

 /**
  * Inner class for /sethome tab completion logic.
  */
 private class SetHomeTabCompleter implements TabCompleter {
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    return Collections.emptyList();
   }
   if (!player.hasPermission("nestedhomes.sethome")) {
    return Collections.emptyList();
   }

   if (args.length == 0) return Collections.emptyList();

   List<String> completions = new ArrayList<>();
   List<String> existingHomes = getAllHomePaths(player.getUniqueId());
   String currentInput = args[args.length - 1].toLowerCase();

   existingHomes.stream()
       .filter(home -> home.toLowerCase().startsWith(currentInput))
       .forEach(completions::add);

   return completions.stream().distinct().collect(Collectors.toList());
  }
 }

 /**
  * Inner class for /delhome command logic.
  */
 private class DelHomeCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    messageManager.sendMessage(sender, "player-only");
    return true;
   }

   if (args.length == 0) {
    messageManager.sendMessage(player, "delhome-usage");
    return true;
   }

   String homePath = String.join("/", args);
   if (deleteHomeLocation(player.getUniqueId(), homePath)) {
    // Refund if enabled
    if (configManager.isRefundOnDelete()) {
     economyManager.refundHome(player);
    }
    messageManager.sendMessage(player, "home-deleted", Map.of("name", homePath));
   } else {
    messageManager.sendMessage(player, "home-not-found", Map.of("name", homePath));
   }
   return true;
  }
 }

 /**
  * Inner class for /delhome tab completion logic.
  */
 private class DelHomeTabCompleter implements TabCompleter {
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    return Collections.emptyList();
   }
   if (!player.hasPermission("nestedhomes.delhome")) {
    return Collections.emptyList();
   }

   List<String> completions = new ArrayList<>();
   ConfigurationSection playerHomesRoot = getConfig().getConfigurationSection(HOMES_SECTION + "." + player.getUniqueId().toString());
   if (playerHomesRoot == null) return Collections.emptyList();

   ConfigurationSection sectionToSearch = playerHomesRoot;
   for (int i = 0; i < args.length - 1; i++) {
    if (sectionToSearch == null) break;
    sectionToSearch = sectionToSearch.getConfigurationSection(args[i]);
   }

   if (sectionToSearch != null) {
    for (String key : sectionToSearch.getKeys(false)) {
     ConfigurationSection childSection = sectionToSearch.getConfigurationSection(key);
     if (childSection != null) {
      boolean isActualHome = childSection.contains("world") && childSection.contains("x") &&
              childSection.contains("y") && childSection.contains("z");

      if (isActualHome) {
       completions.add(key);
      }

      if (childSection.getKeys(false).size() > 0) {
       completions.add(key + "/");
      }
     }
    }
   }
   return completions.stream().distinct().collect(Collectors.toList());
  }
 }

 /**
  * Inner class for /homes command logic.
  */
 private class HomesCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    messageManager.sendMessage(sender, "player-only");
    return true;
   }
   if (!player.hasPermission("nestedhomes.list")) {
    messageManager.sendMessage(player, "no-permission");
    return true;
   }

   List<String> combinedHomes = new ArrayList<>();
   
   // Add personal homes
   List<String> personalHomes = getAllHomePaths(player.getUniqueId());
   personalHomes.forEach(home -> combinedHomes.add(home + " (personal)"));

   // Add homes shared with this player
   Map<String, UUID> sharedHomes = getSharedHomesWithPlayer(player.getUniqueId());
   sharedHomes.forEach((homePath, sharerUUID) -> {
    OfflinePlayer sharer = Bukkit.getOfflinePlayer(sharerUUID);
    String sharerName = sharer.getName() != null ? sharer.getName() : "Unknown";
    combinedHomes.add(homePath + " (shared by " + sharerName + ")");
   });

   if (combinedHomes.isEmpty()) {
    messageManager.sendMessage(player, "no-homes");
   } else {
    messageManager.sendMessage(player, "homes-header");
    Collections.sort(combinedHomes);
    combinedHomes.forEach(home -> messageManager.sendMessage(player, "home-list-item", Map.of("name", home)));
    messageManager.sendMessage(player, "homes-footer");
   }
   return true;
  }
 }

 /**
  * Inner class for /sharehome command logic.
  */
 private class ShareHomeCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!(sender instanceof Player sharerPlayer)) {
    messageManager.sendMessage(sender, "player-only");
    return true;
   }

   if (args.length < 3) {
    messageManager.sendMessage(sharerPlayer, "sharehome-usage");
    return true;
   }

   String action = args[0].toLowerCase();
   String targetPlayerName = args[1];
   String homePath = String.join("/", Arrays.copyOfRange(args, 2, args.length));

   OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
   if (!targetPlayer.hasPlayedBefore()) {
    messageManager.sendMessage(sharerPlayer, "player-not-found", Map.of("name", targetPlayerName));
    return true;
   }

   UUID sharerUUID = sharerPlayer.getUniqueId();
   UUID targetUUID = targetPlayer.getUniqueId();

   // Verify home exists and belongs to sharer
   if (getLocation(HOMES_SECTION + "." + sharerUUID.toString(), homePath) == null) {
    messageManager.sendMessage(sharerPlayer, "home-not-found", Map.of("name", homePath));
    return true;
   }

   if ("add".equals(action)) {
    if (addSharedHomePermission(sharerUUID, targetUUID, homePath)) {
     messageManager.sendMessage(sharerPlayer, "home-shared", Map.of("name", homePath, "player", targetPlayerName));
    } else {
     messageManager.sendMessage(sharerPlayer, "home-already-shared", Map.of("name", homePath, "player", targetPlayerName));
    }
   } else if ("remove".equals(action)) {
    if (removeSharedHomePermission(sharerUUID, targetUUID, homePath)) {
     messageManager.sendMessage(sharerPlayer, "home-unshared", Map.of("name", homePath, "player", targetPlayerName));
    } else {
     messageManager.sendMessage(sharerPlayer, "home-not-shared", Map.of("name", homePath, "player", targetPlayerName));
    }
   } else {
    messageManager.sendMessage(sharerPlayer, "sharehome-usage");
   }
   return true;
  }
 }

 /**
  * Inner class for /sharehome tab completion logic.
  */
 private class ShareHomeTabCompleter implements TabCompleter {
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    return Collections.emptyList();
   }
   if (!player.hasPermission("nestedhomes.share")) {
    return Collections.emptyList();
   }

   List<String> completions = new ArrayList<>();
   String currentInput = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

   if (args.length == 1) {
    if ("add".startsWith(currentInput)) completions.add("add");
    if ("remove".startsWith(currentInput)) completions.add("remove");
   } else if (args.length == 2) {
    Bukkit.getOnlinePlayers().stream()
        .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
        .filter(p -> p.getName().toLowerCase().startsWith(currentInput))
        .map(Player::getName)
        .forEach(completions::add);
   } else if (args.length >= 3) {
    ConfigurationSection playerHomes = getConfig().getConfigurationSection(HOMES_SECTION + "." + player.getUniqueId().toString());
    String[] homeArgs = Arrays.copyOfRange(args, 2, args.length);
    completions.addAll(getTabCompletions(playerHomes, homeArgs, currentInput));
   }

   return completions.stream().distinct().collect(Collectors.toList());
  }
 }

 // Utility methods
 private @NotNull List<String> getAllWarpPaths() {
  return getAllPathsRecursive(getConfig().getConfigurationSection(WARPS_SECTION), "");
 }

 private @NotNull List<String> getAllHomePaths(@NotNull UUID playerUUID) {
  return getAllPathsRecursive(getConfig().getConfigurationSection(HOMES_SECTION + "." + playerUUID.toString()), "");
 }

 private @NotNull List<String> getAllPathsRecursive(@Nullable ConfigurationSection section, String currentPath) {
  List<String> paths = new ArrayList<>();
  if (section == null) return paths;

  for (String key : section.getKeys(false)) {
   String newPath = currentPath.isEmpty() ? key : currentPath + "/" + key;
   ConfigurationSection childSection = section.getConfigurationSection(key);

   if (childSection != null) {
    if (isValidLocation(childSection)) {
     paths.add(newPath);
    }
    paths.addAll(getAllPathsRecursive(childSection, newPath));
   }
  }
  return paths;
 }

 private @NotNull List<String> getTabCompletions(@Nullable ConfigurationSection section, String[] args, String currentInput) {
  List<String> completions = new ArrayList<>();
  if (section == null) return completions;

  ConfigurationSection searchSection = section;
  for (int i = 0; i < args.length - 1; i++) {
   if (searchSection == null) break;
   searchSection = searchSection.getConfigurationSection(args[i]);
  }

  if (searchSection != null) {
   for (String key : searchSection.getKeys(false)) {
    if (key.toLowerCase().startsWith(currentInput.toLowerCase())) {
     ConfigurationSection childSection = searchSection.getConfigurationSection(key);
     if (childSection != null) {
      if (isValidLocation(childSection)) {
       completions.add(key);
      }
      if (!childSection.getKeys(false).isEmpty()) {
       completions.add(key + "/");
      }
     }
    }
   }
  }
  return completions.stream().distinct().collect(Collectors.toList());
 }

 private boolean isValidLocation(@NotNull ConfigurationSection section) {
  return Arrays.stream(LOCATION_KEYS).allMatch(section::contains);
 }

 private void removeSharingEntriesForHome(@NotNull UUID sharerUUID, @NotNull String homePath) {
  ConfigurationSection sharerSection = getConfig().getConfigurationSection(SHARED_HOMES_SECTION + "." + sharerUUID.toString());
  if (sharerSection != null) {
   for (String targetUUIDStr : sharerSection.getKeys(false)) {
    String targetPath = SHARED_HOMES_SECTION + "." + sharerUUID.toString() + "." + targetUUIDStr + "." + homePath.replace("/", ".");
    if (getConfig().contains(targetPath)) {
     getConfig().set(targetPath, null);
    }
   }
  }
 }

 private boolean addSharedHomePermission(@NotNull UUID sharerUUID, @NotNull UUID targetUUID, @NotNull String homePath) {
  String configPath = SHARED_HOMES_SECTION + "." + sharerUUID.toString() + "." + targetUUID.toString() + "." + homePath.replace("/", ".");
  if (!getConfig().contains(configPath)) {
   getConfig().set(configPath, true);
   saveConfig();
   return true;
  }
  return false;
 }

 private boolean removeSharedHomePermission(@NotNull UUID sharerUUID, @NotNull UUID targetUUID, @NotNull String homePath) {
  String configPath = SHARED_HOMES_SECTION + "." + sharerUUID.toString() + "." + targetUUID.toString() + "." + homePath.replace("/", ".");
  if (getConfig().contains(configPath)) {
   getConfig().set(configPath, null);
   saveConfig();
   return true;
  }
  return false;
 }

 // Teleport management
 public void initiateTeleport(Player player, Location destination, String type, String name) {
  UUID playerId = player.getUniqueId();
  
  // Check cooldown
  if (isOnCooldown(playerId)) {
   long remaining = getCooldownRemaining(playerId);
   messageManager.sendMessage(player, "cooldown", Map.of("time", String.valueOf(remaining)));
   return;
  }
  
  // Check economy
  if (!economyManager.canAffordTeleport(player, type)) {
   messageManager.sendMessage(player, "insufficient-funds");
   return;
  }
  
  // Check safety
  if (!safetyManager.isSafeLocation(destination)) {
   messageManager.sendMessage(player, "unsafe-location");
   return;
  }
  
  // Check cross-world permission
  if (!safetyManager.canTeleportCrossWorld(player, player.getWorld(), destination.getWorld(), type)) {
   messageManager.sendMessage(player, "cross-world-denied");
   return;
  }
  
  // Store initial location
  lastLocations.put(playerId, player.getLocation());
  
  // Create teleport request
  TeleportRequest request = new TeleportRequest(destination, type, name, System.currentTimeMillis());
  pendingTeleports.put(playerId, request);
  
  // Start teleport delay
  int delay = configManager.getTeleportDelay();
  if (delay > 0) {
   messageManager.sendMessage(player, "teleport-starting", Map.of("time", String.valueOf(delay)));
   effectsManager.playStartEffect(player);
   
   new BukkitRunnable() {
    @Override
    public void run() {
     if (pendingTeleports.containsKey(playerId)) {
      TeleportRequest currentRequest = pendingTeleports.get(playerId);
      if (currentRequest.equals(request)) {
       executeTeleport(player, destination, type, name);
      }
     }
    }
   }.runTaskLater(this, delay * 20L);
  } else {
   executeTeleport(player, destination, type, name);
  }
 }

 private void executeTeleport(Player player, Location destination, String type, String name) {
  UUID playerId = player.getUniqueId();
  
  // Remove from pending teleports
  pendingTeleports.remove(playerId);
  
  // Check if player is still valid
  if (!player.isOnline()) return;
  
  // Check if player moved too much
  Location initialLocation = lastLocations.get(playerId);
  if (initialLocation != null && configManager.isCancelOnMovement()) {
   double distance = player.getLocation().distance(initialLocation);
   if (distance > configManager.getMovementThreshold()) {
    messageManager.sendMessage(player, "teleport-cancelled-movement");
    return;
   }
  }
  
  // Check if player took damage
  if (configManager.isCancelOnDamage() && player.getHealth() < player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) {
   messageManager.sendMessage(player, "teleport-cancelled-damage");
   return;
  }
  
  // Charge economy
  if (!economyManager.chargeTeleport(player, type)) {
   messageManager.sendMessage(player, "economy-error");
   return;
  }
  
  // Perform teleport
  player.teleport(destination);
  
  // Set cooldown
  setCooldown(playerId);
  
  // Send success message
  Map<String, String> placeholders = Map.of("type", type, "name", name);
  messageManager.sendMessage(player, "teleport-success", placeholders);
  
  // Play effects
  effectsManager.playEndEffect(player);
  
  // Clear stored location
  lastLocations.remove(playerId);
 }

 public void cancelTeleport(UUID playerId) {
  pendingTeleports.remove(playerId);
  lastLocations.remove(playerId);
 }

 private boolean isOnCooldown(UUID playerId) {
  if (!cooldowns.containsKey(playerId)) return false;
  long cooldownTime = configManager.getCooldown() * 1000L;
  return System.currentTimeMillis() - cooldowns.get(playerId) < cooldownTime;
 }

 private long getCooldownRemaining(UUID playerId) {
  if (!cooldowns.containsKey(playerId)) return 0;
  long cooldownTime = configManager.getCooldown() * 1000L;
  long elapsed = System.currentTimeMillis() - cooldowns.get(playerId);
  return Math.max(0, (cooldownTime - elapsed) / 1000L);
 }

 private void setCooldown(UUID playerId) {
  cooldowns.put(playerId, System.currentTimeMillis());
 }

 // Getters for managers
 public ConfigurationManager getConfigManager() { return configManager; }
 public MessageManager getMessageManager() { return messageManager; }
 public EconomyManager getEconomyManager() { return economyManager; }
 public SafetyManager getSafetyManager() { return safetyManager; }
 public EffectsManager getEffectsManager() { return effectsManager; }

 // Command Executors
 private class AdminCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (args.length == 0) {
    messageManager.sendMessage(sender, "admin-usage");
    return true;
   }

   String subCommand = args[0].toLowerCase();
   switch (subCommand) {
    case "reload":
     configManager.reloadConfig();
     messageManager.reloadMessages();
     messageManager.sendMessage(sender, "config-reloaded");
     break;
    case "info":
     messageManager.sendMessage(sender, "plugin-info", Map.of(
        "version", "1.0",
        "warps", String.valueOf(getAllWarpPaths().size()),
        "players", String.valueOf(Bukkit.getOnlinePlayers().size())
     ));
     break;
    default:
     messageManager.sendMessage(sender, "admin-usage");
     break;
   }
   return true;
  }
 }

 // Inner classes for data structures
 private static class TeleportRequest {
  private final Location destination;
  private final String type;
  private final String name;
  private final long timestamp;

  public TeleportRequest(Location destination, String type, String name, long timestamp) {
   this.destination = destination;
   this.type = type;
   this.name = name;
   this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object obj) {
   if (this == obj) return true;
   if (obj == null || getClass() != obj.getClass()) return false;
   TeleportRequest that = (TeleportRequest) obj;
   return timestamp == that.timestamp && 
          Objects.equals(destination, that.destination) && 
          Objects.equals(type, that.type) && 
          Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
   return Objects.hash(destination, type, name, timestamp);
  }
 }
}