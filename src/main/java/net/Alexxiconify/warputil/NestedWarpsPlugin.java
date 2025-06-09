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
import org.bukkit.command.PluginCommand; // Import PluginCommand
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
// Changed from import java.util.Objects; to avoid ambiguity if Object is needed


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
  * Programmatically registers all commands for this plugin.
  * This method is called during onEnable().
  */
 private void registerCommands() {
  // Create and register PluginCommand instances
  // For each command, we create a new instance of an inner class that
  // implements CommandExecutor and TabCompleter.
  // This is PaperMC's recommended way for newer versions.

  // /warp command
  PluginCommand warpCommand = getServer().getPluginCommand("warp");
  if (warpCommand != null) {
   warpCommand.setExecutor(new WarpCommandExecutor());
   warpCommand.setTabCompleter(new WarpTabCompleter());
   warpCommand.setDescription("Teleports to a warp (e.g., /warp creative/build1).");
   warpCommand.setUsage("/<command> [warp_name]");
   warpCommand.setPermission("nestedwarps.warp");
   warpCommand.setPermissionMessage(ChatColor.RED + "You do not have permission to use this command.");
  } else {
   getLogger().severe("Command 'warp' could not be found for registration. Is it defined in plugin.yml (even though Paper plugins don't fully support it)?");
  }

  // /setwarp command
  PluginCommand setWarpCommand = getServer().getPluginCommand("setwarp");
  if (setWarpCommand != null) {
   setWarpCommand.setExecutor(new SetWarpCommandExecutor());
   setWarpCommand.setTabCompleter(new SetWarpTabCompleter()); // Optional: for tab completion of existing names
   setWarpCommand.setDescription("Sets a new warp at your current location (e.g., /setwarp creative/build1).");
   setWarpCommand.setUsage("/<command> <warp_name>");
   setWarpCommand.setPermission("nestedwarps.setwarp");
   setWarpCommand.setPermissionMessage(ChatColor.RED + "You do not have permission to set warps.");
  } else {
   getLogger().severe("Command 'setwarp' could not be found for registration.");
  }

  // /delwarp command
  PluginCommand delWarpCommand = getServer().getPluginCommand("delwarp");
  if (delWarpCommand != null) {
   delWarpCommand.setExecutor(new DelWarpCommandExecutor());
   delWarpCommand.setTabCompleter(new DelWarpTabCompleter());
   delWarpCommand.setDescription("Deletes an existing warp (e.g., /delwarp creative/build1).");
   delWarpCommand.setUsage("/<command> <warp_name>");
   delWarpCommand.setPermission("nestedwarps.delwarp");
   delWarpCommand.setPermissionMessage(ChatColor.RED + "You do not have permission to delete warps.");
  } else {
   getLogger().severe("Command 'delwarp' could not be found for registration.");
  }

  // /warps command
  PluginCommand warpsCommand = getServer().getPluginCommand("warps");
  if (warpsCommand != null) {
   warpsCommand.setExecutor(new WarpsCommandExecutor());
   warpsCommand.setDescription("Lists all available warps.");
   warpsCommand.setUsage("/<command>");
   warpsCommand.setPermission("nestedwarps.list");
   warpsCommand.setPermissionMessage(ChatColor.RED + "You do not have permission to list warps.");
  } else {
   getLogger().severe("Command 'warps' could not be found for registration.");
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
}