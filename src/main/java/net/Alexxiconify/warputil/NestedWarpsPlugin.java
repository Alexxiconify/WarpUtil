// Main plugin class: NestedWarpsPlugin.java
package net.Alexxiconify.warputil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandMap; // Import CommandMap
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager; // To access the CommandMap (though Bukkit.getCommandMap() is direct)
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID; // Import UUID for player homes
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class NestedWarpsPlugin extends org.bukkit.plugin.java.JavaPlugin {

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

  // --- Warp Commands ---
  // /warp command
  Command warpCmd = new Command("warp", "Teleports to a warp (e.g., /warp creative/build1).", "/warp [warp_name]", new ArrayList<>()) {
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    return new WarpCommandExecutor().onCommand(sender, this, commandLabel, args);
   }

   @Override
   public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    return new WarpTabCompleter().onTabComplete(sender, this, alias, args);
   }
  };
  warpCmd.setPermission("nestedwarps.warp");
  warpCmd.setPermissionMessage(ChatColor.RED + "You do not have permission to use this command.");
  commandMap.register("nestedwarps", warpCmd);

  // /setwarp command
  Command setWarpCmd = new Command("setwarp", "Sets a new warp at your current location (e.g., /setwarp creative/build1).", "/setwarp <warp_name>", new ArrayList<>()) {
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    return new SetWarpCommandExecutor().onCommand(sender, this, commandLabel, args);
   }

   @Override
   public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    return new SetWarpTabCompleter().onTabComplete(sender, this, alias, args);
   }
  };
  setWarpCmd.setPermission("nestedwarps.setwarp");
  setWarpCmd.setPermissionMessage(ChatColor.RED + "You do not have permission to set warps.");
  commandMap.register("nestedwarps", setWarpCmd);

  // /delwarp command
  Command delWarpCmd = new Command("delwarp", "Deletes an existing warp (e.g., /delwarp creative/build1).", "/delwarp <warp_name>", new ArrayList<>()) {
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    return new DelWarpCommandExecutor().onCommand(sender, this, commandLabel, args);
   }

   @Override
   public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    return new DelWarpTabCompleter().onTabComplete(sender, this, alias, args);
   }
  };
  delWarpCmd.setPermission("nestedwarps.delwarp");
  delWarpCmd.setPermissionMessage(ChatColor.RED + "You do not have permission to delete warps.");
  commandMap.register("nestedwarps", delWarpCmd);

  // /warps command
  Command warpsCmd = new Command("warps", "Lists all available warps.", "/warps", new ArrayList<>()) {
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    return new WarpsCommandExecutor().onCommand(sender, this, commandLabel, args);
   }
   @Override
   public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    return Collections.emptyList(); // No tab completion needed for /warps
   }
  };
  warpsCmd.setPermission("nestedwarps.list");
  warpsCmd.setPermissionMessage(ChatColor.RED + "You do not have permission to list warps.");
  commandMap.register("nestedwarps", warpsCmd);

  // --- Home Commands ---
  // /home command
  Command homeCmd = new Command("home", "Teleports to your personal home (e.g., /home mybase/farm).", "/home [home_name]", new ArrayList<>()) {
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    return new HomeCommandExecutor().onCommand(sender, this, commandLabel, args);
   }

   @Override
   public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    return new HomeTabCompleter().onTabComplete(sender, this, alias, args);
   }
  };
  homeCmd.setPermission("nestedhomes.home");
  homeCmd.setPermissionMessage(ChatColor.RED + "You do not have permission to use homes.");
  commandMap.register("nestedwarps", homeCmd); // Using "nestedwarps" as fallback prefix

  // /sethome command
  Command setHomeCmd = new Command("sethome", "Sets a personal home at your current location (e.g., /sethome mybase/farm).", "/sethome <home_name>", new ArrayList<>()) {
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    return new SetHomeCommandExecutor().onCommand(sender, this, commandLabel, args);
   }

   @Override
   public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    return new SetHomeTabCompleter().onTabComplete(sender, this, alias, args);
   }
  };
  setHomeCmd.setPermission("nestedhomes.sethome");
  setHomeCmd.setPermissionMessage(ChatColor.RED + "You do not have permission to set homes.");
  commandMap.register("nestedwarps", setHomeCmd);

  // /delhome command
  Command delHomeCmd = new Command("delhome", "Deletes a personal home (e.g., /delhome mybase/farm).", "/delhome <home_name>", new ArrayList<>()) {
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    return new DelHomeCommandExecutor().onCommand(sender, this, commandLabel, args);
   }

   @Override
   public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    return new DelHomeTabCompleter().onTabComplete(sender, this, alias, args);
   }
  };
  delHomeCmd.setPermission("nestedhomes.delhome");
  delHomeCmd.setPermissionMessage(ChatColor.RED + "You do not have permission to delete homes.");
  commandMap.register("nestedwarps", delHomeCmd);

  // /homes command
  Command homesCmd = new Command("homes", "Lists all your personal homes.", "/homes", new ArrayList<>()) {
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    return new HomesCommandExecutor().onCommand(sender, this, commandLabel, args);
   }
   @Override
   public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    return Collections.emptyList(); // No tab completion needed for /homes
   }
  };
  homesCmd.setPermission("nestedhomes.list");
  homesCmd.setPermissionMessage(ChatColor.RED + "You do not have permission to list homes.");
  commandMap.register("nestedwarps", homesCmd);
 }


 /**
  * Retrieves a warp location from the config.
  * Supports nested paths like "creative/build1".
  *
  * @param warpPath The path to the warp (e.g., "hub" or "creative/build1").
  * @return The Location object if found, null otherwise.
  */
 private @Nullable Location getWarpLocation(String warpPath) {
  ConfigurationSection warpsSection = getConfig().getConfigurationSection("warps");
  if (warpsSection == null) return null;

  // Replace '/' with '.' for navigating config sections, as Bukkit config uses dots for nesting
  ConfigurationSection warpData = warpsSection.getConfigurationSection(warpPath.replace("/", "."));

  if (warpData == null) {
   return null; // Warp path not found
  }

  try {
   String worldName = warpData.getString("world");
   if (worldName == null) {
    // This means the path might be a folder that does not itself contain a warp,
    // or a malformed warp entry. A valid warp must have a world defined.
    return null;
   }
   World world = Bukkit.getWorld(worldName);
   if (world == null) {
    getLogger().warning("World '" + worldName + "' for warp '" + warpPath + "' not found! This warp may be invalid.");
    return null;
   }

   double x = warpData.getDouble("x");
   double y = warpData.getDouble("y");
   double z = warpData.getDouble("z");
   float yaw = (float) warpData.getDouble("yaw");
   float pitch = (float) warpData.getDouble("pitch");

   return new Location(world, x, y, z, yaw, pitch);
  } catch (Exception e) {
   getLogger().severe("Error loading warp '" + warpPath + "': " + e.getMessage());
   return null;
  }
 }

 /**
  * Saves a warp location to the config.
  * Supports nested paths like "creative/build1".
  *
  * @param warpPath The path to the warp.
  * @param location The Location object to save.
  */
 private void saveWarpLocation(String warpPath, @NotNull Location location) {
  // Replace '/' with '.' for navigating config sections, as Bukkit config uses dots for nesting
  String configPath = "warps." + warpPath.replace("/", ".");

  getConfig().set(configPath + ".world", location.getWorld().getName());
  getConfig().set(configPath + ".x", location.getX());
  getConfig().set(configPath + ".y", location.getY());
  getConfig().set(configPath + ".z", location.getZ());
  getConfig().set(configPath + ".yaw", location.getYaw());
  getConfig().set(configPath + ".pitch", location.getPitch());
  saveConfig();
 }

 /**
  * Deletes a warp location from the config.
  * Supports nested paths like "creative/build1".
  *
  * @param warpPath The path to the warp to delete.
  * @return true if the warp was found and deleted, false otherwise.
  */
 private boolean deleteWarpLocation(String warpPath) {
  String configPath = "warps." + warpPath.replace("/", ".");
  if (getConfig().contains(configPath)) {
   getConfig().set(configPath, null); // Set to null to remove the section from config
   saveConfig();
   return true;
  }
  return false;
 }

 /**
  * Recursively gets all full warp paths (e.g., "creative/build1", "hub").
  * A warp path is considered valid if its ConfigurationSection contains "world", "x", "y", "z" keys.
  *
  * @param section The current configuration section to search.
  * @param currentPath The path built so far (e.g., "creative").
  * @return A list of full warp paths.
  */
 private @NotNull List<String> getAllWarpPathsRecursive(@Nullable ConfigurationSection section, String currentPath) {
  List<String> warpPaths = new ArrayList<>();
  if (section == null) return warpPaths;

  for (String key : section.getKeys(false)) { // Iterate over direct children only (not nested keys)
   String newPath = currentPath.isEmpty() ? key : currentPath + "/" + key;
   ConfigurationSection childSection = section.getConfigurationSection(key);

   if (childSection != null) {
    // Check if this childSection itself contains warp data.
    // If it does, 'newPath' represents a complete warp.
    if (childSection.contains("world") && childSection.contains("x") &&
            childSection.contains("y") && childSection.contains("z")) {
     warpPaths.add(newPath); // Add the full path of this warp
    }
    // Recursively search for warps inside this child section (which acts as a folder)
    warpPaths.addAll(getAllWarpPathsRecursive(childSection, newPath));
   }
   // If childSection is null, it means 'key' points to a primitive value (e.g., "world", "x", "y", "z", "yaw", "pitch")
   // within the 'section'. These primitive keys do not represent full warp paths themselves,
   // so we do not add them to the list of warpPaths.
  }
  return warpPaths;
 }

 /**
  * Retrieves a home location for a specific player from the config.
  * Supports nested paths like "mybase/farm".
  * Homes are stored under `homes.<player_uuid>.<home_path>`.
  *
  * @param playerUUID The UUID of the player.
  * @param homePath The path to the home (e.g., "mybase" or "mybase/farm").
  * @return The Location object if found, null otherwise.
  */
 private @Nullable Location getHomeLocation(@NotNull UUID playerUUID, String homePath) {
  ConfigurationSection playerHomesSection = getConfig().getConfigurationSection("homes." + playerUUID.toString());
  if (playerHomesSection == null) return null;

  ConfigurationSection homeData = playerHomesSection.getConfigurationSection(homePath.replace("/", "."));
  if (homeData == null) {
   return null; // Home path not found for this player
  }

  try {
   String worldName = homeData.getString("world");
   if (worldName == null) return null;
   World world = Bukkit.getWorld(worldName);
   if (world == null) {
    getLogger().warning("World '" + worldName + "' for home '" + homePath + "' of player " + playerUUID + " not found!");
    return null;
   }

   double x = homeData.getDouble("x");
   double y = homeData.getDouble("y");
   double z = homeData.getDouble("z");
   float yaw = (float) homeData.getDouble("yaw");
   float pitch = (float) homeData.getDouble("pitch");

   return new Location(world, x, y, z, yaw, pitch);
  } catch (Exception e) {
   getLogger().severe("Error loading home '" + homePath + "' for player " + playerUUID + ": " + e.getMessage());
   return null;
  }
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
  String configPath = "homes." + playerUUID.toString() + "." + homePath.replace("/", ".");

  getConfig().set(configPath + ".world", location.getWorld().getName());
  getConfig().set(configPath + ".x", location.getX());
  getConfig().set(configPath + ".y", location.getY());
  getConfig().set(configPath + ".z", location.getZ());
  getConfig().set(configPath + ".yaw", location.getYaw());
  getConfig().set(configPath + ".pitch", location.getPitch());
  saveConfig();
 }

 /**
  * Deletes a home location for a specific player from the config.
  * Supports nested paths like "mybase/farm".
  * Homes are stored under `homes.<player_uuid>.<home_path>`.
  *
  * @param playerUUID The UUID of the player.
  * @param homePath The path to the home to delete.
  * @return true if the home was found and deleted, false otherwise.
  */
 private boolean deleteHomeLocation(@NotNull UUID playerUUID, String homePath) {
  String configPath = "homes." + playerUUID.toString() + "." + homePath.replace("/", ".");
  if (getConfig().contains(configPath)) {
   getConfig().set(configPath, null); // Set to null to remove the section
   saveConfig();
   return true;
  }
  return false;
 }

 /**
  * Recursively gets all full home paths for a specific player.
  * A home path is considered valid if its ConfigurationSection contains "world", "x", "y", "z" keys.
  *
  * @param playerUUID The UUID of the player.
  * @param section The current configuration section to search (starts from player's homes root).
  * @param currentPath The path built so far (e.g., "mybase").
  * @return A list of full home paths for the player.
  */
 private @NotNull List<String> getAllHomePathsRecursive(@NotNull UUID playerUUID, @Nullable ConfigurationSection section, String currentPath) {
  List<String> homePaths = new ArrayList<>();
  if (section == null) return homePaths;

  for (String key : section.getKeys(false)) {
   String newPath = currentPath.isEmpty() ? key : currentPath + "/" + key;
   ConfigurationSection childSection = section.getConfigurationSection(key);

   if (childSection != null) {
    if (childSection.contains("world") && childSection.contains("x") &&
            childSection.contains("y") && childSection.contains("z")) {
     homePaths.add(newPath);
    }
    homePaths.addAll(getAllHomePathsRecursive(playerUUID, childSection, newPath));
   }
  }
  return homePaths;
 }


 /**
  * Inner class for /warp command logic.
  */
 private class WarpCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    sender.sendMessage(ChatColor.RED + "Only players can use the /warp command.");
    return true;
   }

   if (args.length == 0) {
    player.sendMessage(ChatColor.YELLOW + "Usage: /warp <warp_name>");
    player.sendMessage(ChatColor.YELLOW + "Example: /warp creative/build1 or /warp hub");
    player.sendMessage(ChatColor.YELLOW + "Use /warps to see all available warps.");
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

   List<String> completions = new ArrayList<>();
   ConfigurationSection warpsSection = getConfig().getConfigurationSection("warps");
   if (warpsSection == null) return Collections.emptyList();

   String currentInputPart = args[args.length - 1].toLowerCase();

   ConfigurationSection sectionToSearch = warpsSection;
   for (int i = 0; i < args.length - 1; i++) {
    if (sectionToSearch == null) break;
    sectionToSearch = sectionToSearch.getConfigurationSection(args[i]);
   }

   if (sectionToSearch == null) return Collections.emptyList();

   for (String key : sectionToSearch.getKeys(false)) {
    if (key.toLowerCase().startsWith(currentInputPart)) {
     ConfigurationSection childSection = sectionToSearch.getConfigurationSection(key);
     if (childSection != null) {
      boolean isActualWarp = childSection.contains("world") && childSection.contains("x") &&
              childSection.contains("y") && childSection.contains("z");

      if (isActualWarp) {
       completions.add(key);
      }

      if (childSection.getKeys(false).size() > 0) { // Check if it has any children keys
       completions.add(key + "/");
      }
     }
    }
   }
   return completions.stream().distinct().collect(Collectors.toList());
  }
 }

 /**
  * Inner class for /setwarp command logic.
  */
 private class SetWarpCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    sender.sendMessage(ChatColor.RED + "Only players can use the /setwarp command.");
    return true;
   }

   if (!player.hasPermission("nestedwarps.setwarp")) {
    player.sendMessage(ChatColor.RED + "You do not have permission to set warps.");
    return true;
   }

   if (args.length == 0) {
    player.sendMessage(ChatColor.YELLOW + "Usage: /setwarp <warp_name>");
    player.sendMessage(ChatColor.YELLOW + "Example: /setwarp creative/build1 or /setwarp hub");
    return true;
   }

   String warpPath = String.join("/", args);
   saveWarpLocation(warpPath, player.getLocation());
   player.sendMessage(ChatColor.GREEN + "Warp '" + ChatColor.GOLD + warpPath + ChatColor.GREEN + "' set successfully!");
   return true;
  }
 }

 /**
  * Inner class for /setwarp tab completion logic.
  */
 private class SetWarpTabCompleter implements TabCompleter {
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
   if (!sender.hasPermission("nestedwarps.setwarp")) {
    return Collections.emptyList();
   }

   if (args.length == 0) return Collections.emptyList();

   List<String> completions = new ArrayList<>();
   List<String> allExistingWarpPaths = getAllWarpPathsRecursive(getConfig().getConfigurationSection("warps"), "");
   String currentInputPart = args[args.length - 1].toLowerCase();

   for (String existingWarp : allExistingWarpPaths) {
    if (existingWarp.toLowerCase().startsWith(currentInputPart)) {
     completions.add(existingWarp);
    }
   }
   return completions.stream().distinct().collect(Collectors.toList());
  }
 }

 /**
  * Inner class for /delwarp command logic.
  */
 private class DelWarpCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!sender.hasPermission("nestedwarps.delwarp")) {
    sender.sendMessage(ChatColor.RED + "You do not have permission to delete warps.");
    return true;
   }

   if (args.length == 0) {
    sender.sendMessage(ChatColor.YELLOW + "Usage: /delwarp <warp_name>");
    sender.sendMessage(ChatColor.YELLOW + "Example: /delwarp creative/build1 or /delwarp hub");
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

 /**
  * Inner class for /delwarp tab completion logic.
  */
 private class DelWarpTabCompleter implements TabCompleter {
  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
   if (!sender.hasPermission("nestedwarps.delwarp")) {
    return Collections.emptyList();
   }

   List<String> completions = new ArrayList<>();
   ConfigurationSection warpsSection = getConfig().getConfigurationSection("warps");
   if (warpsSection == null) return Collections.emptyList();

   String currentInputPart = args[args.length - 1].toLowerCase();

   ConfigurationSection sectionToSearch = warpsSection;
   for (int i = 0; i < args.length - 1; i++) {
    if (sectionToSearch == null) break;
    sectionToSearch = sectionToSearch.getConfigurationSection(args[i]);
   }

   if (sectionToSearch == null) return Collections.emptyList();

   for (String key : sectionToSearch.getKeys(false)) {
    if (key.toLowerCase().startsWith(currentInputPart)) {
     ConfigurationSection childSection = sectionToSearch.getConfigurationSection(key);
     if (childSection != null) {
      boolean isActualWarp = childSection.contains("world") && childSection.contains("x") &&
              childSection.contains("y") && childSection.contains("z");

      if (isActualWarp) {
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
  * Inner class for /warps command logic.
  */
 private class WarpsCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!sender.hasPermission("nestedwarps.list")) {
    sender.sendMessage(ChatColor.RED + "You do not have permission to list warps.");
    return true;
   }

   List<String> allWarps = getAllWarpPathsRecursive(getConfig().getConfigurationSection("warps"), "");

   if (allWarps.isEmpty()) {
    sender.sendMessage(ChatColor.YELLOW + "No warps have been set yet.");
   } else {
    sender.sendMessage(ChatColor.AQUA + "--- Available Warps ---");
    Collections.sort(allWarps);
    for (String warp : allWarps) {
     sender.sendMessage(ChatColor.GRAY + "- " + warp);
    }
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
    sender.sendMessage(ChatColor.RED + "Only players can use the /home command.");
    return true;
   }

   if (args.length == 0) {
    player.sendMessage(ChatColor.YELLOW + "Usage: /home <home_name>");
    player.sendMessage(ChatColor.YELLOW + "Example: /home mybase/farm or /home default");
    player.sendMessage(ChatColor.YELLOW + "Use /homes to see all your available homes.");
    return true;
   }

   String homePath = String.join("/", args);
   UUID playerUUID = player.getUniqueId();

   if (!player.hasPermission("nestedhomes.home") && !player.hasPermission("nestedhomes.home." + homePath.replace("/", "."))) {
    player.sendMessage(ChatColor.RED + "You do not have permission to teleport to homes.");
    return true;
   }

   Location homeLocation = getHomeLocation(playerUUID, homePath);
   if (homeLocation != null) {
    player.teleport(homeLocation);
    player.sendMessage(ChatColor.GREEN + "Teleported to home: " + ChatColor.GOLD + homePath);
   } else {
    player.sendMessage(ChatColor.RED + "Home '" + homePath + "' not found.");
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
   ConfigurationSection playerHomesRoot = getConfig().getConfigurationSection("homes." + player.getUniqueId().toString());
   if (playerHomesRoot == null) return Collections.emptyList();

   String currentInputPart = args[args.length - 1].toLowerCase();

   ConfigurationSection sectionToSearch = playerHomesRoot;
   for (int i = 0; i < args.length - 1; i++) {
    if (sectionToSearch == null) break;
    sectionToSearch = sectionToSearch.getConfigurationSection(args[i]);
   }

   if (sectionToSearch == null) return Collections.emptyList();

   for (String key : sectionToSearch.getKeys(false)) {
    if (key.toLowerCase().startsWith(currentInputPart)) {
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
  * Inner class for /sethome command logic.
  */
 private class SetHomeCommandExecutor implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
   if (!(sender instanceof Player player)) {
    sender.sendMessage(ChatColor.RED + "Only players can use the /sethome command.");
    return true;
   }

   if (!player.hasPermission("nestedhomes.sethome")) {
    player.sendMessage(ChatColor.RED + "You do not have permission to set homes.");
    return true;
   }

   if (args.length == 0) {
    player.sendMessage(ChatColor.YELLOW + "Usage: /sethome <home_name>");
    player.sendMessage(ChatColor.YELLOW + "Example: /sethome mybase/farm or /sethome default");
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
   ConfigurationSection playerHomesRoot = getConfig().getConfigurationSection("homes." + player.getUniqueId().toString());
   List<String> allExistingHomePaths = getAllHomePathsRecursive(player.getUniqueId(), playerHomesRoot, "");
   String currentInputPart = args[args.length - 1].toLowerCase();

   for (String existingHome : allExistingHomePaths) {
    if (existingHome.toLowerCase().startsWith(currentInputPart)) {
     completions.add(existingHome);
    }
   }
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
    sender.sendMessage(ChatColor.RED + "Only players can use the /delhome command.");
    return true;
   }

   if (!player.hasPermission("nestedhomes.delhome")) {
    player.sendMessage(ChatColor.RED + "You do not have permission to delete homes.");
    return true;
   }

   if (args.length == 0) {
    player.sendMessage(ChatColor.YELLOW + "Usage: /delhome <home_name>");
    player.sendMessage(ChatColor.YELLOW + "Example: /delhome mybase/farm or /delhome default");
    return true;
   }

   String homePath = String.join("/", args);
   UUID playerUUID = player.getUniqueId();

   if (deleteHomeLocation(playerUUID, homePath)) {
    player.sendMessage(ChatColor.GREEN + "Home '" + ChatColor.GOLD + homePath + ChatColor.GREEN + "' deleted successfully!");
   } else {
    player.sendMessage(ChatColor.RED + "Home '" + homePath + "' not found.");
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
   ConfigurationSection playerHomesRoot = getConfig().getConfigurationSection("homes." + player.getUniqueId().toString());
   if (playerHomesRoot == null) return Collections.emptyList();

   String currentInputPart = args[args.length - 1].toLowerCase();

   ConfigurationSection sectionToSearch = playerHomesRoot;
   for (int i = 0; i < args.length - 1; i++) {
    if (sectionToSearch == null) break;
    sectionToSearch = sectionToSearch.getConfigurationSection(args[i]);
   }

   if (sectionToSearch == null) return Collections.emptyList();

   for (String key : sectionToSearch.getKeys(false)) {
    if (key.toLowerCase().startsWith(currentInputPart)) {
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

   ConfigurationSection playerHomesRoot = getConfig().getConfigurationSection("homes." + player.getUniqueId().toString());
   List<String> allHomes = getAllHomePathsRecursive(player.getUniqueId(), playerHomesRoot, "");

   if (allHomes.isEmpty()) {
    player.sendMessage(ChatColor.YELLOW + "You have not set any homes yet.");
   } else {
    player.sendMessage(ChatColor.AQUA + "--- Your Homes ---");
    Collections.sort(allHomes);
    for (String home : allHomes) {
     player.sendMessage(ChatColor.GRAY + "- " + home);
    }
    player.sendMessage(ChatColor.AQUA + "---------------------");
   }
   return true;
  }
 }
}