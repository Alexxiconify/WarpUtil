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

  // --- Share Home Commands ---
  // /sharehome command
  Command shareHomeCmd = new Command("sharehome", "Shares your home with another player or revokes access.", "/sharehome <add|remove> <player_name> <home_name>", new ArrayList<>()) {
   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
    return new ShareHomeCommandExecutor().onCommand(sender, this, commandLabel, args);
   }

   @Override
   public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
    return new ShareHomeTabCompleter().onTabComplete(sender, this, alias, args);
   }
  };
  shareHomeCmd.setPermission("nestedhomes.share");
  shareHomeCmd.setPermissionMessage(ChatColor.RED + "You do not have permission to share homes.");
  commandMap.register("nestedwarps", shareHomeCmd);
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
  * It checks personal homes first, then homes shared by others.
  *
  * @param playerUUID The UUID of the player.
  * @param homePath The path to the home (e.g., "mybase/farm").
  * @return The Location object if found, null otherwise.
  */
 private @Nullable Location getHomeLocation(@NotNull UUID playerUUID, String homePath) {
  // 1. Check personal homes first
  ConfigurationSection playerHomesSection = getConfig().getConfigurationSection("homes." + playerUUID.toString());
  if (playerHomesSection != null) {
   ConfigurationSection personalHomeData = playerHomesSection.getConfigurationSection(homePath.replace("/", "."));
   if (personalHomeData != null && personalHomeData.contains("world")) {
    return loadLocationFromSection(personalHomeData, homePath);
   }
  }

  // 2. If not a personal home, check homes shared with this player
  ConfigurationSection sharedHomesSection = getConfig().getConfigurationSection("shared_homes");
  if (sharedHomesSection != null) {
   for (String sharerUUIDStr : sharedHomesSection.getKeys(false)) {
    ConfigurationSection sharerSection = sharedHomesSection.getConfigurationSection(sharerUUIDStr);
    if (sharerSection != null) {
     ConfigurationSection targetPlayerSection = sharerSection.getConfigurationSection(playerUUID.toString());
     if (targetPlayerSection != null && targetPlayerSection.contains(homePath.replace("/", "."))) {
      // This home is shared with the target player. Now get the actual location from the sharer's personal homes.
      UUID sharerUUID = UUID.fromString(sharerUUIDStr);
      ConfigurationSection actualSharerHomeData = getConfig().getConfigurationSection("homes." + sharerUUID.toString() + "." + homePath.replace("/", "."));
      if (actualSharerHomeData != null && actualSharerHomeData.contains("world")) {
       return loadLocationFromSection(actualSharerHomeData, homePath);
      }
     }
    }
   }
  }

  return null;
 }

 /**
  * Helper method to load a Location from a ConfigurationSection.
  */
 private @Nullable Location loadLocationFromSection(@NotNull ConfigurationSection section, String pathForLogging) {
  try {
   String worldName = section.getString("world");
   if (worldName == null) return null;
   World world = Bukkit.getWorld(worldName);
   if (world == null) {
    getLogger().warning("World '" + worldName + "' for path '" + pathForLogging + "' not found! This entry may be invalid.");
    return null;
   }

   double x = section.getDouble("x");
   double y = section.getDouble("y");
   double z = section.getDouble("z");
   float yaw = (float) section.getDouble("yaw");
   float pitch = (float) section.getDouble("pitch");

   return new Location(world, x, y, z, yaw, pitch);
  } catch (Exception e) {
   getLogger().severe("Error loading location for path '" + pathForLogging + "': " + e.getMessage());
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
  * Also removes any sharing entries for this home.
  *
  * @param playerUUID The UUID of the player.
  * @param homePath The path to the home to delete.
  * @return true if the home was found and deleted, false otherwise.
  */
 private boolean deleteHomeLocation(@NotNull UUID playerUUID, String homePath) {
  String configPath = "homes." + playerUUID.toString() + "." + homePath.replace("/", ".");
  if (getConfig().contains(configPath)) {
   getConfig().set(configPath, null); // Set to null to remove the section
   removeSharingEntriesForHome(playerUUID, homePath); // Remove any sharing entries
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
  * Adds a permission for a player's home to be shared with another player.
  *
  * @param sharerUUID The UUID of the player who owns the home.
  * @param targetUUID The UUID of the player with whom the home is shared.
  * @param homePath The path to the home being shared.
  * @return true if added, false if already shared or cannot be added.
  */
 private boolean addSharedHomePermission(@NotNull UUID sharerUUID, @NotNull UUID targetUUID, @NotNull String homePath) {
  String configPath = "shared_homes." + sharerUUID.toString() + "." + targetUUID.toString() + "." + homePath.replace("/", ".");
  if (!getConfig().contains(configPath)) {
   getConfig().set(configPath, true); // Use a boolean value to indicate presence
   saveConfig();
   return true;
  }
  return false;
 }

 /**
  * Removes a permission for a player's home to be shared with another player.
  *
  * @param sharerUUID The UUID of the player who owns the home.
  * @param targetUUID The UUID of the player from whom the home is unshared.
  * @param homePath The path to the home being unshared.
  * @return true if removed, false if not found.
  */
 private boolean removeSharedHomePermission(@NotNull UUID sharerUUID, @NotNull UUID targetUUID, @NotNull String homePath) {
  String configPath = "shared_homes." + sharerUUID.toString() + "." + targetUUID.toString() + "." + homePath.replace("/", ".");
  if (getConfig().contains(configPath)) {
   getConfig().set(configPath, null); // Remove the entry
   saveConfig();
   return true;
  }
  return false;
 }

 /**
  * Removes all sharing entries for a specific home that has been deleted.
  *
  * @param sharerUUID The UUID of the player whose home was deleted.
  * @param homePath The path to the home that was deleted.
  */
 private void removeSharingEntriesForHome(@NotNull UUID sharerUUID, @NotNull String homePath) {
  ConfigurationSection sharerSharedHomes = getConfig().getConfigurationSection("shared_homes." + sharerUUID.toString());
  if (sharerSharedHomes != null) {
   // Iterate over all target players
   for (String targetUUIDStr : sharerSharedHomes.getKeys(false)) {
    ConfigurationSection targetSection = sharerSharedHomes.getConfigurationSection(targetUUIDStr);
    if (targetSection != null && targetSection.contains(homePath.replace("/", "."))) {
     targetSection.set(homePath.replace("/", "."), null);
    }
   }
  }
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
  ConfigurationSection sharedHomesRoot = getConfig().getConfigurationSection("shared_homes");
  if (sharedHomesRoot == null) return sharedHomes;

  for (String sharerUUIDStr : sharedHomesRoot.getKeys(false)) {
   ConfigurationSection sharerSection = sharedHomesRoot.getConfigurationSection(sharerUUIDStr);
   if (sharerSection != null) {
    ConfigurationSection targetPlayerSection = sharerSection.getConfigurationSection(targetUUID.toString());
    if (targetPlayerSection != null) {
     // Recursively get all shared home paths for this specific target player from this sharer
     // Note: Here, the recursive call is used to get the paths that are explicitly marked as true under
     // 'shared_homes.<sharer_uuid>.<target_uuid>.<home_path>'
     // The actual location still needs to be retrieved from the sharer's 'homes' section.
     List<String> paths = new ArrayList<>();
     // Instead of getAllHomePathsRecursive, we need to iterate directly over the entries in targetPlayerSection
     // which represent the shared homes.
     for (String homeKey : targetPlayerSection.getKeys(false)) {
      // Reconstruct full path for the map key. This assumes 'homeKey' is already the correct nested path.
      // However, to ensure it's a valid home path that was actually shared, we still need to check
      // if the original home exists for the sharer.
      UUID sharerUUID = UUID.fromString(sharerUUIDStr);
      if (getHomeLocation(sharerUUID, homeKey.replace(".", "/")) != null) { // Validate existence
       paths.add(homeKey.replace(".", "/"));
      }
     }

     for (String path : paths) {
      // For display, we use the sharer's name and the home path
      OfflinePlayer sharer = Bukkit.getOfflinePlayer(UUID.fromString(sharerUUIDStr));
      String sharerName = sharer.getName() != null ? sharer.getName() : sharerUUIDStr.substring(0, 8); // Use part of UUID if name not found
      sharedHomes.put(sharerName + "/" + path, UUID.fromString(sharerUUIDStr));
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

   if (!player.hasPermission("nestedhomes.home")) { // Check general home permission
    player.sendMessage(ChatColor.RED + "You do not have permission to teleport to homes.");
    return true;
   }

   Location homeLocation = getHomeLocation(playerUUID, homePath);
   if (homeLocation != null) {
    player.teleport(homeLocation);
    // Check if it's a personal home or shared home for message clarity
    ConfigurationSection personalHomesSection = getConfig().getConfigurationSection("homes." + playerUUID.toString());
    if (personalHomesSection != null && personalHomesSection.contains(homePath.replace("/", "."))) {
     player.sendMessage(ChatColor.GREEN + "Teleported to your personal home: " + ChatColor.GOLD + homePath);
    } else {
     // This implies it's a shared home
     player.sendMessage(ChatColor.GREEN + "Teleported to shared home: " + ChatColor.GOLD + homePath);
    }

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
   String currentInputPart = args[args.length - 1].toLowerCase();

   // --- Add personal home completions ---
   ConfigurationSection playerHomesRoot = getConfig().getConfigurationSection("homes." + player.getUniqueId().toString());
   if (playerHomesRoot != null) {
    ConfigurationSection sectionToSearch = playerHomesRoot;
    for (int i = 0; i < args.length - 1; i++) {
     if (sectionToSearch == null) break;
     sectionToSearch = sectionToSearch.getConfigurationSection(args[i]);
    }

    if (sectionToSearch != null) {
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
    }
   }

   // --- Add shared home completions ---
   Map<String, UUID> sharedHomes = getSharedHomesWithPlayer(player.getUniqueId());
   for (Map.Entry<String, UUID> entry : sharedHomes.entrySet()) {
    String fullSharedHomePath = entry.getKey(); // e.g., "sharer_name/myhome/nest"
    String[] pathParts = fullSharedHomePath.split("/");

    // Check if the current input is matching a shared home's structure
    boolean pathMatches = true;
    if (args.length -1 < pathParts.length) { // Ensure there are enough parts in shared home path
     for (int i = 0; i < args.length - 1; i++) {
      if (!args[i].equalsIgnoreCase(pathParts[i])) {
       pathMatches = false;
       break;
      }
     }
    } else {
     pathMatches = false; // Input is longer than a shared home path
    }


    if (pathMatches) {
     // Only add the next part of the shared home path if it starts with current input
     if (args.length <= pathParts.length && pathParts[args.length -1].toLowerCase().startsWith(currentInputPart)) {
      completions.add(pathParts[args.length -1]);
     }
    } else if (args.length == 1 && fullSharedHomePath.toLowerCase().startsWith(currentInputPart)) {
     // For the first argument, if it's matching the beginning of a shared home path (sharer_name)
     completions.add(pathParts[0]); // Add the top-level part (sharer name)
     if (pathParts.length > 1) {
      completions.add(pathParts[0] + "/"); // Suggest going into folder
     }
    }
   }


   // Filter completions based on what's being typed
   return completions.stream()
           .filter(s -> s.toLowerCase().startsWith(currentInputPart))
           .distinct()
           .collect(Collectors.toList());
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
  * Inner class for /warps command logic.
  */

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
   ConfigurationSection playerHomesRoot = getConfig().getConfigurationSection("homes." + player.getUniqueId().toString());
   List<String> personalHomes = getAllHomePathsRecursive(player.getUniqueId(), playerHomesRoot, "");
   for (String home : personalHomes) {
    combinedHomes.add(home + ChatColor.DARK_GRAY + " (personal)");
   }

   // Add homes shared with this player
   Map<String, UUID> sharedHomesMap = getSharedHomesWithPlayer(player.getUniqueId());
   for (Map.Entry<String, UUID> entry : sharedHomesMap.entrySet()) {
    OfflinePlayer sharer = Bukkit.getOfflinePlayer(entry.getValue());
    String sharerName = sharer.getName() != null ? sharer.getName() : "Unknown";
    combinedHomes.add(entry.getKey() + ChatColor.DARK_GRAY + " (shared by " + sharerName + ")");
   }


   if (combinedHomes.isEmpty()) {
    player.sendMessage(ChatColor.YELLOW + "You have not set or been shared any homes yet.");
   } else {
    player.sendMessage(ChatColor.AQUA + "--- Your Homes ---");
    Collections.sort(combinedHomes); // Sort the combined list
    for (String home : combinedHomes) {
     player.sendMessage(ChatColor.GRAY + "- " + home);
    }
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

   if (!sharerPlayer.hasPermission("nestedhomes.share")) {
    sharerPlayer.sendMessage(ChatColor.RED + "You do not have permission to share homes.");
    return true;
   }

   if (args.length < 3) {
    sharerPlayer.sendMessage(ChatColor.YELLOW + "Usage: /sharehome <add|remove> <player_name> <home_name>");
    sharerPlayer.sendMessage(ChatColor.YELLOW + "Example: /sharehome add Notch mybase/farm");
    return true;
   }

   String action = args[0].toLowerCase(); // "add" or "remove"
   String targetPlayerName = args[1];
   String homePath = String.join("/", Arrays.copyOfRange(args, 2, args.length)); // Get full home path

   OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(targetPlayerName);
   if (!targetOfflinePlayer.hasPlayedBefore() && targetOfflinePlayer.getUniqueId() == null) {
    sharerPlayer.sendMessage(ChatColor.RED + "Player '" + targetPlayerName + "' not found. They must have joined the server before.");
    return true;
   }

   UUID sharerUUID = sharerPlayer.getUniqueId();
   UUID targetUUID = targetOfflinePlayer.getUniqueId();

   // First, ensure the home exists and belongs to the sharer
   // We need to use a direct home path lookup for the owner's home, not the recursive getHomeLocation.
   // The getHomeLocation includes shared homes, which is not what we want to validate for ownership.
   ConfigurationSection sharerHomesSection = getConfig().getConfigurationSection("homes." + sharerUUID.toString());
   Location homeLocation = null;
   if (sharerHomesSection != null) {
    ConfigurationSection personalHomeData = sharerHomesSection.getConfigurationSection(homePath.replace("/", "."));
    if (personalHomeData != null && personalHomeData.contains("world")) {
     homeLocation = loadLocationFromSection(personalHomeData, homePath);
    }
   }

   if (homeLocation == null) {
    sharerPlayer.sendMessage(ChatColor.RED + "Your personal home '" + homePath + "' not found or you do not own it.");
    return true;
   }

   if (action.equals("add")) {
    if (addSharedHomePermission(sharerUUID, targetUUID, homePath)) {
     sharerPlayer.sendMessage(ChatColor.GREEN + "Successfully shared your home '" + ChatColor.GOLD + homePath + ChatColor.GREEN + "' with " + ChatColor.YELLOW + targetPlayerName + ChatColor.GREEN + ".");
    } else {
     sharerPlayer.sendMessage(ChatColor.YELLOW + "Your home '" + homePath + "' is already shared with " + targetPlayerName + ".");
    }
   } else if (action.equals("remove")) {
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
   String currentInputPart = args[args.length - 1].toLowerCase();

   if (args.length == 1) { // Completing "add" or "remove"
    if ("add".startsWith(currentInputPart)) completions.add("add");
    if ("remove".startsWith(currentInputPart)) completions.add("remove");
   } else if (args.length == 2) { // Completing player names
    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
     if (!onlinePlayer.getUniqueId().equals(player.getUniqueId()) && onlinePlayer.getName().toLowerCase().startsWith(currentInputPart)) {
      completions.add(onlinePlayer.getName());
     }
    }
   } else if (args.length >= 3) { // Completing home names
    String action = args[0].toLowerCase();
    String targetPlayerName = args[1]; // Not directly used for completion, but part of context

    // Get personal homes of the sender
    ConfigurationSection playerHomesRoot = getConfig().getConfigurationSection("homes." + player.getUniqueId().toString());
    if (playerHomesRoot != null) {
     // Reconstruct the path for recursive search
     String partialHomePath = String.join("/", Arrays.copyOfRange(args, 2, args.length - 1));
     ConfigurationSection sectionToSearch = playerHomesRoot;
     for (String argPart : Arrays.copyOfRange(args, 2, args.length -1)) {
      if (sectionToSearch == null) break;
      sectionToSearch = sectionToSearch.getConfigurationSection(argPart);
     }

     if (sectionToSearch != null) {
      for (String key : sectionToSearch.getKeys(false)) {
       if (key.toLowerCase().startsWith(currentInputPart)) {
        ConfigurationSection childSection = sectionToSearch.getConfigurationSection(key);
        if (childSection != null && childSection.contains("world") && childSection.contains("x")) { // Is an actual home
         completions.add(key);
        }
        if (childSection != null && childSection.getKeys(false).size() > 0) { // Is a folder
         completions.add(key + "/");
        }
       }
      }
     }
    }
   }
   return completions.stream().distinct().collect(Collectors.toList());
  }
 }
}