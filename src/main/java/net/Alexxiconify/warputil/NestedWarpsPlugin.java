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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull; // Added for @NotNull annotations

import java.util.ArrayList;
import java.util.Collections; // Added for sorting
import java.util.List;
import java.util.Objects; // Added for Objects.requireNonNull
import java.util.stream.Collectors;

@SuppressWarnings("ALL") // Keeping this annotation if the user prefers it
public class Warputil extends JavaPlugin implements CommandExecutor, TabCompleter { // Changed class name for consistency

 @Override
 public void onEnable() {
  // Save the default config.yml if it doesn't exist.
  // This ensures the 'warps' section is available from the start.
  saveDefaultConfig();

  // Register commands and their executors/tab completers
  // Using Objects.requireNonNull to ensure commands are properly registered
  Objects.requireNonNull(getCommand("warp")).setExecutor(this);
  Objects.requireNonNull(getCommand("warp")).setTabCompleter(this);

  Objects.requireNonNull(getCommand("setwarp")).setExecutor(this);
  Objects.requireNonNull(getCommand("setwarp")).setTabCompleter(this);

  Objects.requireNonNull(getCommand("delwarp")).setExecutor(this);
  Objects.requireNonNull(getCommand("delwarp")).setTabCompleter(this);

  Objects.requireNonNull(getCommand("warps")).setExecutor(this);

  getLogger().info("NestedWarps plugin enabled!");
 }

 @Override
 public void onDisable() {
  getLogger().info("NestedWarps plugin disabled!");
 }

 @Override
 public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
  // Handle the /warp command
  if (command.getName().equalsIgnoreCase("warp")) {
   if (!(sender instanceof Player player)) { // Modern Java pattern matching for instanceof
    sender.sendMessage(ChatColor.RED + "Only players can use the /warp command.");
    return true;
   }

   if (args.length == 0) {
    // Show usage or list available warps if no arguments are provided
    player.sendMessage(ChatColor.YELLOW + "Usage: /warp <warp_name>");
    player.sendMessage(ChatColor.YELLOW + "Example: /warp creative/build1 or /warp hub");
    player.sendMessage(ChatColor.YELLOW + "Use /warps to see all available warps.");
    return true;
   }

   String warpPath = String.join("/", args); // Joins arguments to form the full path

   // Check for general warp permission or specific warp permission
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

  // Handle the /setwarp command
  if (command.getName().equalsIgnoreCase("setwarp")) {
   if (!(sender instanceof Player player)) { // Modern Java pattern matching for instanceof
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

  // Handle the /delwarp command
  if (command.getName().equalsIgnoreCase("delwarp")) {
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

  // Handle the /warps command
  if (command.getName().equalsIgnoreCase("warps")) {
   if (!sender.hasPermission("nestedwarps.list")) {
    sender.sendMessage(ChatColor.RED + "You do not have permission to list warps.");
    return true;
   }

   List<String> allWarps = getAllWarpPathsRecursive(getConfig().getConfigurationSection("warps"), "");

   if (allWarps.isEmpty()) {
    sender.sendMessage(ChatColor.YELLOW + "No warps have been set yet.");
   } else {
    sender.sendMessage(ChatColor.AQUA + "--- Available Warps ---");
    // Sort warps alphabetically for cleaner display
    Collections.sort(allWarps);
    for (String warp : allWarps) {
     sender.sendMessage(ChatColor.GRAY + "- " + warp);
    }
    sender.sendMessage(ChatColor.AQUA + "---------------------");
   }
   return true;
  }

  return false; // Command not recognized by this executor
 }

 @Override
 public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
  // Only provide tab completion if the sender has permission for at least one relevant command
  if (!sender.hasPermission("nestedwarps.warp") &&
          !sender.hasPermission("nestedwarps.delwarp") &&
          !sender.hasPermission("nestedwarps.setwarp")) {
   return Collections.emptyList();
  }

  String commandName = command.getName().toLowerCase();
  List<String> completions = new ArrayList<>();
  ConfigurationSection warpsSection = getConfig().getConfigurationSection("warps");

  if (warpsSection == null) {
   return Collections.emptyList();
  }

  // Determine the current argument the user is typing for completion
  String currentInputPart = args[args.length - 1].toLowerCase();

  // Build the path to the current configuration section based on previous arguments
  ConfigurationSection sectionToSearch = warpsSection;
  // The loop goes up to args.length - 1 because the last arg is currentInputPart
  for (int i = 0; i < args.length - 1; i++) {
   if (sectionToSearch == null) break; // Previous part was not a valid section
   sectionToSearch = sectionToSearch.getConfigurationSection(args[i]);
  }

  if (sectionToSearch == null) {
   return Collections.emptyList(); // The preceding path is not a valid folder or section
  }

  // --- Logic for /warp and /delwarp tab completion ---
  if (commandName.equals("warp") || commandName.equals("delwarp")) {
   for (String key : sectionToSearch.getKeys(false)) { // false for direct children only
    // Check if the key starts with the current typed part (case-insensitive)
    if (key.toLowerCase().startsWith(currentInputPart)) {
     ConfigurationSection childSection = sectionToSearch.getConfigurationSection(key);

     if (childSection != null) { // This 'key' represents a ConfigurationSection (potential warp or folder)
      // Check if this section represents a valid warp (contains location data)
      boolean isActualWarp = childSection.contains("world") && childSection.contains("x") &&
              childSection.contains("y") && childSection.contains("z");

      // Add the warp name if it's an actual warp
      if (isActualWarp) {
       completions.add(key);
      }

      // If the section has any direct children (meaning it's a folder, possibly also a warp)
      // then suggest it with a trailing slash to allow drilling down.
      if (childSection.getKeys(false).size() > 0) {
       completions.add(key + "/");
      }
     }
     // If childSection is null, it means 'key' points to a primitive value (like "world", "x", etc.)
     // within the current section. These are not valid warp names or folder names for tab completion,
     // so we don't add them.
    }
   }
  }
  // --- Logic for /setwarp tab completion ---
  else if (commandName.equals("setwarp")) {
   // For /setwarp, we suggest all current full warp paths. This is useful for overwriting existing warps,
   // or providing a template for new nested warp names.
   List<String> allExistingWarpPaths = getAllWarpPathsRecursive(warpsSection, "");
   for (String existingWarp : allExistingWarpPaths) {
    if (existingWarp.toLowerCase().startsWith(currentInputPart)) {
     completions.add(existingWarp);
    }
   }
  }

  // Ensure unique completions before returning
  return completions.stream().distinct().collect(Collectors.toList());
 }


 /**
  * Retrieves a warp location from the config.
  * Supports nested paths like "creative/build1".
  *
  * @param warpPath The path to the warp (e.g., "hub" or "creative/build1").
  * @return The Location object if found, null otherwise.
  */
 private Location getWarpLocation(String warpPath) {
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
 private void saveWarpLocation(String warpPath, @NotNull Location location) { // Added @NotNull
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
 private List<String> getAllWarpPathsRecursive(ConfigurationSection section, String currentPath) {
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
}