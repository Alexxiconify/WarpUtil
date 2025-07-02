// Main plugin class: NestedWarpsPlugin.java
package net.Alexxiconify.warputil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer; // For getting UUID of offline players
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays; // Added for Arrays.copyOfRange
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class NestedWarpsPlugin extends org.bukkit.plugin.java.JavaPlugin {

 private static final String WARPS_SECTION = "warps";
 private static final String HOMES_SECTION = "homes";
 private static final String SHARED_HOMES_SECTION = "shared_homes";
 private static final String[] LOCATION_KEYS = {"world", "x", "y", "z", "yaw", "pitch"};

 @Override
 public void onEnable() {
  saveDefaultConfig();

  // Register commands programmatically
  registerCommands();

  getLogger().info("NestedWarps plugin enabled!");
 }

 @Override
 public void onDisable() {
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
   cmd.setPermissionMessage(ChatColor.RED + "You do not have permission to use this command.");
   commandMap.register("nestedwarps", cmd);
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
    getLogger().severe("Error loading location for path '" + pathForLogging + "': " + e.getMessage());
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
    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
    return true;
   }

   if (args.length == 0) {
    player.sendMessage(ChatColor.YELLOW + "Usage: /warp <warp_name>");
    return true;
   }

   String warpPath = String.join("/", args);
   if (!player.hasPermission("nestedwarps.warp." + warpPath.replace("/", ".")) && !player.hasPermission("nestedwarps.warp")) {
    player.sendMessage(ChatColor.RED + "You do not have permission to warp to " + warpPath + ".");
    return true;
   }

   Location warpLocation = getWarpLocation(warpPath);
   if (warpLocation != null) {
    player.teleport(warpLocation);
    player.sendMessage(ChatColor.GREEN + "Teleported to warp: " + ChatColor.GOLD + warpPath);
   } else {
    player.sendMessage(ChatColor.RED + "Warp '" + warpPath + "' not found.");
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
     sender.sendMessage(ChatColor.RED + "Only players can use this command.");
     return true;
    }

    if (args.length == 0) {
     player.sendMessage(ChatColor.YELLOW + "Usage: /setwarp <warp_name>");
     return true;
    }

    String warpPath = String.join("/", args);
    saveWarpLocation(warpPath, player.getLocation());
    player.sendMessage(ChatColor.GREEN + "Warp '" + ChatColor.GOLD + warpPath + ChatColor.GREEN + "' set successfully!");
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
     sender.sendMessage(ChatColor.YELLOW + "Usage: /delwarp <warp_name>");
     return true;
    }

    String warpPath = String.join("/", args);
    if (deleteWarpLocation(warpPath)) {
     sender.sendMessage(ChatColor.GREEN + "Warp '" + ChatColor.GOLD + warpPath + ChatColor.GREEN + "' deleted successfully!");
    } else {
     sender.sendMessage(ChatColor.RED + "Warp '" + warpPath + "' not found.");
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
    sender.sendMessage(ChatColor.YELLOW + "No warps have been set yet.");
   } else {
    sender.sendMessage(ChatColor.AQUA + "--- Available Warps ---");
    Collections.sort(allWarps);
    allWarps.forEach(warp -> sender.sendMessage(ChatColor.GRAY + "- " + warp));
    sender.sendMessage(ChatColor.AQUA + "---------------------");
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
    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
    return true;
   }

   if (args.length == 0) {
    player.sendMessage(ChatColor.YELLOW + "Usage: /home <home_name>");
    return true;
   }

   String homePath = String.join("/", args);
   Location homeLocation = getHomeLocation(player.getUniqueId(), homePath);
   if (homeLocation != null) {
    player.teleport(homeLocation);
    boolean isPersonal = getConfig().contains(HOMES_SECTION + "." + player.getUniqueId().toString() + "." + homePath.replace("/", "."));
    String message = isPersonal ? "personal home" : "shared home";
    player.sendMessage(ChatColor.GREEN + "Teleported to your " + message + ": " + ChatColor.GOLD + homePath);
   } else {
    player.sendMessage(ChatColor.RED + "Home '" + homePath + "' not found or you do not have access to it.");
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
    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
    return true;
   }

   if (args.length == 0) {
    player.sendMessage(ChatColor.YELLOW + "Usage: /sethome <home_name>");
    return true;
   }

   String homePath = String.join("/", args);
   saveHomeLocation(player.getUniqueId(), homePath, player.getLocation());
   player.sendMessage(ChatColor.GREEN + "Home '" + ChatColor.GOLD + homePath + ChatColor.GREEN + "' set successfully!");
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
    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
    return true;
   }

   if (args.length == 0) {
    player.sendMessage(ChatColor.YELLOW + "Usage: /delhome <home_name>");
    return true;
   }

   String homePath = String.join("/", args);
   if (deleteHomeLocation(player.getUniqueId(), homePath)) {
    player.sendMessage(ChatColor.GREEN + "Home '" + ChatColor.GOLD + homePath + ChatColor.GREEN + "' deleted successfully!");
   } else {
    player.sendMessage(ChatColor.RED + "Home '" + homePath + "' not found or you do not own it.");
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

   String currentInput = args.length > 0 ? args[args.length - 1] : "";

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
    sender.sendMessage(ChatColor.RED + "Only players can list their homes.");
    return true;
   }
   if (!player.hasPermission("nestedhomes.list")) {
    player.sendMessage(ChatColor.RED + "You do not have permission to list homes.");
    return true;
   }

   List<String> combinedHomes = new ArrayList<>();
   
   // Add personal homes
   List<String> personalHomes = getAllHomePaths(player.getUniqueId());
   personalHomes.forEach(home -> combinedHomes.add(home + ChatColor.DARK_GRAY + " (personal)"));

   // Add homes shared with this player
   Map<String, UUID> sharedHomes = getSharedHomesWithPlayer(player.getUniqueId());
   sharedHomes.forEach((homePath, sharerUUID) -> {
    OfflinePlayer sharer = Bukkit.getOfflinePlayer(sharerUUID);
    String sharerName = sharer.getName() != null ? sharer.getName() : "Unknown";
    combinedHomes.add(homePath + ChatColor.DARK_GRAY + " (shared by " + sharerName + ")");
   });

   if (combinedHomes.isEmpty()) {
    player.sendMessage(ChatColor.YELLOW + "You have not set or been shared any homes yet.");
   } else {
    player.sendMessage(ChatColor.AQUA + "--- Your Homes ---");
    Collections.sort(combinedHomes);
    combinedHomes.forEach(home -> player.sendMessage(ChatColor.GRAY + "- " + home));
    player.sendMessage(ChatColor.AQUA + "---------------------");
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
    sender.sendMessage(ChatColor.RED + "Only players can share homes.");
    return true;
   }

   if (args.length < 3) {
    sharerPlayer.sendMessage(ChatColor.YELLOW + "Usage: /sharehome <add|remove> <player_name> <home_name>");
    return true;
   }

   String action = args[0].toLowerCase();
   String targetPlayerName = args[1];
   String homePath = String.join("/", Arrays.copyOfRange(args, 2, args.length));

   OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
   if (!targetPlayer.hasPlayedBefore()) {
    sharerPlayer.sendMessage(ChatColor.RED + "Player '" + targetPlayerName + "' not found.");
    return true;
   }

   UUID sharerUUID = sharerPlayer.getUniqueId();
   UUID targetUUID = targetPlayer.getUniqueId();

   // Verify home exists and belongs to sharer
   if (getLocation(HOMES_SECTION + "." + sharerUUID.toString(), homePath) == null) {
    sharerPlayer.sendMessage(ChatColor.RED + "Your personal home '" + homePath + "' not found.");
    return true;
   }

   if ("add".equals(action)) {
    if (addSharedHomePermission(sharerUUID, targetUUID, homePath)) {
     sharerPlayer.sendMessage(ChatColor.GREEN + "Successfully shared your home '" + ChatColor.GOLD + homePath + ChatColor.GREEN + "' with " + ChatColor.YELLOW + targetPlayerName + ChatColor.GREEN + ".");
    } else {
     sharerPlayer.sendMessage(ChatColor.YELLOW + "Your home '" + homePath + "' is already shared with " + targetPlayerName + ".");
    }
   } else if ("remove".equals(action)) {
    if (removeSharedHomePermission(sharerUUID, targetUUID, homePath)) {
     sharerPlayer.sendMessage(ChatColor.GREEN + "Successfully unshared your home '" + ChatColor.GOLD + homePath + ChatColor.GREEN + "' from " + ChatColor.YELLOW + targetPlayerName + ChatColor.GREEN + ".");
    } else {
     sharerPlayer.sendMessage(ChatColor.YELLOW + "Your home '" + homePath + "' was not shared with " + targetPlayerName + ".");
    }
   } else {
    sharerPlayer.sendMessage(ChatColor.YELLOW + "Usage: /sharehome <add|remove> <player_name> <home_name>");
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
}